#!/usr/bin/env python3
"""
Jarvey data access tests: read ability, comprehension, and correct locations.

Verifies:
  1. **Location** — All data files exist at the paths Jarvey expects
  2. **Read** — Jarvey can open and read each file without error
  3. **Comprehension** — Loaded content has expected structure and key terms

Run: python scripts/coordinator-email/test_jarvey_data_access.py
Or: python -m pytest scripts/coordinator-email/test_jarvey_data_access.py -v
"""

import json
import os
import sys
import unittest

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.path.dirname(__file__) and os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

REPO_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))

# Canonical data paths used by Jarvey (must match context_loader, coordinator_listener, template_registry)
JARVEY_DATA_PATHS = {
    "coordinator_instructions": os.path.join(REPO_ROOT, "docs", "agents", "coordinator-instructions.md"),
    "coordinator_project_context": os.path.join(REPO_ROOT, "docs", "agents", "coordinator-project-context.md"),
    "jarvey_brain": os.path.join(REPO_ROOT, "docs", "agents", "JARVEY_PROJECT_BRAIN.md"),
    "known_truths": os.path.join(REPO_ROOT, "docs", "agents", "KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md"),
    "roadmap": os.path.join(REPO_ROOT, "docs", "product", "ROADMAP.md"),
    "project_timeline": os.path.join(SCRIPT_DIR, "project_timeline.json"),
    "email_notes": os.path.join(REPO_ROOT, "docs", "agents", "EMAIL_NOTES.md"),
    "intents": os.path.join(SCRIPT_DIR, "intents", "intents.json"),
    "capability_menu": os.path.join(SCRIPT_DIR, "jarvey_capability_menu.json"),
    "templates_dir": os.path.join(SCRIPT_DIR, "templates"),
}


def _read_file(path: str) -> str:
    """Read file content. Raises on error."""
    with open(path, encoding="utf-8") as f:
        return f.read()


class TestDataLocations(unittest.TestCase):
    """All Jarvey data files exist at expected paths."""

    def test_coordinator_instructions_exists(self):
        self.assertTrue(os.path.isfile(JARVEY_DATA_PATHS["coordinator_instructions"]))

    def test_coordinator_project_context_exists(self):
        self.assertTrue(os.path.isfile(JARVEY_DATA_PATHS["coordinator_project_context"]))

    def test_jarvey_brain_exists(self):
        self.assertTrue(os.path.isfile(JARVEY_DATA_PATHS["jarvey_brain"]))

    def test_known_truths_exists(self):
        self.assertTrue(os.path.isfile(JARVEY_DATA_PATHS["known_truths"]))

    def test_roadmap_exists(self):
        self.assertTrue(os.path.isfile(JARVEY_DATA_PATHS["roadmap"]))

    def test_project_timeline_exists(self):
        self.assertTrue(os.path.isfile(JARVEY_DATA_PATHS["project_timeline"]))

    def test_intents_exists(self):
        self.assertTrue(os.path.isfile(JARVEY_DATA_PATHS["intents"]))

    def test_capability_menu_exists(self):
        self.assertTrue(os.path.isfile(JARVEY_DATA_PATHS["capability_menu"]))

    def test_templates_dir_exists(self):
        self.assertTrue(os.path.isdir(JARVEY_DATA_PATHS["templates_dir"]))

    def test_email_notes_exists_or_creatable(self):
        path = JARVEY_DATA_PATHS["email_notes"]
        self.assertTrue(os.path.isfile(path) or os.path.isdir(os.path.dirname(path)))


class TestDataRead(unittest.TestCase):
    """Jarvey can read each data file without error."""

    def test_read_coordinator_instructions(self):
        content = _read_file(JARVEY_DATA_PATHS["coordinator_instructions"])
        self.assertIsInstance(content, str)
        self.assertGreater(len(content), 100)

    def test_read_coordinator_project_context(self):
        content = _read_file(JARVEY_DATA_PATHS["coordinator_project_context"])
        self.assertIsInstance(content, str)
        self.assertGreater(len(content), 50)

    def test_read_jarvey_brain(self):
        content = _read_file(JARVEY_DATA_PATHS["jarvey_brain"])
        self.assertIsInstance(content, str)
        self.assertGreater(len(content), 100)

    def test_read_known_truths(self):
        content = _read_file(JARVEY_DATA_PATHS["known_truths"])
        self.assertIsInstance(content, str)
        self.assertGreater(len(content), 100)

    def test_read_roadmap(self):
        content = _read_file(JARVEY_DATA_PATHS["roadmap"])
        self.assertIsInstance(content, str)
        self.assertGreater(len(content), 50)

    def test_read_project_timeline(self):
        content = _read_file(JARVEY_DATA_PATHS["project_timeline"])
        self.assertIsInstance(content, str)

    def test_read_intents(self):
        content = _read_file(JARVEY_DATA_PATHS["intents"])
        self.assertIsInstance(content, str)

    def test_read_capability_menu(self):
        content = _read_file(JARVEY_DATA_PATHS["capability_menu"])
        self.assertIsInstance(content, str)

    def test_read_email_notes_if_exists(self):
        path = JARVEY_DATA_PATHS["email_notes"]
        if os.path.isfile(path):
            content = _read_file(path)
            self.assertIsInstance(content, str)


class TestDataComprehension(unittest.TestCase):
    """Loaded content has expected structure and key terms."""

    def test_instructions_comprehends_jarvey_role(self):
        content = _read_file(JARVEY_DATA_PATHS["coordinator_instructions"])
        self.assertIn("Jarvey", content)
        self.assertTrue(
            "coordinator" in content.lower() or "Coordinator" in content,
            "Instructions should define coordinator role."
        )

    def test_project_context_comprehends_outofroutebuddy(self):
        content = _read_file(JARVEY_DATA_PATHS["coordinator_project_context"])
        self.assertIn("OutOfRouteBuddy", content)
        self.assertIn("Jarvey", content)

    def test_brain_comprehends_intent_map(self):
        content = _read_file(JARVEY_DATA_PATHS["jarvey_brain"])
        self.assertTrue(
            "Intent map" in content or "User question" in content or "Intent" in content,
            "Brain should contain intent/user-question mapping."
        )
        self.assertIn("Jarvey", content)

    def test_known_truths_comprehends_ssot(self):
        content = _read_file(JARVEY_DATA_PATHS["known_truths"])
        self.assertTrue(
            "End trip" in content or "Clear trip" in content,
            "KNOWN_TRUTHS should define End/Clear trip semantics."
        )
        self.assertTrue(
            "TripCrashRecoveryManager" in content or "Room" in content,
            "KNOWN_TRUTHS should reference recovery or persistence."
        )

    def test_roadmap_comprehends_priorities(self):
        content = _read_file(JARVEY_DATA_PATHS["roadmap"])
        roadmap_markers = ["Auto drive", "Reports", "History"]
        found = sum(1 for m in roadmap_markers if m in content)
        self.assertGreaterEqual(found, 2, f"ROADMAP should contain next-three priorities; found {found}")

    def test_project_timeline_valid_json(self):
        content = _read_file(JARVEY_DATA_PATHS["project_timeline"])
        data = json.loads(content)
        self.assertIsInstance(data, list)

    def test_intents_valid_json_with_structure(self):
        content = _read_file(JARVEY_DATA_PATHS["intents"])
        data = json.loads(content)
        self.assertIsInstance(data, list)
        self.assertGreater(len(data), 5)
        first = data[0]
        self.assertIn("name", first)
        self.assertIn("keywords", first)
        self.assertIn("sources", first)

    def test_capability_menu_valid_json_with_options(self):
        content = _read_file(JARVEY_DATA_PATHS["capability_menu"])
        data = json.loads(content)
        self.assertIn("options", data)
        opts = data["options"]
        self.assertIsInstance(opts, list)
        self.assertGreater(len(opts), 3)

    def test_templates_loadable_and_have_match_logic(self):
        templates_dir = JARVEY_DATA_PATHS["templates_dir"]
        count = 0
        for name in os.listdir(templates_dir):
            if not name.endswith(".json"):
                continue
            path = os.path.join(templates_dir, name)
            with open(path, encoding="utf-8") as f:
                t = json.load(f)
            # Templates use keywords, or condition (e.g. short_body) for matching
            has_match = "keywords" in t or "condition" in t
            self.assertTrue(has_match, f"Template {name} should have keywords or condition")
            self.assertIn("key", t, f"Template {name} should have key")
            self.assertIn("body", t, f"Template {name} should have body")
            count += 1
        self.assertGreater(count, 5, "Should have multiple templates")


class TestContextLoaderIntegration(unittest.TestCase):
    """Context loader actually loads and returns the data."""

    def test_load_context_includes_brain(self):
        from context_loader import load_context_for_user_message

        ctx = load_context_for_user_message("Re: X", "What's next?")
        self.assertIn("Auto", ctx)
        self.assertIn("Jarvey", ctx)

    def test_load_context_includes_roadmap_for_whats_next(self):
        from context_loader import load_context_for_user_message

        ctx = load_context_for_user_message("Re: X", "What's next?")
        self.assertTrue(
            "Reports" in ctx or "History" in ctx or "roadmap" in ctx.lower(),
            "Context for 'What's next?' should include roadmap data."
        )

    def test_load_context_includes_recovery_for_recovery_question(self):
        from context_loader import load_context_for_user_message

        ctx = load_context_for_user_message("Re: X", "How does trip recovery work?")
        self.assertTrue(
            "recovery" in ctx.lower() or "TripCrash" in ctx or "crash" in ctx.lower(),
            "Context for recovery question should include recovery data."
        )


if __name__ == "__main__":
    unittest.main()
