#!/usr/bin/env python3
"""
Jarvey health check script. Validates config and tests connectivity.
Use for cron, systemd, or external monitors.

Usage:
  python health_check.py           # Run all checks, exit 0/1, print JSON to stdout
  python health_check.py --quiet   # Exit 0/1 only, no JSON output
"""

import json
import os
import sys
import urllib.request
import urllib.error

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)


def _load_env() -> dict[str, str]:
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


def check_config() -> tuple[bool, str]:
    """Validate .env config. Returns (ok, message)."""
    try:
        from config_schema import validate_config
        validate_config(mode="email", exit_on_error=False)
        return True, "Config valid"
    except ValueError as e:
        return False, str(e)
    except Exception as e:
        return False, f"Config error: {e}"


def check_imap() -> tuple[bool, str]:
    """Test IMAP connect + auth. Returns (ok, message)."""
    env = _load_env()
    user = env.get("COORDINATOR_IMAP_USER") or env.get("COORDINATOR_SMTP_USER")
    password = env.get("COORDINATOR_IMAP_PASSWORD") or env.get("COORDINATOR_SMTP_PASSWORD")
    host = env.get("COORDINATOR_IMAP_HOST") or "imap.gmail.com"
    try:
        port = int(env.get("COORDINATOR_IMAP_PORT", "993"))
    except ValueError:
        port = 993

    if not user or not password:
        return False, "Missing IMAP credentials"

    try:
        import imaplib
        conn = imaplib.IMAP4_SSL(host, port)
        conn.login(user, password)
        conn.select("INBOX")
        conn.close()
        conn.logout()
        return True, "IMAP OK"
    except Exception as e:
        return False, f"IMAP failed: {e}"


def check_smtp() -> tuple[bool, str]:
    """Test SMTP connect + auth. Returns (ok, message)."""
    env = _load_env()
    host = env.get("COORDINATOR_SMTP_HOST")
    try:
        port = int(env.get("COORDINATOR_SMTP_PORT", "587"))
    except ValueError:
        port = 587
    user = env.get("COORDINATOR_SMTP_USER")
    password = env.get("COORDINATOR_SMTP_PASSWORD")

    if not host or not user or not password:
        return False, "Missing SMTP credentials"

    try:
        import smtplib
        with smtplib.SMTP(host, port, timeout=10) as server:
            server.starttls()
            server.login(user, password)
        return True, "SMTP OK"
    except Exception as e:
        return False, f"SMTP failed: {e}"


def check_ollama() -> tuple[bool, str]:
    """Check Ollama reachability. Returns (ok, message)."""
    env = _load_env()
    url = env.get("COORDINATOR_LISTENER_OLLAMA_URL") or env.get("OLLAMA_URL", "http://localhost:11434")
    url = url.rstrip("/")
    check_url = f"{url}/api/tags" if "/v1" not in url else f"{url}/models"

    try:
        req = urllib.request.Request(check_url)
        with urllib.request.urlopen(req, timeout=5) as resp:
            if resp.status in (200, 201):
                return True, "Ollama reachable"
            return False, f"Ollama returned {resp.status}"
    except urllib.error.URLError as e:
        return False, f"Ollama unreachable: {e.reason}"
    except Exception as e:
        return False, f"Ollama check failed: {e}"


def check_openai() -> tuple[bool, str]:
    """Check OpenAI API key is set (no network call to avoid cost). Returns (ok, message)."""
    env = _load_env()
    key = env.get("COORDINATOR_LISTENER_OPENAI_API_KEY") or env.get("OPENAI_API_KEY")
    if key and len(key) > 10:
        return True, "OpenAI key configured"
    return False, "OpenAI key not set"


def check_anthropic() -> tuple[bool, str]:
    """Check Anthropic API key is set. Returns (ok, message)."""
    env = _load_env()
    key = env.get("ANTHROPIC_API_KEY") or env.get("COORDINATOR_LISTENER_ANTHROPIC_API_KEY")
    if key and len(key) > 10:
        return True, "Anthropic key configured"
    return False, "Anthropic key not set"


def check_rag() -> tuple[bool, str]:
    """Check RAG index exists and model matches when JARVEY_RAG_ENABLED=1. Returns (ok, message)."""
    env = _load_env()
    enabled = (env.get("JARVEY_RAG_ENABLED") or "").strip().lower() in ("1", "true", "yes")
    if not enabled:
        return True, "RAG not enabled (skipped)"

    try:
        from jarvey_rag import _get_index_path, _get_embedding_model_name
    except ImportError:
        return False, "RAG enabled but sentence-transformers not installed"

    index_path = _get_index_path()
    if not index_path:
        return False, "RAG index path not set"
    if not os.path.isfile(index_path):
        return False, f"RAG index missing: {index_path}"

    expected_model = _get_embedding_model_name()
    if expected_model:
        try:
            with open(index_path, encoding="utf-8") as f:
                data = json.load(f)
            index_model = data.get("model_name") or ""
            if index_model and index_model != expected_model:
                return False, f"RAG model mismatch: index has {index_model!r}, env has {expected_model!r}. Rebuild: python build_rag_index.py"
        except (json.JSONDecodeError, OSError):
            pass

    return True, "RAG index OK"


def check_transformers_backend() -> tuple[bool, str]:
    """Check transformers package is available when COORDINATOR_LISTENER_LLM_BACKEND=transformers. Returns (ok, message)."""
    env = _load_env()
    backend = (env.get("COORDINATOR_LISTENER_LLM_BACKEND") or "").strip().lower()
    if backend != "transformers":
        return True, "Transformers backend not configured (skipped)"

    try:
        import transformers  # noqa: F401
        return True, "Transformers backend OK"
    except ImportError:
        return False, "Transformers backend configured but transformers not installed (pip install transformers accelerate torch)"


def run_all() -> dict:
    """Run all health checks. Returns summary dict."""
    results = {}
    all_ok = True

    ok, msg = check_config()
    results["config"] = {"ok": ok, "message": msg}
    if not ok:
        all_ok = False

    ok, msg = check_imap()
    results["imap"] = {"ok": ok, "message": msg}
    if not ok:
        all_ok = False

    ok, msg = check_smtp()
    results["smtp"] = {"ok": ok, "message": msg}
    if not ok:
        all_ok = False

    env = _load_env()
    has_ollama = bool(env.get("COORDINATOR_LISTENER_OLLAMA_URL") or env.get("OLLAMA_URL"))
    has_openai = bool(env.get("COORDINATOR_LISTENER_OPENAI_API_KEY") or env.get("OPENAI_API_KEY"))
    has_anthropic = bool(env.get("ANTHROPIC_API_KEY") or env.get("COORDINATOR_LISTENER_ANTHROPIC_API_KEY"))

    if has_ollama:
        ok, msg = check_ollama()
        results["ollama"] = {"ok": ok, "message": msg}
        if not ok:
            all_ok = False
    else:
        results["ollama"] = {"ok": None, "message": "Not configured (skipped)"}

    results["openai"] = {"ok": has_openai, "message": "Key configured" if has_openai else "Not configured"}
    results["anthropic"] = {"ok": has_anthropic, "message": "Key configured" if has_anthropic else "Not configured"}

    rag_enabled = (env.get("JARVEY_RAG_ENABLED") or "").strip().lower() in ("1", "true", "yes")
    if rag_enabled:
        ok, msg = check_rag()
        results["rag"] = {"ok": ok, "message": msg}
        if not ok:
            all_ok = False
    else:
        results["rag"] = {"ok": None, "message": "Not enabled (skipped)"}

    transformers_backend = (env.get("COORDINATOR_LISTENER_LLM_BACKEND") or "").strip().lower() == "transformers"
    if transformers_backend:
        ok, msg = check_transformers_backend()
        results["transformers"] = {"ok": ok, "message": msg}
        if not ok:
            all_ok = False
    else:
        results["transformers"] = {"ok": None, "message": "Not configured (skipped)"}

    return {"healthy": all_ok, "checks": results}


def main():
    quiet = "--quiet" in sys.argv or "-q" in sys.argv
    listener_mode = "--listener" in sys.argv
    summary = run_all()
    if listener_mode:
        # For listener, require at least one LLM backend
        llm_ok = (
            summary["checks"].get("ollama", {}).get("ok") is True
            or summary["checks"].get("openai", {}).get("ok") is True
            or summary["checks"].get("anthropic", {}).get("ok") is True
            or summary["checks"].get("transformers", {}).get("ok") is True
        )
        summary["healthy"] = summary["healthy"] and llm_ok
    if not quiet:
        print(json.dumps(summary, indent=2))
    sys.exit(0 if summary["healthy"] else 1)


if __name__ == "__main__":
    main()
