#!/usr/bin/env python3
"""
Diagnose why "Same-inbox mode: FROM and TO are the same address" appears
when you have different addresses configured.

Run from this folder: python diagnose_from_to.py

Shows raw and normalized COORDINATOR_EMAIL_FROM / COORDINATOR_EMAIL_TO
and whether same-inbox is triggered (and why).
"""

import os
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

from read_replies import load_env, _normalize_email


def main():
    env = load_env()
    from_raw = env.get("COORDINATOR_EMAIL_FROM", "").strip()
    to_raw = env.get("COORDINATOR_EMAIL_TO", "").strip()

    our_from = from_raw
    if our_from:
        our_from = _normalize_email(our_from) or our_from
    user_emails = set()
    if to_raw:
        for addr in to_raw.split(","):
            a = _normalize_email(addr.strip())
            if a:
                user_emails.add(a)

    print("=== FROM / TO diagnostic ===\n")
    print("From .env (raw):")
    print(f"  COORDINATOR_EMAIL_FROM = {env.get('COORDINATOR_EMAIL_FROM', '')!r}")
    print(f"  COORDINATOR_EMAIL_TO   = {env.get('COORDINATOR_EMAIL_TO', '')!r}")
    print()
    print("After normalization (used by read_replies):")
    print(f"  our_from (FROM)    = {our_from!r}")
    print(f"  user_emails (TO)   = {sorted(user_emails)!r}")
    print()

    # Current logic: same-inbox only when single TO equals FROM
    same_inbox = bool(our_from) and len(user_emails) == 1 and our_from in user_emails
    # Old logic (would trigger if FROM was in TO list at all)
    old_same_inbox = bool(our_from) and bool(user_emails) and our_from in user_emails

    print("Same-inbox detection:")
    print(f"  Current (single TO equals FROM): {same_inbox}")
    if old_same_inbox and not same_inbox:
        print(f"  Previous logic (FROM in TO list):  True  <- was causing the message")
        print()
        print("Cause: COORDINATOR_EMAIL_TO contained multiple addresses including")
        print("       COORDINATOR_EMAIL_FROM. Same-inbox is now only when there is")
        print("       one TO address and it equals FROM.")
    elif same_inbox:
        print("  -> FROM and TO are the same single address (same-inbox mode).")
    else:
        print("  -> FROM and TO differ (or TO has multiple addresses). No same-inbox message.")
    print()
    return 0


if __name__ == "__main__":
    sys.exit(main())
