#!/usr/bin/env python3
"""
Experimental: compose email draft using postbot/t5-small-kw2email-v2.
Uses text2text-generation to expand keywords into a short draft.
Standalone script for experimentation; not integrated into main coordinator flow.

Usage:
  python compose_reply_email_model.py "Subject here" "Body or keywords here"
  Or as module: from compose_reply_email_model import compose_reply_email_model
"""

import os
import re
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

# Simple stopwords for keyword extraction (English)
_STOPWORDS = frozenset({
    "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for",
    "of", "with", "by", "from", "as", "is", "was", "are", "were", "been",
    "be", "have", "has", "had", "do", "does", "did", "will", "would",
    "could", "should", "may", "might", "must", "shall", "can", "need",
    "i", "you", "he", "she", "it", "we", "they", "me", "him", "her",
    "us", "them", "my", "your", "his", "its", "our", "their",
    "this", "that", "these", "those", "what", "which", "who", "when",
    "where", "why", "how", "all", "each", "every", "both", "few",
    "more", "most", "other", "some", "such", "no", "nor", "not",
    "only", "own", "same", "so", "than", "too", "very", "just",
})


def _extract_keywords(text: str, max_words: int = 15) -> str:
    """Extract significant words from text for keyword-to-email model input."""
    if not text or not text.strip():
        return ""
    # Normalize: lowercase, split on non-alphanumeric
    words = re.findall(r"[a-zA-Z0-9]+", text.lower())
    seen = set()
    result = []
    for w in words:
        if len(w) < 2:
            continue
        if w in _STOPWORDS:
            continue
        if w in seen:
            continue
        seen.add(w)
        result.append(w)
        if len(result) >= max_words:
            break
    return " ".join(result) if result else text[:200].strip()


def compose_reply_email_model(
    subject: str,
    body: str,
    model: str = "postbot/t5-small-kw2email-v2",
    max_length: int = 150,
) -> str:
    """
    Generate a short email draft from subject+body using the T5 kw2email model.
    Extracts keywords from input and uses text2text-generation to produce a draft.
    """
    try:
        from transformers import pipeline
    except ImportError:
        raise RuntimeError(
            "Email model requires: pip install transformers. "
            "See requirements.txt."
        )
    combined = f"{subject or ''} {body or ''}".strip()
    if not combined:
        return ""
    keywords = _extract_keywords(combined)
    if not keywords:
        keywords = combined[:100]
    # T5 kw2email typically expects keyword-style input
    pipe = pipeline("text2text-generation", model=model)
    out = pipe(
        keywords,
        max_length=max_length,
        min_length=20,
        num_return_sequences=1,
        do_sample=True,
        temperature=0.8,
    )
    draft = (out[0].get("generated_text") or "").strip()
    return draft


def main():
    if len(sys.argv) < 2:
        print("Usage: python compose_reply_email_model.py <subject> [body]", file=sys.stderr)
        print("  Extracts keywords and generates a short draft via postbot/t5-small-kw2email-v2.", file=sys.stderr)
        sys.exit(1)
    subject = sys.argv[1]
    body = sys.argv[2] if len(sys.argv) > 2 else ""
    try:
        draft = compose_reply_email_model(subject, body)
        print(draft)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
