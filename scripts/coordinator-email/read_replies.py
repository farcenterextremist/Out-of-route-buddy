#!/usr/bin/env python3
"""
Read the latest reply from the user's inbox (IMAP).
Use when the user says they replied to a coordinator email.
Requires .env with COORDINATOR_EMAIL_* and COORDINATOR_IMAP_* (or SMTP user/password for Gmail IMAP).
Writes the most recent matching reply to last_reply.txt in this folder.

For agent use: pass --json to get structured output on stdout (subject, body, date) instead of only
writing last_reply.txt. The agent can then use the reply in context without reading a file.
"""

import json
import os
import sys
import email
import imaplib
from email.header import decode_header
from email.utils import getaddresses

from retry_utils import with_retry

# Same env loader as send_email
def load_env():
    env_path = os.path.join(os.path.dirname(__file__), ".env")
    if not os.path.isfile(env_path):
        return {}
    env = {}
    with open(env_path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line and not line.startswith("#") and "=" in line:
                k, v = line.split("=", 1)
                env[k.strip()] = v.strip().strip('"').strip("'")
    return env


def decode_mime_header(s):
    if not s:
        return ""
    parts = decode_header(s)
    out = []
    for part, enc in parts:
        if isinstance(part, bytes):
            out.append(part.decode(enc or "utf-8", errors="replace"))
        else:
            out.append(part)
    return "".join(out)


def get_body(msg):
    body = ""
    if msg.is_multipart():
        for part in msg.walk():
            ct = part.get_content_type()
            if ct == "text/plain":
                try:
                    payload = part.get_payload(decode=True)
                    if payload:
                        body = payload.decode(part.get_content_charset() or "utf-8", errors="replace")
                except Exception:
                    pass
                if body:
                    break
    else:
        try:
            payload = msg.get_payload(decode=True)
            if payload:
                body = payload.decode(msg.get_content_charset() or "utf-8", errors="replace")
        except Exception:
            pass
    return body.strip()


def _is_agent_sent(msg) -> bool:
    """True if message has X-OutOfRouteBuddy-Sent header (agent-sent; skip when reading)."""
    return msg.get("X-OutOfRouteBuddy-Sent", "").strip().lower() == "true"


def _normalize_email(addr):
    """Return lowercase email address for comparison, or None if not parseable."""
    if not addr or not isinstance(addr, str):
        return None
    addrs = getaddresses([addr])
    if not addrs:
        return None
    # getaddresses returns list of (display_name, addr_spec)
    _, addr_spec = addrs[0]
    if not addr_spec or "@" not in addr_spec:
        return None
    return addr_spec.strip().lower()


# Public aliases for use by diagnose_inbox and other scripts
normalize_email = _normalize_email
is_agent_sent = _is_agent_sent


@with_retry
def read_replies():
    env = load_env()
    # Gmail IMAP: use same credentials as SMTP for same account
    user = env.get("COORDINATOR_IMAP_USER") or env.get("COORDINATOR_SMTP_USER")
    password = env.get("COORDINATOR_IMAP_PASSWORD") or env.get("COORDINATOR_SMTP_PASSWORD")
    host = env.get("COORDINATOR_IMAP_HOST") or "imap.gmail.com"
    port = int(env.get("COORDINATOR_IMAP_PORT", "993"))

    if not user or not password:
        raise RuntimeError("Missing COORDINATOR_IMAP_USER/IMAP_PASSWORD or SMTP_USER/SMTP_PASSWORD in .env")

    our_from = env.get("COORDINATOR_EMAIL_FROM", "").strip().lower()
    if our_from:
        our_from = _normalize_email(our_from) or our_from

    # Only consider messages FROM the user (COORDINATOR_EMAIL_TO = consultation recipient)
    # Supports comma-separated addresses so reply from work/personal both work
    user_email_raw = env.get("COORDINATOR_EMAIL_TO", "").strip()
    user_emails = set()
    if user_email_raw:
        for addr in user_email_raw.split(","):
            a = _normalize_email(addr.strip())
            if a:
                user_emails.add(a)

    # Same-inbox only when there is a single allowed reply-from address and it equals our FROM.
    # If COORDINATOR_EMAIL_TO has multiple addresses (e.g. user + bot), do not treat as same-inbox.
    same_inbox = (
        bool(our_from)
        and len(user_emails) == 1
        and our_from in user_emails
    )
    if same_inbox:
        print("Same-inbox mode: FROM and TO are the same address", file=sys.stderr)

    mail = imaplib.IMAP4_SSL(host, port)
    mail.login(user, password)
    mail.select("INBOX")

    # Search ALL so we find user messages even if already marked read (e.g. by email client).
    # Previously we used UNSEEN first; that skipped user messages that were SEEN, so Jarvey never responded.
    _, msgnums = mail.search(None, "ALL")
    msg_ids = msgnums[0].split() if msgnums[0] else []
    if not msg_ids:
        mail.logout()
        return None, None, None, None

    latest_subject = None
    latest_body = None
    latest_date = None
    latest_msg_id = None

    for mid in reversed(msg_ids[-50:]):  # last 50
        _, data = mail.fetch(mid, "(RFC822)")
        if not data or not data[0]:
            continue
        raw = data[0][1]
        msg = email.message_from_bytes(raw)
        subj = decode_mime_header(msg.get("Subject", ""))
        # Skip messages we sent (agent-sent have this header; user replies do not)
        if _is_agent_sent(msg):
            continue
        # Skip messages from ourselves (when we have a dedicated bot address).
        # In same-inbox mode (our_from == user_email), we rely on X-OutOfRouteBuddy-Sent only;
        # otherwise we'd skip the user's messages too (they're FROM the same address).
        if our_from and our_from not in user_emails:
            from_header = msg.get("From", "")
            sender = _normalize_email(from_header)
            if sender and sender == our_from:
                continue
        # Only consider messages FROM the user (when COORDINATOR_EMAIL_TO is set)
        if user_emails:
            from_header = msg.get("From", "")
            sender = _normalize_email(from_header)
            if not sender or sender not in user_emails:
                continue
        body = get_body(msg)
        date_str = msg.get("Date", "")
        msg_id_header = (msg.get("Message-ID") or "").strip() or None
        latest_subject = subj
        latest_body = body
        latest_date = date_str
        latest_msg_id = msg_id_header or (mid.decode() if isinstance(mid, bytes) else str(mid))
        # Mark as read so we don't reprocess
        mail.store(mid, "+FLAGS", "\\Seen")
        break

    mail.logout()

    return latest_subject, latest_body, latest_date, latest_msg_id


def main():
    use_json = "--json" in sys.argv
    out_dir = os.path.dirname(__file__)
    out_file = os.path.join(out_dir, "last_reply.txt")
    try:
        subject, body, date, message_id = read_replies()
        if not body and not subject:
            if use_json:
                print(json.dumps({"found": False, "subject": None, "body": None, "date": None, "message_id": None}))
            else:
                print("No reply found (no message from user in inbox).", file=sys.stderr)
            sys.exit(0)
        lines = []
        if date:
            lines.append(f"Date: {date}")
        if subject:
            lines.append(f"Subject: {subject}")
        lines.append("")
        lines.append(body or "(no body)")
        content = "\n".join(lines)
        with open(out_file, "w", encoding="utf-8") as f:
            f.write(content)
        if use_json:
            print(json.dumps({"found": True, "subject": subject, "body": body or "", "date": date, "message_id": message_id}))
        else:
            print(f"Latest reply written to {out_file}")
    except Exception as e:
        if use_json:
            print(json.dumps({"found": False, "error": str(e)}))
        else:
            print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
