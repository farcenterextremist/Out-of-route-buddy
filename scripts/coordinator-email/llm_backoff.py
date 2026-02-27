#!/usr/bin/env python3
"""
LLM backoff and circuit breaker. On repeated LLM failures, skip compose to avoid
hammering a failing provider. State stored in llm_backoff_state.json.
"""

import json
import os
import time

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
STATE_FILE = os.path.join(SCRIPT_DIR, "llm_backoff_state.json")


def _load_env() -> dict:
    """Load .env from coordinator-email dir."""
    env_path = os.path.join(SCRIPT_DIR, ".env")
    if not os.path.isfile(env_path):
        return {}
    env = {}
    try:
        with open(env_path, encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith("#") and "=" in line:
                    k, v = line.split("=", 1)
                    env[k.strip()] = v.strip().strip('"').strip("'")
    except OSError:
        pass
    return env


def _get_backoff_threshold() -> int:
    """Failures before backoff. Env: JARVEY_LLM_BACKOFF_THRESHOLD (default 3)."""
    env = _load_env()
    val = env.get("JARVEY_LLM_BACKOFF_THRESHOLD") or os.environ.get("JARVEY_LLM_BACKOFF_THRESHOLD", "")
    if not val:
        return 3
    try:
        return max(1, min(int(val), 20))
    except ValueError:
        return 3


def _get_circuit_open_threshold() -> int:
    """Failures before circuit opens. Env: JARVEY_LLM_CIRCUIT_OPEN_THRESHOLD (default 5)."""
    env = _load_env()
    val = env.get("JARVEY_LLM_CIRCUIT_OPEN_THRESHOLD") or os.environ.get("JARVEY_LLM_CIRCUIT_OPEN_THRESHOLD", "")
    if not val:
        return 5
    try:
        return max(1, min(int(val), 50))
    except ValueError:
        return 5


def _get_circuit_reset_seconds() -> int:
    """Seconds before circuit resets. Env: JARVEY_LLM_CIRCUIT_RESET_SECONDS (default 3600)."""
    env = _load_env()
    val = env.get("JARVEY_LLM_CIRCUIT_RESET_SECONDS") or os.environ.get("JARVEY_LLM_CIRCUIT_RESET_SECONDS", "")
    if not val:
        return 3600
    try:
        return max(60, min(int(val), 86400))
    except ValueError:
        return 3600


def _load_state() -> dict:
    if not os.path.isfile(STATE_FILE):
        return {"failures": 0, "last_failure_ts": 0, "last_success_ts": 0}
    try:
        with open(STATE_FILE, encoding="utf-8") as f:
            return json.load(f)
    except (json.JSONDecodeError, OSError):
        return {"failures": 0, "last_failure_ts": 0, "last_success_ts": 0}


def _save_state(state: dict) -> None:
    try:
        with open(STATE_FILE, "w", encoding="utf-8") as f:
            json.dump(state, f, indent=2)
    except OSError:
        pass


def record_failure() -> None:
    """Increment failure count and set last_failure_ts."""
    state = _load_state()
    state["failures"] = state.get("failures", 0) + 1
    state["last_failure_ts"] = time.time()
    _save_state(state)


def record_success() -> None:
    """Reset failure count and set last_success_ts."""
    state = _load_state()
    state["failures"] = 0
    state["last_success_ts"] = time.time()
    _save_state(state)


def reset_circuit() -> None:
    """Reset circuit breaker state (failures to 0). Use when Ollama is fixed and ready again."""
    state = _load_state()
    state["failures"] = 0
    state["last_success_ts"] = time.time()
    _save_state(state)


def get_backoff_seconds() -> int:
    """Return seconds to wait before retry; 0 if no backoff needed."""
    state = _load_state()
    failures = state.get("failures", 0)
    threshold = _get_backoff_threshold()
    reset_sec = _get_circuit_reset_seconds()
    if failures < threshold:
        return 0
    return min(60 * (2 ** (failures - threshold)), reset_sec)


def is_circuit_open() -> bool:
    """True if circuit should be open (skip LLM calls)."""
    state = _load_state()
    failures = state.get("failures", 0)
    last_failure = state.get("last_failure_ts", 0)
    threshold = _get_circuit_open_threshold()
    reset_sec = _get_circuit_reset_seconds()
    if failures < threshold:
        return False
    if (time.time() - last_failure) >= reset_sec:
        return False  # circuit resets after timeout
    return True
