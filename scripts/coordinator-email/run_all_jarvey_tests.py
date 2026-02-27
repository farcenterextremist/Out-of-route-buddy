#!/usr/bin/env python3
"""
Ultimate Jarvey testing suite — single entry point for all tests and scenarios.

Usage:
  python run_all_jarvey_tests.py              # Unit tests only (fast, no LLM)
  python run_all_jarvey_tests.py --all        # Unit + LLM regression + benchmark
  python run_all_jarvey_tests.py --llm       # Unit + LLM regression only
  python run_all_jarvey_tests.py --benchmark # Unit + full 9-scenario benchmark

Unit tests cover:
  - Template selection (choose_response), state, dedupe, cooldown
  - Quoted stripping, sign-off, read_replies contract
  - Context loader (intents, ROADMAP intent-only, project index)
  - Empty message handling, default→LLM fallthrough
  - Anti-hallucination regression (body "test")
  - send_email dry run, read_replies get_body

LLM tests require COORDINATOR_LISTENER_OPENAI_API_KEY or OLLAMA_URL.
See docs/agents/TESTING_SUITE.md for full documentation.
"""

import os
import subprocess
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))

# Test modules (unit tests, no LLM required)
UNIT_TEST_MODULES = [
    "test_check_and_respond",
    "test_context_loader",
    "test_read_replies",
    "test_send_email",
    "test_coordinator_listener",
    "test_mock_llm",
    "test_responded_state",
    "test_structured_output",
    "test_edge_case_scenarios",
    "test_jarvey_wiring",
    "test_data_model_link",
    "test_jarvey_data_access",
]

# LLM-dependent tests (skipped if no API key)
LLM_TEST_MODULES = [
    "test_scenario_regression",
]


def _has_pytest() -> bool:
    """True if pytest is available."""
    try:
        import pytest  # noqa: F401
        return True
    except ImportError:
        return False


def run_pytest(modules: list[str], extra_args: list[str] | None = None) -> int:
    """Run pytest on given modules. Returns exit code."""
    args = ["-m", "pytest", "-v", "--tb=short"]
    if extra_args:
        args.extend(extra_args)
    for mod in modules:
        args.append(f"{SCRIPT_DIR}/{mod}.py")
    return subprocess.call(
        [sys.executable] + args,
        cwd=REPO_ROOT,
    )


def run_unittest(modules: list[str]) -> int:
    """Run unittest on given modules. Returns exit code."""
    args = [sys.executable, "-m", "unittest"]
    for mod in modules:
        args.append(mod)
    return subprocess.call(args, cwd=SCRIPT_DIR)


def run_benchmark() -> int:
    """Run full 9-scenario Jarvey benchmark. Returns exit code."""
    return subprocess.call(
        [sys.executable, os.path.join(SCRIPT_DIR, "run_jarvey_benchmark.py")],
        cwd=SCRIPT_DIR,
    )


def has_llm_config() -> bool:
    """True if OpenAI API key or Ollama URL is configured."""
    try:
        import coordinator_listener as cl
        env = cl.load_env()
        api = env.get("COORDINATOR_LISTENER_OPENAI_API_KEY") or env.get("OPENAI_API_KEY")
        ollama = env.get("COORDINATOR_LISTENER_OLLAMA_URL") or env.get("OLLAMA_URL")
        return bool(api or ollama)
    except Exception:
        return False


def main():
    os.chdir(SCRIPT_DIR)
    if REPO_ROOT not in sys.path:
        sys.path.insert(0, REPO_ROOT)

    args = sys.argv[1:]
    run_llm = "--llm" in args or "--all" in args
    run_bench = "--benchmark" in args or "--all" in args

    print("=" * 70)
    print("Jarvey Ultimate Test Suite")
    print("=" * 70)

    # 1. Unit tests (always run)
    print("\n[1/3] Unit tests (no LLM required)...")
    if _has_pytest():
        exit_unit = run_pytest(UNIT_TEST_MODULES)
    else:
        print("(pytest not installed; using unittest)", file=sys.stderr)
        exit_unit = run_unittest(UNIT_TEST_MODULES)
    if exit_unit != 0:
        print("\nUnit tests FAILED. Stopping.")
        sys.exit(exit_unit)
    print("\nUnit tests: PASSED")

    # 2. LLM regression (optional)
    if run_llm or run_bench:
        if not has_llm_config():
            print("\n[2/3] LLM tests: SKIPPED (no COORDINATOR_LISTENER_OPENAI_API_KEY or OLLAMA_URL)")
        else:
            print("\n[2/3] LLM regression tests...")
            if _has_pytest():
                exit_llm = run_pytest(LLM_TEST_MODULES, extra_args=["-s"])
            else:
                exit_llm = run_unittest(LLM_TEST_MODULES)
            if exit_llm != 0:
                print("\nLLM regression tests FAILED.")
                sys.exit(exit_llm)
            print("\nLLM regression: PASSED")
    else:
        print("\n[2/3] LLM tests: SKIPPED (use --llm or --all to run)")

    # 3. Benchmark (optional)
    if run_bench:
        if not has_llm_config():
            print("\n[3/3] Benchmark: SKIPPED (no LLM configured)")
        else:
            print("\n[3/3] Jarvey benchmark (9 scenarios)...")
            exit_bench = run_benchmark()
            if exit_bench != 0:
                print("\nBenchmark FAILED.")
                sys.exit(exit_bench)
            print("\nBenchmark: PASSED")
    else:
        print("\n[3/3] Benchmark: SKIPPED (use --benchmark or --all to run)")

    print("\n" + "=" * 70)
    print("All requested tests completed successfully.")
    print("=" * 70)
    sys.exit(0)


if __name__ == "__main__":
    main()
