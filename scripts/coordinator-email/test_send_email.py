#!/usr/bin/env python3
"""
Unit tests for send_email: dry run, required env.
Run from repo root: python scripts/coordinator-email/test_send_email.py
"""

import os
import sys
import unittest
from unittest.mock import patch

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.path.dirname(__file__) and os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

from send_email import send


class TestSendDryRun(unittest.TestCase):
    """COORDINATOR_DRY_RUN or dry_run=True skips actual send."""

    def test_dry_run_param_skips_send(self):
        """When dry_run=True, send returns without connecting to SMTP."""
        with patch("send_email.load_env") as mock_env:
            mock_env.return_value = {
                "COORDINATOR_EMAIL_TO": "user@example.com",
                "COORDINATOR_EMAIL_FROM": "bot@example.com",
                "COORDINATOR_SMTP_HOST": "smtp.example.com",
                "COORDINATOR_SMTP_PORT": "587",
                "COORDINATOR_SMTP_USER": "user",
                "COORDINATOR_SMTP_PASSWORD": "pass",
            }
            with patch("send_email.smtplib.SMTP") as mock_smtp:
                send("Subject", "Body", dry_run=True)
        mock_smtp.assert_not_called()

    def test_env_dry_run_skips_send(self):
        """When COORDINATOR_DRY_RUN=1, send returns without connecting."""
        with patch("send_email.load_env") as mock_env:
            mock_env.return_value = {
                "COORDINATOR_DRY_RUN": "1",
                "COORDINATOR_EMAIL_TO": "user@example.com",
                "COORDINATOR_SMTP_HOST": "smtp.example.com",
                "COORDINATOR_SMTP_USER": "user",
                "COORDINATOR_SMTP_PASSWORD": "pass",
            }
            with patch("send_email.smtplib.SMTP") as mock_smtp:
                send("Subject", "Body")
        mock_smtp.assert_not_called()


if __name__ == "__main__":
    unittest.main()
