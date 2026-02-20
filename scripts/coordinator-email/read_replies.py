#!/usr/bin/env python3
"""
Read the latest reply from the user's inbox (IMAP).
Use when the user says they replied to a coordinator email.
Requires .env with COORDINATOR_EMAIL_* and COORDINATOR_IMAP_* (or SMTP user/password for Gmail IMAP).
Writes the most recent matching reply to last_reply.txt in this folder.
"""

import os
import sys
import email
import imaplib
from email.header import decode_header

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


def read_replies():
    env = load_env()
    # Gmail IMAP: use same credentials as SMTP for same account
    user = env.get("COORDINATOR_IMAP_USER") or env.get("COORDINATOR_SMTP_USER")
    password = env.get("COORDINATOR_IMAP_PASSWORD") or env.get("COORDINATOR_SMTP_PASSWORD")
    host = env.get("COORDINATOR_IMAP_HOST") or "imap.gmail.com"
    port = int(env.get("COORDINATOR_IMAP_PORT", "993"))

    if not user or not password:
        raise RuntimeError("Missing COORDINATOR_IMAP_USER/IMAP_PASSWORD or SMTP_USER/SMTP_PASSWORD in .env")

    mail = imaplib.IMAP4_SSL(host, port)
    mail.login(user, password)
    mail.select("INBOX")

    # Find emails that look like replies to us (Re: OutOfRouteBuddy or subject contains OutOfRouteBuddy)
    _, msgnums = mail.search(None, "ALL")
    msg_ids = msgnums[0].split()
    if not msg_ids:
        mail.logout()
        return None, None, None

    latest_subject = None
    latest_body = None
    latest_date = None

    for mid in reversed(msg_ids[-50:]):  # last 50
        _, data = mail.fetch(mid, "(RFC822)")
        if not data or not data[0]:
            continue
        raw = data[0][1]
        msg = email.message_from_bytes(raw)
        subj = decode_mime_header(msg.get("Subject", ""))
        if "OutOfRouteBuddy" not in subj and "OutOfRoute" not in subj and "Re:" not in subj:
            continue
        # Prefer replies (Re:)
        if "Re:" in subj or "RE:" in subj.upper():
            body = get_body(msg)
            date_str = msg.get("Date", "")
            latest_subject = subj
            latest_body = body
            latest_date = date_str
            # Mark as read so we don't reprocess
            mail.store(mid, "+FLAGS", "\\Seen")
            break

    mail.logout()

    return latest_subject, latest_body, latest_date


def main():
    out_dir = os.path.dirname(__file__)
    out_file = os.path.join(out_dir, "last_reply.txt")
    try:
        subject, body, date = read_replies()
        if not body and not subject:
            print("No reply found (no message with Re: and OutOfRouteBuddy in subject).", file=sys.stderr)
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
        print(f"Latest reply written to {out_file}")
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
