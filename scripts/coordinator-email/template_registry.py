#!/usr/bin/env python3
"""
Template registry for Jarvey. Loads templates from templates/*.json,
matches user messages, resolves placeholders via fetchers, and returns (subject, body, key).
"""

import json
import os
import re
from typing import Any

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
TEMPLATES_DIR = os.path.join(SCRIPT_DIR, "templates")

_FETCHERS: dict[str, callable] = {}


def _register_fetchers():
    """Register built-in fetchers. Lazy import to avoid circular deps."""
    if _FETCHERS:
        return

    def _roadmap(cap: int = 600) -> str:
        from context_loader import get_roadmap_summary
        return get_roadmap_summary(cap=cap) or ""

    def _version() -> str:
        from context_loader import get_version_summary
        return get_version_summary() or ""

    def _timeline(cap: int = 600) -> str:
        from context_loader import get_project_timeline
        return get_project_timeline(cap_chars=cap) or ""

    def _capability_menu() -> str:
        menu_path = os.path.join(SCRIPT_DIR, "jarvey_capability_menu.json")
        if not os.path.isfile(menu_path):
            return "1. Roadmap status 2. Recent changes 3. Prioritize 4. Where is X? 5. Recovery 6. Version 7. Assign work 8. Save note 9. Send to coworker 10. Something else"
        try:
            with open(menu_path, encoding="utf-8") as f:
                data = json.load(f)
            options = data.get("options") or []
            lines = [f"{i}. {(opt.get('label') or str(opt)).strip()}" for i, opt in enumerate(options[:10], 1) if (opt.get("label") or str(opt)).strip()]
            return "\n".join(lines) if lines else "1. Roadmap 2. Recent changes 3. Prioritize 4. Where is X? 5. Recovery 6. Version 7. Assign work 8. Save note 9. Send to coworker 10. Something else"
        except (json.JSONDecodeError, OSError):
            return "1. Roadmap status 2. Recent changes 3. Prioritize 4. Where is X? 5. Recovery 6. Version 7. Assign work 8. Save note 9. Send to coworker 10. Something else"

    _FETCHERS["roadmap"] = _roadmap
    _FETCHERS["version"] = _version
    _FETCHERS["timeline"] = _timeline
    _FETCHERS["capability_menu"] = _capability_menu


def _load_templates() -> list[dict[str, Any]]:
    """Load all template JSON files, sorted by priority (desc)."""
    templates = []
    if not os.path.isdir(TEMPLATES_DIR):
        return []
    for name in sorted(os.listdir(TEMPLATES_DIR)):
        if not name.endswith(".json"):
            continue
        path = os.path.join(TEMPLATES_DIR, name)
        try:
            with open(path, encoding="utf-8") as f:
                t = json.load(f)
            t["_priority"] = t.get("priority", 50)
            templates.append(t)
        except (json.JSONDecodeError, OSError):
            continue
    templates.sort(key=lambda t: t["_priority"], reverse=True)
    return templates


def _match_keywords(combined: str, template: dict) -> bool:
    """Return True if keywords match. combined is lowercased subject + body."""
    matched, _ = _match_keywords_with_confidence(combined, template)
    return matched


def _match_keywords_with_confidence(combined: str, template: dict) -> tuple[bool, float]:
    """
    Return (matched, confidence). Confidence 0-1: exact match=1.0, regex=0.9.
    Used for confidence-based routing: if confidence < threshold, route to LLM.
    """
    keywords = template.get("keywords") or []
    if not keywords:
        return False, 0.0
    combine = template.get("keywords_combine", "any")
    confidences = []
    for kw in keywords:
        if _looks_regex(kw):
            m = re.search(kw, combined)
            if m:
                confidences.append(0.9)  # Regex match slightly lower (can be broad)
        else:
            if kw in combined:
                # Exact substring: 1.0 if word-boundary-like, else 0.95
                confidences.append(1.0)
    if not confidences:
        return False, 0.0
    if combine == "and":
        if len(confidences) < len(keywords):
            return False, 0.0
        return True, min(confidences)
    return True, max(confidences)


def _looks_regex(s: str) -> bool:
    """Heuristic: contains regex metachars."""
    return bool(re.search(r"[\\^$*+?\[\]()|{}]", s))


def _check_short_body(body: str, template: dict) -> bool:
    """Return True if short_body condition matches (body is short and not whitelisted).
    When JARVEY_BREVITY_USE_DETECTOR=1, uses brevity_detector.should_use_unclear_template
    so optional implicature-intent can be used for non-whitelisted short messages.
    """
    if template.get("condition") != "short_body":
        return False
    max_len = template.get("short_body_max_len", 15)
    try:
        max_len = int(max_len) if isinstance(max_len, str) else max_len
    except (ValueError, TypeError):
        max_len = 15
    whitelist = tuple(s.strip() for s in template.get("short_body_whitelist") or [])

    # Optional: use brevity_detector module (supports implicature-intent when enabled)
    try:
        env_path = os.path.join(SCRIPT_DIR, ".env")
        use_detector = False
        use_implicature = False
        if os.path.isfile(env_path):
            with open(env_path, encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if line.startswith("JARVEY_BREVITY_USE_DETECTOR") and "=" in line:
                        v = line.split("=", 1)[1].strip().strip('"\'').lower()
                        use_detector = v in ("1", "true", "yes")
                    if line.startswith("JARVEY_BREVITY_USE_IMPLICATURE") and "=" in line:
                        v = line.split("=", 1)[1].strip().strip('"\'').lower()
                        use_implicature = v in ("1", "true", "yes")
        if use_detector:
            from brevity_detector import should_use_unclear_template
            return should_use_unclear_template(
                body or "",
                max_len=max_len,
                whitelist=whitelist if whitelist else None,
                use_implicature=use_implicature,
            )
    except Exception:
        pass

    # Default: same logic as before (no extra dependency)
    body_stripped = (body or "").strip()
    if len(body_stripped) >= max_len:
        return False
    whitelist_lower = tuple(s.lower() for s in whitelist)
    return body_stripped.lower() not in whitelist_lower


def _resolve_body(template: dict, subject: str, body: str) -> str:
    """Resolve placeholders in body via fetchers."""
    _register_fetchers()
    body_tpl = template.get("body", "")
    fetcher_name = template.get("fetcher")
    if not fetcher_name or "{{" not in body_tpl:
        return body_tpl

    fetcher = _FETCHERS.get(fetcher_name)
    if not fetcher:
        fallback = template.get("fetcher_fallback", "")
        return body_tpl.replace("{{" + fetcher_name + "}}", fallback or "")

    try:
        if fetcher_name == "roadmap":
            val = fetcher(600)
        elif fetcher_name == "version":
            val = fetcher()
        elif fetcher_name == "timeline":
            val = fetcher(600)
        elif fetcher_name == "capability_menu":
            val = fetcher()
        else:
            val = fetcher()
    except Exception:
        val = template.get("fetcher_fallback", "")

    if fetcher_name == "timeline" and not val and template.get("body_empty"):
        return template["body_empty"]

    placeholder = "{{" + fetcher_name + "}}"
    return body_tpl.replace(placeholder, val or template.get("fetcher_fallback", ""))


def _get_confidence_threshold() -> float:
    """Read JARVEY_TEMPLATE_CONFIDENCE_THRESHOLD from env; default 0.7."""
    try:
        import os
        env_path = os.path.join(SCRIPT_DIR, ".env")
        if os.path.isfile(env_path):
            with open(env_path, encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if line.startswith("JARVEY_TEMPLATE_CONFIDENCE_THRESHOLD") and "=" in line:
                        v = line.split("=", 1)[1].strip().strip('"\'')
                        return max(0.0, min(1.0, float(v)))
    except (OSError, ValueError):
        pass
    return 0.7


def choose_response(subject: str, body: str) -> tuple[str, str, str]:
    """
    Match user message to a template and return (subject, body, template_key).
    Templates are checked in priority order. First match wins.
    """
    return choose_response_with_confidence(subject, body)[:3]


def choose_response_with_confidence(subject: str, body: str) -> tuple[str, str, str, float]:
    """
    Same as choose_response but also returns match_confidence (0-1).
    When confidence < JARVEY_TEMPLATE_CONFIDENCE_THRESHOLD (default 0.7),
    caller should route to LLM instead of using template.
    """
    combined = f"{subject or ''} {body or ''}".lower()
    body_raw = (body or "").strip()
    threshold = _get_confidence_threshold()

    for template in _load_templates():
        key = template.get("key", "")
        if key == "default":
            continue  # default is fallback, checked last

        # Short-body condition (unclear) - always high confidence
        if _check_short_body(body_raw, template):
            body_text = template.get("body", "")
            return template.get("subject", "Re: OutOfRouteBuddy"), body_text, key, 1.0

        # Keyword match with confidence
        matched, confidence = _match_keywords_with_confidence(combined, template)
        if matched:
            if confidence < threshold:
                # Low confidence: skip template, fall through to default/LLM
                continue
            body_text = _resolve_body(template, subject, body)
            return template.get("subject", "Re: OutOfRouteBuddy"), body_text, key, confidence

    # Default fallback
    for template in _load_templates():
        if template.get("key") == "default":
            body_text = template.get("body", "")
            return template.get("subject", "Re: OutOfRouteBuddy"), body_text, "default", 0.5

    # Hardcoded fallback if no default template
    return (
        "Re: OutOfRouteBuddy — we got your message",
        "Hi,\n\nWe got your message. We'll follow up in the next session or by email.\n\n— Jarvey",
        "default",
        0.5,
    )
