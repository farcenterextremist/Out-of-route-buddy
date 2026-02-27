#!/usr/bin/env python3
"""
Structured output / tool use. Parse LLM reply for JSON actions
(e.g. {"action": "send_digest", "params": {...}}) and execute via coordinator.
"""

import json
import re
from typing import Any

_SUPPORTED_ACTIONS = frozenset({"send_digest", "clarify", "save_note", "assign_work", "add_to_timeline", "offer_options"})


def parse_structured_reply(reply_text: str) -> dict | None:
    """
    Attempt to parse structured action from LLM reply.
    Looks for JSON block (```json ... ```) or plain {"action": ...} in text.
    Returns {"action": str, "params": dict} when valid; else None.
    """
    if not reply_text or not reply_text.strip():
        return None
    text = reply_text.strip()

    # Try ```json ... ``` block first
    json_block = re.search(r"```\s*json\s*\n([\s\S]*?)\n\s*```", text, re.IGNORECASE)
    if json_block:
        try:
            data = json.loads(json_block.group(1).strip())
            if isinstance(data, dict) and data.get("action") in _SUPPORTED_ACTIONS:
                return {
                    "action": str(data["action"]),
                    "params": data.get("params") if isinstance(data.get("params"), dict) else {},
                }
        except json.JSONDecodeError:
            pass

    # Try plain {"action": ...} anywhere in text (including nested params)
    for match in re.finditer(r'\{\s*"action"\s*:\s*"([^"]+)"', text):
        start = match.start()
        depth = 0
        end = -1
        for i, c in enumerate(text[start:], start):
            if c == "{":
                depth += 1
            elif c == "}":
                depth -= 1
                if depth == 0:
                    end = i
                    break
        if end > start:
            try:
                obj = json.loads(text[start : end + 1])
                if isinstance(obj, dict) and obj.get("action") in _SUPPORTED_ACTIONS:
                    return {
                        "action": str(obj["action"]),
                        "params": obj.get("params") if isinstance(obj.get("params"), dict) else {},
                    }
            except json.JSONDecodeError:
                continue
    return None


def execute_action(action: str, params: dict, env: dict) -> str | None:
    """
    Execute a structured action and return the reply body.
    Returns None if action is unknown or execution fails.
    For save_note: performs side effect (append to file), returns None so caller keeps main reply.
    """
    if action not in _SUPPORTED_ACTIONS:
        return None
    try:
        if action == "send_digest":
            return _execute_send_digest(params, env)
        if action == "clarify":
            return _execute_clarify(params, env)
        if action == "save_note":
            _execute_save_note(params, env)
            return None  # Side effect only; caller keeps main reply and strips JSON
        if action == "assign_work":
            _execute_assign_work(params, env)
            return None  # Side effect only; caller keeps main reply
        if action == "add_to_timeline":
            _execute_add_to_timeline(params, env)
            return None  # Side effect only; caller keeps main reply
        if action == "offer_options":
            return _execute_offer_options(params, env)
    except Exception:
        pass
    return None


def strip_structured_from_reply(reply_text: str) -> str:
    """
    Remove JSON action block from reply text so the email body does not include it.
    Handles ```json ... ``` blocks and plain {...} JSON objects.
    """
    if not reply_text or not reply_text.strip():
        return reply_text or ""
    text = reply_text.strip()
    # Remove ```json ... ``` block
    text = re.sub(r"```\s*json\s*\n[\s\S]*?\n\s*```", "", text, flags=re.IGNORECASE)
    # Remove plain {"action": "X", "params": {...}} - match balanced braces
    for match in re.finditer(r'\{\s*"action"\s*:\s*"([^"]+)"', text):
        start = match.start()
        depth = 0
        end = -1
        for i, c in enumerate(text[start:], start):
            if c == "{":
                depth += 1
            elif c == "}":
                depth -= 1
                if depth == 0:
                    end = i
                    break
        if end > start:
            text = (text[:start] + text[end + 1:]).strip()
            break  # Remove first match only
    return text.strip() or ""


def _execute_send_digest(params: dict, env: dict) -> str:
    """Return weekly digest body. Uses template registry or roadmap fetcher."""
    try:
        from template_registry import _load_templates, _resolve_body
        for t in _load_templates():
            if t.get("key") == "weekly_digest":
                return _resolve_body(t, "", "weekly digest")
    except Exception:
        pass
    try:
        from context_loader import get_roadmap_summary, get_project_timeline
        roadmap = get_roadmap_summary(cap=600) or ""
        timeline = get_project_timeline(cap_chars=400) or ""
        parts = []
        if roadmap:
            parts.append("Roadmap / priorities:\n" + roadmap)
        if timeline:
            parts.append("Recent timeline:\n" + timeline)
        if parts:
            return "Hi,\n\n" + "\n\n".join(parts) + "\n\n— Jarvey"
    except Exception:
        pass
    return "Hi,\n\nWe'll set up a weekly Board Meeting short summary. Reply if you'd like a different format.\n\n— Jarvey"


def _execute_clarify(params: dict, env: dict) -> str:
    """Return clarification template body."""
    try:
        from template_registry import _load_templates, _resolve_body
        for t in _load_templates():
            if t.get("key") == "unclear":
                return _resolve_body(t, "", "")
    except Exception:
        pass
    return (
        "Hi,\n\nI got your message but couldn't make out what you need. "
        "Could you specify: (a) what's next / roadmap, (b) a specific question about the project, or (c) something else?\n\n— Jarvey"
    )


def _execute_save_note(params: dict, env: dict) -> None:
    """
    Append note from user email to docs/agents/EMAIL_NOTES.md.
    Side effect only; caller keeps main reply and strips JSON from it.
    """
    note = (params.get("note") or "").strip()
    if not note:
        return
    topic = (params.get("topic") or "").strip()
    try:
        from context_loader import append_to_notes
        append_to_notes(note, topic=topic)
    except Exception:
        pass


def _execute_assign_work(params: dict, env: dict) -> None:
    """
    Append assigned work to docs/agents/EMAIL_NOTES.md (topic: Assigned to {role}).
    Side effect only; caller keeps main reply.
    """
    role = (params.get("role") or "").strip()
    task = (params.get("task") or "").strip()
    context = (params.get("context") or "").strip()
    if not task:
        return
    note = task
    if context:
        note += f". Context: {context}"
    topic = f"Assigned to {role}" if role else "Assigned work"
    try:
        from context_loader import append_to_notes
        append_to_notes(note, topic=topic)
    except Exception:
        pass


def _execute_add_to_timeline(params: dict, env: dict) -> None:
    """
    Append entry to project_timeline.json.
    Side effect only; caller keeps main reply.
    """
    entry = (params.get("entry") or "").strip()
    phase = (params.get("phase") or "").strip()
    if not entry:
        return
    from datetime import datetime
    date_str = datetime.utcnow().strftime("%Y-%m-%d")
    etype = phase or "email"
    try:
        from context_loader import append_to_timeline
        append_to_timeline(date_str, etype, entry, phase or "")
    except Exception:
        pass


def _execute_offer_options(params: dict, env: dict) -> str:
    """Return body with numbered options. options is list of strings."""
    options = params.get("options")
    if not isinstance(options, list) or not options:
        return ""
    lines = ["Hi,\n\nHere are some options. Reply with a number or describe what you need:\n"]
    for i, opt in enumerate(options[:10], 1):
        opt_str = str(opt).strip()
        if opt_str:
            lines.append(f"{i}. {opt_str}")
    lines.append("\n— Jarvey")
    return "\n".join(lines)


def get_action_schema() -> dict[str, Any]:
    """Return schema for supported actions. For documentation and future validation."""
    return {
        "send_digest": {"description": "Send weekly board digest", "params": {}},
        "clarify": {"description": "Ask user for clarification", "params": {"options": "list[str]"}},
        "save_note": {
            "description": "Save note from user email to project (docs/agents/EMAIL_NOTES.md)",
            "params": {"note": "str (required)", "topic": "str (optional)"},
        },
        "assign_work": {
            "description": "Assign work to a role (recorded in EMAIL_NOTES)",
            "params": {"role": "str", "task": "str (required)", "context": "str (optional)"},
        },
        "add_to_timeline": {
            "description": "Add entry to project timeline",
            "params": {"entry": "str (required)", "phase": "str (optional)"},
        },
        "offer_options": {
            "description": "Return body with numbered options for user to choose",
            "params": {"options": "list[str] (required)"},
        },
    }
