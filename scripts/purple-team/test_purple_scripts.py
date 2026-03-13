#!/usr/bin/env python3
"""
Test Purple Team Scripts — Cyber Security Loop

Verifies all purple-team scripts are wired and runnable.
Run from repo root: python scripts/purple-team/test_purple_scripts.py

Exit 0 if all pass; exit 1 if any fail.
"""

import json
import subprocess
import sys
from pathlib import Path


def repo_root() -> Path:
    script_dir = Path(__file__).resolve().parent
    return script_dir.parent.parent


def run(cmd: list[str], cwd: Path) -> tuple[int, str]:
    """Run command; return (exit_code, combined stdout+stderr)."""
    try:
        r = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True, timeout=60)
        return r.returncode, (r.stdout or "") + (r.stderr or "")
    except subprocess.TimeoutExpired:
        return -1, "Timeout"
    except Exception as e:
        return -1, str(e)


def test_audit_rules(repo: Path) -> bool:
    """audit_rules.py must exit 0 (no suspicious patterns)."""
    code, out = run([sys.executable, "scripts/purple-team/audit_rules.py"], repo)
    if code != 0:
        print(f"FAIL audit_rules.py: exit {code}\n{out}")
        return False
    print("PASS audit_rules.py")
    return True


def test_prompt_injection_harness(repo: Path) -> bool:
    """prompt_injection_harness.py must produce valid JSON."""
    code, out = run([sys.executable, "scripts/purple-team/prompt_injection_harness.py"], repo)
    if code != 0:
        print(f"FAIL prompt_injection_harness.py: exit {code}\n{out}")
        return False
    try:
        data = json.loads(out)
        if "test_cases" not in data or not isinstance(data["test_cases"], list):
            print("FAIL prompt_injection_harness.py: output missing test_cases list")
            return False
    except json.JSONDecodeError as e:
        print(f"FAIL prompt_injection_harness.py: invalid JSON: {e}")
        return False
    print("PASS prompt_injection_harness.py")
    return True


def test_diff_training_runs(repo: Path) -> bool:
    """diff_training_runs.py with same file twice must exit 0 (no regression)."""
    artifact = repo / "docs/agents/data-sets/security-exercises/artifacts"
    jsons = list(artifact.glob("*-purple-training.json"))
    if not jsons:
        print("SKIP diff_training_runs.py: no training JSON found (run securitySimulations first)")
        return True
    path = str(jsons[0].relative_to(repo))
    code, out = run(
        [sys.executable, "scripts/purple-team/diff_training_runs.py", path, path],
        repo,
    )
    if code != 0:
        print(f"FAIL diff_training_runs.py: exit {code}\n{out}")
        return False
    print("PASS diff_training_runs.py")
    return True


def test_run_purple_validation_only(repo: Path) -> bool:
    """run_purple_simulations.py --validation-only must exit 0."""
    code, out = run(
        [sys.executable, "scripts/purple-team/run_purple_simulations.py", "--validation-only"],
        repo,
    )
    if code != 0:
        print(f"FAIL run_purple_simulations.py --validation-only: exit {code}\n{out}")
        return False
    print("PASS run_purple_simulations.py --validation-only")
    return True


def main() -> int:
    repo = repo_root()
    tests = [
        test_audit_rules,
        test_prompt_injection_harness,
        test_diff_training_runs,
        test_run_purple_validation_only,
    ]
    passed = 0
    for t in tests:
        if t(repo):
            passed += 1
    print(f"\n{passed}/{len(tests)} script tests passed")
    return 0 if passed == len(tests) else 1


if __name__ == "__main__":
    sys.exit(main())
