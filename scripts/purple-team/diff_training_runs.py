#!/usr/bin/env python3
"""
Diff Training Runs — Cyber Security Loop

Compares two purple-training.json artifacts for regression.
Exit 0 if current >= baseline; exit 1 if regression (fewer passed).

Usage:
  python diff_training_runs.py <baseline.json> <current.json>
  python diff_training_runs.py artifacts/2026-03-10-purple-training.json artifacts/2026-03-11-purple-training.json

Output:
  Pass/fail; diff summary to stdout.
"""

import argparse
import json
import sys
from pathlib import Path


def repo_root() -> Path:
    """Find repo root (parent of scripts/)."""
    script_dir = Path(__file__).resolve().parent
    return script_dir.parent.parent


def load_training(path: Path) -> dict:
    """Load training JSON. Path may be relative to repo root."""
    if not path.is_absolute():
        path = repo_root() / path
    if not path.exists():
        raise FileNotFoundError(f"Training file not found: {path}")
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def get_validation_passed(data: dict) -> int:
    """Extract validation_passed count from training JSON."""
    summary = data.get("summary", {})
    if "validation_passed" in summary:
        return summary["validation_passed"]
    # Fallback: count passed in validation_simulations
    sims = data.get("validation_simulations", [])
    return sum(1 for s in sims if s.get("passed", False))


def get_validation_total(data: dict) -> int:
    """Extract validation_total count."""
    summary = data.get("summary", {})
    if "validation_total" in summary:
        return summary["validation_total"]
    return len(data.get("validation_simulations", []))


def main() -> int:
    parser = argparse.ArgumentParser(description="Compare two purple-training.json runs for regression")
    parser.add_argument("baseline", help="Path to baseline training JSON")
    parser.add_argument("current", help="Path to current training JSON")
    parser.add_argument("-q", "--quiet", action="store_true", help="Only exit code; no output")
    args = parser.parse_args()

    try:
        baseline = load_training(Path(args.baseline))
        current = load_training(Path(args.current))
    except FileNotFoundError as e:
        print(str(e), file=sys.stderr)
        return 2

    base_passed = get_validation_passed(baseline)
    base_total = get_validation_total(baseline)
    curr_passed = get_validation_passed(current)
    curr_total = get_validation_total(current)

    regression = curr_passed < base_passed or (base_total > 0 and curr_total < base_total)

    if not args.quiet:
        print(f"Baseline: {base_passed}/{base_total} validation passed")
        print(f"Current:  {curr_passed}/{curr_total} validation passed")
        if regression:
            print("REGRESSION: Current has fewer passed than baseline.")
        else:
            print("OK: No regression.")

    return 1 if regression else 0


if __name__ == "__main__":
    sys.exit(main())
