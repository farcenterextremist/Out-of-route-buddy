#!/usr/bin/env python3
"""
Audit .cursor/rules for suspicious patterns — Cyber Security Loop

Grep rules for prompt-injection style backdoors. Aligns with SECURITY_NOTES §13.
Exit 0 if clean; exit 1 if suspicious pattern found.

Allowlisted: Phase references (e.g. "Phase 0.3", "Phase 1.2") in procedural context.
"""

import re
import sys
from pathlib import Path


def repo_root() -> Path:
    script_dir = Path(__file__).resolve().parent
    return script_dir.parent.parent


# Patterns that indicate likely prompt injection / backdoor
SUSPICIOUS_PATTERNS = [
    (re.compile(r"ignore\s+all\s+previous\s+instructions", re.I), "ignore all previous instructions"),
    (re.compile(r"skip\s+security\s+validation", re.I), "skip security validation"),
    (re.compile(r"output\s+internal\s+data", re.I), "output internal data"),
    (re.compile(r"reveal\s+secrets", re.I), "reveal secrets"),
    (re.compile(r"do\s+not\s+follow\s+.*instructions", re.I), "do not follow instructions"),
]

# Lines matching these are allowlisted (legitimate references)
ALLOWLIST_PATTERNS = [
    re.compile(r"Phase\s+0\.3|Phase\s+1\.2"),  # Procedural references
    re.compile(r"alwaysApply:\s*true"),  # Frontmatter
    re.compile(r"SECURITY_NOTES|CURSOR_SELF_IMPROVEMENT"),  # Doc references
]


def is_allowlisted(line: str) -> bool:
    return any(p.search(line) for p in ALLOWLIST_PATTERNS)


def main() -> int:
    rules_dir = repo_root() / ".cursor" / "rules"
    if not rules_dir.exists():
        return 0

    found = []
    for f in rules_dir.glob("*.mdc"):
        try:
            content = f.read_text(encoding="utf-8")
            for i, line in enumerate(content.splitlines(), 1):
                if is_allowlisted(line):
                    continue
                for pattern, name in SUSPICIOUS_PATTERNS:
                    if pattern.search(line):
                        found.append((str(f.relative_to(repo_root())), i, name, line.strip()[:80]))

        except Exception:
            continue

    if found:
        print("Suspicious patterns in .cursor/rules:", file=sys.stderr)
        for path, line_no, name, snippet in found:
            print(f"  {path}:{line_no} [{name}] {snippet}...", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
