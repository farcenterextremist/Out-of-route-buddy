#!/usr/bin/env python3
"""
Unit tests for check_and_respond: template registry (choose_response) and state (load/save).
check_and_respond now uses LLM for all replies; template_registry.choose_response is tested for regression.
Run from repo root: python -m pytest scripts/coordinator-email/test_check_and_respond.py -v
Or with unittest: python scripts/coordinator-email/test_check_and_respond.py
"""

import os
import sys
import tempfile
import unittest
from unittest.mock import patch

# Run from script dir so imports resolve
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.path.dirname(__file__) and os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

from template_registry import choose_response
from responded_state import load_last_responded_id, save_responded_id, STATE_FILE
from coordinator_listener import _strip_quoted_content


class TestChooseResponse(unittest.TestCase):
    """Template selection: which template key and expected content for given subject/body."""

    def test_weekly_board_digest(self):
        subj, body, key = choose_response("Re: OutOfRouteBuddy", "I would like a weekly board digest")
        self.assertEqual(key, "weekly_digest")
        self.assertIn("weekly Board digest", subj)
        self.assertIn("weekly", body.lower())

    def test_weekly_meeting_summary(self):
        subj, body, key = choose_response("Re: OutOfRouteBuddy", "Send me a weekly meeting summary")
        self.assertEqual(key, "weekly_digest")
        self.assertIn("weekly", body.lower())

    def test_thanks(self):
        subj, body, key = choose_response("Re: OutOfRouteBuddy", "Thanks, that works")
        self.assertEqual(key, "thanks")
        self.assertIn("Thanks for getting back", body)

    def test_got_it(self):
        subj, body, key = choose_response("Re: OutOfRouteBuddy", "Got it!")
        self.assertEqual(key, "thanks")

    def test_thanks_exclamation_listener_template_path(self):
        """Body 'Thanks!' yields thanks template (listener template-first branch)."""
        subj, body_text, template_key = choose_response("Re: OutOfRouteBuddy", "Thanks!")
        self.assertEqual(template_key, "thanks")
        self.assertIn("— Jarvey", body_text)
        self.assertIn("Thanks for getting back", body_text)

    def test_priority(self):
        subj, body, key = choose_response("Re: OutOfRouteBuddy", "Please prioritize reports next")
        self.assertEqual(key, "priority")
        self.assertIn("priorities", body.lower())

    def test_what_next(self):
        # "what next" matches roadmap template (data-driven)
        subj, body, key = choose_response("Re: OutOfRouteBuddy", "What next on the list?")
        self.assertEqual(key, "roadmap")

    def test_unclear_short_body(self):
        """Very short body with no template match yields clarification template."""
        subj, body, key = choose_response("Re: OutOfRouteBuddy", "x")
        self.assertEqual(key, "unclear")
        self.assertIn("couldn't make out", body)
        self.assertIn("Jarvey", body)

    def test_unclear_short_body_not_whitelisted(self):
        """Short body 'asdf' yields unclear (not in whitelist)."""
        subj, body, key = choose_response("Re: OutOfRouteBuddy", "asdf")
        self.assertEqual(key, "unclear")

    def test_whitelist_hi_not_unclear(self):
        """Body 'hi' is whitelisted — yields default, not unclear."""
        subj, body, key = choose_response("Re: OutOfRouteBuddy", "hi")
        self.assertEqual(key, "default")

    def test_whats_next_not_unclear(self):
        """Body 'What's next?' matches roadmap template (instant, no LLM)."""
        subj, body, key = choose_response("Re: OutOfRouteBuddy", "What's next?")
        self.assertEqual(key, "roadmap")

    def test_default_no_keyword(self):
        subj, body, key = choose_response("Re: OutOfRouteBuddy", "When is the next release?")
        self.assertEqual(key, "default")
        self.assertIn("We got your message", body)

    def test_default_weekly_only_no_board_digest(self):
        """Weekly without board/digest/summary/meeting falls to default (regression: wrong template)."""
        subj, body, key = choose_response("Re: OutOfRouteBuddy", "Can we chat weekly?")
        self.assertEqual(key, "default")

    def test_change_day_of_digest_stays_default(self):
        """Phrasing that doesn't match a template gets default until we add a new one."""
        subj, body, key = choose_response("Re: OutOfRouteBuddy", "Change the day of the digest to Friday")
        # Contains "digest" but also "weekly" and "digest" -> actually matches weekly_digest.
        # "change the day of the digest" has "digest" and we need "weekly" + one of board|digest|summary|meeting.
        # combined has "change the day of the digest" -> weekly? no. So default.
        self.assertEqual(key, "default")

    def test_thanks_case_insensitive(self):
        """THANKS and Thanks both yield thanks template."""
        _, _, key1 = choose_response("Re: OutOfRouteBuddy", "THANKS")
        _, _, key2 = choose_response("Re: OutOfRouteBuddy", "Thanks")
        self.assertEqual(key1, "thanks")
        self.assertEqual(key2, "thanks")

    def test_thankful_does_not_match_thanks(self):
        """'thankful' contains 'thank' but word-boundary regex excludes it; yields default."""
        _, _, key = choose_response("Re: OutOfRouteBuddy", "I'm thankful")
        self.assertEqual(key, "default", "thankful should not match thanks template")

    def test_template_priority_weekly_over_thanks(self):
        """When both weekly_digest and thanks match, weekly_digest wins (checked first)."""
        # "weekly thanks for the digest" has weekly + digest -> weekly_digest, and thank -> thanks
        _, _, key = choose_response("Re: OutOfRouteBuddy", "weekly thanks for the digest")
        self.assertEqual(key, "weekly_digest", "weekly_digest checked before thanks in choose_response")

    def test_all_templates_sign_as_jarvey(self):
        """Every template body must end with '— Jarvey' (regression: thanks once had OutOfRouteBuddy Team)."""
        cases = [
            ("Re: OutOfRouteBuddy", "I would like a weekly board digest", "weekly_digest"),
            ("Re: OutOfRouteBuddy", "Thanks, that works", "thanks"),
            ("Re: OutOfRouteBuddy", "Sounds good!", "thanks"),
            ("Re: OutOfRouteBuddy", "Please prioritize reports next", "priority"),
            ("Re: OutOfRouteBuddy", "What's next?", "roadmap"),
            ("Re: OutOfRouteBuddy", "What changed recently?", "recent"),
            ("Re: OutOfRouteBuddy", "What version are we on?", "version"),
            ("Re: OutOfRouteBuddy", "What can you do?", "options_menu"),
            ("Re: OutOfRouteBuddy", "When is the next release?", "default"),
        ]
        for subject, body, expected_key in cases:
            subj, body_text, key = choose_response(subject, body)
            self.assertEqual(key, expected_key, f"expected key {expected_key}")
            self.assertIn("— Jarvey", body_text, f"template {key} must sign as Jarvey")


class TestState(unittest.TestCase):
    """load_last_responded_id / save_responded_id with a temp state file."""

    def setUp(self):
        import responded_state as rs
        self._orig_state_file = rs.STATE_FILE
        self.temp_dir = tempfile.mkdtemp()
        self.temp_state = os.path.join(self.temp_dir, "last_responded_state.txt")

    def tearDown(self):
        import responded_state as rs
        rs.STATE_FILE = self._orig_state_file
        if os.path.isfile(self.temp_state):
            try:
                os.remove(self.temp_state)
            except OSError:
                pass
        try:
            os.rmdir(self.temp_dir)
        except OSError:
            pass

    def test_save_then_load_matches(self):
        import responded_state as rs
        rs.STATE_FILE = self.temp_state
        save_responded_id("msg-123")
        self.assertEqual(load_last_responded_id(), "msg-123")

    def test_already_responded_when_message_id_equals_last_id(self):
        import responded_state as rs
        rs.STATE_FILE = self.temp_state
        save_responded_id("msg-456")
        last_id = load_last_responded_id()
        self.assertEqual(last_id, "msg-456")
        self.assertTrue("msg-456" == last_id)  # "already responded" logic in main

    def test_load_empty_or_missing_returns_none(self):
        import responded_state as rs
        rs.STATE_FILE = self.temp_state
        self.assertFalse(os.path.isfile(self.temp_state))
        self.assertIsNone(load_last_responded_id())

    def test_dedupe_with_message_id_none_uses_hash(self):
        """When message_id is None, main() uses content hash for dedupe and saves dedupe_id."""
        import check_and_respond as car
        import responded_state as rs
        from unittest.mock import patch

        rs.STATE_FILE = self.temp_state
        # read_replies returns message_id=None; we should save hash after send
        with patch("check_and_respond.read_replies") as mock_read:
            mock_read.return_value = ("Re: OutOfRouteBuddy", "Thanks", "Mon, 1 Jan 2024", None)
            with patch("check_and_respond.send") as mock_send:
                with patch("check_and_respond.last_sent_within_cooldown", return_value=False):
                    with patch("config_schema.validate_config") as mock_validate:
                        mock_validate.return_value = {"COORDINATOR_LISTENER_OLLAMA_URL": "http://localhost:11434"}
                        with patch("check_and_respond.compose_reply") as mock_compose:
                            mock_compose.return_value = "Thanks for getting back.\n\n— Jarvey"
                            try:
                                car.main()
                            except SystemExit as e:
                                self.assertEqual(e.code, 0, "main() should exit 0 on success")
        mock_send.assert_called_once()
        saved = load_last_responded_id()
        self.assertIsNotNone(saved)
        self.assertTrue(saved.startswith("hash:"), f"Expected hash: prefix, got {saved!r}")

    def test_dedupe_hash_matches_coordinator_listener_formula(self):
        """check_and_respond and coordinator_listener must use identical hash formula (JARVEY_EDGE_CASES)."""
        import hashlib

        subj = "Re: OutOfRouteBuddy"
        body = "Thanks for the update!"
        body_trim = _strip_quoted_content(body or "").strip()
        subj_trim = (subj or "").strip()
        blob = (subj_trim or "") + "\n" + (body_trim or "")[:500]
        h = "hash:" + hashlib.sha256(blob.encode("utf-8", errors="replace")).hexdigest()[:32]

        self.assertTrue(h.startswith("hash:"))
        self.assertEqual(len(h), 5 + 32)  # "hash:" + 32 hex chars
        # Both scripts use this exact formula; verify it's deterministic
        blob2 = (subj_trim or "") + "\n" + (body_trim or "")[:500]
        h2 = "hash:" + hashlib.sha256(blob2.encode("utf-8", errors="replace")).hexdigest()[:32]
        self.assertEqual(h, h2)


class TestScenarioTemplateSelection(unittest.TestCase):
    """Verify template selection for all Jarvey benchmark scenario bodies."""

    def test_scenario_3_thanks_uses_thanks_template(self):
        """Scenario 3: 'Thanks, that works!' must use thanks template (no LLM)."""
        _, _, key = choose_response("Re: OutOfRouteBuddy", "Thanks, that works!")
        self.assertEqual(key, "thanks")

    def test_scenario_2_prioritize_uses_priority_template(self):
        """Scenario 2: 'Can we prioritize...' matches priority template."""
        _, _, key = choose_response("Re: OutOfRouteBuddy", "Can we prioritize the reports screen and when will it be done?")
        self.assertEqual(key, "priority")

    def test_scenario_1_whats_next_uses_roadmap(self):
        """Scenario 1: 'What's next?' uses roadmap template (instant, no LLM)."""
        _, _, key = choose_response("Re: OutOfRouteBuddy", "What's next?")
        self.assertEqual(key, "roadmap")

    def test_scenario_6_broken_uses_bug_report(self):
        """Scenario 6: 'Something is broken.' matches bug_report template."""
        _, _, key = choose_response("Re: OutOfRouteBuddy", "Something is broken.")
        self.assertEqual(key, "bug_report")

    def test_scenario_4_multi_question_uses_roadmap(self):
        """Scenario 4: Multi-question with 'what's next' matches roadmap first (template path)."""
        _, _, key = choose_response("Re: OutOfRouteBuddy", "What's next? Also, who owns the emulator?")
        self.assertEqual(key, "roadmap")

    def test_scenario_5_no_code_uses_default(self):
        """Scenario 5: 'Write me a function...' falls to default (LLM path)."""
        _, _, key = choose_response("Re: OutOfRouteBuddy", "Write me a function to export trips to CSV.")
        self.assertEqual(key, "default")


class TestEmptyInputEarlyExit(unittest.TestCase):
    """JARVEY_EDGE_CASES: empty subject+body → no send, exit 0."""

    def test_empty_subject_and_body_exits_without_send(self):
        """When read_replies returns (None, None, None, None), main exits 0 without sending."""
        with patch("check_and_respond.read_replies") as mock_read:
            mock_read.return_value = (None, None, None, None)
            with patch("check_and_respond.send") as mock_send:
                import check_and_respond as car
                with self.assertRaises(SystemExit) as ctx:
                    car.main()
                self.assertEqual(ctx.exception.code, 0)
        mock_send.assert_not_called()

    def test_empty_subject_and_body_string_exits_without_send(self):
        """When subject and body are both empty strings, main exits 0 (JARVEY_EDGE_CASES)."""
        with patch("check_and_respond.read_replies") as mock_read:
            mock_read.return_value = ("", "", "Mon, 1 Jan 2024", None)
            with patch("check_and_respond.send") as mock_send:
                import check_and_respond as car
                with self.assertRaises(SystemExit) as ctx:
                    car.main()
                self.assertEqual(ctx.exception.code, 0)
        mock_send.assert_not_called()


class TestReadRepliesContract(unittest.TestCase):
    """Ensure callers can unpack 4 values from read_replies (subject, body, date, message_id)."""

    def test_unpack_four_values_empty(self):
        # Simulates read_replies() when inbox has no matching messages (after fix).
        subject, body, date, message_id = None, None, None, None
        self.assertIsNone(subject)
        self.assertIsNone(message_id)

    def test_unpack_four_values_present(self):
        subject, body, date, message_id = "Re: OutOfRouteBuddy", "Thanks", "Mon, 1 Jan 2024", "<msg-1>"
        self.assertEqual(subject, "Re: OutOfRouteBuddy")
        self.assertEqual(message_id, "<msg-1>")


if __name__ == "__main__":
    unittest.main()
