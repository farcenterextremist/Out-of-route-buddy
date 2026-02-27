#!/usr/bin/env python3
"""
Persist Jarvey capability menu sends and user choices.
Stores to jarvey_choices.json; prunes to last N entries to avoid unbounded growth.
"""

import json
import os
from datetime import datetime

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
CHOICES_FILE = os.path.join(SCRIPT_DIR, "jarvey_choices.json")
MAX_CAPABILITY_SENDS = 100
MAX_USER_CHOICES = 100


def _load_state() -> dict:
    """Load jarvey_choices.json."""
    if not os.path.isfile(CHOICES_FILE):
        return {"capability_sends": [], "user_choices": []}
    try:
        with open(CHOICES_FILE, encoding="utf-8") as f:
            data = json.load(f)
            return {
                "capability_sends": data.get("capability_sends", []),
                "user_choices": data.get("user_choices", []),
            }
    except (json.JSONDecodeError, OSError):
        return {"capability_sends": [], "user_choices": []}


def _save_state(state: dict) -> None:
    """Write jarvey_choices.json."""
    try:
        with open(CHOICES_FILE, "w", encoding="utf-8") as f:
            json.dump(state, f, indent=2)
    except OSError:
        pass


def log_capability_send(subject: str, options: list) -> None:
    """
    Log a capability menu send.
    options: list of {"id": str, "label": str} from jarvey_capability_menu.json.
    """
    state = _load_state()
    entry = {
        "timestamp": datetime.utcnow().isoformat() + "Z",
        "subject": subject,
        "options_sent": options,
    }
    state["capability_sends"].append(entry)
    state["capability_sends"] = state["capability_sends"][-MAX_CAPABILITY_SENDS:]
    _save_state(state)


def log_user_choice(user_message: str, choice_or_reply: str, thread_id: str | None = None) -> None:
    """
    Log a user choice or reply.
    choice_or_reply: e.g. "1", "2", or full text if they described what they need.
    """
    state = _load_state()
    entry = {
        "timestamp": datetime.utcnow().isoformat() + "Z",
        "user_message": (user_message or "")[:500],
        "choice_or_reply": (choice_or_reply or "")[:500],
        "thread_id": thread_id or "default",
    }
    state["user_choices"].append(entry)
    state["user_choices"] = state["user_choices"][-MAX_USER_CHOICES:]
    _save_state(state)
