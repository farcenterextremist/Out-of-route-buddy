#!/usr/bin/env python3
"""
Tests for mock_llm and LLM-path simulations without Ollama/OpenAI.
Run: python -m pytest scripts/coordinator-email/test_mock_llm.py -v
"""

import os
import sys
import unittest
from unittest.mock import patch

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.path.dirname(__file__) and os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)


class TestMockLLMResponses(unittest.TestCase):
    """Mock LLM returns deterministic replies for common intents."""

    def test_capability_query(self):
        from mock_llm import compose_reply_mock

        reply = compose_reply_mock("Re: OutOfRouteBuddy", "What are you capable of?")
        self.assertIn("Jarvey", reply)
        self.assertIn("Master Branch Coordinator", reply)
        self.assertIn("HITL", reply)
        self.assertIn("roadmap", reply.lower())

    def test_whats_next_returns_roadmap_placeholder(self):
        from mock_llm import compose_reply_mock

        reply = compose_reply_mock("Re: OutOfRouteBuddy", "What's next?")
        self.assertIn("roadmap", reply.lower())
        self.assertIn("Jarvey", reply)

    def test_default_fallback(self):
        from mock_llm import compose_reply_mock

        reply = compose_reply_mock("Re: OutOfRouteBuddy", "Random message")
        self.assertIn("Thanks for your message", reply)
        self.assertIn("Jarvey", reply)

    def test_custom_default_reply(self):
        from mock_llm import compose_reply_mock

        reply = compose_reply_mock("Re: Test", "Hi", default_reply="Custom response here.")
        self.assertIn("Custom response", reply)
        self.assertIn("Jarvey", reply)


class TestComposeReplyWithMock(unittest.TestCase):
    """compose_reply.py --mock works without LLM."""

    def test_compose_reply_mock_flag(self):
        """compose_reply with --mock returns capability reply for capability query."""
        import subprocess

        result = subprocess.run(
            [sys.executable, "compose_reply.py", "Re: OutOfRouteBuddy", "What are you capable of?", "--mock"],
            capture_output=True,
            text=True,
            cwd=SCRIPT_DIR,
        )
        self.assertEqual(result.returncode, 0, f"stderr: {result.stderr}")
        self.assertIn("Jarvey", result.stdout)
        self.assertIn("HITL", result.stdout)


class TestRunOnceWithMockLLM(unittest.TestCase):
    """Full run_once flow with mocked compose (no real LLM)."""

    def test_llm_path_uses_mock_compose_and_sends(self):
        """When template_key=default, run_once composes via LLM (mocked) and sends."""
        import coordinator_listener as listener
        from mock_llm import compose_reply_mock

        env = {"COORDINATOR_LISTENER_OPENAI_API_KEY": "sk-test"}

        with patch("read_replies.read_replies") as mock_read:
            mock_read.return_value = (
                "Re: OutOfRouteBuddy",
                "What are you capable of?",
                "Mon, 1 Jan 2024",
                "<msg-capability>",
            )
            with patch("coordinator_listener.compose_reply_openai") as mock_compose:
                    mock_compose.side_effect = lambda s, b, *a, **k: compose_reply_mock(s, b)
                    with patch("send_email.send") as mock_send:
                        with patch("coordinator_listener.load_last_responded_id", return_value=None):
                            with patch("coordinator_listener.last_sent_within_cooldown", return_value=False):
                                with patch("coordinator_listener.save_responded_id"):
                                    with patch("coordinator_listener.write_sent_timestamp"):
                                        listener.run_once(env)

        mock_send.assert_called_once()
        sent_body = mock_send.call_args[0][1]
        self.assertIn("Jarvey", sent_body)
        self.assertIn("HITL", sent_body)


if __name__ == "__main__":
    unittest.main()
