#!/usr/bin/env python3
"""
Mock LLM for Jarvey tests and simulations. No Ollama or OpenAI required.

Use for:
  - Fast CI without LLM
  - compose_reply.py --mock
  - Scenario regression tests when JARVEY_USE_MOCK_LLM=1

Returns deterministic replies based on simple keyword matching.
"""

import re
from typing import Optional

# Canned responses for common queries (deterministic, no LLM)
_CAPABILITY_REPLY = """I'm Jarvey, the Master Branch Coordinator and Human-in-the-Loop Manager for OutOfRouteBuddy.

I can:
- Answer questions about the project roadmap, timeline, and priorities
- Point you to where things are defined (emulator, recovery, tests, etc.)
- Summarize recent changes and project status
- Help with delegation (who owns what)
- Send weekly board digests when requested
- Clarify next steps and check-ins

I'm a HITL manager—I coordinate and respond, but I don't execute code or make changes without human approval.

— Jarvey"""

_ROADMAP_PLACEHOLDER = """You asked about what's next on the roadmap. The top priorities are: Auto drive, Reports screen, and History improvements. I'll email details.

— Jarvey"""

# Benchmark simulation: replies that pass heuristic checks. Order matters: more specific first.
_MOCK_BENCHMARK_REPLIES = {
    "who owns the emulator": "You asked about next steps and the emulator. Next: Auto drive, Reports, History. Emulator: see DATA_SETS_AND_DELEGATION_PLAN. — Jarvey",
    "prioritize the reports": "You asked about prioritizing the reports screen. I'll follow up with the team on timeline and priority. — Jarvey",
    "export trips to csv": "You asked for a function to export trips. I'll assign this to the Back-end Engineer and follow up. — Jarvey",
    "something is broken": "You mentioned something is broken. Which screen or flow? I'll follow up once I have details. — Jarvey",
    "trip recovery": "Trip recovery: TripCrashRecoveryManager saves state every 30s. On restart after crash, recoveredTripState loads. — Jarvey",
    "app version": "You asked about the app version. Check app/build.gradle.kts for versionName (e.g. 1.0.2). — Jarvey",
    "tripinputviewmodel": "TripInputViewModel is in app/src; see the project index for the exact path. — Jarvey",
    "recent project changes": "You asked for recent project changes. No curated timeline entries yet; I'll summarize once the team adds phase completions. — Jarvey",
    "what's next": _ROADMAP_PLACEHOLDER,
    "tell me something": "You asked me to tell you something. Here's a brief update: Auto drive, Reports screen, and History improvements are top priorities. I can share more on roadmap, recent changes, or next steps—just ask. — Jarvey",
    "tell me anything": "You asked me to tell you something. Here's a brief update: Auto drive, Reports screen, and History improvements are top priorities. I can share more on roadmap, recent changes, or next steps—just ask. — Jarvey",
    "share something": "You asked me to share something. Here's a brief update: Auto drive, Reports screen, and History improvements are top priorities. I can share more on roadmap, recent changes, or next steps—just ask. — Jarvey",
    "give me an update": "You asked for an update. Brief status: Auto drive, Reports screen, and History improvements are top priorities. I can share roadmap or recent changes—just ask. — Jarvey",
    "anything new": "You asked what's new. No curated timeline entries yet; I'll summarize once the team adds phase completions. Top priorities: Auto drive, Reports, History. — Jarvey",
    "catch me up": "You asked to catch you up. Brief status: Auto drive, Reports screen, and History improvements are top priorities. I can share roadmap, recent changes, or next steps—just ask. — Jarvey",
    "fill me in": "You asked to fill you in. Here's a brief update: Auto drive, Reports screen, and History improvements are top priorities. I can share more on roadmap or recent changes—just ask. — Jarvey",
    "what's happening": "You asked what's happening. Top priorities: Auto drive, Reports screen, History improvements. No curated timeline yet; I'll summarize when the team adds phase completions. — Jarvey",
    "what's going on": "You asked what's going on. Brief status: Auto drive, Reports, History are top priorities. I can share roadmap or recent changes—just ask. — Jarvey",
    "quick summary": "You asked for a quick summary. Top priorities: Auto drive, Reports screen, History improvements. No curated timeline yet. — Jarvey",
    "what's the latest": "You asked for the latest. No curated timeline entries yet; I'll summarize once the team adds phase completions. Top priorities: Auto drive, Reports, History. — Jarvey",
    "update me": "You asked for an update. Brief status: Auto drive, Reports screen, and History improvements are top priorities. I can share roadmap or recent changes—just ask. — Jarvey",
    "news?": "You asked for news. No curated timeline yet; top priorities are Auto drive, Reports, History. I can share more—just ask. — Jarvey",
    "status?": "You asked for status. Top priorities: Auto drive, Reports screen, History improvements. I can share roadmap or recent changes—just ask. — Jarvey",
}

_DEFAULT_REPLY = """Thanks for your message. I've noted it and will follow up as needed.

— Jarvey"""


def _matches(body: str, *patterns: str) -> bool:
    """True if body (lowercased) matches any pattern (substring or regex)."""
    lower = body.lower().strip()
    for p in patterns:
        if p in lower:
            return True
        try:
            if re.search(p, lower, re.IGNORECASE):
                return True
        except re.error:
            pass
    return False


def compose_reply_mock(
    subject: str,
    body: str,
    *,
    default_reply: Optional[str] = None,
) -> str:
    """
    Return a deterministic mock reply. No LLM call.

    Keyword matching for common intents:
      - capability / what can you do / what are you capable of
      - roadmap / what's next / priorities
      - default fallback
    """
    combined = f"{subject}\n{body}".lower()

    if _matches(combined, "capable", "what can you", "what are you capable", "capabilities"):
        return _CAPABILITY_REPLY

    # Benchmark simulation: match scenario bodies (more specific first)
    for key, reply in _MOCK_BENCHMARK_REPLIES.items():
        if key in combined:
            return reply

    if _matches(combined, "what's next", "whats next", "roadmap", "priorities", "next steps"):
        return _ROADMAP_PLACEHOLDER

    if default_reply:
        return default_reply if "— Jarvey" in default_reply else default_reply.rstrip() + "\n\n— Jarvey"

    return _DEFAULT_REPLY


def get_mock_compose_fn():
    """Return a callable that mocks compose_reply_ollama/openai. Accepts (base_url, model, subject, body) or (subject, body, api_key)."""

    def _fn(*args, **kwargs):
        # Normalize: compose_reply_ollama(base_url, model, subject, body) or compose_reply_openai(subject, body, api_key)
        if len(args) >= 4:
            subject, body = args[2], args[3]
        elif len(args) >= 2:
            subject, body = args[0], args[1]
        else:
            subject = kwargs.get("subject", "")
            body = kwargs.get("body", "")
        return compose_reply_mock(subject, body)

    return _fn
