#!/usr/bin/env python3
"""
Audit Jarvey training data for out-of-scope content.
Scans coordinator-instructions, coordinator-project-context, scenario files, benchmark_output.
Flags: generic products, non-OutOfRouteBuddy topics, emulator-as-Jarvey conflation.

Usage:
  python audit_jarvey_training_data.py           # Dry-run: print report only
  python audit_jarvey_training_data.py --apply   # Append out-of-scope items to TRAINING_DATA_REMOVED.md
"""

import os
import re
import sys
from datetime import datetime

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

REPO_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))
SCENARIOS_DIR = os.path.join(REPO_ROOT, "docs", "agents", "data-sets", "jarvey-scenarios")
TRAINING_REMOVED_PATH = os.path.join(SCENARIOS_DIR, "TRAINING_DATA_REMOVED.md")

# Paths to scan
INSTRUCTIONS_PATH = os.path.join(REPO_ROOT, "docs", "agents", "coordinator-instructions.md")
CONTEXT_PATH = os.path.join(REPO_ROOT, "docs", "agents", "coordinator-project-context.md")
BENCHMARK_OUTPUT_DIR = os.path.join(SCENARIOS_DIR, "benchmark_output")

# Heuristics for out-of-scope content (case-insensitive)
OUT_OF_SCOPE_PATTERNS = [
    (r"\bchatgpt\b", "Generic product reference; not OutOfRouteBuddy"),
    (r"\bclaude\b", "Generic product reference; not OutOfRouteBuddy"),
    (r"\bgpt-4\b", "Generic product reference; not OutOfRouteBuddy"),
    (r"jarvey\s+(implements|builds|develops)\s+the\s+emulator", "Emulator-as-Jarvey conflation; Jarvey coordinates, does not implement"),
    (r"jarvey\s+is\s+the\s+emulator", "Emulator-as-Jarvey conflation"),
    (r"jarvey\s+is\s+the\s+(app|application)", "App-as-Jarvey conflation"),
    (r"we\s+are\s+(building|developing)\s+[^Oo]ut", "Generic 'we build' not about OutOfRouteBuddy"),
]

def _read_file(path: str) -> str:
    """Read file content or return empty string."""
    if not os.path.isfile(path):
        return ""
    try:
        with open(path, encoding="utf-8") as f:
            return f.read()
    except OSError:
        return ""


def _scan_content(content: str, path: str) -> list[tuple[str, str]]:
    """Scan content for out-of-scope patterns. Returns list of (match_snippet, reason)."""
    findings = []
    for pattern, reason in OUT_OF_SCOPE_PATTERNS:
        for m in re.finditer(pattern, content, re.IGNORECASE):
            snippet = m.group(0)
            if len(snippet) > 50:
                snippet = snippet[:47] + "..."
            findings.append((snippet, reason))
    return findings


def _scan_benchmark_output() -> list[tuple[str, str, str]]:
    """Scan benchmark_output for out-of-scope content. Returns (file_path, snippet, reason)."""
    findings = []
    if not os.path.isdir(BENCHMARK_OUTPUT_DIR):
        return findings
    for name in os.listdir(BENCHMARK_OUTPUT_DIR):
        if name.startswith(".") or name == "removed":
            continue
        path = os.path.join(BENCHMARK_OUTPUT_DIR, name)
        if not os.path.isfile(path) or not name.endswith(".txt"):
            continue
        content = _read_file(path)
        for snippet, reason in _scan_content(content, path):
            findings.append((name, snippet, reason))
    return findings


def main():
    do_apply = "--apply" in sys.argv

    findings = []

    # Scan instructions
    content = _read_file(INSTRUCTIONS_PATH)
    for snippet, reason in _scan_content(content, INSTRUCTIONS_PATH):
        findings.append(("coordinator-instructions.md", snippet, reason))

    # Scan context
    content = _read_file(CONTEXT_PATH)
    for snippet, reason in _scan_content(content, CONTEXT_PATH):
        findings.append(("coordinator-project-context.md", snippet, reason))

    # Scan scenario .md files
    if os.path.isdir(SCENARIOS_DIR):
        for name in os.listdir(SCENARIOS_DIR):
            if name.endswith(".md") and not name.startswith("TRAINING"):
                path = os.path.join(SCENARIOS_DIR, name)
                content = _read_file(path)
                for snippet, reason in _scan_content(content, path):
                    findings.append((name, snippet, reason))

    # Scan benchmark_output
    for name, snippet, reason in _scan_benchmark_output():
        findings.append((name, snippet, reason))

    # Report
    print("Jarvey Training Data Audit", file=sys.stderr)
    print("=" * 50, file=sys.stderr)
    if not findings:
        print("No out-of-scope content detected.", file=sys.stderr)
        return 0

    print(f"Found {len(findings)} potential out-of-scope item(s):", file=sys.stderr)
    for file_or_id, snippet, reason in findings:
        print(f"  - {file_or_id}: {snippet!r} -> {reason}", file=sys.stderr)

    if do_apply:
        date_str = datetime.utcnow().strftime("%Y-%m-%d")
        for file_or_id, snippet, reason in findings:
            line = f"| {date_str} | {file_or_id} | Out-of-scope: {reason} |\n"
            with open(TRAINING_REMOVED_PATH, "a", encoding="utf-8") as f:
                f.write(line)
        print(f"\nAppended {len(findings)} entries to {TRAINING_REMOVED_PATH}", file=sys.stderr)
    else:
        print("\nDry-run. Use --apply to append to TRAINING_DATA_REMOVED.md", file=sys.stderr)

    return 0


if __name__ == "__main__":
    sys.exit(main())
