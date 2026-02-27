#!/usr/bin/env python3
"""
Run all 10 Jarvey scenarios and report pass/fail.
Scenario 3 uses the template path (no LLM); scenarios 1, 2, 4, 5, 6, 7, 8, 9, 10 use the LLM.

Usage:
  python run_jarvey_benchmark.py
  python run_jarvey_benchmark.py --llm-only   # Skip scenario 3 (template)
  python run_jarvey_benchmark.py --record     # Write TRAINING_SESSION_RECORD.json
  python run_jarvey_benchmark.py --remove-failures  # Move failed outputs to removed/; append to TRAINING_DATA_REMOVED.md
  python run_jarvey_benchmark.py --simulate   # Use mock LLM (no Ollama/OpenAI); fast; validates pipeline
  python run_jarvey_benchmark.py --live       # Stream output to timestamped log file (benchmark_run_*.log)

Requires: .env with COORDINATOR_LISTENER_OPENAI_API_KEY or COORDINATOR_LISTENER_OLLAMA_URL
  for LLM scenarios. Scenario 3 runs without LLM. Use --simulate to run without LLM.
"""

import json
import os
import re
import shutil
import sys
from datetime import datetime

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

REPO_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))
BENCHMARK_OUTPUT_DIR = os.path.join(
    REPO_ROOT, "docs", "agents", "data-sets", "jarvey-scenarios", "benchmark_output"
)
BENCHMARK_REMOVED_DIR = os.path.join(BENCHMARK_OUTPUT_DIR, "removed")
SCENARIOS_DIR = os.path.join(REPO_ROOT, "docs", "agents", "data-sets", "jarvey-scenarios")
BENCHMARK_LOGS_DIR = os.path.join(SCENARIOS_DIR, "benchmark_logs")
TRAINING_RECORD_PATH = os.path.join(SCENARIOS_DIR, "TRAINING_SESSION_RECORD.json")
TRAINING_REMOVED_PATH = os.path.join(SCENARIOS_DIR, "TRAINING_DATA_REMOVED.md")

# Manifest: scenario_id -> {subject, body, type: "llm"|"template", name}
SCENARIOS = {
    1: {
        "subject": "Re: OutOfRouteBuddy",
        "body": "What's next?",
        "type": "llm",
        "name": "simple_whats_next",
    },
    2: {
        "subject": "Re: OutOfRouteBuddy",
        "body": "Can we prioritize the reports screen and when will it be done?",
        "type": "llm",
        "name": "semi_simple_prioritize_reports",
    },
    3: {
        "subject": "Re: OutOfRouteBuddy",
        "body": "Thanks, that works!",
        "type": "template",
        "name": "thanks_template",
    },
    4: {
        "subject": "Re: OutOfRouteBuddy",
        "body": "What's next? Also, who owns the emulator?",
        "type": "llm",
        "name": "multi_question",
    },
    5: {
        "subject": "Re: OutOfRouteBuddy",
        "body": "Write me a function to export trips to CSV.",
        "type": "llm",
        "name": "no_code",
    },
    6: {
        "subject": "Re: OutOfRouteBuddy",
        "body": "Something is broken.",
        "type": "llm",
        "name": "unclear",
    },
    7: {
        "subject": "Re: OutOfRouteBuddy",
        "body": "How does trip recovery work?",
        "type": "llm",
        "name": "recovery",
    },
    8: {
        "subject": "Re: OutOfRouteBuddy",
        "body": "What's the latest app version?",
        "type": "llm",
        "name": "version",
    },
    9: {
        "subject": "Re: OutOfRouteBuddy",
        "body": "Where is TripInputViewModel defined?",
        "type": "llm",
        "name": "where_is_tripinputviewmodel",
    },
    10: {
        "subject": "Re: OutOfRouteBuddy",
        "body": "Tell me recent project changes",
        "type": "llm",
        "name": "recent_changes",
    },
}

# Code indicators that must NOT appear in scenario 5 (no-code) reply.
# Use precise patterns to avoid false positives (e.g. "TripTrackingService class" in delegation).
CODE_INDICATORS = [
    r"\bdef\s+\w+\s*\(",  # function definition
    r"```\s*python",  # code block
    r"```python",
    r"^\s*import\s+",  # import at line start
    r"\n\s*import\s+",
    r"^\s*class\s+\w+\s*:",  # class definition
    r"\n\s*class\s+\w+\s*:",
    r"^\s*return\s+",  # return statement
    r"\n\s*return\s+",
]


def check_scenario(n: int, reply: str) -> tuple[bool, str]:
    """
    Heuristic pass/fail for scenario n. Returns (passed, reason).
    """
    if not reply or not reply.strip():
        return False, "Empty reply"

    reply_lower = reply.lower()
    reply_stripped = reply.strip()

    if n == 1:
        # Simple: Jarvey; project-aware (Auto, Reports, History, ROADMAP, next)
        if "jarvey" not in reply_lower:
            return False, "Missing Jarvey sign-off"
        keywords = ["auto", "reports", "history", "roadmap", "next"]
        if not any(k in reply_lower for k in keywords):
            return False, "Missing project context (Auto/Reports/History/ROADMAP/next)"
        return True, "OK"

    if n == 2:
        # Semi-simple: Jarvey; report or priorit; no code blocks
        if "jarvey" not in reply_lower:
            return False, "Missing Jarvey sign-off"
        if "report" not in reply_lower and "priorit" not in reply_lower:
            return False, "Missing report/priority mention"
        if "```" in reply or "```python" in reply_lower:
            return False, "Contains code block"
        return True, "OK"

    if n == 3:
        # Thanks template: "Thanks for getting back" and "— Jarvey"
        if "thanks for getting back" not in reply_lower:
            return False, "Missing thanks template phrase"
        if "— jarvey" not in reply_lower and "- jarvey" not in reply_lower:
            return False, "Missing Jarvey sign-off"
        return True, "OK"

    if n == 4:
        # Multi-question: Jarvey; addresses both (emulator or next)
        if "jarvey" not in reply_lower:
            return False, "Missing Jarvey sign-off"
        if "emulator" not in reply_lower and "next" not in reply_lower:
            return False, "Does not address both questions (emulator/next)"
        return True, "OK"

    if n == 5:
        # No code: Jarvey; must NOT contain code indicators
        if "jarvey" not in reply_lower:
            return False, "Missing Jarvey sign-off"
        for pattern in CODE_INDICATORS:
            if re.search(pattern, reply, re.IGNORECASE):
                return False, f"Contains code indicator: {pattern!r}"
        return True, "OK"

    if n == 6:
        # Unclear: Jarvey; must NOT invent meeting or detailed report
        if "jarvey" not in reply_lower:
            return False, "Missing Jarvey sign-off"
        # For "Something is broken" - should ask for clarification, not invent
        if "detailed report" in reply_lower or "scheduling a meeting" in reply_lower:
            return False, "Invents meeting/report for vague input"
        if "meeting" in reply_lower and "schedule" in reply_lower:
            return False, "Invents meeting for vague input"
        return True, "OK"

    if n == 7:
        # Recovery: Jarvey; cites recovery (crash, TripCrashRecoveryManager, persistence, etc.)
        if "jarvey" not in reply_lower:
            return False, "Missing Jarvey sign-off"
        recovery_keywords = ["recovery", "crash", "persistence", "trip", "restart", "recover"]
        if not any(k in reply_lower for k in recovery_keywords):
            return False, "Missing recovery context"
        return True, "OK"

    if n == 8:
        # Version: Jarvey; cites version (1.0.2 or versionCode)
        if "jarvey" not in reply_lower:
            return False, "Missing Jarvey sign-off"
        version_keywords = ["1.0", "version", "build"]
        if not any(k in reply_lower for k in version_keywords):
            return False, "Missing version info"
        return True, "OK"

    if n == 9:
        # Where is X defined: Jarvey; references project file path (TripInputViewModel.kt)
        if "jarvey" not in reply_lower:
            return False, "Missing Jarvey sign-off"
        if "tripinputviewmodel" not in reply_lower:
            return False, "Does not mention TripInputViewModel"
        if ".kt" not in reply_lower and "app/" not in reply_lower and "src/" not in reply_lower:
            return False, "Does not reference file path from project index"
        return True, "OK"

    if n == 10:
        # Recent changes: Jarvey; must NOT contain raw git commit hashes when timeline empty
        if "jarvey" not in reply_lower:
            return False, "Missing Jarvey sign-off"
        # Must not output raw git hashes (7+ hex chars) unless user asked for commit history
        if re.search(r"\b[a-f0-9]{7,}\b", reply):
            return False, "Contains raw git commit hash (should use curated timeline, not raw git)"
        # Should acknowledge "recent" or contain timeline/curated/no curated
        if "recent" not in reply_lower and "timeline" not in reply_lower and "curated" not in reply_lower:
            return False, "Does not acknowledge recent changes or timeline"
        return True, "OK"

    return False, "Unknown scenario"


def run_scenario(n: int, manifest: dict, env: dict) -> tuple[str, bool, str]:
    """
    Run scenario n. Returns (reply_text, passed, reason).
    """
    subject = manifest["subject"]
    body = manifest["body"]
    stype = manifest["type"]
    name = manifest["name"]

    if stype == "template":
        from template_registry import choose_response

        _, reply_body, _ = choose_response(subject, body)
        reply = reply_body
    else:
        # LLM path (or mock when --simulate)
        import coordinator_listener as cl

        use_simulate = env.get("JARVEY_BENCHMARK_SIMULATE", "").strip().lower() in ("1", "true", "yes")

        if use_simulate:
            from mock_llm import compose_reply_mock
            reply = compose_reply_mock(subject, body)
            reply = cl._ensure_jarvey_signoff(reply)
        else:
            api_key = env.get("COORDINATOR_LISTENER_OPENAI_API_KEY") or env.get("OPENAI_API_KEY")
            ollama_url = env.get("COORDINATOR_LISTENER_OLLAMA_URL") or env.get("OLLAMA_URL")

            if not api_key and not ollama_url:
                return "[SKIP: No LLM configured]", False, "No API key or Ollama URL"

            try:
                if api_key:
                    reply = cl.compose_reply_openai(subject, body, api_key, timeout=90)
                else:
                    base_url = ollama_url or "http://localhost:11434"
                    model = env.get("COORDINATOR_LISTENER_OLLAMA_MODEL", "llama3.2")
                    ollama_timeout = 900  # 15 min; match coordinator_listener default
                    try:
                        t = env.get("COORDINATOR_LISTENER_OLLAMA_TIMEOUT")
                        if t:
                            ollama_timeout = int(t)
                    except ValueError:
                        pass
                    reply = cl.compose_reply_ollama(base_url, model, subject, body, timeout=ollama_timeout)
                reply = cl._ensure_jarvey_signoff(reply)
            except Exception as e:
                return f"[ERROR: {e}]", False, str(e)

    passed, reason = check_scenario(n, reply)
    return reply, passed, reason


def _sanitize_reason_for_filename(reason: str) -> str:
    """Replace spaces and special chars for use in filename."""
    return re.sub(r"[^\w\-]", "_", reason)[:60]


def _append_removed_entry(filename: str, reason: str) -> None:
    """Append one entry to TRAINING_DATA_REMOVED.md."""
    date_str = datetime.utcnow().strftime("%Y-%m-%d")
    line = f"| {date_str} | {filename} | FAIL: {reason} |\n"
    with open(TRAINING_REMOVED_PATH, "a", encoding="utf-8") as f:
        f.write(line)


def _live_log(msg: str, log_file, also_stderr: bool = True) -> None:
    """Write to log file and optionally stderr. Flush for live streaming."""
    if log_file:
        log_file.write(msg + "\n")
        log_file.flush()
    if also_stderr:
        print(msg, file=sys.stderr)
        sys.stderr.flush()


def main():
    llm_only = "--llm-only" in sys.argv
    do_record = "--record" in sys.argv
    do_remove_failures = "--remove-failures" in sys.argv
    do_simulate = "--simulate" in sys.argv
    do_live = "--live" in sys.argv

    os.makedirs(BENCHMARK_OUTPUT_DIR, exist_ok=True)
    log_file = None
    if do_live:
        os.makedirs(BENCHMARK_LOGS_DIR, exist_ok=True)
        ts = datetime.utcnow().strftime("%Y-%m-%d_%H-%M-%S")
        mode = "simulate" if do_simulate else "llm"
        log_path = os.path.join(BENCHMARK_LOGS_DIR, f"benchmark_run_{ts}_{mode}.log")
        log_file = open(log_path, "w", encoding="utf-8")
        log_file.write(f"Jarvey Benchmark — Live Log — {ts}\n")
        log_file.write(f"Mode: {'simulate' if do_simulate else 'LLM'}\n")
        log_file.write("=" * 70 + "\n")
        log_file.flush()
        print(f"Live log: {log_path}", file=sys.stderr)
    if do_remove_failures:
        os.makedirs(BENCHMARK_REMOVED_DIR, exist_ok=True)

    import coordinator_listener as cl

    env = cl.load_env()
    if do_simulate:
        env["JARVEY_BENCHMARK_SIMULATE"] = "1"
    else:
        # Pre-warm Ollama to reduce first-request latency
        ollama_url = env.get("COORDINATOR_LISTENER_OLLAMA_URL") or env.get("OLLAMA_URL")
        if ollama_url and not env.get("COORDINATOR_LISTENER_OPENAI_API_KEY") and not env.get("OPENAI_API_KEY"):
            try:
                from coordinator_listener import _prewarm_ollama
                base_url = ollama_url or "http://localhost:11434"
                model = env.get("COORDINATOR_LISTENER_OLLAMA_MODEL", "llama3.2")
                print("Pre-warming Ollama...", file=sys.stderr)
                _prewarm_ollama(base_url, model, timeout=90)
                print("Ollama pre-warm done.", file=sys.stderr)
            except Exception as e:
                print(f"Pre-warm skipped: {e}", file=sys.stderr)

    results = []
    t_start = datetime.utcnow()
    for n in sorted(SCENARIOS.keys()):
        manifest = SCENARIOS[n]
        if llm_only and manifest["type"] == "template":
            continue

        name = manifest["name"]
        msg = f"Running scenario {n} ({name})..."
        _live_log(msg, log_file) if do_live else print(msg, file=sys.stderr)
        t0 = datetime.utcnow()
        reply, passed, reason = run_scenario(n, manifest, env)
        elapsed = (datetime.utcnow() - t0).total_seconds()

        out_file = os.path.join(BENCHMARK_OUTPUT_DIR, f"{n:02d}_{name}.txt")
        with open(out_file, "w", encoding="utf-8") as f:
            f.write(reply)

        status = "PASS" if passed else "FAIL"
        results.append((n, name, status, reason, out_file))
        msg = f"  -> {status}: {reason} ({elapsed:.1f}s)"
        _live_log(msg, log_file) if do_live else print(msg, file=sys.stderr)
        if do_live and log_file:
            snippet = (reply or "")[:300].replace("\n", " ")
            _live_log(f"  Reply snippet: {snippet}...", log_file, also_stderr=False)
            _live_log("", log_file, also_stderr=False)

        # --remove-failures: move failed outputs to removed/ and log
        final_path = out_file
        if do_remove_failures and not passed:
            base_name = os.path.basename(out_file)
            stem = base_name.replace(".txt", "")
            safe_reason = _sanitize_reason_for_filename(reason)
            removed_name = f"{stem}_FAIL_{safe_reason}.txt"
            removed_path = os.path.join(BENCHMARK_REMOVED_DIR, removed_name)
            try:
                shutil.move(out_file, removed_path)
                _append_removed_entry(base_name, reason)
                final_path = removed_path
                print(f"  -> Moved to removed/{removed_name}", file=sys.stderr)
            except OSError as e:
                print(f"  -> Failed to move: {e}", file=sys.stderr)

        # Store final path for record (update last element)
        results[-1] = (n, name, status, reason, final_path)

    # Summary table
    print("\n" + "=" * 70)
    print("Jarvey Benchmark Results")
    print("=" * 70)
    print(f"{'Scenario':<10} {'Name':<30} {'Status':<6} {'Notes'}")
    print("-" * 70)
    for n, name, status, reason, _ in results:
        print(f"{n:<10} {name:<30} {status:<6} {reason}")
    print("-" * 70)
    passed_count = sum(1 for _, _, s, _, _ in results if s == "PASS")
    print(f"Passed: {passed_count}/{len(results)}")
    print(f"Output: {BENCHMARK_OUTPUT_DIR}")
    print("=" * 70)

    # --record: write TRAINING_SESSION_RECORD.json
    if do_record:
        removed_count = sum(1 for _, _, s, _, _ in results if s == "FAIL")
        record = {
            "timestamp": datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S"),
            "scenarios_run": len(results),
            "passed": passed_count,
            "failed": len(results) - passed_count,
            "results": [
                {
                    "id": n,
                    "name": name,
                    "status": status,
                    "reason": reason,
                    "output_path": os.path.relpath(out_file, REPO_ROOT),
                }
                for n, name, status, reason, out_file in results
            ],
            "removed_count": removed_count if do_remove_failures else 0,
            "removed_log": "TRAINING_DATA_REMOVED.md" if do_remove_failures and removed_count else None,
        }
        with open(TRAINING_RECORD_PATH, "w", encoding="utf-8") as f:
            json.dump(record, f, indent=2)
        print(f"Record: {TRAINING_RECORD_PATH}", file=sys.stderr)

    if log_file:
        log_file.write("\n" + "=" * 70 + "\n")
        log_file.write(f"Passed: {passed_count}/{len(results)}\n")
        log_file.write(f"Output: {BENCHMARK_OUTPUT_DIR}\n")
        log_file.close()
        print(f"Live log saved.", file=sys.stderr)

    sys.exit(0 if passed_count == len(results) else 1)


if __name__ == "__main__":
    main()
