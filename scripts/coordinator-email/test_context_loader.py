#!/usr/bin/env python3
"""
Unit tests for context_loader: RAG integration, truncation, env top_k.
Run from repo root: python -m pytest scripts/coordinator-email/test_context_loader.py -v
Or with unittest: python scripts/coordinator-email/test_context_loader.py
"""

import os
import sys
import unittest
from unittest.mock import patch, MagicMock

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.path.dirname(__file__) and os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)


class TestTruncateAtBoundary(unittest.TestCase):
    """_truncate_at_boundary truncates at natural boundaries, never mid-word."""

    def test_short_text_unchanged(self):
        from context_loader import _truncate_at_boundary

        text = "Short."
        self.assertEqual(_truncate_at_boundary(text, 100), text)

    def test_empty_unchanged(self):
        from context_loader import _truncate_at_boundary

        self.assertEqual(_truncate_at_boundary("", 50), "")
        self.assertEqual(_truncate_at_boundary(None, 50), None)

    def test_truncates_at_sentence_end(self):
        from context_loader import _truncate_at_boundary, TRUNCATE_SUFFIX

        text = "First sentence. Second sentence. Third sentence."
        # reserve=len(TRUNCATE_SUFFIX)+5; cut_at must include ". " at 15, so max_chars >= 15+1+reserve
        result = _truncate_at_boundary(text, 45)
        self.assertTrue(result.endswith(TRUNCATE_SUFFIX))
        self.assertIn("First sentence.", result)
        self.assertNotIn("Third", result)

    def test_truncates_at_newline(self):
        from context_loader import _truncate_at_boundary, TRUNCATE_SUFFIX

        text = "Line one\nLine two\nLine three"
        result = _truncate_at_boundary(text, 25)
        self.assertTrue(result.endswith(TRUNCATE_SUFFIX))
        self.assertIn("Line one", result)

    def test_truncates_at_space(self):
        from context_loader import _truncate_at_boundary, TRUNCATE_SUFFIX

        text = "word1 word2 word3 word4 word5 word6 word7 word8"
        # cut_at=45-22=23 includes "word3"; use max_chars=34 so cut_at=12, truncates before word3
        result = _truncate_at_boundary(text, 34)
        self.assertTrue(result.endswith(TRUNCATE_SUFFIX))
        self.assertNotIn("word3", result)


class TestRAGIntegration(unittest.TestCase):
    """RAG search integration: uses env top_k, respects JARVEY_RAG_ENABLED."""

    def test_rag_uses_env_top_k(self):
        from context_loader import load_context_for_user_message

        with patch.dict(os.environ, {"JARVEY_RAG_ENABLED": "1"}, clear=False):
            with patch("jarvey_rag.search") as mock_search:
                with patch("jarvey_rag._get_top_k", return_value=5):
                    mock_search.return_value = []
                    load_context_for_user_message(
                        "Re: OutOfRouteBuddy", "search the docs for GPS"
                    )
                    mock_search.assert_called_once()
                    call_kwargs = mock_search.call_args[1]
                    self.assertEqual(call_kwargs.get("top_k"), 5)

    def test_rag_disabled_no_search_call(self):
        from context_loader import load_context_for_user_message

        with patch("context_loader._load_env", return_value={}):
            with patch.dict(os.environ, {"JARVEY_RAG_ENABLED": ""}, clear=False):
                with patch("jarvey_rag.search") as mock_search:
                    load_context_for_user_message(
                        "Re: OutOfRouteBuddy", "search the docs for GPS"
                    )
                    mock_search.assert_not_called()

    def test_load_context_rag_includes_chunks(self):
        from context_loader import load_context_for_user_message

        with patch.dict(os.environ, {"JARVEY_RAG_ENABLED": "1"}, clear=False):
            with patch("jarvey_rag.search") as mock_search:
                mock_search.return_value = ["chunk1 about GPS", "chunk2 wiring"]
                result = load_context_for_user_message(
                    "Re: OutOfRouteBuddy", "search the docs for GPS"
                )
                self.assertIn("chunk1 about GPS", result)
                self.assertIn("chunk2 wiring", result)


if __name__ == "__main__":
    unittest.main()
