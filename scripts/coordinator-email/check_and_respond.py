#!/usr/bin/env python3
"""
Check for a new reply from the user and send an automatic response via LLM.
Use with a scheduled task (e.g. every 15–30 min) so replies are answered without starting a session.

All replies are composed by the LLM with full project context (no preset templates).

Usage:
  python check_and_respond.py           → read inbox; if new reply, compose via LLM and send
  python check_and_respond.py --dry-run → compose via LLM but write to draft file only (no send)
  python check_and_respond.py --once    → same as default (for cron); exit 0 if no new mail or already responded
  python check_and_respond.py --log     → append workflow steps to jarvey_workflow.log (or set JARVEY_LOG=1 in .env)

State: last_responded_state.txt in this folder stores the message_id we last replied to so we don't double-respond.
"""

import hashlib
import os
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

from coordinator_listener import _strip_quoted_content, compose_reply, _ensure_jarvey_signoff
from read_replies import read_replies
from send_email import send
from context_loader import detect_intents
from responded_state import (
    STATE_FILE,
    load_last_responded_id,
    save_responded_id,
    last_sent_within_cooldown,
    write_sent_timestamp,
)

DRAFT_FILE = os.path.join(SCRIPT_DIR, "auto_reply_draft.txt")


def _log_workflow(msg: str) -> None:
    """Log to jarvey_workflow.log when JARVEY_LOG=1 (live workflow documentation)."""
    try:
        from jarvey_log import log_workflow
        log_workflow(f"check_and_respond: {msg}")
    except Exception:
        pass


def main():
    dry_run = "--dry-run" in sys.argv
    if "--log" in sys.argv:
        os.environ["JARVEY_LOG"] = "1"

    _log_workflow("Reading inbox...")
    try:
        subject, body, date, message_id = read_replies()
    except Exception as e:
        err_msg = str(e).lower()
        hint = ""
        if "authenticationfailed" in err_msg or "invalid credentials" in err_msg:
            hint = (
                " — Check COORDINATOR_IMAP_USER/IMAP_PASSWORD (or SMTP_USER/SMTP_PASSWORD) in .env; "
                "use an app password if using Gmail. Ensure IMAP is enabled for the account."
            )
        _log_workflow(f"Error reading inbox: {e}{hint}")
        print(f"Error reading inbox: {e}{hint}", file=sys.stderr)
        sys.exit(1)

    if not subject and not body:
        _log_workflow("No message found. Nothing to respond to.")
        if not dry_run:
            print("No reply found. Nothing to respond to.")
        sys.exit(0)

    # Dedupe: use message_id when present; otherwise content hash (align with coordinator_listener)
    subj_trim = (subject or "").strip()
    body_trim = _strip_quoted_content(body or "").strip()
    dedupe_id = message_id
    if not dedupe_id:
        blob = (subj_trim or "") + "\n" + (body_trim or "")[:500]
        dedupe_id = "hash:" + hashlib.sha256(blob.encode("utf-8", errors="replace")).hexdigest()[:32]

    last_id = load_last_responded_id()
    if last_id and dedupe_id == last_id:
        _log_workflow("Dedupe: already responded to this message. Skipping.")
        if not dry_run:
            print("Already responded to this message. Skipping.")
        sys.exit(0)

    _log_workflow(f"Found: subject={subj_trim[:60]!r} body={body_trim[:100]!r}...")

    try:
        from config_schema import validate_config
        env = validate_config(mode="listener", exit_on_error=True)
    except SystemExit:
        raise
    except Exception as e:
        print(f"Config error: {e}", file=sys.stderr)
        sys.exit(1)
    # Compose via LLM with full project context
    _log_workflow("Composing via LLM...")
    intents = detect_intents(subject or "", body_trim or "")
    try:
        reply_body = compose_reply(subject or "", body_trim or "", env, intents=intents)
    except Exception as e:
        _log_workflow(f"Error composing reply: {e}")
        print(f"Error composing reply: {e}", file=sys.stderr)
        sys.exit(1)

    # Structured output: execute save_note, send_digest, clarify if LLM returned JSON action
    try:
        from structured_output import parse_structured_reply, execute_action, strip_structured_from_reply
        parsed = parse_structured_reply(reply_body or "")
        if parsed:
            action_body = execute_action(parsed["action"], parsed.get("params") or {}, env)
            if parsed["action"] in ("save_note", "assign_work", "add_to_timeline"):
                reply_body = strip_structured_from_reply(reply_body or "")
            elif action_body:
                reply_body = action_body
    except Exception:
        pass

    reply_body = _ensure_jarvey_signoff(reply_body)
    s = subject or ""
    reply_subj = s if s.strip().lower().startswith("re:") else f"Re: {s}"

    body_snippet = (body or "")[:80].replace("\n", " ")
    if len(body or "") > 80:
        body_snippet += "..."
    print(f"Chosen LLM (subject: {(subject or '')[:50]!r}, body: {body_snippet!r})", file=sys.stderr)

    if dry_run:
        with open(DRAFT_FILE, "w", encoding="utf-8") as f:
            f.write(f"Subject: {reply_subj}\n\n{reply_body}")
        print(f"Dry run: draft written to {DRAFT_FILE}. No email sent.")
        sys.exit(0)

    # Hard rate limit: do not send if we (or coordinator_listener) sent in last 2 min
    if last_sent_within_cooldown():
        _log_workflow("Cooldown: last send < 2 min ago. Skipping.")
        print("Skipping send: last email was less than 2 minutes ago (rate limit).", file=sys.stderr)
        sys.exit(0)

    try:
        send(reply_subj, reply_body)
        save_responded_id(dedupe_id)
        write_sent_timestamp()
        try:
            from jarvey_choices import log_user_choice
            from conversation_memory import get_thread_id
            tid = get_thread_id(subject or "", message_id)
            log_user_choice(body_trim or "", body_trim or "", tid)
        except Exception:
            pass
        _log_workflow("SENT. Responded via LLM.")
        print("Auto-reply sent successfully. Responded via LLM.")
    except Exception as e:
        _log_workflow(f"Failed to send: {e}")
        print(f"Failed to send auto-reply: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
