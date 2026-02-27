#!/usr/bin/env python3
"""
Conversation memory for multi-turn email threads. Stores user/agent exchanges
keyed by thread_id (normalized subject). Used when JARVEY_CONVERSATION_MEMORY=1.
"""

import json
import os
import re

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
HISTORY_FILE = os.path.join(SCRIPT_DIR, "conversation_history.json")
MAX_EXCHANGES_PER_THREAD = 20


def _normalize_thread_id(subject: str, message_id: str | None = None) -> str:
    """Return a stable thread ID from subject or message_id."""
    if message_id and message_id.startswith("<") and message_id.endswith(">"):
        # Use message_id for threading when available (e.g. In-Reply-To threads)
        return "msg:" + message_id[:80].replace("/", "_").replace("\\", "_")
    s = (subject or "").strip()
    s = re.sub(r"^re:\s*", "", s, flags=re.IGNORECASE)
    s = s.lower().strip()
    return (s[:80] if len(s) > 80 else s) or "default"


def _load_state() -> dict:
    """Load conversation history from file."""
    if not os.path.isfile(HISTORY_FILE):
        return {}
    try:
        with open(HISTORY_FILE, encoding="utf-8") as f:
            return json.load(f)
    except (json.JSONDecodeError, OSError):
        return {}


def _save_state(state: dict) -> None:
    """Write conversation history to file."""
    try:
        with open(HISTORY_FILE, "w", encoding="utf-8") as f:
            json.dump(state, f, indent=2)
    except OSError:
        pass


def load_history(thread_id: str | None, max_exchanges: int = 5) -> str:
    """
    Load last N exchanges for thread_id. Returns formatted string for use in context.
    thread_id can be from _normalize_thread_id(subject, message_id).
    """
    if not thread_id:
        return ""
    state = _load_state()
    exchanges = state.get(thread_id, [])
    if not exchanges:
        return ""
    recent = exchanges[-max_exchanges:]
    lines = []
    for ex in recent:
        lines.append(f"User: {ex.get('user', '')[:200]}")
        lines.append(f"Jarvey: {ex.get('agent', '')[:200]}")
    return "\n".join(lines)


def append_exchange(thread_id: str | None, user_msg: str, agent_reply: str) -> None:
    """
    Append one exchange to thread. Prunes to MAX_EXCHANGES_PER_THREAD per thread.
    """
    if not thread_id:
        return
    state = _load_state()
    if thread_id not in state:
        state[thread_id] = []
    state[thread_id].append({"user": user_msg, "agent": agent_reply})
    state[thread_id] = state[thread_id][-MAX_EXCHANGES_PER_THREAD:]
    _save_state(state)


def get_thread_id(subject: str, message_id: str | None = None) -> str:
    """Convenience: return normalized thread_id for use with load_history/append_exchange."""
    return _normalize_thread_id(subject, message_id)
