#!/usr/bin/env python3
"""
Send an email summarizing recent Jarvey updates.
Uses the LLM to compose a brief summary with full project context (jarvey_self, jarvey_context intents).

Usage: python send_jarvey_updates.py
Requires: .env with COORDINATOR_EMAIL_* and COORDINATOR_SMTP_* (see README).
"""

import os
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

from config_schema import validate_config
from coordinator_listener import compose_reply, _ensure_jarvey_signoff
from context_loader import detect_intents
from send_email import send

UPDATES_SUBJECT = "Re: OutOfRouteBuddy — Recent Jarvey Updates"

UPDATES_PROMPT = """Compose a brief email summarizing recent updates to the Jarvey coordinator model.
Include: new fixes (same-inbox, UNSEEN, prompt clarity), Re:/forwarded email support, context plan, response patterns, and any new intents.
Keep it concise (3–5 bullets). Sign as Jarvey."""


def main():
    env = validate_config(mode="listener", exit_on_error=True)
    intents = detect_intents(UPDATES_SUBJECT, UPDATES_PROMPT)
    body = compose_reply(UPDATES_SUBJECT, UPDATES_PROMPT, env, intents=intents)
    body = _ensure_jarvey_signoff(body)
    try:
        send(UPDATES_SUBJECT, body)
        print("Jarvey updates email sent successfully.")
    except Exception as e:
        print(f"Failed to send Jarvey updates: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
