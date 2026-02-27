#!/usr/bin/env python3
"""
Tests the link between quality data and the model.

Verifies that when the user asks a question, the system prompt sent to the LLM
actually contains the relevant quality data (ROADMAP, KNOWN_TRUTHS, etc.).
This tests the chain: quality data → context_loader → load_coordinator_system_prompt → (would go to LLM).

No LLM required. We assert the prompt construction, not the model output.
"""

import os
import sys
import unittest

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.path.dirname(__file__) and os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

REPO_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))


class TestRoadmapDataInPrompt(unittest.TestCase):
    """When user asks 'What's next?', the prompt must contain roadmap quality data."""

    def test_whats_next_prompt_contains_roadmap_data(self):
        from coordinator_listener import load_coordinator_system_prompt

        prompt = load_coordinator_system_prompt("Re: OutOfRouteBuddy", "What's next?")
        # ROADMAP.md contains these next-three priorities
        roadmap_markers = ["Auto drive", "Reports", "History"]
        found = sum(1 for m in roadmap_markers if m in prompt)
        self.assertGreaterEqual(
            found, 2,
            f"Prompt should contain at least 2 of {roadmap_markers} from ROADMAP.md; found {found}. "
            "Quality data not reaching model."
        )

    def test_priorities_prompt_contains_roadmap(self):
        from coordinator_listener import load_coordinator_system_prompt

        prompt = load_coordinator_system_prompt("Re: X", "What are the priorities?")
        self.assertTrue(
            "Auto" in prompt or "Reports" in prompt or "roadmap" in prompt.lower(),
            "Prompt should contain roadmap content for priorities question."
        )


class TestRecoveryDataInPrompt(unittest.TestCase):
    """When user asks about recovery, the prompt must contain KNOWN_TRUTHS / recovery data."""

    def test_recovery_prompt_contains_known_truths(self):
        from coordinator_listener import load_coordinator_system_prompt

        prompt = load_coordinator_system_prompt("Re: X", "How does trip recovery work?")
        # KNOWN_TRUTHS and RECOVERY_WIRING contain these
        recovery_markers = [
            "TripCrashRecoveryManager",
            "recovery",
            "crash",
            "persistence",
            "TripPersistenceManager",
        ]
        found = sum(1 for m in recovery_markers if m in prompt)
        self.assertGreaterEqual(
            found, 2,
            f"Prompt should contain recovery data; expected 2+ of {recovery_markers}, found {found}."
        )

    def test_lost_trip_prompt_contains_recovery_context(self):
        from coordinator_listener import load_coordinator_system_prompt

        prompt = load_coordinator_system_prompt("Re: X", "I lost my trip after a crash")
        self.assertTrue(
            "recovery" in prompt.lower() or "crash" in prompt.lower() or "TripCrash" in prompt,
            "Prompt should contain recovery context for lost-trip question."
        )


class TestEntityPathDataInPrompt(unittest.TestCase):
    """When user asks 'Where is X?', the prompt must contain entity path from project index."""

    def test_where_is_tripinputviewmodel_contains_path(self):
        from coordinator_listener import load_coordinator_system_prompt

        prompt = load_coordinator_system_prompt("Re: X", "Where is TripInputViewModel?")
        # Project index or entity hints should include path
        self.assertIn("TripInputViewModel", prompt)
        self.assertTrue(
            ".kt" in prompt or "app/" in prompt or "presentation" in prompt or "viewmodel" in prompt.lower(),
            "Prompt should contain file path or package hint for TripInputViewModel."
        )

    def test_where_is_repository_contains_project_context(self):
        from coordinator_listener import load_coordinator_system_prompt

        prompt = load_coordinator_system_prompt("Re: X", "Where is TripRepository defined?")
        self.assertIn("TripRepository", prompt)
        self.assertTrue(
            "Repository" in prompt and ("app" in prompt or "data" in prompt or ".kt" in prompt),
            "Prompt should contain project context for TripRepository."
        )


class TestRecentChangesDataInPrompt(unittest.TestCase):
    """When user asks about recent changes, the prompt must contain timeline or curated instruction."""

    def test_recent_changes_prompt_contains_timeline_or_curated(self):
        from coordinator_listener import load_coordinator_system_prompt

        prompt = load_coordinator_system_prompt("Re: X", "What changed recently?")
        # Either project_timeline content or the "curated" instruction
        has_timeline = (
            "timeline" in prompt.lower()
            or "curated" in prompt.lower()
            or "project timeline" in prompt.lower()
        )
        self.assertTrue(
            has_timeline,
            "Prompt should contain timeline/curated context for recent-changes question."
        )


class TestBaseContextAlwaysPresent(unittest.TestCase):
    """Core quality data (coordinator-project-context, brain, SSOT) should be in every prompt."""

    def test_prompt_contains_jarvey_boundaries(self):
        from coordinator_listener import load_coordinator_system_prompt

        prompt = load_coordinator_system_prompt("Re: X", "Hello")
        # coordinator-project-context defines boundaries
        self.assertTrue(
            "Jarvey" in prompt and "OutOfRouteBuddy" in prompt,
            "Prompt should always contain Jarvey/OutOfRouteBuddy identity."
        )

    def test_prompt_contains_project_brain_or_ssot(self):
        from coordinator_listener import load_coordinator_system_prompt

        prompt = load_coordinator_system_prompt("Re: X", "Hi")
        # Brain or SSOT condensed should be present
        has_core = (
            "End trip" in prompt
            or "Clear trip" in prompt
            or "Intent map" in prompt
            or "Entity" in prompt
            or "SSOT" in prompt
            or "Room" in prompt
        )
        self.assertTrue(
            has_core,
            "Prompt should contain core quality data (brain/SSOT) for all queries."
        )


class TestIntentSpecificDataLoaded(unittest.TestCase):
    """Intent-specific sources should be loaded for matching queries."""

    def test_version_prompt_contains_version_info(self):
        from coordinator_listener import load_coordinator_system_prompt

        prompt = load_coordinator_system_prompt("Re: X", "What version is the app?")
        # Version fetcher reads app/build.gradle.kts
        self.assertTrue(
            "version" in prompt.lower() or "1.0" in prompt or "gradle" in prompt.lower(),
            "Prompt should contain version context for version question."
        )


if __name__ == "__main__":
    unittest.main()
