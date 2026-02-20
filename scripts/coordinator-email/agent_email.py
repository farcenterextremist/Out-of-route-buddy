#!/usr/bin/env python3
"""
Unified entrypoint for the agent to read and reply to coordinator email.

Usage:
  python agent_email.py read              → stdout: JSON { "found", "subject", "body", "date" } or { "found": false }
  python agent_email.py send "Subject" "Body"   → send email (body can be @path/to/body.txt)

Run from repo root or from scripts/coordinator-email/. Uses .env in this folder.
"""

import json
import os
import sys

# Run from this script's directory so imports and .env resolve
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

from read_replies import read_replies
from send_email import send


def cmd_read():
    subject, body, date = read_replies()
    if not subject and not body:
        out = {"found": False, "subject": None, "body": None, "date": None}
    else:
        out = {"found": True, "subject": subject or "", "body": body or "", "date": date or ""}
        # Keep last_reply.txt in sync so existing "read last_reply.txt" flow still works
        lines = []
        if date:
            lines.append(f"Date: {date}")
        if subject:
            lines.append(f"Subject: {subject}")
        lines.append("")
        lines.append(body or "(no body)")
        with open(os.path.join(SCRIPT_DIR, "last_reply.txt"), "w", encoding="utf-8") as f:
            f.write("\n".join(lines))
    print(json.dumps(out))


def cmd_send(args):
    if len(args) < 2:
        print('Usage: python agent_email.py send "Subject" "Body" or send "Subject" @path/to/body.txt', file=sys.stderr)
        sys.exit(1)
    subject = args[0]
    body_arg = args[1]
    if body_arg.startswith("@"):
        path = os.path.expanduser(body_arg[1:])
        if not os.path.isabs(path):
            path = os.path.join(SCRIPT_DIR, path)
        with open(path, encoding="utf-8") as f:
            body = f.read()
    else:
        body = body_arg
    send(subject, body)
    print(json.dumps({"sent": True, "subject": subject}))


def main():
    if len(sys.argv) < 2:
        print("Usage: python agent_email.py read | send \"Subject\" \"Body\"", file=sys.stderr)
        sys.exit(1)
    cmd = sys.argv[1].lower()
    if cmd == "read":
        try:
            cmd_read()
        except Exception as e:
            print(json.dumps({"found": False, "error": str(e)}))
            sys.exit(1)
    elif cmd == "send":
        try:
            cmd_send(sys.argv[2:])
        except Exception as e:
            print(json.dumps({"sent": False, "error": str(e)}), file=sys.stderr)
            sys.exit(1)
    else:
        print("Unknown command. Use: read | send \"Subject\" \"Body\"", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
