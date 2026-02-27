#!/usr/bin/env python3
"""
Send an email on behalf of the Human-in-the-Loop Manager.
Usage: python send_email.py "Subject" "Body text"
Requires .env with COORDINATOR_EMAIL_* and COORDINATOR_SMTP_* variables (see README).
"""

import os
import sys
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

from retry_utils import with_retry


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


@with_retry
def send(subject: str, body: str, dry_run: bool = False, to_addr: str | None = None) -> None:
    """
    Send an email. If dry_run is True, or env COORDINATOR_DRY_RUN is set (1, true, yes),
    log and return without sending (for tests / integration).
    to_addr: optional override; when None, uses COORDINATOR_EMAIL_TO (or COORDINATOR_EMAIL_COWORKER/FAMILY when send_to intent).
    """
    env = load_env()
    dry_run = dry_run or (env.get("COORDINATOR_DRY_RUN", "").strip().lower() in ("1", "true", "yes"))
    to_addr = to_addr or env.get("COORDINATOR_EMAIL_TO")
    if dry_run:
        print(f"[DRY RUN] Would send to {to_addr or '?'}: subject={subject!r}, body length={len(body)}", file=sys.stderr)
        return
    from_addr = env.get("COORDINATOR_EMAIL_FROM") or to_addr
    host = env.get("COORDINATOR_SMTP_HOST")
    port = int(env.get("COORDINATOR_SMTP_PORT", "587"))
    user = env.get("COORDINATOR_SMTP_USER")
    password = env.get("COORDINATOR_SMTP_PASSWORD")

    if not to_addr or not host or not user or not password:
        missing = [
            k for k, v in [
                ("COORDINATOR_EMAIL_TO", to_addr),
                ("COORDINATOR_SMTP_HOST", host),
                ("COORDINATOR_SMTP_USER", user),
                ("COORDINATOR_SMTP_PASSWORD", password),
            ] if not v
        ]
        raise RuntimeError(
            "Missing required env vars. Copy .env.example to .env and set: " + ", ".join(missing)
        )

    msg = MIMEMultipart("alternative")
    msg["Subject"] = subject
    msg["From"] = from_addr
    msg["To"] = to_addr
    msg["X-OutOfRouteBuddy-Sent"] = "true"
    msg.attach(MIMEText(body, "plain"))

    with smtplib.SMTP(host, port) as server:
        server.starttls()
        server.login(user, password)
        server.sendmail(from_addr, [to_addr], msg.as_string())


def main():
    if len(sys.argv) < 2:
        print("Usage: python send_email.py \"Subject\" \"Body text\" | \"Subject\" @path/to/body.txt | \"Subject\" -f path/to/body.txt", file=sys.stderr)
        sys.exit(1)
    subject = sys.argv[1]
    body = ""
    if len(sys.argv) >= 3:
        body_arg = sys.argv[2]
        if body_arg == "-f" and len(sys.argv) >= 4:
            path = os.path.normpath(os.path.join(os.getcwd(), sys.argv[3]))
            if not os.path.isfile(path):
                path = os.path.normpath(os.path.join(os.path.dirname(__file__), sys.argv[3]))
            with open(path, encoding="utf-8") as f:
                body = f.read()
        elif body_arg.startswith("@"):
            path = os.path.expanduser(body_arg[1:])
            if not os.path.isabs(path):
                path = os.path.join(os.path.dirname(__file__), path)
            with open(path, encoding="utf-8") as f:
                body = f.read()
        else:
            body = body_arg
    if not body or not body.strip():
        print("Error: Email body is empty. Refusing to send.", file=sys.stderr)
        sys.exit(1)
    try:
        send(subject, body)
        print("Email sent successfully.")
    except Exception as e:
        print(f"Failed to send email: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
