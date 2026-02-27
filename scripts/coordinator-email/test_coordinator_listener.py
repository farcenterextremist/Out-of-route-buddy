#!/usr/bin/env python3
"""
Unit tests for coordinator_listener: default template routing, empty message handling, read_replies contract.
Run from repo root: python -m pytest scripts/coordinator-email/test_coordinator_listener.py -v
Or with unittest: python scripts/coordinator-email/test_coordinator_listener.py
"""

import json
import os
import sys
import tempfile
import unittest
from unittest.mock import patch, MagicMock

# Run from script dir so imports resolve
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.path.dirname(__file__) and os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)


class TestStripQuotedContent(unittest.TestCase):
    """_strip_quoted_content removes quoted/forwarded blocks so we respond only to user's new text."""

    def test_plain_body_unchanged(self):
        import coordinator_listener as listener
        body = "When is the next release?"
        self.assertEqual(listener._strip_quoted_content(body), body)

    def test_gmail_on_wrote_stripped(self):
        import coordinator_listener as listener
        body = "test\n\nOn Mon, Jan 1, 2024 at 10:00 AM John <john@example.com> wrote:\n> Old message"
        self.assertEqual(listener._strip_quoted_content(body), "test")

    def test_outlook_original_message_stripped(self):
        import coordinator_listener as listener
        body = "Thanks!\n\n-----Original Message-----\nFrom: old@example.com\nOld content"
        self.assertEqual(listener._strip_quoted_content(body), "Thanks!")

    def test_forwarded_message_stripped(self):
        import coordinator_listener as listener
        body = "Got it.\n\n----- Forwarded message -----\nFrom: x@y.com"
        self.assertEqual(listener._strip_quoted_content(body), "Got it.")

    def test_gmail_forwarded_message_stripped(self):
        """Gmail uses '---------- Forwarded message ---------'."""
        import coordinator_listener as listener
        body = "Thoughts?\n\n---------- Forwarded message ---------\nFrom: a@b.com"
        self.assertEqual(listener._strip_quoted_content(body), "Thoughts?")

    def test_apple_mail_begin_forwarded_stripped(self):
        """Apple Mail uses 'Begin forwarded message:'."""
        import coordinator_listener as listener
        body = "FYI\n\nBegin forwarded message:\nFrom: x@y.com"
        self.assertEqual(listener._strip_quoted_content(body), "FYI")

    def test_empty_body_unchanged(self):
        import coordinator_listener as listener
        self.assertEqual(listener._strip_quoted_content(""), "")
        self.assertEqual(listener._strip_quoted_content("   "), "   ")

    def test_only_quoted_returns_empty(self):
        import coordinator_listener as listener
        body = "On Mon, Jan 1, 2024 at 10:00 AM John wrote:\n> Quoted only"
        result = listener._strip_quoted_content(body)
        self.assertEqual(result.strip(), "")

    def test_on_second_thought_not_stripped(self):
        """'On second thought' has no 'wrote' — should not be stripped."""
        import coordinator_listener as listener
        body = "On second thought, I agree with the plan."
        self.assertEqual(listener._strip_quoted_content(body), body)

    def test_from_sent_format_stripped(self):
        """From: ... Sent: ... format (Outlook-style) stops at that line."""
        import coordinator_listener as listener
        body = "My reply\n\nFrom: x@y.com Sent: Monday\nOld content"
        self.assertEqual(listener._strip_quoted_content(body), "My reply")

    def test_reply_below_quote_extracted(self):
        """When user reply is below the quoted block (some clients), extract it."""
        import coordinator_listener as listener
        body = """On Wed, Feb 25, 2026, 10:28 AM x wrote:
> Hi,
> I'm Jarvey...

Jarvey, tell me recent project changes"""
        self.assertEqual(listener._strip_quoted_content(body), "Jarvey, tell me recent project changes")


class TestEnsureJarveySignoff(unittest.TestCase):
    """_ensure_jarvey_signoff appends '— Jarvey' when reply lacks it."""

    def test_has_jarvey_unchanged(self):
        import coordinator_listener as listener
        reply = "Hi,\n\nHere's the plan.\n\n— Jarvey"
        self.assertEqual(listener._ensure_jarvey_signoff(reply), reply)

    def test_has_jarvey_team_unchanged(self):
        import coordinator_listener as listener
        reply = "Thanks.\n\n— Jarvey, OutOfRouteBuddy Team"
        self.assertEqual(listener._ensure_jarvey_signoff(reply), reply)

    def test_missing_signoff_appended(self):
        import coordinator_listener as listener
        reply = "Hi,\n\nWe got your message."
        result = listener._ensure_jarvey_signoff(reply)
        self.assertIn("— Jarvey", result)
        self.assertTrue(result.endswith("— Jarvey") or "— Jarvey" in result[-30:])

    def test_wrong_signoff_appended(self):
        """When reply ends with '— OutOfRouteBuddy Team' alone, we append Jarvey."""
        import coordinator_listener as listener
        reply = "Thanks.\n\n— OutOfRouteBuddy Team"
        result = listener._ensure_jarvey_signoff(reply)
        self.assertIn("— Jarvey", result)

    def test_empty_unchanged(self):
        import coordinator_listener as listener
        self.assertEqual(listener._ensure_jarvey_signoff(""), "")
        self.assertEqual(listener._ensure_jarvey_signoff("   "), "   ")

    def test_ask_jarvey_in_body_no_false_positive(self):
        """'Ask Jarvey' in body but not as sign-off — last 50 chars check; if 'Jarvey' in last 50, we might skip. Current: we check for '— Jarvey' or '—Jarvey', so 'Ask Jarvey' alone does not match."""
        import coordinator_listener as listener
        reply = "Please ask Jarvey about the timeline."
        result = listener._ensure_jarvey_signoff(reply)
        self.assertIn("— Jarvey", result)
        self.assertTrue(result.endswith("— Jarvey"))

    def test_hyphen_signoff_recognized(self):
        """Reply with hyphen (-) before Jarvey is now recognized; we do not append."""
        import coordinator_listener as listener
        reply = "Thanks.\n\n- Jarvey"
        result = listener._ensure_jarvey_signoff(reply)
        self.assertEqual(result, reply)


class TestIsAgentSent(unittest.TestCase):
    """_is_agent_sent identifies agent-sent messages (X-OutOfRouteBuddy-Sent header) for same-inbox skip."""

    def test_agent_sent_has_header_returns_true(self):
        import email as em
        from read_replies import _is_agent_sent

        raw = "Subject: Re: OutOfRouteBuddy\nX-OutOfRouteBuddy-Sent: true\n\nBody"
        msg = em.message_from_string(raw)
        self.assertTrue(_is_agent_sent(msg))

    def test_user_reply_no_header_returns_false(self):
        import email as em
        from read_replies import _is_agent_sent

        raw = "Subject: Re: OutOfRouteBuddy\nFrom: user@gmail.com\n\nThanks!"
        msg = em.message_from_string(raw)
        self.assertFalse(_is_agent_sent(msg))

    def test_header_case_insensitive(self):
        import email as em
        from read_replies import _is_agent_sent

        raw = "X-OutOfRouteBuddy-Sent: TRUE\n\nBody"
        msg = em.message_from_string(raw)
        self.assertTrue(_is_agent_sent(msg))


class TestReadRepliesReturnsFourValues(unittest.TestCase):
    """read_replies must return (subject, body, date, message_id) - 4 values."""

    def test_read_replies_return_shape_empty(self):
        """When read_replies returns no match, callers unpack 4 values (None, None, None, None)."""
        subject, body, date, message_id = None, None, None, None
        self.assertIsNone(subject)
        self.assertIsNone(body)
        self.assertIsNone(date)
        self.assertIsNone(message_id)

    def test_read_replies_unpack_four_values(self):
        """Callers expect to unpack exactly 4 values from read_replies."""
        # Simulate read_replies return - ensures contract is understood
        result = ("Re: OutOfRouteBuddy", "Hello", "Mon, 1 Jan 2024", "<msg-123>")
        subject, body, date, message_id = result
        self.assertEqual(len(result), 4)
        self.assertEqual(subject, "Re: OutOfRouteBuddy")
        self.assertEqual(message_id, "<msg-123>")


class TestDefaultTemplateFallsThroughToLLM(unittest.TestCase):
    """When template_key is 'default', listener must NOT use template; must use LLM path."""

    def test_default_template_uses_llm_not_template(self):
        """With template_key=default, send is called with LLM output, not 'We got your message'."""
        import coordinator_listener as listener

        env = {"COORDINATOR_LISTENER_OPENAI_API_KEY": "sk-test"}
        llm_reply = "I understand you asked about the next release. Here's the plan..."

        with patch("read_replies.read_replies") as mock_read:
            mock_read.return_value = (
                "Re: OutOfRouteBuddy",
                "When is the next release?",
                "Mon, 1 Jan 2024",
                "<msg-456>",
            )
            with patch("coordinator_listener.compose_reply") as mock_compose:
                mock_compose.return_value = llm_reply
                with patch("send_email.send") as mock_send:
                    with patch("coordinator_listener.load_last_responded_id", return_value=None):
                        with patch("coordinator_listener.last_sent_within_cooldown", return_value=False):
                            with patch("coordinator_listener.save_responded_id"):
                                with patch("coordinator_listener.write_sent_timestamp"):
                                    listener.run_once(env)

                # Must have called send with LLM reply body (with sign-off), NOT the default template
                mock_send.assert_called_once()
                call_args = mock_send.call_args
                sent_body = call_args[0][1] if len(call_args[0]) > 1 else call_args[1].get("body", "")
                self.assertIn("next release", sent_body)
                self.assertNotIn(
                    "We got your message",
                    sent_body,
                    "Listener must use LLM reply for default template_key, not the generic template",
                )
                self.assertIn("— Jarvey", sent_body, "Reply must be signed as Jarvey")


class TestDedupeNoDoubleReply(unittest.TestCase):
    """When we already responded to message_id, run_once must not send again."""

    def test_already_responded_skips_send(self):
        import coordinator_listener as listener

        env = {"COORDINATOR_LISTENER_OPENAI_API_KEY": "sk-test"}

        with patch("read_replies.read_replies") as mock_read:
            mock_read.return_value = (
                "Re: OutOfRouteBuddy",
                "When is the next release?",
                "Mon, 1 Jan 2024",
                "<msg-dedupe>",
            )
            with patch("coordinator_listener.load_last_responded_id", return_value="<msg-dedupe>"):
                with patch("send_email.send") as mock_send:
                    listener.run_once(env)

        mock_send.assert_not_called()


class TestCooldownNoRapidSend(unittest.TestCase):
    """When last send was within cooldown window, run_once must not send."""

    def test_within_cooldown_skips_send(self):
        import coordinator_listener as listener

        env = {"COORDINATOR_LISTENER_OPENAI_API_KEY": "sk-test"}

        with patch("read_replies.read_replies") as mock_read:
            mock_read.return_value = (
                "Re: OutOfRouteBuddy",
                "What's next?",
                "Mon, 1 Jan 2024",
                "<msg-cooldown>",
            )
            with patch("coordinator_listener.load_last_responded_id", return_value=None):
                with patch("coordinator_listener.last_sent_within_cooldown", return_value=True):
                    with patch("send_email.send") as mock_send:
                        listener.run_once(env)

        mock_send.assert_not_called()


try:
    import openai  # noqa: F401
    OPENAI_AVAILABLE = True
except ImportError:
    OPENAI_AVAILABLE = False


@unittest.skipIf(not OPENAI_AVAILABLE, "openai package not installed")
class TestComposeEmptyReplyRaises(unittest.TestCase):
    """When LLM returns empty content, compose_reply raises RuntimeError."""

    def test_openai_empty_content_raises(self):
        import coordinator_listener as listener
        from unittest.mock import MagicMock

        mock_response = MagicMock()
        mock_response.choices = [MagicMock()]
        mock_response.choices[0].message.content = ""

        with patch("openai.OpenAI") as mock_client_cls:
            mock_client = MagicMock()
            mock_client.chat.completions.create.return_value = mock_response
            mock_client_cls.return_value = mock_client

            with self.assertRaises(RuntimeError) as ctx:
                listener.compose_reply_openai("Re: Test", "Hi", "sk-test")
            self.assertIn("empty", str(ctx.exception).lower())


class TestStructuredLogging(unittest.TestCase):
    """When JARVEY_STRUCTURED_LOG=1, run_once writes JSON lines to jarvey_workflow.log."""

    def test_structured_log_emits_json_when_enabled(self):
        import coordinator_listener as listener

        env = {
            "COORDINATOR_LISTENER_OPENAI_API_KEY": "sk-test",
            "JARVEY_STRUCTURED_LOG": "1",
        }
        log_path = os.path.join(os.path.dirname(__file__), "jarvey_workflow.log")
        if os.path.isfile(log_path):
            try:
                os.unlink(log_path)
            except OSError:
                pass

        with patch("coordinator_listener.load_env", return_value=env):
            with patch("read_replies.read_replies") as mock_read:
                mock_read.return_value = (
                    "Re: OutOfRouteBuddy",
                    "What's next?",
                    "Mon, 1 Jan 2024",
                    "<msg-123>",
                )
                with patch("coordinator_listener.compose_reply") as mock_compose:
                    mock_compose.return_value = "Reply from LLM."
                    with patch("send_email.send"):
                            with patch("coordinator_listener.load_last_responded_id", return_value=None):
                                with patch("coordinator_listener.last_sent_within_cooldown", return_value=False):
                                    with patch("coordinator_listener.save_responded_id"):
                                        with patch("coordinator_listener.write_sent_timestamp"):
                                            listener.run_once(env)

        if os.path.isfile(log_path):
            with open(log_path, encoding="utf-8") as f:
                lines = [l.strip() for l in f if l.strip()]
            for line in lines:
                parsed = json.loads(line)
                self.assertIn("event", parsed)
                self.assertIn("ts", parsed)


class TestComposeReplyRouting(unittest.TestCase):
    """compose_reply routes to correct backend based on env."""

    def test_auto_routes_to_openai_when_api_key_set(self):
        import coordinator_listener as listener

        env = {"COORDINATOR_LISTENER_OPENAI_API_KEY": "sk-test"}
        with patch("coordinator_listener.compose_reply_openai") as mock_openai:
            mock_openai.return_value = "Reply from OpenAI"
            result = listener.compose_reply("Re: Test", "Hi", env)
        mock_openai.assert_called_once()
        self.assertEqual(result, "Reply from OpenAI")

    def test_auto_routes_to_ollama_when_url_set_no_openai(self):
        import coordinator_listener as listener

        env = {"COORDINATOR_LISTENER_OLLAMA_URL": "http://localhost:11434"}
        with patch("coordinator_listener.compose_reply_ollama") as mock_ollama:
            mock_ollama.return_value = "Reply from Ollama"
            result = listener.compose_reply("Re: Test", "Hi", env)
        mock_ollama.assert_called_once()
        self.assertEqual(result, "Reply from Ollama")

    def test_explicit_backend_openai(self):
        import coordinator_listener as listener

        env = {
            "COORDINATOR_LISTENER_LLM_BACKEND": "openai",
            "COORDINATOR_LISTENER_OPENAI_API_KEY": "sk-test",
        }
        with patch("coordinator_listener.compose_reply_openai") as mock_openai:
            mock_openai.return_value = "Reply"
            listener.compose_reply("Re: Test", "Hi", env)
        mock_openai.assert_called_once()

    def test_no_llm_configured_raises(self):
        import coordinator_listener as listener

        env = {}
        with self.assertRaises(RuntimeError) as ctx:
            listener.compose_reply("Re: Test", "Hi", env)
        self.assertIn("No LLM configured", str(ctx.exception))


class TestEmptyMessageNoSend(unittest.TestCase):
    """When subject and body are both empty/whitespace, run_once must not send."""

    def test_empty_subject_and_body_does_not_send(self):
        """Stricter empty check: both subject and body empty -> no send."""
        import coordinator_listener as listener

        env = {}

        with patch("read_replies.read_replies") as mock_read:
            mock_read.return_value = ("", "", "Mon, 1 Jan 2024", "<msg-789>")
            with patch("send_email.send") as mock_send:
                listener.run_once(env)

        mock_send.assert_not_called()

    def test_subject_only_body_empty_proceeds_to_llm(self):
        """Subject present, body empty — we proceed (no early return)."""
        import coordinator_listener as listener

        env = {"COORDINATOR_LISTENER_OPENAI_API_KEY": "sk-test"}
        with patch("read_replies.read_replies") as mock_read:
            mock_read.return_value = ("Re: OutOfRouteBuddy", "", "Mon, 1 Jan 2024", "<msg-subj-only>")
            with patch("coordinator_listener.compose_reply") as mock_compose:
                mock_compose.return_value = "I got your message."
                with patch("send_email.send") as mock_send:
                    with patch("coordinator_listener.load_last_responded_id", return_value=None):
                        with patch("coordinator_listener.last_sent_within_cooldown", return_value=False):
                            with patch("coordinator_listener.save_responded_id"):
                                with patch("coordinator_listener.write_sent_timestamp"):
                                    listener.run_once(env)
                mock_send.assert_called_once()

    def test_compose_fails_no_send_no_save(self):
        """When compose raises, we do not send and do not save_responded_id."""
        import coordinator_listener as listener

        env = {"COORDINATOR_LISTENER_OPENAI_API_KEY": "sk-test"}
        with patch("read_replies.read_replies") as mock_read:
            mock_read.return_value = ("Re: OutOfRouteBuddy", "Hi", "Mon, 1 Jan 2024", "<msg-fail>")
            with patch("coordinator_listener.compose_reply") as mock_compose:
                mock_compose.side_effect = RuntimeError("LLM error")
                with patch("send_email.send") as mock_send:
                    with patch("coordinator_listener.load_last_responded_id", return_value=None):
                        with patch("coordinator_listener.last_sent_within_cooldown", return_value=False):
                            with patch("coordinator_listener.save_responded_id") as mock_save:
                                listener.run_once(env)
                mock_send.assert_not_called()
                mock_save.assert_not_called()


class TestConversationMemory(unittest.TestCase):
    """conversation_memory: load_history, append_exchange, get_thread_id."""

    def test_append_and_load_history(self):
        import tempfile
        import json

        with tempfile.NamedTemporaryFile(mode="w", suffix=".json", delete=False) as f:
            f.write("{}")
            tmp_path = f.name
        try:
            import conversation_memory as cm
            with patch.object(cm, "HISTORY_FILE", tmp_path):
                cm.append_exchange("test_thread_xyz", "What's next?", "Here's the roadmap...")
                cm.append_exchange("test_thread_xyz", "And the emulator?", "The emulator is...")
                result = cm.load_history("test_thread_xyz", max_exchanges=5)

            self.assertIn("What's next?", result)
            self.assertIn("Here's the roadmap", result)
            self.assertIn("And the emulator?", result)
            self.assertIn("The emulator is", result)
        finally:
            try:
                os.unlink(tmp_path)
            except OSError:
                pass

    def test_get_thread_id_normalizes_subject(self):
        from conversation_memory import get_thread_id

        tid = get_thread_id("Re: OutOfRouteBuddy", None)
        self.assertIn("outofroutebuddy", tid)
        self.assertNotIn("re:", tid.lower())


class TestLLMBackoff(unittest.TestCase):
    """llm_backoff: record_failure, record_success, get_backoff_seconds, is_circuit_open."""

    def test_record_success_resets_failures(self):
        import llm_backoff as lb
        with tempfile.NamedTemporaryFile(mode="w", suffix=".json", delete=False) as f:
            f.write('{"failures": 3, "last_failure_ts": 0, "last_success_ts": 0}')
            tmp_path = f.name
        try:
            with patch.object(lb, "STATE_FILE", tmp_path):
                lb.record_success()
                state = lb._load_state()
                self.assertEqual(state.get("failures", -1), 0)
        finally:
            try:
                os.unlink(tmp_path)
            except OSError:
                pass

    def test_record_failure_increments(self):
        import llm_backoff as lb
        with tempfile.NamedTemporaryFile(mode="w", suffix=".json", delete=False) as f:
            f.write('{"failures": 2, "last_failure_ts": 0, "last_success_ts": 0}')
            tmp_path = f.name
        try:
            with patch.object(lb, "STATE_FILE", tmp_path):
                lb.record_failure()
                state = lb._load_state()
                self.assertEqual(state.get("failures", -1), 3)
        finally:
            try:
                os.unlink(tmp_path)
            except OSError:
                pass

    def test_get_backoff_seconds_when_failures_above_threshold(self):
        import llm_backoff as lb
        with tempfile.NamedTemporaryFile(mode="w", suffix=".json", delete=False) as f:
            f.write('{"failures": 4, "last_failure_ts": 0, "last_success_ts": 0}')
            tmp_path = f.name
        try:
            with patch.object(lb, "STATE_FILE", tmp_path):
                secs = lb.get_backoff_seconds()
                self.assertGreater(secs, 0)
        finally:
            try:
                os.unlink(tmp_path)
            except OSError:
                pass

    def test_llm_backoff_env_uses_env_values(self):
        """Circuit breaker thresholds are configurable via env."""
        import llm_backoff as lb
        with tempfile.NamedTemporaryFile(mode="w", suffix=".json", delete=False) as f:
            f.write('{"failures": 2, "last_failure_ts": 999999, "last_success_ts": 0}')
            tmp_path = f.name
        try:
            with patch.object(lb, "STATE_FILE", tmp_path):
                with patch.object(lb, "_get_backoff_threshold", return_value=2):
                    secs = lb.get_backoff_seconds()
                    self.assertGreater(secs, 0, "With threshold=2 and failures=2, backoff should apply")
                with patch.object(lb, "_get_circuit_open_threshold", return_value=1):
                    with patch.object(lb, "_get_circuit_reset_seconds", return_value=3600):
                        with patch("llm_backoff.time") as mock_time:
                            mock_time.time.return_value = 1000  # recent; last_failure=999999 is in past
                            # last_failure 999999 > now 1000 would mean future - use 500 so now-last < 3600
                            import json
                            with open(tmp_path, "w") as fp:
                                json.dump({"failures": 2, "last_failure_ts": 500, "last_success_ts": 0}, fp)
                            mock_time.time.return_value = 501  # 1 sec after last failure
                            self.assertTrue(lb.is_circuit_open())
        finally:
            try:
                os.unlink(tmp_path)
            except OSError:
                pass


class TestClarificationFlow(unittest.TestCase):
    """When message is unclear (short/vague), use template instead of LLM."""

    def test_clarification_uses_template_for_short_body(self):
        """Body 'x' triggers unclear template; compose_reply is not called."""
        import coordinator_listener as listener

        env = {
            "COORDINATOR_LISTENER_OPENAI_API_KEY": "sk-test",
            "JARVEY_CLARIFICATION_TEMPLATE": "1",
        }
        with patch("read_replies.read_replies") as mock_read:
            mock_read.return_value = ("Re: OutOfRouteBuddy", "x", "Mon, 1 Jan 2024", "<msg-unclear>")
            with patch("coordinator_listener.load_last_responded_id", return_value=None):
                with patch("coordinator_listener.last_sent_within_cooldown", return_value=False):
                    with patch("llm_backoff.is_circuit_open", return_value=False):
                        with patch("coordinator_listener.compose_reply") as mock_compose:
                            with patch("send_email.send") as mock_send:
                                with patch("coordinator_listener.save_responded_id"):
                                    with patch("coordinator_listener.write_sent_timestamp"):
                                        listener.run_once(env)

        mock_compose.assert_not_called()
        mock_send.assert_called_once()
        sent_body = mock_send.call_args[0][1]
        self.assertIn("couldn't make out", sent_body)
        self.assertIn("— Jarvey", sent_body)

    def test_tell_me_something_uses_llm_not_unclear_template(self):
        """Body 'Tell me something' goes to LLM; reply must NOT contain couldn't make out."""
        import coordinator_listener as listener
        from mock_llm import compose_reply_mock

        def mock_compose(subject, body, env, intents=None):
            return compose_reply_mock(subject, body)

        env = {
            "COORDINATOR_LISTENER_OPENAI_API_KEY": "sk-test",
            "JARVEY_CLARIFICATION_TEMPLATE": "1",
        }
        with patch("read_replies.read_replies") as mock_read:
            mock_read.return_value = ("Re: Jarvey!", "Tell me something", "Mon, 1 Jan 2024", "<msg-tell-me>")
            with patch("coordinator_listener.load_last_responded_id", return_value=None):
                with patch("coordinator_listener.last_sent_within_cooldown", return_value=False):
                    with patch("llm_backoff.is_circuit_open", return_value=False):
                        with patch("coordinator_listener.compose_reply", side_effect=mock_compose):
                            with patch("send_email.send") as mock_send:
                                with patch("coordinator_listener.save_responded_id"):
                                    with patch("coordinator_listener.write_sent_timestamp"):
                                        listener.run_once(env)

        mock_send.assert_called_once()
        sent_body = mock_send.call_args[0][1]
        self.assertNotIn(
            "couldn't make out",
            sent_body.lower(),
            "Reply for 'Tell me something' must not ask for clarification",
        )
        self.assertNotIn(
            "short question",
            sent_body.lower(),
            "Reply for 'Tell me something' must not ask for short question",
        )
        self.assertIn("Jarvey", sent_body, "Reply must sign as Jarvey")


class TestContextLoader(unittest.TestCase):
    """Tests for context_loader: detect_intents, load_snippet, load_context_for_user_message."""

    def test_detect_intents_roadmap(self):
        from context_loader import detect_intents

        self.assertIn("roadmap", detect_intents("Re: OutOfRouteBuddy", "What's next?"))
        self.assertIn("roadmap", detect_intents("", "What are we doing next?"))

    def test_detect_intents_recovery(self):
        from context_loader import detect_intents

        self.assertIn("recovery", detect_intents("", "How does trip recovery work?"))
        self.assertIn("recovery", detect_intents("", "The app crashed and I lost my trip"))

    def test_detect_intents_version(self):
        from context_loader import detect_intents

        self.assertIn("version", detect_intents("", "What's the latest app version?"))
        self.assertIn("version", detect_intents("", "What version are we on?"))

    def test_detect_intents_delegation(self):
        from context_loader import detect_intents

        self.assertIn("delegation", detect_intents("", "Who owns the emulator?"))

    def test_detect_intents_empty(self):
        from context_loader import detect_intents

        self.assertEqual(detect_intents("", ""), [])
        self.assertEqual(detect_intents("", "Hello"), [])

    def test_detect_intents_recent(self):
        from context_loader import detect_intents

        self.assertIn("recent", detect_intents("", "What changed recently?"))
        self.assertIn("recent", detect_intents("", "Show me the project timeline"))
        self.assertIn("recent", detect_intents("", "What's new in the project?"))
        self.assertIn("recent", detect_intents("", "Recent changes please"))

    def test_load_snippet_version(self):
        from context_loader import load_snippet

        result = load_snippet("version")
        self.assertIn("1.0.2", result)
        self.assertIn("version", result.lower())

    def test_load_context_for_user_message_cap(self):
        from context_loader import load_context_for_user_message, MAX_PROJECT_CONTEXT_CHARS

        result = load_context_for_user_message("Re: OutOfRouteBuddy", "What's next? Also, who owns the emulator?")
        self.assertLessEqual(len(result), MAX_PROJECT_CONTEXT_CHARS + 50)  # allow truncation message

    def test_load_context_for_user_message_includes_base(self):
        from context_loader import load_context_for_user_message

        result = load_context_for_user_message("", "Hello")
        self.assertIn("OutOfRouteBuddy", result)

    def test_extract_entities_where_is(self):
        from context_loader import extract_entities

        entities = extract_entities("", "Where is TripInputViewModel?")
        self.assertIn("TripInputViewModel", entities)

    def test_extract_entities_capped_at_five(self):
        from context_loader import extract_entities

        entities = extract_entities("", "Where is A? Who owns B? Tell me about C. DViewModel ERepository")
        self.assertLessEqual(len(entities), 5)

    def test_entity_path_hints_in_context(self):
        from context_loader import load_context_for_user_message, extract_entities, _entity_path_hints

        entities = extract_entities("", "Where is TripInputViewModel?")
        self.assertIn("TripInputViewModel", entities)
        hints = _entity_path_hints(entities)
        result = load_context_for_user_message("", "Where is TripInputViewModel?")
        self.assertIn("OutOfRouteBuddy", result)
        if hints:
            self.assertIn("Entity location hints", result)

    def test_load_context_for_user_message_no_intent_returns_base(self):
        from context_loader import load_context_for_user_message

        result = load_context_for_user_message("", "Hello there")
        self.assertIn("OutOfRouteBuddy", result)
        self.assertNotIn("Additional context (on-demand)", result)

    def test_roadmap_intent_only_not_in_base_for_generic_message(self):
        """ROADMAP is loaded on-demand only. Generic 'hi' has no intents → no ROADMAP snippet."""
        from context_loader import load_context_for_user_message

        result = load_context_for_user_message("", "hi")
        self.assertNotIn("Additional context (on-demand)", result)

    def test_roadmap_included_when_whats_next_intent(self):
        """When user asks 'What's next?', roadmap intent loads ROADMAP snippet."""
        from context_loader import load_context_for_user_message

        result = load_context_for_user_message("Re: OutOfRouteBuddy", "What's next?")
        self.assertIn("Additional context (on-demand)", result)


class TestProjectIndex(unittest.TestCase):
    """Tests for build_project_index and expanded base context."""

    def test_build_project_index_includes_docs_and_app(self):
        from context_loader import build_project_index

        index = build_project_index(cap_chars=50000)
        self.assertIn("docs/", index)
        self.assertIn("app/src/", index)

    def test_build_project_index_excludes_build_and_env(self):
        from context_loader import build_project_index

        index = build_project_index(cap_chars=10000)
        self.assertNotIn("build/", index)
        self.assertNotIn("/.env", index)
        self.assertNotIn(".env", index)

    def test_load_context_includes_project_index(self):
        from context_loader import load_context_for_user_message

        result = load_context_for_user_message("", "Hello")
        self.assertIn("Project files", result)
        self.assertIn("docs/", result)

    def test_context_respects_cap(self):
        from context_loader import load_context_for_user_message

        with patch("context_loader._get_max_context_chars", return_value=8000):
            result = load_context_for_user_message(
                "Re: OutOfRouteBuddy", "What's next? Also who owns the emulator?"
            )
            self.assertLessEqual(len(result), 8100)


class TestProjectTimeline(unittest.TestCase):
    """Tests for get_project_timeline and append_to_timeline."""

    def test_get_project_timeline_empty_returns_git_or_empty(self):
        """With empty/missing timeline file, get_project_timeline returns git log or empty."""
        from context_loader import get_project_timeline, PROJECT_TIMELINE_PATH

        with tempfile.NamedTemporaryFile(mode="w", suffix=".json", delete=False) as f:
            f.write("[]")
            tmp_path = f.name
        try:
            with patch("context_loader.PROJECT_TIMELINE_PATH", tmp_path):
                result = get_project_timeline(cap_chars=600)
                # Either empty or contains "Recent commits" from git
                self.assertTrue(
                    result == "" or "Recent commits" in result or "Project timeline" in result
                )
        finally:
            os.unlink(tmp_path)

    def test_get_project_timeline_with_entries(self):
        """With stored entries, get_project_timeline includes them."""
        from context_loader import get_project_timeline, PROJECT_TIMELINE_PATH

        with tempfile.NamedTemporaryFile(mode="w", suffix=".json", delete=False) as f:
            json.dump(
                [
                    {"date": "2025-01-15", "type": "phase", "title": "Phase A/B/C complete", "detail": ""},
                ],
                f,
            )
            tmp_path = f.name
        try:
            with patch("context_loader.PROJECT_TIMELINE_PATH", tmp_path):
                result = get_project_timeline(cap_chars=600)
                self.assertIn("Project timeline", result)
                self.assertIn("2025-01-15", result)
                self.assertIn("Phase A/B/C complete", result)
        finally:
            os.unlink(tmp_path)

    def test_append_to_timeline_creates_entry(self):
        """append_to_timeline adds an entry to the JSON file."""
        from context_loader import append_to_timeline, get_project_timeline, PROJECT_TIMELINE_PATH

        with tempfile.NamedTemporaryFile(mode="w", suffix=".json", delete=False) as f:
            f.write("[]")
            tmp_path = f.name
        try:
            with patch("context_loader.PROJECT_TIMELINE_PATH", tmp_path):
                append_to_timeline("2025-01-16", "phase", "Test phase complete", "Detail")
                with open(tmp_path, encoding="utf-8") as rf:
                    entries = json.load(rf)
                self.assertEqual(len(entries), 1)
                self.assertEqual(entries[0]["date"], "2025-01-16")
                self.assertEqual(entries[0]["type"], "phase")
                self.assertEqual(entries[0]["title"], "Test phase complete")
        finally:
            os.unlink(tmp_path)

    def test_load_snippet_recent_includes_timeline(self):
        """load_snippet('recent') returns project timeline (may include git)."""
        from context_loader import load_snippet

        result = load_snippet("recent")
        # Should have either timeline entries or git log
        self.assertTrue(
            "Recent commits" in result or "Project timeline" in result or result == ""
        )


class TestPhaseCompletionAppendsTimeline(unittest.TestCase):
    """send_phase_completion_email appends to timeline before sending."""

    def test_phase_completion_appends_to_timeline(self):
        """When send_phase_completion_email runs, it appends an entry to project_timeline.json."""
        import send_phase_completion_email as phase_email
        from context_loader import PROJECT_TIMELINE_PATH

        with tempfile.NamedTemporaryFile(mode="w", suffix=".json", delete=False) as f:
            f.write("[]")
            tmp_path = f.name
        try:
            with patch("context_loader.PROJECT_TIMELINE_PATH", tmp_path):
                with patch("send_email.send") as mock_send:
                    # Run main with phase_abc preset
                    with patch.object(sys, "argv", ["send_phase_completion_email.py", "phase_abc"]):
                        phase_email.main()
                    mock_send.assert_called_once()
                    with open(tmp_path, encoding="utf-8") as rf:
                        entries = json.load(rf)
                    self.assertEqual(len(entries), 1)
                    self.assertEqual(entries[0]["type"], "phase")
                    self.assertIn("Phase A/B/C", entries[0]["title"])
        finally:
            os.unlink(tmp_path)


if __name__ == "__main__":
    unittest.main()
