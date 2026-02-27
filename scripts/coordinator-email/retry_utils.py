#!/usr/bin/env python3
"""
Retry decorator for IMAP/SMTP transient failures.
Uses exponential backoff. Configurable via MAX_RETRIES and RETRY_DELAY in .env.
"""

import functools
import os
import time
from typing import Callable, TypeVar

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

T = TypeVar("T")

# Transient errors that warrant a retry
TRANSIENT_EXCEPTIONS = (
    ConnectionError,
    TimeoutError,
    OSError,
)
# Also match by message for smtplib/imaplib which may wrap errors
TRANSIENT_PATTERNS = ("connection", "timeout", "temporarily", "try again", "reset", "refused")


def _is_transient(e: BaseException) -> bool:
    """Return True if exception looks transient."""
    if type(e) in TRANSIENT_EXCEPTIONS:
        return True
    msg = str(e).lower()
    return any(p in msg for p in TRANSIENT_PATTERNS)


def _get_retry_config() -> tuple[int, float]:
    """Read MAX_RETRIES and RETRY_DELAY from .env. Returns (max_retries, base_delay_sec)."""
    max_retries = 3
    retry_delay = 2.0
    env_path = os.path.join(SCRIPT_DIR, ".env")
    if os.path.isfile(env_path):
        try:
            with open(env_path, encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if line.startswith("#") or "=" not in line:
                        continue
                    k, v = line.split("=", 1)
                    v = v.strip().strip('"\'')
                    if k.strip() == "JARVEY_MAX_RETRIES":
                        max_retries = max(0, min(10, int(v)))
                    elif k.strip() == "JARVEY_RETRY_DELAY":
                        retry_delay = max(0.5, min(60.0, float(v)))
        except (ValueError, OSError):
            pass
    return max_retries, retry_delay


def with_retry(func: Callable[..., T]) -> Callable[..., T]:
    """
    Decorator that retries on transient failures with exponential backoff.
    Use on read_replies, send, or similar I/O functions.
    """

    @functools.wraps(func)
    def wrapper(*args: object, **kwargs: object) -> T:
        max_retries, base_delay = _get_retry_config()
        last_exc = None
        for attempt in range(max_retries + 1):
            try:
                return func(*args, **kwargs)
            except Exception as e:
                last_exc = e
                if attempt >= max_retries or not _is_transient(e):
                    raise
                delay = base_delay * (2**attempt)
                time.sleep(delay)
        raise last_exc  # type: ignore

    return wrapper  # type: ignore
