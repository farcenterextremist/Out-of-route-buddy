#!/usr/bin/env python3
"""
Send an opener email from Jarvey (proactive intro, not a reply).
Uses jarvey_capability_menu.json for 5-10 multiple-choice options.
Logs sends to jarvey_choices.json.

Usage: python send_opener.py
Requires: .env with COORDINATOR_EMAIL_* and COORDINATOR_SMTP_* (see README).
"""

import json
import os
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

CAPABILITY_MENU_PATH = os.path.join(SCRIPT_DIR, "jarvey_capability_menu.json")
OPENER_SUBJECT = "OutOfRouteBuddy — Jarvey here"


def _load_capability_menu() -> list:
    """Load options from jarvey_capability_menu.json."""
    if not os.path.isfile(CAPABILITY_MENU_PATH):
        return []
    try:
        with open(CAPABILITY_MENU_PATH, encoding="utf-8") as f:
            data = json.load(f)
            return data.get("options", [])
    except (json.JSONDecodeError, OSError):
        return []


def _build_capability_body(options: list) -> str:
    """Build email body: brief intro + numbered list + instruction."""
    intro = (
        "Hi,\n\n"
        "I'm Jarvey, the Master Branch Coordinator for OutOfRouteBuddy. "
        "I can help you stay in the loop on the project, assign work, and answer questions.\n\n"
        "Here's what I can help with:\n\n"
    )
    lines = [intro]
    for opt in options:
        oid = opt.get("id", "")
        label = opt.get("label", "")
        lines.append(f"  {oid}. {label}")
    lines.append("")
    lines.append("Reply with a number (1–10) or describe what you need.")
    lines.append("")
    lines.append("— Jarvey")
    return "\n".join(lines)


def send_capability_opener(env: dict | None = None) -> bool:
    """
    Send the capability menu opener email. Returns True on success.
    If env is None, validates config internally.
    """
    if env is None:
        from config_schema import validate_config
        env = validate_config(mode="listener", exit_on_error=True)

    options = _load_capability_menu()
    if not options:
        # Fallback if menu file missing
        options = [
            {"id": "1", "label": "Roadmap status"},
            {"id": "2", "label": "Recent changes"},
            {"id": "3", "label": "Prioritize a task"},
            {"id": "4", "label": "Where is X defined?"},
            {"id": "5", "label": "Something else (describe)"},
        ]

    body = _build_capability_body(options)
    try:
        from send_email import send
        send(OPENER_SUBJECT, body)
        from jarvey_choices import log_capability_send
        log_capability_send(OPENER_SUBJECT, options)
        return True
    except Exception:
        return False


def main():
    from config_schema import validate_config
    env = validate_config(mode="listener", exit_on_error=True)
    if send_capability_opener(env):
        print("Opener email sent successfully.")
    else:
        print("Failed to send opener.", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
