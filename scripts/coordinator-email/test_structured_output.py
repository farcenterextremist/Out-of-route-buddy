#!/usr/bin/env python3
"""
Unit tests for structured_output: parse_structured_reply, execute_action.
"""

import os
import sys
import unittest

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.path.dirname(__file__) and os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)


class TestParseStructuredReply(unittest.TestCase):
    """parse_structured_reply extracts action and params from LLM reply."""

    def test_valid_json_block_returns_action(self):
        from structured_output import parse_structured_reply

        reply = 'Here is the response.\n\n```json\n{"action": "send_digest", "params": {}}\n```'
        result = parse_structured_reply(reply)
        self.assertIsNotNone(result)
        self.assertEqual(result["action"], "send_digest")
        self.assertEqual(result.get("params"), {})

    def test_plain_json_object_returns_action(self):
        from structured_output import parse_structured_reply

        reply = '{"action": "clarify", "params": {"options": ["a", "b"]}}'
        result = parse_structured_reply(reply)
        self.assertIsNotNone(result)
        self.assertEqual(result["action"], "clarify")
        self.assertEqual(result.get("params"), {"options": ["a", "b"]})

    def test_unsupported_action_returns_none(self):
        from structured_output import parse_structured_reply

        reply = '```json\n{"action": "unknown_action", "params": {}}\n```'
        result = parse_structured_reply(reply)
        self.assertIsNone(result)

    def test_invalid_json_returns_none(self):
        from structured_output import parse_structured_reply

        reply = "Some text with no valid JSON"
        result = parse_structured_reply(reply)
        self.assertIsNone(result)

    def test_empty_returns_none(self):
        from structured_output import parse_structured_reply

        self.assertIsNone(parse_structured_reply(""))
        self.assertIsNone(parse_structured_reply("   "))

    def test_save_note_json_block_returns_action(self):
        from structured_output import parse_structured_reply

        reply = 'I\'ve added that.\n\n```json\n{"action": "save_note", "params": {"note": "Prioritize Reports", "topic": "priorities"}}\n```'
        result = parse_structured_reply(reply)
        self.assertIsNotNone(result)
        self.assertEqual(result["action"], "save_note")
        self.assertEqual(result.get("params"), {"note": "Prioritize Reports", "topic": "priorities"})


class TestStripStructuredFromReply(unittest.TestCase):
    """strip_structured_from_reply removes JSON blocks from reply text."""

    def test_strips_json_block(self):
        from structured_output import strip_structured_from_reply

        reply = "Thanks. I've added that to the project notes.\n\n```json\n{\"action\": \"save_note\", \"params\": {\"note\": \"Prioritize Reports\"}}\n```"
        result = strip_structured_from_reply(reply)
        self.assertNotIn("```json", result)
        self.assertNotIn("save_note", result)
        self.assertIn("Thanks", result)

    def test_empty_unchanged(self):
        from structured_output import strip_structured_from_reply

        self.assertEqual(strip_structured_from_reply(""), "")
        self.assertEqual(strip_structured_from_reply(""), "")


class TestExecuteAction(unittest.TestCase):
    """execute_action runs structured actions and returns body."""

    def test_execute_action_send_digest_returns_non_empty(self):
        from structured_output import execute_action

        env = {}
        result = execute_action("send_digest", {}, env)
        self.assertIsNotNone(result)
        self.assertIn("Jarvey", result)
        self.assertGreater(len(result), 20)

    def test_execute_action_clarify_returns_non_empty(self):
        from structured_output import execute_action

        env = {}
        result = execute_action("clarify", {}, env)
        self.assertIsNotNone(result)
        self.assertIn("couldn't make out", result)
        self.assertIn("Jarvey", result)

    def test_unknown_action_returns_none(self):
        from structured_output import execute_action

        result = execute_action("unknown", {}, {})
        self.assertIsNone(result)

    def test_save_note_returns_none(self):
        from structured_output import execute_action

        result = execute_action("save_note", {"note": "Test note", "topic": "test"}, {})
        self.assertIsNone(result)


if __name__ == "__main__":
    unittest.main()
