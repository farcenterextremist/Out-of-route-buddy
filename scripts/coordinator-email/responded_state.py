#!/usr/bin/env python3
"""
Shared state for Jarvey response deduplication and rate limiting.
Used by coordinator_listener and check_and_respond so they don't double-respond.
"""

import os
import time

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
STATE_FILE = os.path.join(SCRIPT_DIR, "last_responded_state.txt")
COOLDOWN_FILE = os.path.join(SCRIPT_DIR, "last_sent_timestamp.txt")
REPLIES_PER_HOUR_FILE = os.path.join(SCRIPT_DIR, "replies_sent_timestamps.txt")


def _get_cooldown_seconds():
    """Cooldown in seconds; configurable via JARVEY_COOLDOWN_SECONDS env (default 120)."""
    try:
        return int(os.environ.get("JARVEY_COOLDOWN_SECONDS", "120"))
    except ValueError:
        return 120


# Backward compat: COOLDOWN_SECONDS used by diagnose_jarvey
def get_cooldown_seconds():
    return _get_cooldown_seconds()


def load_last_responded_id():
    if not os.path.isfile(STATE_FILE):
        return None
    with open(STATE_FILE, encoding="utf-8") as f:
        line = f.read().strip()
    return line or None


def save_responded_id(message_id):
    with open(STATE_FILE, "w", encoding="utf-8") as f:
        f.write(message_id or "")


def last_sent_within_cooldown():
    """True if we sent an email less than cooldown seconds ago (hard rate limit)."""
    if not os.path.isfile(COOLDOWN_FILE):
        return False
    try:
        with open(COOLDOWN_FILE, encoding="utf-8") as f:
            ts = float(f.read().strip())
        return (time.time() - ts) < _get_cooldown_seconds()
    except (ValueError, OSError):
        return False


def write_sent_timestamp():
    with open(COOLDOWN_FILE, "w", encoding="utf-8") as f:
        f.write(str(time.time()))
    # Append to per-hour tracking for optional rate cap (coordinator_listener)
    try:
        with open(REPLIES_PER_HOUR_FILE, "a", encoding="utf-8") as f:
            f.write(str(time.time()) + "\n")
    except OSError:
        pass


def replies_in_last_hour():
    """Count replies sent in the last 3600 seconds. Used for optional per-hour cap."""
    if not os.path.isfile(REPLIES_PER_HOUR_FILE):
        return 0
    try:
        with open(REPLIES_PER_HOUR_FILE, encoding="utf-8") as f:
            lines = f.readlines()
        now = time.time()
        cutoff = now - 3600
        count = 0
        valid_lines = []
        for line in lines:
            line = line.strip()
            if not line:
                continue
            try:
                ts = float(line)
                if ts >= cutoff:
                    count += 1
                    valid_lines.append(line + "\n")
            except ValueError:
                pass
        # Prune old entries
        if len(valid_lines) < len(lines):
            try:
                with open(REPLIES_PER_HOUR_FILE, "w", encoding="utf-8") as f:
                    f.writelines(valid_lines)
            except OSError:
                pass
        return count
    except (OSError, ValueError):
        return 0
