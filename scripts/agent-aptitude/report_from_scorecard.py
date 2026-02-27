#!/usr/bin/env python3
"""
Read the filled markdown scorecard, compute per-agent overalls and lowest dimension,
apply interpretation rules, and write the training priority report.
"""
from __future__ import annotations

import argparse
import re
from pathlib import Path

# Order and agent_id must match prompt_manifest.json and scorecard table order
AGENT_ORDER = [
    "coordinator",
    "design_creative",
    "ui_ux",
    "frontend",
    "backend",
    "devops",
    "qa",
    "security",
    "email_editor",
    "file_organizer",
    "human_in_the_loop",
    "red_team",
    "blue_team",
]

AGENT_DISPLAY_NAMES = {
    "coordinator": "Master Branch Coordinator",
    "design_creative": "Design/Creative Manager",
    "ui_ux": "UI/UX Specialist",
    "frontend": "Front-end Engineer",
    "backend": "Back-end Engineer",
    "devops": "DevOps Engineer",
    "qa": "QA Engineer",
    "security": "Security Specialist",
    "email_editor": "Email Editor / Market Guru",
    "file_organizer": "File Organizer",
    "human_in_the_loop": "Human-in-the-Loop Manager",
    "red_team": "Red Team",
    "blue_team": "Blue Team",
}

DIMENSIONS = ["scope", "data_set", "output", "handoff", "voice"]
DIMENSION_ACTIONS = {
    "scope": "Training: tighten role card (out-of-scope list, handoffs). Add 1–2 negative examples.",
    "data_set": "Data injection: ensure data-set file exists and is linked; add concrete paths and example artifacts.",
    "output": "Training + data: clarify expected artifact format in role card; add example output in data set.",
    "handoff": "Training: in role card list 'When to hand off to X' and one example handoff phrase.",
    "voice": "Training: add 2–3 role-specific phrases or artifact names to the agent card.",
}


def parse_scorecard(path: Path) -> list[dict]:
    """Parse the first scorecard table (Agent, Prompt, Scope, Data set, Output, Handoff, Voice, Overall)."""
    text = path.read_text(encoding="utf-8")
    lines = text.splitlines()
    rows = []
    i = 0
    while i < len(lines):
        line = lines[i]
        if "| Agent |" in line and "Prompt" in line and "Scope" in line:
            i += 1
            if i < len(lines) and re.match(r"^\s*\|[\s\-:]+\|", lines[i]):
                i += 1
            while i < len(lines) and len(rows) < 26:
                line = lines[i]
                i += 1
                if "|" not in line:
                    break
                cells = [c.strip() for c in line.split("|")]
                while cells and cells[0] == "":
                    cells.pop(0)
                while cells and cells[-1] == "":
                    cells.pop()
                if len(cells) < 2:
                    continue
                if re.match(r"^[-:\s]+$", cells[0].replace(" ", "")):
                    continue
                scope = _parse_score(cells[2]) if len(cells) > 2 else None
                data_set = _parse_score(cells[3]) if len(cells) > 3 else None
                output = _parse_score(cells[4]) if len(cells) > 4 else None
                handoff = _parse_score(cells[5]) if len(cells) > 5 else None
                voice = _parse_score(cells[6]) if len(cells) > 6 else None
                overall = _parse_score(cells[7]) if len(cells) > 7 else None
                rows.append({
                    "agent": cells[0],
                    "prompt": cells[1],
                    "scope": scope,
                    "data_set": data_set,
                    "output": output,
                    "handoff": handoff,
                    "voice": voice,
                    "overall": overall,
                })
            break
        i += 1
    return rows[:26]


def _parse_score(s: str) -> int | None:
    if not s or not s.strip():
        return None
    s = s.strip()
    try:
        n = int(s)
        if 1 <= n <= 5:
            return n
    except ValueError:
        pass
    return None


def compute_overall(row: dict) -> float | None:
    scores = [row.get(d) for d in DIMENSIONS if row.get(d) is not None]
    if not scores:
        return None
    return sum(scores) / len(scores)


def agent_overall_and_lowest(rows: list[dict]) -> list[dict]:
    """Group by agent (pairs of Simple, Semi-simple), compute agent overall and lowest dimension."""
    results = []
    for i in range(0, len(rows), 2):
        agent_id = AGENT_ORDER[i // 2] if i // 2 < len(AGENT_ORDER) else f"agent_{i//2}"
        r1 = rows[i]
        r2 = rows[i + 1] if i + 1 < len(rows) else {}
        o1 = r1.get("overall") if r1.get("overall") is not None else compute_overall(r1)
        o2 = r2.get("overall") if r2.get("overall") is not None else compute_overall(r2) if r2 else None
        if o1 is None and o2 is None:
            agent_avg = None
            lowest_dim = None
            lowest_val = None
        else:
            vals = [v for v in [o1, o2] if v is not None]
            agent_avg = sum(vals) / len(vals) if vals else None
            all_scores = {}
            for d in DIMENSIONS:
                v1 = r1.get(d)
                v2 = r2.get(d) if r2 else None
                vs = [v for v in [v1, v2] if v is not None]
                if vs:
                    all_scores[d] = min(vs)
            lowest_dim = min(all_scores, key=all_scores.get) if all_scores else None
            lowest_val = all_scores.get(lowest_dim) if lowest_dim else None
        results.append({
            "agent_id": agent_id,
            "display_name": AGENT_DISPLAY_NAMES.get(agent_id, r1.get("agent", agent_id)),
            "agent_overall": agent_avg,
            "lowest_dimension": lowest_dim,
            "lowest_value": lowest_val,
            "row1": r1,
            "row2": r2,
        })
    return results


def recommend_action(lowest_dim: str | None, lowest_val: int | None, agent_overall: float | None) -> str:
    if lowest_dim and lowest_val is not None and (lowest_val <= 2 or (agent_overall is not None and agent_overall < 3.5)):
        return DIMENSION_ACTIONS.get(lowest_dim, "Review role card and data set.")
    return "No critical gap; optional refinement."


def write_report(out_path: Path, results: list[dict], scorecard_path: Path) -> None:
    lines = [
        "# Agent Aptitude — Training Priority Report",
        "",
        f"Generated from scorecard: `{scorecard_path.name}`",
        "",
        "## Interpretation reminder",
        "",
        "- **Scope 1–2** → Training: tighten role card, out-of-scope list.",
        "- **Data set 1–2** → Data injection: add/update data-set file, concrete paths.",
        "- **Output 1–2** → Training + data: clarify artifact format, example output.",
        "- **Handoff 1–2** → Training: \"When to hand off to X\" + example in role card.",
        "- **Voice 1–2** → Training: role-specific phrases in agent card.",
        "- **Overall < 3.5 or any dimension ≤ 2** → Improve lowest dimension first, then re-test.",
        "",
        "---",
        "",
        "## Ranked by priority (needs improvement first)",
        "",
    ]
    sorted_results = sorted(
        results,
        key=lambda r: (
            0 if r["agent_overall"] is None else (1 if r["agent_overall"] < 3.5 else 2),
            0 if r["lowest_value"] is None else (1 if r["lowest_value"] <= 2 else 2),
            -(r["agent_overall"] or 0),
        ),
    )
    for r in sorted_results:
        ao = r["agent_overall"]
        ld = r["lowest_dimension"]
        lv = r["lowest_value"]
        rec = recommend_action(ld, lv, ao)
        lines.append(f"### {r['display_name']}")
        lines.append("")
        ao_fmt = f"{ao:.2f}" if isinstance(ao, (int, float)) else "(not scored)"
        lines.append(f"- **Agent overall:** {ao_fmt}")
        if ld:
            lines.append(f"- **Lowest dimension:** {ld} ({lv})" if lv is not None else f"- **Lowest dimension:** {ld}")
        lines.append(f"- **Recommended action:** {rec}")
        lines.append("")
    lines.append("---")
    lines.append("")
    lines.append("## All agents (by name)")
    lines.append("")
    for r in results:
        ao = r["agent_overall"]
        ld = r["lowest_dimension"]
        lv = r["lowest_value"]
        rec = recommend_action(ld, lv, ao)
        ao_str = f"{ao:.2f}" if isinstance(ao, (int, float)) else "—"
        ld_str = f"{ld or '—'}{f' ({lv})' if lv is not None else ''}"
        lines.append(f"- **{r['display_name']}**: overall={ao_str}, lowest={ld_str} → {rec}")
    out_path.write_text("\n".join(lines), encoding="utf-8")


def main() -> None:
    script_dir = Path(__file__).resolve().parent
    default_base = script_dir.parent.parent
    default_scorecard = default_base / "docs" / "agents" / "data-sets" / "agent-aptitude-scorecard.md"
    default_report = default_base / "docs" / "agents" / "data-sets" / "aptitude-training-priority-report.md"
    parser = argparse.ArgumentParser(description="Generate training priority report from filled scorecard.")
    parser.add_argument("scorecard", type=Path, nargs="?", default=default_scorecard, help="Path to scorecard markdown file")
    parser.add_argument("-o", "--output", type=Path, default=default_report, help="Output report path")
    args = parser.parse_args()
    scorecard_path = args.scorecard.resolve()
    if not scorecard_path.exists():
        raise SystemExit(f"Scorecard not found: {scorecard_path}")
    rows = parse_scorecard(scorecard_path)
    if len(rows) < 26:
        print(f"Warning: expected 26 rows, got {len(rows)}. Proceeding with available rows.")
    results = agent_overall_and_lowest(rows)
    out_path = args.output.resolve()
    out_path.parent.mkdir(parents=True, exist_ok=True)
    write_report(out_path, results, scorecard_path)
    print(f"Wrote {out_path}")


if __name__ == "__main__":
    main()
