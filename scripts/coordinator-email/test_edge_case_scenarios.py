#!/usr/bin/env python3
"""
Edge case scenario tests: short check-in phrases that must NOT get "couldn't make out" response.
Uses mock LLM (no Ollama/OpenAI); fast CI.

Run: python scripts/coordinator-email/test_edge_case_scenarios.py
"""

import os
import sys
import unittest
from unittest.mock import patch

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.path.dirname(__file__) and os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

import coordinator_listener as listener
from mock_llm import compose_reply_mock


def _mock_compose(subject, body, env, intents=None):
    return compose_reply_mock(subject, body)


# Edge case bodies that must get project update, not clarification
_EDGE_CASE_BODIES = [
    "Tell me something",
    "Tell me anything",
    "Share something",
    "Give me an update",
    "Anything new?",
    "Catch me up",
    "Fill me in",
    "What's happening?",
    "What's going on?",
    "Quick summary",
    "What's the latest?",
    "Update me",
    "News?",
    "Status?",
]


class TestEdgeCaseScenarios(unittest.TestCase):
    """Edge case bodies must NOT get 'couldn't make out' or 'short question' response."""

    def _run_scenario(self, body: str) -> str:
        """Run scenario with mock LLM; return sent body."""
        env = {
            "COORDINATOR_LISTENER_OPENAI_API_KEY": "sk-test",
            "JARVEY_CLARIFICATION_TEMPLATE": "1",
        }
        with patch("read_replies.read_replies") as mock_read:
            mock_read.return_value = ("Re: OutOfRouteBuddy", body, "Mon, 1 Jan 2024", "<msg-edge>")
            with patch("coordinator_listener.load_last_responded_id", return_value=None):
                with patch("coordinator_listener.last_sent_within_cooldown", return_value=False):
                    with patch("llm_backoff.is_circuit_open", return_value=False):
                        with patch("coordinator_listener.compose_reply", side_effect=_mock_compose):
                            with patch("send_email.send") as mock_send:
                                with patch("coordinator_listener.save_responded_id"):
                                    with patch("coordinator_listener.write_sent_timestamp"):
                                        listener.run_once(env)
        return mock_send.call_args[0][1]

    def test_edge_case_no_clarification(self):
        """All edge case bodies must NOT contain 'couldn't make out' or 'short question'."""
        for body in _EDGE_CASE_BODIES:
            with self.subTest(body=body):
                sent = self._run_scenario(body)
                sent_lower = sent.lower()
                self.assertNotIn(
                    "couldn't make out",
                    sent_lower,
                    f"Reply for {body!r} must not ask for clarification",
                )
                self.assertNotIn(
                    "short question",
                    sent_lower,
                    f"Reply for {body!r} must not ask for short question",
                )
                self.assertIn("Jarvey", sent, f"Reply for {body!r} must sign as Jarvey")

    def test_edge_case_has_project_update(self):
        """Edge case replies should mention project context (roadmap, priorities, etc.)."""
        for body in ["Tell me something", "Anything new?", "Update me"]:
            with self.subTest(body=body):
                sent = self._run_scenario(body)
                sent_lower = sent.lower()
                # Should mention roadmap, priorities, recent, or next steps
                has_context = any(
                    k in sent_lower for k in ["roadmap", "priorities", "recent", "next", "auto", "reports", "history"]
                )
                self.assertTrue(
                    has_context,
                    f"Reply for {body!r} should mention project context; got: {sent[:150]}...",
                )


if __name__ == "__main__":
    unittest.main()
