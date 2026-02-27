#!/usr/bin/env python3
"""
LLM-assisted scoring for agent aptitude responses.

Reads the 26 response files (in manifest order), calls an LLM to suggest
scores 1–5 on Scope, Data set, Output, Handoff, Voice per response, and
writes the results into the given scorecard markdown file.

Requires: OPENAI_API_KEY in the environment. Install: pip install openai
"""
from __future__ import annotations

import argparse
import json
import os
import re
from pathlib import Path

# Scorecard table uses these exact labels (must match agent-aptitude-scorecard.md)
SCORECARD_AGENT_LABELS = [
    "Coordinator",
    "Coordinator",
    "Design/Creative",
    "Design/Creative",
    "UI/UX Specialist",
    "UI/UX Specialist",
    "Front-end Engineer",
    "Front-end Engineer",
    "Back-end Engineer",
    "Back-end Engineer",
    "DevOps Engineer",
    "DevOps Engineer",
    "QA Engineer",
    "QA Engineer",
    "Security Specialist",
    "Security Specialist",
    "Email Editor / Market Guru",
    "Email Editor / Market Guru",
    "File Organizer",
    "File Organizer",
    "Human-in-the-Loop Manager",
    "Human-in-the-Loop Manager",
    "Red Team",
    "Red Team",
    "Blue Team",
    "Blue Team",
]

PROMPT_LABELS = ["Simple", "Semi-simple"] * 13  # 26 rows

RUBRIC = """
Score each dimension 1–5:
- **Scope**: 1 = did other roles' work; 3 = mostly in scope; 5 = strictly in role, cited "out of scope" when needed.
- **Data set**: 1 = no reference to data set/paths; 3 = mentioned data set or one path; 5 = cited data-set file and concrete paths/artifacts.
- **Output**: 1 = vague, wrong, or unusable; 3 = partially actionable; 5 = concrete, actionable, correct for the role.
- **Handoff**: 1 = no handoff or wrong role; 3 = handed off but unclear; 5 = clear handoff to correct role with brief.
- **Voice**: 1 = generic; 3 = some role-specific language; 5 = clearly in role (artifacts, terminology, constraints).
"""


def repo_root(script_dir: Path) -> Path:
    return script_dir.parent.parent


def load_manifest(manifest_path: Path) -> list[dict]:
    with open(manifest_path, encoding="utf-8") as f:
        return json.load(f)


def extract_sections(path: Path) -> tuple[str, str]:
    """Extract 'Look for' and 'Response' section bodies from a response file."""
    text = path.read_text(encoding="utf-8")
    look_for = ""
    response = ""

    # Look for (scoring) ... up to --- or ## Response
    m = re.search(r"## Look for \(scoring\)\s*\n(.*?)(?=\n---|\n## Response)", text, re.DOTALL)
    if m:
        look_for = m.group(1).strip()

    # Response ... to end or next ##
    m = re.search(r"## Response\s*\n(.*)", text, re.DOTALL)
    if m:
        response = m.group(1).strip()
    return look_for, response


def call_llm_for_scores(look_for: str, response_text: str) -> dict[str, int]:
    """Call OpenAI API to get suggested scores. Returns dict with scope, data_set, output, handoff, voice (1–5)."""
    try:
        from openai import OpenAI
    except ImportError:
        raise SystemExit(
            "LLM scoring requires the openai package. Install with: pip install openai"
        )
    api_key = os.environ.get("OPENAI_API_KEY")
    if not api_key:
        raise SystemExit(
            "Set OPENAI_API_KEY in the environment to use LLM-assisted scoring."
        )

    client = OpenAI()
    user_content = f"""**Look for (scoring criteria for this prompt):**
{look_for}

**Agent response to score:**
{response_text}

{RUBRIC}

Respond with exactly one JSON object, no other text, with keys: scope, data_set, output, handoff, voice. Each value must be an integer 1–5."""

    resp = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=[
            {
                "role": "system",
                "content": "You are a strict grader. Score agent responses 1–5 on each dimension. Return only valid JSON.",
            },
            {"role": "user", "content": user_content},
        ],
        temperature=0.2,
    )
    raw = (resp.choices[0].message.content or "").strip()
    # Allow markdown code block
    if raw.startswith("```"):
        raw = re.sub(r"^```\w*\n?", "", raw)
        raw = re.sub(r"\n?```\s*$", "", raw)
    try:
        out = json.loads(raw)
        for k in ("scope", "data_set", "output", "handoff", "voice"):
            if k not in out or not isinstance(out[k], (int, float)):
                out[k] = 3
            else:
                v = int(out[k])
                out[k] = max(1, min(5, v))
        return out
    except (json.JSONDecodeError, TypeError):
        return {
            "scope": 3,
            "data_set": 3,
            "output": 3,
            "handoff": 3,
            "voice": 3,
        }


def compute_overall(scores: dict[str, int]) -> float:
    return sum(scores[k] for k in ("scope", "data_set", "output", "handoff", "voice")) / 5.0


def build_scorecard_table_rows(suggested: list[dict]) -> list[str]:
    """Build the 26 table rows for the scorecard (Agent, Prompt, Scope, Data set, Output, Handoff, Voice, Overall)."""
    rows = []
    for i, s in enumerate(suggested):
        agent = SCORECARD_AGENT_LABELS[i]
        prompt = PROMPT_LABELS[i]
        scope = s["scope"]
        data_set = s["data_set"]
        output = s["output"]
        handoff = s["handoff"]
        voice = s["voice"]
        overall = compute_overall(s)
        overall_str = f"{overall:.2f}" if overall == int(overall) else f"{overall:.2f}"
        rows.append(
            f"| {agent} | {prompt} | {scope} | {data_set} | {output} | {handoff} | {voice} | {overall_str} |"
        )
    return rows


def build_agent_overall_rows(suggested: list[dict]) -> list[str]:
    """Build the 13 rows for the Agent overall table."""
    rows = []
    for i in range(0, 26, 2):
        s1 = suggested[i]
        s2 = suggested[i + 1]
        o1 = compute_overall(s1)
        o2 = compute_overall(s2)
        agent_avg = (o1 + o2) / 2.0
        all_dims = ["scope", "data_set", "output", "handoff", "voice"]
        mins = [min(s1[k], s2[k]) for k in all_dims]
        idx = mins.index(min(mins))
        lowest_dim = all_dims[idx]
        lowest_val = mins[idx]
        dim_display = {
            "scope": "Scope",
            "data_set": "Data set",
            "output": "Output",
            "handoff": "Handoff",
            "voice": "Voice",
        }[lowest_dim]
        agent_label = SCORECARD_AGENT_LABELS[i]
        agent_avg_str = f"{agent_avg:.2f}" if agent_avg != int(agent_avg) else f"{agent_avg:.1f}"
        rec = "Training or data injection per rubric." if lowest_val <= 2 or agent_avg < 3.5 else "Optional refinement."
        rows.append(f"| {agent_label} | {agent_avg_str} | {dim_display} ({lowest_val}) | {rec} |")
    return rows


def write_scorecard(
    scorecard_path: Path,
    suggested: list[dict],
    run_date: str,
) -> None:
    """Overwrite the scorecard file with the header up to the table, then filled table, then rest."""
    text = scorecard_path.read_text(encoding="utf-8")
    lines = text.splitlines()

    # Find "## Run:" and replace the blank with run_date
    new_lines = []
    for line in lines:
        if "## Run:" in line and "_______________" in line:
            new_lines.append(f"## Run: {run_date}")
        else:
            new_lines.append(line)

    # Replace the first scorecard table body (26 data rows)
    table_start = None
    table_sep = None
    table_end = None
    i = 0
    while i < len(new_lines):
        if "| Agent |" in new_lines[i] and "Prompt" in new_lines[i] and "Scope" in new_lines[i]:
            table_start = i
            i += 1
            if i < len(new_lines) and re.match(r"^\s*\|[\s\-:]+\|", new_lines[i]):
                table_sep = i
                i += 1
            while i < len(new_lines) and "|" in new_lines[i]:
                # Skip existing data rows
                i += 1
            table_end = i
            break
        i += 1

    if table_start is None or table_sep is None:
        raise SystemExit("Scorecard file does not contain the expected table header.")

    score_rows = build_scorecard_table_rows(suggested)
    before = new_lines[: table_sep + 1]
    after = new_lines[table_end:]
    new_lines = before + score_rows + after

    # Replace Agent overall table (13 data rows)
    ao_start = None
    ao_sep = None
    ao_end = None
    i = 0
    while i < len(new_lines):
        if "| Agent |" in new_lines[i] and "Agent overall" in new_lines[i]:
            ao_start = i
            i += 1
            if i < len(new_lines) and re.match(r"^\s*\|[\s\-:]+\|", new_lines[i]):
                ao_sep = i
                i += 1
            while i < len(new_lines) and "|" in new_lines[i] and "Agent overall" not in new_lines[i]:
                i += 1
            ao_end = i
            break
        i += 1

    if ao_start is not None and ao_sep is not None:
        ao_rows = build_agent_overall_rows(suggested)
        before_ao = new_lines[: ao_sep + 1]
        after_ao = new_lines[ao_end:]
        new_lines = before_ao + ao_rows + after_ao

    scorecard_path.write_text("\n".join(new_lines) + "\n", encoding="utf-8")


def main() -> None:
    script_dir = Path(__file__).resolve().parent
    default_base = repo_root(script_dir)
    default_responses = default_base / "docs" / "agents" / "data-sets" / "aptitude-responses"
    default_scorecard = default_base / "docs" / "agents" / "data-sets" / "agent-aptitude-scorecard.md"

    parser = argparse.ArgumentParser(
        description="LLM-assisted scoring: read response files, suggest scores, write scorecard."
    )
    parser.add_argument(
        "--response-dir",
        type=Path,
        default=default_responses,
        help="Directory containing the 26 aptitude response .md files",
    )
    parser.add_argument(
        "scorecard",
        type=Path,
        nargs="?",
        default=default_scorecard,
        help="Path to scorecard markdown file to fill (e.g. a dated copy)",
    )
    parser.add_argument(
        "--date",
        type=str,
        default="",
        help="Run date to write in scorecard (default: today YYYY-MM-DD)",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Only print suggested scores; do not write scorecard",
    )
    args = parser.parse_args()

    response_dir = args.response_dir.resolve()
    scorecard_path = args.scorecard.resolve()
    if not response_dir.exists():
        raise SystemExit(f"Response directory not found: {response_dir}")

    manifest_path = script_dir / "prompt_manifest.json"
    if not manifest_path.exists():
        raise SystemExit(f"Manifest not found: {manifest_path}")
    manifest = load_manifest(manifest_path)

    # Build list of (response_file, look_for) in manifest order
    step = 1
    items = []
    for entry in manifest:
        for prompt_type, look_for_key in [
            ("simple", "simple_look_for"),
            ("semi_simple", "semi_simple_look_for"),
        ]:
            filename = f"{step:02d}_{entry['agent_id']}_{prompt_type}.md"
            path = response_dir / filename
            look_for = entry.get(look_for_key, "")
            items.append((path, look_for))
            step += 1

    suggested = []
    for idx, (path, look_for_manifest) in enumerate(items):
        if not path.exists():
            print(f"Warning: {path.name} not found, using 3 for all dimensions.")
            suggested.append(
                {"scope": 3, "data_set": 3, "output": 3, "handoff": 3, "voice": 3}
            )
            continue
        look_for, response_text = extract_sections(path)
        look_for = look_for or look_for_manifest
        if not response_text or response_text.startswith("(Paste the agent"):
            print(f"Warning: {path.name} has no response body, using 3 for all dimensions.")
            suggested.append(
                {"scope": 3, "data_set": 3, "output": 3, "handoff": 3, "voice": 3}
            )
            continue
        print(f"Scoring {path.name}...")
        scores = call_llm_for_scores(look_for, response_text)
        suggested.append(scores)
        if args.dry_run:
            print(f"  -> {scores}")

    if len(suggested) != 26:
        raise SystemExit(f"Expected 26 scored responses, got {len(suggested)}.")

    if args.dry_run:
        print("Dry run: not writing scorecard.")
        return

    if not scorecard_path.exists():
        raise SystemExit(
            f"Scorecard not found: {scorecard_path}. Copy the template first (e.g. to a dated file)."
        )

    run_date = args.date or __import__("datetime").datetime.now().strftime("%Y-%m-%d")
    write_scorecard(scorecard_path, suggested, run_date)
    print(f"Wrote scores to {scorecard_path} (Run: {run_date}).")


if __name__ == "__main__":
    main()
