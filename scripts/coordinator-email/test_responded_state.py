#!/usr/bin/env python3
"""
Unit tests for responded_state: cooldown, dedupe state, env config.
Run from repo root: python -m pytest scripts/coordinator-email/test_responded_state.py -v
"""

import os
import sys
import tempfile
import time
import unittest
from unittest.mock import patch

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.path.dirname(__file__) and os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)


class TestCooldownEnv(unittest.TestCase):
    """Cooldown uses JARVEY_COOLDOWN_SECONDS from env when set."""

    def test_get_cooldown_seconds_default(self):
        """When JARVEY_COOLDOWN_SECONDS is unset, default is 120."""
        import responded_state as rs
        with patch.dict(os.environ, {}, clear=False):
            if "JARVEY_COOLDOWN_SECONDS" in os.environ:
                del os.environ["JARVEY_COOLDOWN_SECONDS"]
            self.assertEqual(rs.get_cooldown_seconds(), 120)

    def test_get_cooldown_seconds_uses_env(self):
        """When JARVEY_COOLDOWN_SECONDS=60, get_cooldown_seconds returns 60."""
        import responded_state as rs
        with patch.dict(os.environ, {"JARVEY_COOLDOWN_SECONDS": "60"}):
            self.assertEqual(rs.get_cooldown_seconds(), 60)

    def test_last_sent_within_cooldown_uses_env(self):
        """When JARVEY_COOLDOWN_SECONDS=60, last_sent_within_cooldown uses env value."""
        import responded_state as rs
        with tempfile.TemporaryDirectory() as tmp:
            cooldown_file = os.path.join(tmp, "last_sent_timestamp.txt")
            # Write timestamp from 45 seconds ago
            with open(cooldown_file, "w") as f:
                f.write(str(time.time() - 45))
            with patch.object(rs, "COOLDOWN_FILE", cooldown_file):
                with patch.dict(os.environ, {"JARVEY_COOLDOWN_SECONDS": "60"}):
                    self.assertTrue(rs.last_sent_within_cooldown())
                with patch.dict(os.environ, {"JARVEY_COOLDOWN_SECONDS": "30"}):
                    self.assertFalse(rs.last_sent_within_cooldown())

    def test_get_cooldown_seconds_invalid_falls_back(self):
        """When JARVEY_COOLDOWN_SECONDS is invalid, fallback to 120."""
        import responded_state as rs
        with patch.dict(os.environ, {"JARVEY_COOLDOWN_SECONDS": "not_a_number"}):
            self.assertEqual(rs.get_cooldown_seconds(), 120)


if __name__ == "__main__":
    unittest.main()
