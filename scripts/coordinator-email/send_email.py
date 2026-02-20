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


def send(subject: str, body: str) -> None:
    env = load_env()
    to_addr = env.get("COORDINATOR_EMAIL_TO")
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
    msg.attach(MIMEText(body, "plain"))

    with smtplib.SMTP(host, port) as server:
        server.starttls()
        server.login(user, password)
        server.sendmail(from_addr, [to_addr], msg.as_string())


def main():
    if len(sys.argv) < 3:
        print("Usage: python send_email.py \"Subject\" \"Body text\" or \"Subject\" @path/to/body.txt", file=sys.stderr)
        sys.exit(1)
    subject = sys.argv[1]
    body_arg = sys.argv[2]
    if body_arg.startswith("@"):
        path = os.path.expanduser(body_arg[1:])
        if not os.path.isabs(path):
            path = os.path.join(os.path.dirname(__file__), path)
        with open(path, encoding="utf-8") as f:
            body = f.read()
    else:
        body = body_arg
    try:
        send(subject, body)
        print("Email sent successfully.")
    except Exception as e:
        print(f"Failed to send email: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
