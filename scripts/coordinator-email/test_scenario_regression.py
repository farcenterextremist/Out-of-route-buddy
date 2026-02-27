#!/usr/bin/env python3
"""
Automated scenario regression test: body "test" → reply is check-in (here/ready), no invented content.
Requires Ollama or OpenAI API. Run from repo root:
  python scripts/coordinator-email/test_scenario_regression.py
  python -m pytest scripts/coordinator-email/test_scenario_regression.py -v -s
"""

import os
import sys
import unittest

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.path.dirname(__file__) and os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

import coordinator_listener as cl


def _can_run_llm():
    """True if API key or Ollama URL is configured."""
    env = cl.load_env()
    api_key = env.get("COORDINATOR_LISTENER_OPENAI_API_KEY") or env.get("OPENAI_API_KEY")
    ollama = env.get("COORDINATOR_LISTENER_OLLAMA_URL") or env.get("OLLAMA_URL")
    return bool(api_key or ollama)


# Anti-hallucination phrases that must NOT appear when user did not ask for roadmap/priorities
_HALLUCINATION_PHRASES = ("auto drive", "reports screen", "history improvements")


@unittest.skipIf(not _can_run_llm(), "No LLM configured (OLLAMA_URL or OPENAI_API_KEY)")
class TestScenarioRegressionCheckIn(unittest.TestCase):
    """Body 'test' → reply says here/ready, does not invent meeting/report."""

    def _compose_reply(self, subject: str, body: str) -> str:
        """Compose reply via OpenAI or Ollama."""
        env = cl.load_env()
        api_key = env.get("COORDINATOR_LISTENER_OPENAI_API_KEY") or env.get("OPENAI_API_KEY")
        if api_key:
            reply = cl.compose_reply_openai(subject, body, api_key)
        else:
            base_url = env.get("COORDINATOR_LISTENER_OLLAMA_URL") or env.get("OLLAMA_URL", "http://localhost:11434")
            model = env.get("COORDINATOR_LISTENER_OLLAMA_MODEL", "llama3.2")
            reply = cl.compose_reply_ollama(base_url, model, subject, body)
        return cl._ensure_jarvey_signoff(reply)

    def test_test_body_check_in_response(self):
        body = "test"
        subject = "Re: OutOfRouteBuddy"
        reply = self._compose_reply(subject, body)
        reply_lower = reply.lower()
        # Check-in or clarification: here/ready, or ask for more detail (couldn't make out, short question)
        is_checkin = "here" in reply_lower or "ready" in reply_lower
        is_clarify = "couldn't make out" in reply_lower or "short question" in reply_lower or "clarif" in reply_lower
        self.assertTrue(
            is_checkin or is_clarify,
            f"Reply for 'test' should be check-in (here/ready) or ask for clarification. Got: {reply[:200]}..."
        )
        # Should not invent specifics (meeting, report)
        self.assertNotIn(
            "meeting",
            reply_lower,
            "Reply for 'test' should not invent a meeting"
        )
        self.assertNotIn(
            "report",
            reply_lower,
            "Reply for 'test' should not invent a report"
        )
        # Anti-hallucination: do not add roadmap/priorities when user did not ask
        for phrase in ("auto drive", "reports screen", "history improvements"):
            self.assertNotIn(
                phrase,
                reply_lower,
                f"Reply for 'test' should not hallucinate roadmap; must not contain '{phrase}'"
            )
        self.assertIn("Jarvey", reply, "Reply must sign as Jarvey")

    def test_hi_body_no_hallucination(self):
        """Body 'hi' → reply should not invent roadmap/priorities (anti-hallucination)."""
        reply = self._compose_reply("Re: OutOfRouteBuddy", "hi")
        reply_lower = reply.lower()
        for phrase in _HALLUCINATION_PHRASES:
            self.assertNotIn(
                phrase,
                reply_lower,
                f"Reply for 'hi' must not hallucinate; must not contain '{phrase}'",
            )
        self.assertIn("Jarvey", reply, "Reply must sign as Jarvey")

    def test_ok_body_no_hallucination(self):
        """Body 'ok' → reply should not invent roadmap/priorities (anti-hallucination)."""
        reply = self._compose_reply("Re: OutOfRouteBuddy", "ok")
        reply_lower = reply.lower()
        for phrase in _HALLUCINATION_PHRASES:
            self.assertNotIn(
                phrase,
                reply_lower,
                f"Reply for 'ok' must not hallucinate; must not contain '{phrase}'",
            )
        self.assertIn("Jarvey", reply, "Reply must sign as Jarvey")

    def test_tell_me_something_no_clarification(self):
        """Body 'Tell me something' → reply must NOT say couldn't make out or ask for short question."""
        reply = self._compose_reply("Re: OutOfRouteBuddy", "Tell me something")
        reply_lower = reply.lower()
        self.assertNotIn(
            "couldn't make out",
            reply_lower,
            "Reply for 'Tell me something' must not ask for clarification; got miscommunication response",
        )
        self.assertNotIn(
            "short question",
            reply_lower,
            "Reply for 'Tell me something' must not ask for short question",
        )
        self.assertIn("Jarvey", reply, "Reply must sign as Jarvey")


if __name__ == "__main__":
    unittest.main()
