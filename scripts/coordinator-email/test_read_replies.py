#!/usr/bin/env python3
"""
Unit tests for read_replies: _normalize_email, get_body.
Run from repo root: python -m pytest scripts/coordinator-email/test_read_replies.py -v
"""

import email
import os
import sys
import unittest

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.path.dirname(__file__) and os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

from read_replies import _normalize_email, get_body


class TestNormalizeEmail(unittest.TestCase):
    """_normalize_email returns lowercase email or None."""

    def test_simple_address(self):
        self.assertEqual(_normalize_email("user@example.com"), "user@example.com")

    def test_angle_bracket_format(self):
        self.assertEqual(_normalize_email("John Doe <john@example.com>"), "john@example.com")

    def test_uppercase_normalized_to_lowercase(self):
        self.assertEqual(_normalize_email("User@Example.COM"), "user@example.com")

    def test_empty_returns_none(self):
        self.assertIsNone(_normalize_email(""))
        self.assertIsNone(_normalize_email(None))

    def test_not_string_returns_none(self):
        self.assertIsNone(_normalize_email(123))

    def test_no_at_returns_none(self):
        self.assertIsNone(_normalize_email("invalid"))

    def test_whitespace_stripped(self):
        self.assertEqual(_normalize_email("  user@example.com  "), "user@example.com")


class TestGetBody(unittest.TestCase):
    """get_body extracts plain text from email message."""

    def test_simple_plain_text(self):
        raw = "Subject: Test\n\nHello world"
        msg = email.message_from_string(raw)
        self.assertEqual(get_body(msg), "Hello world")

    def test_multipart_plain_first(self):
        raw = """MIME-Version: 1.0
Content-Type: multipart/alternative; boundary="bound"

--bound
Content-Type: text/plain

Plain body here
--bound
Content-Type: text/html

<html>HTML body</html>
--bound--
"""
        msg = email.message_from_string(raw)
        self.assertEqual(get_body(msg), "Plain body here")

    def test_empty_payload_returns_empty(self):
        raw = "Subject: Test\n\n"
        msg = email.message_from_string(raw)
        self.assertEqual(get_body(msg), "")

    def test_strips_whitespace(self):
        raw = "Subject: Test\n\n  body with spaces  \n"
        msg = email.message_from_string(raw)
        self.assertEqual(get_body(msg), "body with spaces")

    def test_multipart_html_only_returns_empty(self):
        """When multipart has only text/html, no text/plain, get_body returns empty."""
        raw = """MIME-Version: 1.0
Content-Type: multipart/alternative; boundary="bound"

--bound
Content-Type: text/html

<html><body>HTML only</body></html>
--bound--
"""
        msg = email.message_from_string(raw)
        self.assertEqual(get_body(msg), "")


if __name__ == "__main__":
    unittest.main()
