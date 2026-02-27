#!/usr/bin/env python3
"""
Unit tests for sync_service validation logic (Security Plan 2025-02-20).

Tests key allowlist, size limit constants, and design validation.
Run: python test_sync_service.py
"""

import json
import sys
import os

# Add parent so we can import from sync_service
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from sync_service import (
    EMULATOR_TO_PROJECT,
    MAX_REQUEST_BODY_SIZE,
    validate_design_keys,
    get_paths_from_dict,
)


def test_validate_design_keys_allows_known_paths():
    """Known paths in EMULATOR_TO_PROJECT should pass."""
    design = {"toolbar": {"title": "App"}, "loadedMiles": {"hint": "Enter"}}
    assert validate_design_keys(design) is True


def test_validate_design_keys_rejects_unknown_top_level():
    """Unknown top-level key should fail."""
    design = {"evil": {"key": "value"}}
    assert validate_design_keys(design) is False


def test_validate_design_keys_rejects_unknown_nested():
    """Unknown nested path should fail."""
    design = {"toolbar": {"title": "App", "evil": "value"}}
    assert validate_design_keys(design) is False


def test_validate_design_keys_rejects_non_dict():
    """Non-dict should fail."""
    assert validate_design_keys("not a dict") is False
    assert validate_design_keys(123) is False


def test_validate_design_keys_empty_dict():
    """Empty dict should pass (no unknown keys)."""
    assert validate_design_keys({}) is True


def test_max_request_body_size():
    """Size limit should be 64KB."""
    assert MAX_REQUEST_BODY_SIZE == 64 * 1024


def test_get_paths_from_dict():
    """Path extraction should work for nested dicts."""
    d = {"a": {"b": 1, "c": 2}}
    paths = get_paths_from_dict(d)
    assert "a.b" in paths
    assert "a.c" in paths
    assert len(paths) == 2


if __name__ == "__main__":
    tests = [
        test_validate_design_keys_allows_known_paths,
        test_validate_design_keys_rejects_unknown_top_level,
        test_validate_design_keys_rejects_unknown_nested,
        test_validate_design_keys_rejects_non_dict,
        test_validate_design_keys_empty_dict,
        test_max_request_body_size,
        test_get_paths_from_dict,
    ]
    failed = 0
    for t in tests:
        try:
            t()
            print(f"  OK {t.__name__}")
        except AssertionError as e:
            print(f"  FAIL {t.__name__}: {e}")
            failed += 1
    if failed:
        sys.exit(1)
    print(f"\nAll {len(tests)} tests passed.")
