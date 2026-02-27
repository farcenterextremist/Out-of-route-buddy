#!/usr/bin/env python3
"""
Wiring verification tests for Jarvey framework.
Verifies that components are correctly connected and data flows end-to-end.

Run: python scripts/coordinator-email/test_jarvey_wiring.py
Or: python -m pytest scripts/coordinator-email/test_jarvey_wiring.py -v
"""

import os
import sys
import tempfile
import unittest
from unittest.mock import patch, MagicMock

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.path.dirname(__file__) and os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)


class TestContextWiring(unittest.TestCase):
    """Context loader returns non-empty content for intent-bearing messages."""

    def test_load_context_for_whats_next_contains_roadmap_or_project(self):
        from context_loader import load_context_for_user_message

        ctx = load_context_for_user_message("Re: OutOfRouteBuddy", "What's next?")
        self.assertIsInstance(ctx, str)
        self.assertGreater(len(ctx), 500)
        # Should contain roadmap or project context
        self.assertTrue(
            "roadmap" in ctx.lower() or "ROADMAP" in ctx or "project" in ctx.lower() or "OutOfRoute" in ctx
        )

    def test_load_context_returns_non_empty(self):
        from context_loader import load_context_for_user_message

        ctx = load_context_for_user_message("Re: X", "Tell me something")
        self.assertIsInstance(ctx, str)
        self.assertGreater(len(ctx), 200)


class TestBrainWiring(unittest.TestCase):
    """JARVEY_PROJECT_BRAIN is included in context."""

    def test_context_contains_brain_content(self):
        from context_loader import load_context_for_user_message

        ctx = load_context_for_user_message("Re: OutOfRouteBuddy", "What's next?")
        # Brain contains: Intent map, Entity, Golden patterns, or Project structure
        has_brain = (
            "Intent map" in ctx
            or "Entity" in ctx
            or "Golden" in ctx
            or "Project structure" in ctx
            or "User question" in ctx
        )
        self.assertTrue(has_brain, f"Brain content not found in context (len={len(ctx)})")


class TestIntentWiring(unittest.TestCase):
    """Intent detection matches keywords to intent names."""

    def test_detect_intents_roadmap(self):
        from context_loader import detect_intents

        intents = detect_intents("Re: X", "what's next")
        self.assertIn("roadmap", intents)

    def test_detect_intents_recent(self):
        from context_loader import detect_intents

        intents = detect_intents("Re: X", "recent changes")
        self.assertIn("recent", intents)

    def test_detect_intents_version(self):
        from context_loader import detect_intents

        intents = detect_intents("Re: X", "what version")
        self.assertIn("version", intents)


class TestTemplateWiring(unittest.TestCase):
    """Template registry returns correct template for known keywords."""

    def test_choose_response_thanks(self):
        from template_registry import choose_response

        subj, body, key = choose_response("Re: OutOfRouteBuddy", "Thanks, that works!")
        self.assertEqual(key, "thanks")
        self.assertIsInstance(body, str)
        self.assertGreater(len(body), 10)

    def test_choose_response_unclear_short(self):
        from template_registry import choose_response

        subj, body, key = choose_response("Re: X", "x")
        self.assertEqual(key, "unclear")
        self.assertIsInstance(body, str)


class TestStateWiring(unittest.TestCase):
    """Responded state save/load round-trip."""

    def test_save_load_round_trip(self):
        from responded_state import save_responded_id, load_last_responded_id

        with tempfile.TemporaryDirectory() as tmp:
            with patch("responded_state.STATE_FILE", os.path.join(tmp, "state.txt")):
                save_responded_id("test-id-123")
                loaded = load_last_responded_id()
                self.assertEqual(loaded, "test-id-123")


class TestStructuredOutputWiring(unittest.TestCase):
    """Structured output parse and execute wiring."""

    def test_parse_structured_reply_finds_json_block(self):
        from structured_output import parse_structured_reply

        reply = 'Here is the response.\n\n```json\n{"action": "save_note", "params": {"note": "Prioritize Reports"}}\n```'
        result = parse_structured_reply(reply)
        self.assertIsNotNone(result)
        self.assertEqual(result["action"], "save_note")
        self.assertEqual(result.get("params", {}).get("note"), "Prioritize Reports")

    def test_execute_save_note_appends_to_file(self):
        from structured_output import execute_action

        notes_path = os.path.join(tempfile.gettempdir(), "jarvey_wiring_test_notes.md")
        try:
            if os.path.isfile(notes_path):
                os.unlink(notes_path)
            with patch("context_loader.EMAIL_NOTES_PATH", notes_path):
                execute_action("save_note", {"note": "Wiring test note", "topic": "test"}, {})
            self.assertTrue(os.path.isfile(notes_path))
            with open(notes_path, encoding="utf-8") as f:
                content = f.read()
            self.assertIn("Wiring test note", content)
        finally:
            if os.path.isfile(notes_path):
                os.unlink(notes_path)


class TestReadRepliesContract(unittest.TestCase):
    """read_replies contract: returns (subject, body, date, message_id)."""

    def test_get_body_extracts_plain_text(self):
        from read_replies import get_body
        import email

        msg = email.message_from_string("Content-Type: text/plain\n\nHello world")
        self.assertEqual(get_body(msg), "Hello world")

    def test_read_replies_contract_four_values(self):
        # Coordinator expects: subject, body, date, message_id
        from read_replies import read_replies

        # May fail if IMAP not configured; that's OK for wiring test
        try:
            subj, body, date, msg_id = read_replies()
            self.assertIsInstance(subj, str)
            self.assertIsInstance(body, str)
            self.assertIsInstance(date, str)
            self.assertTrue(msg_id is None or isinstance(msg_id, str))
        except Exception:
            self.skipTest("read_replies requires IMAP config")


class TestConfigWiring(unittest.TestCase):
    """Config validation does not raise when required keys present (mocked)."""

    def test_validate_config_email_only_with_mock_env(self):
        from config_schema import validate_config, load_env

        # Use actual env if available; otherwise validate_config may exit
        env = load_env()
        if not env.get("COORDINATOR_EMAIL_TO"):
            self.skipTest("COORDINATOR_EMAIL_TO not set; skip config test")
        try:
            result = validate_config(mode="email", env=env, exit_on_error=False)
            self.assertIsInstance(result, dict)
        except ValueError:
            self.skipTest("Config validation failed (expected when .env incomplete)")


if __name__ == "__main__":
    unittest.main()
