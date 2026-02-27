#!/usr/bin/env python3
"""
Diagnose why read_replies might not find messages.
Connects to IMAP, fetches last 20 messages, prints filter results.
Run: python diagnose_inbox.py
"""

import os
import sys
import email
import imaplib

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

from read_replies import load_env, decode_mime_header, normalize_email, is_agent_sent


def main():
    env = load_env()
    user = env.get("COORDINATOR_IMAP_USER") or env.get("COORDINATOR_SMTP_USER")
    password = env.get("COORDINATOR_IMAP_PASSWORD") or env.get("COORDINATOR_SMTP_PASSWORD")
    host = env.get("COORDINATOR_IMAP_HOST") or "imap.gmail.com"
    port = int(env.get("COORDINATOR_IMAP_PORT", "993"))

    if not user or not password:
        print("ERROR: Missing IMAP credentials in .env")
        sys.exit(1)

    our_from = env.get("COORDINATOR_EMAIL_FROM", "").strip().lower()
    if our_from:
        our_from = normalize_email(our_from) or our_from
    user_email = env.get("COORDINATOR_EMAIL_TO", "").strip()
    if user_email:
        user_email = normalize_email(user_email) or user_email

    print("Config:")
    print(f"  COORDINATOR_EMAIL_TO (user_email): {user_email!r}")
    print(f"  COORDINATOR_EMAIL_FROM (our_from): {our_from!r}")
    print(f"  Same-inbox: {our_from == user_email if (our_from and user_email) else 'N/A'}")
    print()

    mail = imaplib.IMAP4_SSL(host, port)
    mail.login(user, password)
    mail.select("INBOX")

    _, msgnums = mail.search(None, "ALL")
    msg_ids = (msgnums[0] or b"").split()
    if not msg_ids:
        print("No messages in inbox.")
        mail.logout()
        return

    # Last 20 messages, newest first
    to_check = list(reversed(msg_ids[-20:]))
    print(f"Checking last {len(to_check)} messages (newest first):\n")

    for i, mid in enumerate(to_check):
        try:
            _, data = mail.fetch(mid, "(RFC822)")
            if not data or not data[0]:
                continue
            raw = data[0][1]
            msg = email.message_from_bytes(raw)
            subj = decode_mime_header(msg.get("Subject", ""))
            from_h = msg.get("From", "")
            sender = normalize_email(from_h)
            agent = is_agent_sent(msg)

            # Apply filters (same logic as read_replies)
            has_subj = "OutOfRouteBuddy" in subj or "OutOfRoute" in subj
            skip_agent = agent
            skip_our = bool(our_from and our_from != user_email and sender == our_from)
            from_user = not user_email or (sender and sender == user_email)

            would_accept = has_subj and not skip_agent and not skip_our and from_user

            print(f"--- Message {i+1} (id={mid}) ---")
            print(f"  Subject: {subj[:60]!r}...")
            print(f"  From: {from_h[:50]!r}")
            print(f"  Sender (normalized): {sender!r}")
            print(f"  Agent-sent: {agent}")
            print(f"  Has OutOfRouteBuddy/OutOfRoute: {has_subj}")
            print(f"  Skip (agent): {skip_agent}")
            print(f"  Skip (our_from): {skip_our}")
            print(f"  From user: {from_user}")
            print(f"  WOULD ACCEPT: {would_accept}")
            if would_accept:
                print("  ^^^ MATCH - this would be returned by read_replies")
            print()
        except Exception as e:
            print(f"  Error: {e}\n")

    mail.logout()


if __name__ == "__main__":
    main()
