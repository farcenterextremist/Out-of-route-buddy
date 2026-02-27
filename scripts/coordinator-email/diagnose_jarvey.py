#!/usr/bin/env python3
"""
Jarvey diagnosis script — runs the diagnostic steps from the Jarvey Diagnosis Plan.
Use when Jarvey is not responding to messages.

Usage:
  python diagnose_jarvey.py              # Run all diagnostics, report findings
  python diagnose_jarvey.py --fix-dedupe # Clear state file to unblock (use with care)
  python diagnose_jarvey.py --reset-circuit # Reset LLM circuit breaker (when Ollama is fixed)
"""

import json
import os
import sys
import time

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

from responded_state import STATE_FILE, COOLDOWN_FILE, get_cooldown_seconds


def _log(msg: str):
    print(msg, file=sys.stderr)


def step1_read_inbox():
    """Step 1: Run read_replies to see if any message is found."""
    _log("\n--- Step 1: Inbox read (read_replies.py) ---")
    try:
        from read_replies import read_replies

        subject, body, date, message_id = read_replies()
        if not subject and not body:
            _log("Result: No message found.")
            _log("  -> Check: message must be FROM COORDINATOR_EMAIL_TO (Re: and Fwd: both work)")
            _log("  -> Reply vs original: When replying, ensure you reply FROM the same address as COORDINATOR_EMAIL_TO")
            _log("  -> Tip: Use comma-separated COORDINATOR_EMAIL_TO=addr1,addr2 to accept replies from multiple addresses")
            return None, None, None, None
        _log(f"Result: Found message")
        _log(f"  subject: {subject[:60]!r}...")
        _log(f"  body snippet: {(body or '')[:80]!r}...")
        _log(f"  message_id: {message_id}")
        return subject, body, date, message_id
    except Exception as e:
        _log(f"ERROR: {e}")
        return None, None, None, None


def step2_inspect_state():
    """Step 2: Inspect last_responded_state.txt."""
    _log("\n--- Step 2: Dedupe state (last_responded_state.txt) ---")
    if not os.path.isfile(STATE_FILE):
        _log("State file: not present (no previous response)")
        return None
    with open(STATE_FILE, encoding="utf-8") as f:
        last_id = f.read().strip() or None
    if not last_id:
        _log("State file: empty")
        return None
    _log(f"State file contains: {last_id[:60]}...")
    return last_id


def step3_inspect_cooldown():
    """Step 3: Inspect last_sent_timestamp.txt."""
    _log("\n--- Step 3: Cooldown (last_sent_timestamp.txt) ---")
    if not os.path.isfile(COOLDOWN_FILE):
        _log("Cooldown file: not present (no recent send)")
        return False
    try:
        with open(COOLDOWN_FILE, encoding="utf-8") as f:
            ts = float(f.read().strip())
        elapsed = time.time() - ts
        cooldown_sec = get_cooldown_seconds()
        in_cooldown = elapsed < cooldown_sec
        _log(f"Last send: {elapsed:.0f}s ago")
        if in_cooldown:
            _log(f"  -> IN COOLDOWN ({cooldown_sec - elapsed:.0f}s remaining)")
        else:
            _log("  -> Not in cooldown")
        return in_cooldown
    except (ValueError, OSError) as e:
        _log(f"Cooldown file error: {e}")
        return False


def step4_dedupe_check(message_id, last_id):
    """Step 4: Would we skip due to dedupe?"""
    _log("\n--- Step 4: Dedupe check ---")
    if not message_id:
        _log("No message_id (would use content hash for dedupe)")
        return False
    if not last_id:
        _log("No last_responded_state — would NOT skip")
        return False
    # Normalize: Message-IDs may have angle brackets; state might have been saved with or without
    mid_norm = message_id.strip().strip("<>")
    last_norm = last_id.strip().strip("<>")
    would_skip = mid_norm == last_norm or message_id == last_id
    if would_skip:
        _log(f"WOULD SKIP: message_id matches last_responded_state")
        _log(f"  message_id: {message_id[:50]}...")
        _log(f"  last_id:    {last_id[:50]}...")
    else:
        _log("Would NOT skip (different message)")
    return would_skip


def main():
    from config_schema import validate_config
    try:
        validate_config(mode="email", exit_on_error=False)
    except ValueError:
        _log("Config validation failed (missing .env or SMTP/IMAP). Some steps may fail.")

    fix_dedupe = "--fix-dedupe" in sys.argv
    reset_circuit_flag = "--reset-circuit" in sys.argv

    if reset_circuit_flag:
        _log("=" * 60)
        _log("Jarvey Diagnosis — Reset Circuit")
        _log("=" * 60)
        try:
            from llm_backoff import reset_circuit
            reset_circuit()
            _log("Circuit breaker reset. LLM calls will be attempted again.")
        except Exception as e:
            _log(f"ERROR: {e}")
        _log("\n" + "=" * 60)
        sys.exit(0)

    _log("=" * 60)
    _log("Jarvey Diagnosis")
    _log("=" * 60)

    subject, body, date, message_id = step1_read_inbox()
    last_id = step2_inspect_state()
    in_cooldown = step3_inspect_cooldown()
    would_skip_dedupe = step4_dedupe_check(message_id, last_id)

    _log("\n--- Summary ---")
    if not subject and not body:
        _log("No message found. Jarvey has nothing to respond to.")
        _log("  -> Ensure message is from COORDINATOR_EMAIL_TO (Re: and Fwd: both work)")
        sys.exit(1)
    if would_skip_dedupe:
        _log("Dedupe would block: we think we already responded to this message.")
        if fix_dedupe:
            _log("Clearing state (--fix-dedupe)...")
            if os.path.isfile(STATE_FILE):
                os.remove(STATE_FILE)
                _log("State cleared. Restart Jarvey or run trace_jarvey_workflow.py")
            else:
                _log("State file already absent.")
        else:
            _log("  -> To force a reply: python diagnose_jarvey.py --fix-dedupe")
            _log("  -> WARNING: May cause duplicate reply if we did send.")
    elif in_cooldown:
        _log("Cooldown active: wait 2 minutes or clear last_sent_timestamp.txt")
    else:
        _log("Pipeline should proceed. Run: python trace_jarvey_workflow.py")
        _log("  Or ensure coordinator_listener.py is running.")

    _log("\n" + "=" * 60)


if __name__ == "__main__":
    main()
