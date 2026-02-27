#!/usr/bin/env python3
"""
Trace Jarvey's workflow for one cycle: read → decide → compose → send.
Use to see or record what Jarvey does. Writes to jarvey_workflow.log by default.

Usage:
  python trace_jarvey_workflow.py              # Run once, log to jarvey_workflow.log
  python trace_jarvey_workflow.py --out path  # Log to custom path
  python trace_jarvey_workflow.py --dry-run   # Compose but don't send (no SMTP)
  python trace_jarvey_workflow.py --no-send   # Same as --dry-run
"""

import os
import sys
import time
from datetime import datetime

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

DEFAULT_LOG = os.path.join(SCRIPT_DIR, "jarvey_workflow.log")


def log(msg: str, log_path: str, also_print: bool = True):
    ts = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    line = f"[{ts}] {msg}"
    if also_print:
        print(line, file=sys.stderr)
    if log_path:
        try:
            with open(log_path, "a", encoding="utf-8") as f:
                f.write(line + "\n")
        except OSError:
            pass


def main():
    args = sys.argv[1:]
    log_path = DEFAULT_LOG
    dry_run = "--dry-run" in args or "--no-send" in args
    if "--out" in args:
        i = args.index("--out")
        if i + 1 < len(args):
            log_path = args[i + 1]

    log(f"--- Jarvey workflow trace (dry_run={dry_run}) ---", log_path)
    log(f"Log file: {log_path}", log_path)

    import coordinator_listener as cl

    env = cl.load_env()
    api_key = env.get("COORDINATOR_LISTENER_OPENAI_API_KEY") or env.get("OPENAI_API_KEY")
    ollama_url = env.get("COORDINATOR_LISTENER_OLLAMA_URL") or env.get("OLLAMA_URL")

    if not api_key and not ollama_url:
        log("ERROR: No LLM configured. Set OPENAI_API_KEY or OLLAMA_URL in .env", log_path)
        sys.exit(1)

    # Step 1: Read inbox
    log("Step 1: Reading inbox (read_replies)...", log_path)
    try:
        from read_replies import read_replies

        subject, body, date, message_id = read_replies()
    except Exception as e:
        log(f"ERROR reading inbox: {e}", log_path)
        sys.exit(1)

    if not subject and not body:
        log("No message found. (No message from user in inbox.)", log_path)
        log("--- End trace (nothing to do) ---", log_path)
        sys.exit(0)

    body_trim = cl._strip_quoted_content(body or "").strip()
    subj_trim = (subject or "").strip()
    log(f"Found: subject={subj_trim[:60]!r} body={body_trim[:100]!r}...", log_path)
    log(f"  date={date} message_id={message_id}", log_path)

    if not subj_trim and not body_trim:
        log("Message empty after stripping quoted content. Skipping.", log_path)
        sys.exit(0)

    # Step 2: Dedupe
    import hashlib

    dedupe_id = message_id or (
        "hash:" + hashlib.sha256((subj_trim + "\n" + (body_trim or "")[:500]).encode("utf-8", errors="replace")).hexdigest()[:32]
    )
    last_id = cl.load_last_responded_id()
    if last_id and dedupe_id == last_id:
        log(f"Dedupe: Already responded to {dedupe_id[:50]}... Skipping.", log_path)
        log("--- End trace (dedupe) ---", log_path)
        sys.exit(0)

    if cl.last_sent_within_cooldown():
        log("Cooldown: Last send was < 2 min ago. Skipping.", log_path)
        log("--- End trace (cooldown) ---", log_path)
        sys.exit(0)

    # Step 3: Compose via LLM (all replies use LLM with full project context; no templates)
    backend = "OpenAI" if api_key else "Ollama"
    log(f"Step 2: Composing via {backend}...", log_path)
    from context_loader import detect_intents
    intents = detect_intents(subject or "", body_trim or "")
    t0 = time.time()
    try:
        reply_body = cl.compose_reply(subject or "", body_trim or "", env, intents=intents)
    except Exception as e:
        log(f"ERROR composing: {e}", log_path)
        sys.exit(1)
    elapsed = time.time() - t0
    log(f"Composed in {elapsed:.1f}s. Reply length: {len(reply_body)} chars", log_path)

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

    reply_body = cl._ensure_jarvey_signoff(reply_body)
    s = subject or ""
    reply_subj = s if s.strip().lower().startswith("re:") else f"Re: {s}"
    log(f"Reply subject: {reply_subj[:60]!r}", log_path)
    log(f"Reply body:\n---\n{reply_body}\n---", log_path)

    if dry_run:
        log("[DRY-RUN] Would send. Not sending.", log_path)
        log("--- End trace (dry-run) ---", log_path)
        sys.exit(0)

    # Step 4: Send
    log("Step 3: Sending...", log_path)
    try:
        from send_email import send

        send(reply_subj, reply_body)
        cl.save_responded_id(dedupe_id)
        cl.write_sent_timestamp()
        log("SENT.", log_path)
    except Exception as e:
        log(f"ERROR sending: {e}", log_path)
        sys.exit(1)

    log("--- End trace (sent) ---", log_path)


if __name__ == "__main__":
    main()
