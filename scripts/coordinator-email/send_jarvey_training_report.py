#!/usr/bin/env python3
"""
Run full Jarvey training session and email the report.
Orchestrates: run_jarvey_benchmark.py --record --remove-failures,
audit_jarvey_training_data.py --apply, then sends email with summary.

Usage:
  python send_jarvey_training_report.py
  python send_jarvey_training_report.py --dry-run   # Build report but do not send
  python send_jarvey_training_report.py --use-existing   # Skip benchmark; use existing TRAINING_SESSION_RECORD.json
  python send_jarvey_training_report.py --simulate   # Run benchmark with mock LLM (fast, no Ollama/OpenAI)
  python send_jarvey_training_report.py --live       # Stream benchmark output to timestamped log file

Requires: .env with COORDINATOR_EMAIL_*, COORDINATOR_SMTP_*. LLM required for benchmark unless --use-existing or --simulate.
"""

import json
import os
import subprocess
import sys
from datetime import datetime

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

REPO_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))
SCENARIOS_DIR = os.path.join(REPO_ROOT, "docs", "agents", "data-sets", "jarvey-scenarios")
TRAINING_RECORD_PATH = os.path.join(SCENARIOS_DIR, "TRAINING_SESSION_RECORD.json")
TRAINING_REMOVED_PATH = os.path.join(SCENARIOS_DIR, "TRAINING_DATA_REMOVED.md")


def _read_file(path: str) -> str:
    if not os.path.isfile(path):
        return ""
    try:
        with open(path, encoding="utf-8") as f:
            return f.read()
    except OSError:
        return ""


def _run_benchmark(use_simulate: bool = False, use_live: bool = False) -> bool:
    """Run benchmark with --record --remove-failures. Returns True if all passed."""
    args = ["run_jarvey_benchmark.py", "--record", "--remove-failures"]
    if use_simulate:
        args.append("--simulate")
    if use_live:
        args.append("--live")
    result = subprocess.run(
        [sys.executable] + args,
        cwd=SCRIPT_DIR,
        capture_output=True,
        text=True,
    )
    if result.stdout:
        print(result.stdout, file=sys.stderr)
    if result.stderr:
        print(result.stderr, file=sys.stderr)
    return result.returncode == 0


def _run_audit() -> None:
    """Run audit with --apply."""
    subprocess.run(
        [sys.executable, "audit_jarvey_training_data.py", "--apply"],
        cwd=SCRIPT_DIR,
        capture_output=False,
    )


def _build_report_body() -> str:
    """Build email body from TRAINING_SESSION_RECORD.json and TRAINING_DATA_REMOVED.md."""
    parts = []

    # Jarvey boundaries reminder
    parts.append("Jarvey boundaries: Jarvey is the email coordinator. Jarvey is not the OutOfRouteBuddy app and is not the emulator. When asked about the app or emulator, Jarvey coordinates and delegates—does not implement.")
    parts.append("")

    # Record summary
    if os.path.isfile(TRAINING_RECORD_PATH):
        try:
            with open(TRAINING_RECORD_PATH, encoding="utf-8") as f:
                record = json.load(f)
            parts.append("## Training Session Summary")
            parts.append("")
            parts.append(f"Timestamp: {record.get('timestamp', 'N/A')}")
            parts.append(f"Scenarios run: {record.get('scenarios_run', 0)}")
            parts.append(f"Passed: {record.get('passed', 0)}")
            parts.append(f"Failed: {record.get('failed', 0)}")
            parts.append("")
            parts.append("| Scenario | Name | Status | Reason |")
            parts.append("|----------|------|--------|--------|")
            for r in record.get("results", []):
                parts.append(f"| {r.get('id', '')} | {r.get('name', '')} | {r.get('status', '')} | {r.get('reason', '')} |")
            parts.append("")
        except (json.JSONDecodeError, OSError):
            parts.append("(Could not read TRAINING_SESSION_RECORD.json)")
            parts.append("")
    else:
        parts.append("(No TRAINING_SESSION_RECORD.json — benchmark may not have run)")
        parts.append("")

    # Removed data log
    removed = _read_file(TRAINING_REMOVED_PATH)
    if removed.strip():
        parts.append("## Removed Data (TRAINING_DATA_REMOVED.md)")
        parts.append("")
        # Include only the table rows (skip header if we want; or include full content)
        parts.append(removed.strip())
        parts.append("")
    else:
        parts.append("(No removed data logged)")
        parts.append("")

    parts.append("— Jarvey")
    return "\n".join(parts)


def main():
    dry_run = "--dry-run" in sys.argv
    use_existing = "--use-existing" in sys.argv
    use_simulate = "--simulate" in sys.argv
    use_live = "--live" in sys.argv

    if not use_existing:
        print("Running Jarvey training session...", file=sys.stderr)
        _run_benchmark(use_simulate=use_simulate, use_live=use_live)

    print("Running audit for out-of-scope content...", file=sys.stderr)
    _run_audit()

    date_str = datetime.utcnow().strftime("%Y-%m-%d")
    subject = f"Jarvey Training Session Report — {date_str}"
    body = _build_report_body()

    if dry_run:
        print("\n[DRY RUN] Would send:", file=sys.stderr)
        print(f"Subject: {subject}", file=sys.stderr)
        print(f"Body:\n{body}", file=sys.stderr)
        return 0

    try:
        from send_email import send
        send(subject, body)
        print("Training report email sent successfully.", file=sys.stderr)
    except Exception as e:
        print(f"Failed to send email: {e}", file=sys.stderr)
        return 1

    return 0


if __name__ == "__main__":
    sys.exit(main())
