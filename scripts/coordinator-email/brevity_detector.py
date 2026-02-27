#!/usr/bin/env python3
"""
Brevity detector: decide if a short user message should trigger the "unclear" template
or be treated as valid (e.g. menu choice "6", "ok", "what's next?").

Supports optional integration with the implicature-intent library for short messages
that are not on the whitelist (see docs/agents/JARVEY_BREVITY_DETECTION_RESEARCH.md).
"""

import os
from typing import Sequence

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))


def is_brief(body: str, max_len: int = 15) -> bool:
    """Return True if body is non-empty and shorter than max_len (after strip)."""
    if not body:
        return False
    return len((body or "").strip()) < max_len


def get_default_whitelist() -> tuple[str, ...]:
    """Return the default whitelist of short phrases that are never treated as unclear."""
    return (
        "test", "hi", "ok", "ready", "testing", "hello", "thankful", "i'm thankful",
        "what's next?", "what's next", "tell me something", "tell me anything",
        "share something", "give me an update", "update me", "news?", "status?",
        "catch me up", "fill me in", "anything new?", "what's new?", "quick summary",
        "what's happening?", "what's the latest?",
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
    )


def _normalize(s: str) -> str:
    return (s or "").strip().lower()


def is_whitelisted(body: str, whitelist: Sequence[str] | None = None) -> bool:
    """Return True if body (normalized) is in the whitelist."""
    if whitelist is None:
        whitelist = get_default_whitelist()
    key = _normalize(body)
    if not key:
        return False
    return key in tuple(_normalize(w) for w in whitelist)


def _implicature_needs_clarification(body: str) -> bool | None:
    """
    If implicature-intent is installed, return whether the library says this message
    needs clarification. Returns None if the library is not available or errors.
    """
    try:
        from implicature_intent import analyze_intent  # type: ignore[import-untyped]
        result = analyze_intent(body)
        return bool(result.get("needs_clarification", True))
    except Exception:
        return None


def should_use_unclear_template(
    body: str,
    *,
    max_len: int = 15,
    whitelist: Sequence[str] | None = None,
    use_implicature: bool = False,
) -> bool:
    """
    Return True if this message should get the "unclear" clarification template.

    Logic:
    - If body is not brief (length >= max_len), return False (not unclear).
    - If body is whitelisted (e.g. "6", "ok", "what's next?"), return False.
    - If use_implicature is True and implicature-intent is installed, ask the library:
      if it says needs_clarification is False with reasonable confidence, return False.
    - Otherwise return True (use unclear template).
    """
    body_stripped = (body or "").strip()
    if not body_stripped:
        return False
    if len(body_stripped) >= max_len:
        return False
    if is_whitelisted(body_stripped, whitelist):
        return False
    if use_implicature:
        needs = _implicature_needs_clarification(body_stripped)
        if needs is False:
            return False
        if needs is True:
            return True
        # None: library unavailable or error → fall through to default (unclear)
    return True


def is_capability_menu_number(body: str) -> bool:
    """Return True if body is exactly a capability menu choice (e.g. '6')."""
    return _normalize(body) in ("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")


def load_use_implicature_from_env() -> bool:
    """Read JARVEY_BREVITY_USE_IMPLICATURE from .env; default False."""
    try:
        env_path = os.path.join(SCRIPT_DIR, ".env")
        if os.path.isfile(env_path):
            with open(env_path, encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if line.startswith("JARVEY_BREVITY_USE_IMPLICATURE") and "=" in line:
                        v = line.split("=", 1)[1].strip().strip('"\'').lower()
                        return v in ("1", "true", "yes")
    except OSError:
        pass
    return False


if __name__ == "__main__":
    # Quick tests
    assert is_brief("6", max_len=15) is True
    assert is_brief("hello world this is long", max_len=15) is False
    assert is_whitelisted("6") is True
    assert is_whitelisted(" 6 ") is True
    assert is_whitelisted("xyz") is False
    assert should_use_unclear_template("6", max_len=15) is False
    assert should_use_unclear_template("ok") is False
    assert should_use_unclear_template("x") is True
    print("brevity_detector: OK")
