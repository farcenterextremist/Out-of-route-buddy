#!/usr/bin/env python3
"""
Config validation for Jarvey coordinator-email system.
Validates .env and environment variables at startup.
Exits with clear errors if SMTP/IMAP/LLM are misconfigured.
"""

import os
import sys
from typing import Any

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))


def load_env() -> dict[str, str]:
    """Load .env from coordinator-email dir. Returns dict of key=value."""
    env_path = os.path.join(SCRIPT_DIR, ".env")
    if not os.path.isfile(env_path):
        return {}
    env: dict[str, str] = {}
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


# Schema: (key, required, type, description)
EMAIL_REQUIRED = [
    ("COORDINATOR_EMAIL_TO", True, str, "Recipient email address"),
    ("COORDINATOR_EMAIL_FROM", True, str, "Sender email address"),
    ("COORDINATOR_SMTP_HOST", True, str, "SMTP host (e.g. smtp.gmail.com)"),
    ("COORDINATOR_SMTP_PORT", True, (int, str), "SMTP port (e.g. 587)"),
    ("COORDINATOR_SMTP_USER", True, str, "SMTP username"),
    ("COORDINATOR_SMTP_PASSWORD", True, str, "SMTP app password"),
]

IMAP_OPTIONAL = [
    ("COORDINATOR_IMAP_HOST", False, str, "IMAP host (defaults to smtp host)"),
    ("COORDINATOR_IMAP_PORT", False, (int, str), "IMAP port (default 993)"),
    ("COORDINATOR_IMAP_USER", False, str, "IMAP user (defaults to SMTP user)"),
    ("COORDINATOR_IMAP_PASSWORD", False, str, "IMAP password (defaults to SMTP password)"),
]

LLM_KEYS = [
    "COORDINATOR_LISTENER_OPENAI_API_KEY",
    "OPENAI_API_KEY",
    "COORDINATOR_LISTENER_OLLAMA_URL",
    "OLLAMA_URL",
    "ANTHROPIC_API_KEY",
    "COORDINATOR_LISTENER_ANTHROPIC_API_KEY",
]


def _get(env: dict[str, str], key: str, alt: str | None = None) -> str | None:
    """Get value, checking alternate key if primary missing."""
    v = env.get(key)
    if v and v.strip():
        return v.strip()
    if alt:
        v = env.get(alt)
        if v and v.strip():
            return v.strip()
    return None


def _validate_type(val: Any, expected: type | tuple) -> bool:
    """Check value matches expected type."""
    if isinstance(expected, tuple):
        return any(isinstance(val, t) for t in expected)
    return isinstance(val, expected)


def validate_email_config(env: dict[str, str] | None = None) -> list[str]:
    """
    Validate email/SMTP config. Returns list of error messages (empty if valid).
    IMAP: if COORDINATOR_IMAP_* not set, SMTP credentials are used for Gmail.
    """
    env = env or load_env()
    errors: list[str] = []

    for key, required, expected_type, _desc in EMAIL_REQUIRED:
        val = env.get(key)
        if not val or not str(val).strip():
            if required:
                errors.append(f"Missing required: {key}")
            continue
        val = val.strip()
        if expected_type == (int, str) or expected_type == int:
            try:
                int(val)
            except ValueError:
                errors.append(f"Invalid {key}: must be integer (e.g. 587)")
        elif expected_type == str and not val:
            if required:
                errors.append(f"Empty required: {key}")

    return errors


def validate_imap_config(env: dict[str, str] | None = None) -> list[str]:
    """
    Validate IMAP config. If IMAP vars not set, SMTP user/password are used (Gmail).
    Returns list of error messages.
    """
    env = env or load_env()
    errors: list[str] = []

    # IMAP can use SMTP creds as fallback; need at least user + password
    imap_user = env.get("COORDINATOR_IMAP_USER") or env.get("COORDINATOR_SMTP_USER")
    imap_pass = env.get("COORDINATOR_IMAP_PASSWORD") or env.get("COORDINATOR_SMTP_PASSWORD")

    if not imap_user or not imap_pass:
        errors.append("IMAP: need COORDINATOR_IMAP_USER/PASSWORD or COORDINATOR_SMTP_USER/PASSWORD")

    return errors


def validate_llm_config(env: dict[str, str] | None = None) -> list[str]:
    """
    Validate that at least one LLM backend is configured.
    Returns list of error messages.
    """
    env = env or load_env()
    errors: list[str] = []

    has_openai = bool(_get(env, "COORDINATOR_LISTENER_OPENAI_API_KEY", "OPENAI_API_KEY"))
    has_ollama = bool(_get(env, "COORDINATOR_LISTENER_OLLAMA_URL", "OLLAMA_URL"))
    has_anthropic = bool(_get(env, "ANTHROPIC_API_KEY", "COORDINATOR_LISTENER_ANTHROPIC_API_KEY"))
    has_transformers = (env.get("COORDINATOR_LISTENER_LLM_BACKEND") or "").strip().lower() == "transformers"

    if not (has_openai or has_ollama or has_anthropic or has_transformers):
        errors.append(
            "LLM: need one of COORDINATOR_LISTENER_OPENAI_API_KEY, "
            "COORDINATOR_LISTENER_OLLAMA_URL, ANTHROPIC_API_KEY, or COORDINATOR_LISTENER_LLM_BACKEND=transformers"
        )

    return errors


def validate_config(
    mode: str = "listener",
    env: dict[str, str] | None = None,
    exit_on_error: bool = True,
) -> dict[str, str]:
    """
    Validate config for given mode. Returns env dict if valid.

    Modes:
      - "email": SMTP + IMAP (for send_opener, read_replies)
      - "listener": email + LLM (for coordinator_listener)

    If exit_on_error and validation fails, prints errors and exits 1.
    """
    env = env or load_env()
    all_errors: list[str] = []

    email_errors = validate_email_config(env)
    all_errors.extend(email_errors)

    imap_errors = validate_imap_config(env)
    all_errors.extend(imap_errors)

    if mode == "listener":
        llm_errors = validate_llm_config(env)
        all_errors.extend(llm_errors)

    if all_errors:
        if exit_on_error:
            print("Config validation failed:", file=sys.stderr)
            for e in all_errors:
                print(f"  - {e}", file=sys.stderr)
            print("\nCopy .env.example to .env and fill in required values.", file=sys.stderr)
            sys.exit(1)
        raise ValueError("Config validation failed: " + "; ".join(all_errors))

    return env


def main():
    """CLI: validate config and exit 0/1."""
    mode = "listener"
    if len(sys.argv) > 1 and sys.argv[1] == "--email-only":
        mode = "email"

    try:
        validate_config(mode=mode, exit_on_error=True)
        print("Config OK")
    except SystemExit:
        raise
    except Exception as e:
        print(f"Config error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
