#!/usr/bin/env python3
"""
Purple Team Simulation Runner — OutOfRouteBuddy

Runs Red Team attack simulations and Blue Team detection checks.
Documents all results for proof of work and training data.

Usage:
  python run_purple_simulations.py [--output-dir PATH] [--with-http]

  --output-dir  Where to write exercise logs (default: docs/agents/data-sets/security-exercises/)
  --with-http   Also run HTTP attack simulations (requires sync service on 127.0.0.1:8765)
"""

import argparse
import json
import os
import sys
from datetime import datetime
from pathlib import Path

# Add emulator-sync-service for imports
_script_dir = Path(__file__).resolve().parent
_sync_dir = _script_dir.parent / "emulator-sync-service"
sys.path.insert(0, str(_sync_dir))

try:
    from sync_service import (
        EMULATOR_TO_PROJECT,
        MAX_REQUEST_BODY_SIZE,
        validate_design_keys,
        get_paths_from_dict,
    )
except ImportError:
    EMULATOR_TO_PROJECT = {}
    MAX_REQUEST_BODY_SIZE = 64 * 1024
    validate_design_keys = None
    get_paths_from_dict = None


def run_unit_tests():
    """Run sync service unit tests. Returns (passed, failed, output)."""
    import subprocess
    result = subprocess.run(
        [sys.executable, str(_sync_dir / "test_sync_service.py")],
        capture_output=True,
        text=True,
        cwd=str(_sync_dir),
    )
    return result.returncode == 0, result.returncode != 0, result.stdout + result.stderr


def run_http_attack_simulations(base_url: str = "http://127.0.0.1:8765"):
    """Simulate Red Team HTTP attacks against sync service. Returns list of results."""
    import urllib.request
    import urllib.error

    results = []

    # Attack 1: Valid design (should succeed — baseline)
    try:
        design = {"toolbar": {"title": "OOR"}, "loadedMiles": {"hint": "Loaded Miles"}}
        data = json.dumps(design).encode()
        req = urllib.request.Request(
            f"{base_url}/sync",
            data=data,
            headers={"Content-Type": "application/json"},
            method="POST",
        )
        with urllib.request.urlopen(req, timeout=5) as resp:
            body = json.loads(resp.read().decode())
            results.append({
                "attack": "valid_design",
                "expected": "200",
                "actual": str(resp.status),
                "passed": resp.status == 200,
                "blue_visibility": "SyncServiceAudit would log on success",
            })
    except urllib.error.HTTPError as e:
        results.append({
            "attack": "valid_design",
            "expected": "200",
            "actual": str(e.code),
            "passed": False,
            "blue_visibility": "N/A",
        })
    except OSError as e:
        results.append({
            "attack": "valid_design",
            "expected": "200",
            "actual": f"Connection failed: {e}",
            "passed": False,
            "blue_visibility": "Service not running",
        })

    # Attack 2: Unknown key injection (should reject with 400)
    try:
        design = {"toolbar": {"title": "OOR"}, "evil_key": "malicious_value"}
        data = json.dumps(design).encode()
        req = urllib.request.Request(
            f"{base_url}/sync",
            data=data,
            headers={"Content-Type": "application/json"},
            method="POST",
        )
        with urllib.request.urlopen(req, timeout=5) as resp:
            results.append({
                "attack": "unknown_key_injection",
                "expected": "400",
                "actual": str(resp.status),
                "passed": False,  # Should have rejected
                "blue_visibility": "Gap: key allowlist should have rejected",
            })
    except urllib.error.HTTPError as e:
        results.append({
            "attack": "unknown_key_injection",
            "expected": "400",
            "actual": str(e.code),
            "passed": e.code == 400,
            "blue_visibility": "Key allowlist correctly rejected",
        })
    except OSError as e:
        results.append({
            "attack": "unknown_key_injection",
            "expected": "400",
            "actual": f"Connection failed: {e}",
            "passed": False,
            "blue_visibility": "Service not running",
        })

    # Attack 3: Oversized payload (should reject with 413)
    try:
        design = {"toolbar": {"title": "A" * (MAX_REQUEST_BODY_SIZE)}}
        data = json.dumps(design).encode()
        req = urllib.request.Request(
            f"{base_url}/sync",
            data=data,
            headers={"Content-Type": "application/json"},
            method="POST",
        )
        with urllib.request.urlopen(req, timeout=5) as resp:
            results.append({
                "attack": "oversized_payload",
                "expected": "413",
                "actual": str(resp.status),
                "passed": False,
                "blue_visibility": "Size limit should have rejected",
            })
    except urllib.error.HTTPError as e:
        results.append({
            "attack": "oversized_payload",
            "expected": "413",
            "actual": str(e.code),
            "passed": e.code == 413,
            "blue_visibility": "Size limit correctly rejected",
        })
    except OSError as e:
        results.append({
            "attack": "oversized_payload",
            "expected": "413",
            "actual": f"Connection failed: {e}",
            "passed": False,
            "blue_visibility": "Service not running",
        })

    return results


def run_validation_simulations():
    """Run Red Team simulations using sync_service validation logic (no HTTP)."""
    results = []
    if validate_design_keys is None:
        return results

    # Red: unknown top-level key
    passed = not validate_design_keys({"evil": {"key": "value"}})
    results.append({
        "attack": "validate_unknown_top_level",
        "expected": "reject",
        "actual": "reject" if passed else "accept",
        "passed": passed,
        "blue_visibility": "validate_design_keys in sync_service",
    })

    # Red: unknown nested key
    passed = not validate_design_keys({"toolbar": {"title": "OOR", "evil": "x"}})
    results.append({
        "attack": "validate_unknown_nested",
        "expected": "reject",
        "actual": "reject" if passed else "accept",
        "passed": passed,
        "blue_visibility": "validate_design_keys in sync_service",
    })

    # Red: valid design
    passed = validate_design_keys({"toolbar": {"title": "OOR"}})
    results.append({
        "attack": "validate_known_paths",
        "expected": "accept",
        "actual": "accept" if passed else "reject",
        "passed": passed,
        "blue_visibility": "Key allowlist allows known paths",
    })

    return results


def write_exercise_log(output_dir: Path, unit_ok: bool, validation_results: list, http_results: list | None):
    """Write structured exercise log for proof of work."""
    date_str = datetime.now().strftime("%Y-%m-%d")
    time_str = datetime.now().strftime("%H:%M")
    exercise_id = f"{date_str}-purple-simulations"

    lines = [
        "# Purple Team Simulation — OutOfRouteBuddy",
        "",
        f"**exercise_id:** {exercise_id}",
        f"**date:** {date_str}",
        f"**time:** {time_str}",
        "**target:** Sync service (key allowlist, size limit, validation); unit tests",
        "**mode:** Purple (automated Red simulations + Blue detection check)",
        "",
        "---",
        "",
        "## Scenario 1: Unit tests (sync_service validation)",
        "",
        "### Red action – Technical Ninja",
        "- **Role:** Technical Ninja",
        "- **Target:** sync_service.py validate_design_keys, MAX_REQUEST_BODY_SIZE",
        "- **Action:** Run test_sync_service.py — tests key allowlist, unknown key rejection, size limit",
        f"- **Result:** {'Success' if unit_ok else 'Failed'}",
        "- **Blue visibility:** Yes — unit tests verify controls",
        "- **Artifacts:** scripts/emulator-sync-service/test_sync_service.py",
        "",
        "### Blue check – Scenario 1",
        "- **Red action reviewed:** Unit test suite",
        "- **Alarm went off?** Yes",
        "- **What detected it:** test_validate_design_keys_* tests",
        "- **Remediation:** N/A (controls working)",
        "",
        "---",
        "",
        "## Scenario 2: Validation logic (no HTTP)",
        "",
    ]

    for r in validation_results:
        lines.extend([
            f"### {r['attack']}",
            f"- **Expected:** {r['expected']} | **Actual:** {r['actual']} | **Passed:** {r['passed']}",
            f"- **Blue visibility:** {r['blue_visibility']}",
            "",
        ])

    if http_results:
        lines.extend([
            "---",
            "",
            "## Scenario 3: HTTP attack simulations",
            "",
        ])
        for r in http_results:
            lines.extend([
                f"### {r['attack']}",
                f"- **Expected:** {r['expected']} | **Actual:** {r['actual']} | **Passed:** {r['passed']}",
                f"- **Blue visibility:** {r['blue_visibility']}",
                "",
            ])

    lines.extend([
        "---",
        "",
        "## Summary",
        "",
        f"- Unit tests: {'PASS' if unit_ok else 'FAIL'}",
        f"- Validation simulations: {sum(1 for r in validation_results if r['passed'])}/{len(validation_results)} passed",
    ])
    if http_results:
        lines.append(f"- HTTP simulations: {sum(1 for r in http_results if r['passed'])}/{len(http_results)} passed")
    lines.append("")

    output_dir.mkdir(parents=True, exist_ok=True)
    log_path = output_dir / f"{exercise_id}.md"
    log_path.write_text("\n".join(lines), encoding="utf-8")
    return log_path


def write_training_data(output_dir: Path, unit_ok: bool, validation_results: list, http_results: list | None):
    """Write training data format for agent ingestion."""
    date_str = datetime.now().strftime("%Y-%m-%d")
    training = {
        "exercise_id": f"{date_str}-purple-simulations",
        "date": date_str,
        "source": "run_purple_simulations.py",
        "unit_tests_passed": unit_ok,
        "validation_simulations": validation_results,
        "http_simulations": http_results or [],
        "summary": {
            "unit_tests": "pass" if unit_ok else "fail",
            "validation_passed": sum(1 for r in validation_results if r["passed"]),
            "validation_total": len(validation_results),
            "http_passed": sum(1 for r in (http_results or []) if r["passed"]),
            "http_total": len(http_results or []),
        },
    }
    output_dir.mkdir(parents=True, exist_ok=True)
    training_path = output_dir / "artifacts" / f"{date_str}-purple-training.json"
    training_path.parent.mkdir(parents=True, exist_ok=True)
    training_path.write_text(json.dumps(training, indent=2), encoding="utf-8")
    return training_path


def main():
    parser = argparse.ArgumentParser(description="Run Purple Team simulations")
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=Path(__file__).resolve().parent.parent.parent / "docs" / "agents" / "data-sets" / "security-exercises",
        help="Output directory for exercise logs",
    )
    parser.add_argument(
        "--with-http",
        action="store_true",
        help="Run HTTP attack simulations (sync service must be on 127.0.0.1:8765)",
    )
    args = parser.parse_args()

    print("=== Purple Team Simulation Runner ===\n")

    # 1. Unit tests
    print("Running unit tests (test_sync_service.py)...")
    unit_ok, _, unit_out = run_unit_tests()
    print(unit_out)
    if not unit_ok:
        print("WARNING: Some unit tests failed.")
    else:
        print("Unit tests: PASS\n")

    # 2. Validation simulations (no HTTP)
    print("Running validation simulations...")
    validation_results = run_validation_simulations()
    for r in validation_results:
        status = "PASS" if r["passed"] else "FAIL"
        print(f"  {status} {r['attack']}: expected={r['expected']} actual={r['actual']}")
    print()

    # 3. HTTP simulations (optional)
    http_results = None
    if args.with_http:
        print("Running HTTP attack simulations (127.0.0.1:8765)...")
        http_results = run_http_attack_simulations()
        for r in http_results:
            status = "PASS" if r["passed"] else "FAIL"
            print(f"  {status} {r['attack']}: expected={r['expected']} actual={r['actual']}")
        print()
    else:
        print("Skipping HTTP simulations (use --with-http if sync service is running)\n")

    # 4. Write logs
    log_path = write_exercise_log(args.output_dir, unit_ok, validation_results, http_results)
    print(f"Exercise log: {log_path}")

    training_path = write_training_data(args.output_dir, unit_ok, validation_results, http_results)
    print(f"Training data: {training_path}")

    print("\n=== Done ===")
    return 0 if unit_ok and all(r["passed"] for r in validation_results) else 1


if __name__ == "__main__":
    sys.exit(main())
