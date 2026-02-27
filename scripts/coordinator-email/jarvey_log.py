#!/usr/bin/env python3
"""
Structured logging for Jarvey. When JARVEY_LOG=structured or JARVEY_STRUCTURED_LOG=1,
writes JSON lines to jarvey_workflow.log with trace_id, event, latency_ms, etc.
"""

import json
import logging
import os
import time
from datetime import datetime, timezone
from typing import Any

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
LOG_PATH = os.path.join(SCRIPT_DIR, "jarvey_workflow.log")


def _is_structured_enabled() -> bool:
    """Check if structured logging is enabled via env."""
    env_path = os.path.join(SCRIPT_DIR, ".env")
    if not os.path.isfile(env_path):
        return os.environ.get("JARVEY_LOG", "").lower() == "structured" or \
               os.environ.get("JARVEY_STRUCTURED_LOG", "").lower() in ("1", "true", "yes")
    try:
        with open(env_path, encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line.startswith("#") or "=" not in line:
                    continue
                k, v = line.split("=", 1)
                v = v.strip().strip('"').strip("'")
                if k.strip() == "JARVEY_LOG" and v.lower() == "structured":
                    return True
                if k.strip() == "JARVEY_STRUCTURED_LOG" and v.lower() in ("1", "true", "yes"):
                    return True
    except OSError:
        pass
    return False


def _is_workflow_log_enabled() -> bool:
    """Check if human-readable workflow logging is enabled (JARVEY_LOG=1)."""
    # Env override (e.g. --log sets JARVEY_LOG=1 before import)
    if os.environ.get("JARVEY_LOG", "").strip().lower() in ("1", "true", "yes"):
        return True
    env_path = os.path.join(SCRIPT_DIR, ".env")
    if not os.path.isfile(env_path):
        return False
    try:
        with open(env_path, encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line.startswith("#") or "=" not in line:
                    continue
                k, v = line.split("=", 1)
                v = v.strip().strip('"').strip("'")
                if k.strip() == "JARVEY_LOG" and v.lower() in ("1", "true", "yes"):
                    return True
    except OSError:
        pass
    return False


def log_workflow(msg: str) -> None:
    """
    Append human-readable workflow line to jarvey_workflow.log when JARVEY_LOG=1.
    Use for live documentation of Jarvey's workflow (check_and_respond, trace, etc.).
    """
    if not _is_workflow_log_enabled():
        return
    ts = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    line = f"[{ts}] {msg}"
    try:
        with open(LOG_PATH, "a", encoding="utf-8") as f:
            f.write(line + "\n")
    except OSError:
        pass


def log_structured(
    event: str,
    trace_id: str | None = None,
    template_key: str | None = None,
    message_id: str | None = None,
    intent: str | None = None,
    latency_ms: float | None = None,
    level: str = "info",
    **extra: Any,
) -> None:
    """
    Write one JSON line to jarvey_workflow.log when structured logging is enabled.
    Fields: ts, event, level, trace_id, template_key, message_id, intent, latency_ms, + extra.
    """
    if not _is_structured_enabled():
        return
    rec: dict[str, Any] = {
        "ts": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3] + "Z",
        "event": event,
        "level": level,
    }
    if trace_id:
        rec["trace_id"] = str(trace_id)
    if template_key is not None:
        rec["template_key"] = template_key
    if message_id is not None:
        rec["message_id"] = str(message_id)[:80]  # truncate for log
    if intent is not None:
        rec["intent"] = intent
    if latency_ms is not None:
        rec["latency_ms"] = round(latency_ms, 1)
    rec.update(extra)
    try:
        with open(LOG_PATH, "a", encoding="utf-8") as f:
            f.write(json.dumps(rec) + "\n")
    except OSError:
        pass


def get_logger(name: str = "jarvey") -> logging.Logger:
    """
    Return a logger that, when structured mode is on, uses a JSON formatter.
    Falls back to standard logging otherwise.
    """
    logger = logging.getLogger(name)
    if logger.handlers:
        return logger
    handler = logging.StreamHandler()
    if _is_structured_enabled():

        class JsonFormatter(logging.Formatter):
            def format(self, record: logging.LogRecord) -> str:
                rec = {
                    "ts": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3] + "Z",
                    "level": record.levelname.lower(),
                    "event": getattr(record, "event", record.getMessage()),
                    "logger": record.name,
                }
                for k, v in record.__dict__.items():
                    if k not in ("name", "msg", "args", "created", "filename", "funcName",
                                 "levelname", "levelno", "lineno", "module", "msecs",
                                 "pathname", "process", "processName", "relativeCreated",
                                 "stack_info", "exc_info", "exc_text", "thread", "threadName",
                                 "message", "event"):
                        if v is not None:
                            rec[k] = v
                return json.dumps(rec)
        handler.setFormatter(JsonFormatter())
    else:
        handler.setFormatter(logging.Formatter("%(asctime)s %(levelname)s %(message)s"))
    logger.addHandler(handler)
    logger.setLevel(logging.INFO)
    return logger


class LatencyTracker:
    """Context manager to track latency and log it."""

    def __init__(self, event: str, trace_id: str | None = None, **extra: Any):
        self.event = event
        self.trace_id = trace_id
        self.extra = extra
        self.start = 0.0

    def __enter__(self):
        self.start = time.perf_counter()
        return self

    def __exit__(self, *args):
        elapsed_ms = (time.perf_counter() - self.start) * 1000
        log_structured(
            self.event,
            trace_id=self.trace_id,
            latency_ms=elapsed_ms,
            **self.extra,
        )
