#!/usr/bin/env python3
"""
Generate the agent aptitude response collection: 26 response files and a runbook.
Reads prompt_manifest.json and writes to docs/agents/data-sets/aptitude-responses/
and docs/agents/AGENT_APTITUDE_RUNBOOK.md.
"""
from __future__ import annotations

import argparse
import json
import os
from pathlib import Path


def repo_root(script_dir: Path) -> Path:
    """Assume script is in scripts/agent-aptitude/; repo root is parent of scripts/."""
    return script_dir.parent.parent


def load_manifest(manifest_path: Path) -> list[dict]:
    with open(manifest_path, encoding="utf-8") as f:
        return json.load(f)


def slug(s: str) -> str:
    """Replace spaces and special chars for filenames."""
    return s.replace(" ", "_").replace("/", "_").replace("&", "and").lower()


def write_response_file(
    output_path: Path,
    agent_id: str,
    display_name: str,
    role_card_path: str,
    prompt_type: str,
    prompt_text: str,
    look_for: str,
) -> None:
    content = f"""# {display_name} — {prompt_type.replace("_", " ").title()}

- **Role card:** `{role_card_path}`
- **Prompt type:** {prompt_type.replace("_", " ").title()}

## Prompt (copy-paste)

```
{prompt_text}
```

## Look for (scoring)

{look_for}

---

## Response

(Paste the agent's reply below, or leave empty for future API-filled response.)

"""
    output_path.write_text(content, encoding="utf-8")


def write_runbook(runbook_path: Path, manifest: list[dict], response_dir: Path, base_dir: Path) -> None:
    try:
        rel_response_dir = response_dir.relative_to(base_dir)
    except ValueError:
        rel_response_dir = response_dir
    lines = [
        "# Agent Aptitude Test — Runbook",
        "",
        "Follow these 26 steps in order. For each step:",
        "1. Invoke the agent in Cursor (with the role card).",
        "2. Paste the prompt from the response file or from this runbook.",
        "3. Paste the agent's reply into the **Response** section of the corresponding file in `docs/agents/data-sets/aptitude-responses/`.",
        "",
        "---",
        "",
    ]
    step = 1
    for entry in manifest:
        for prompt_type, prompt_text, look_for in [
            ("simple", entry["simple_prompt"], entry["simple_look_for"]),
            ("semi_simple", entry["semi_simple_prompt"], entry["semi_simple_look_for"]),
        ]:
            filename = f"{step:02d}_{entry['agent_id']}_{prompt_type}.md"
            response_file = rel_response_dir / filename
            lines.append(f"## Step {step}: {entry['display_name']} — {prompt_type.replace('_', ' ').title()}")
            lines.append("")
            lines.append(f"- **Role card:** `{entry['role_card_path']}`")
            lines.append(f"- **Response file:** `{response_file}`")
            lines.append("")
            lines.append("**Prompt:**")
            lines.append("")
            lines.append(f"> {prompt_text}")
            lines.append("")
            lines.append("---")
            lines.append("")
            step += 1
    runbook_path.write_text("\n".join(lines), encoding="utf-8")


def main() -> None:
    script_dir = Path(__file__).resolve().parent
    default_base = repo_root(script_dir)
    parser = argparse.ArgumentParser(description="Generate agent aptitude response files and runbook.")
    parser.add_argument(
        "--base-dir",
        type=Path,
        default=default_base,
        help=f"Repo base directory (default: {default_base})",
    )
    args = parser.parse_args()
    base_dir = args.base_dir.resolve()
    manifest_path = script_dir / "prompt_manifest.json"
    if not manifest_path.exists():
        raise SystemExit(f"Manifest not found: {manifest_path}")
    manifest = load_manifest(manifest_path)
    response_dir = base_dir / "docs" / "agents" / "data-sets" / "aptitude-responses"
    response_dir.mkdir(parents=True, exist_ok=True)
    step = 1
    for entry in manifest:
        for prompt_type, prompt_text, look_for in [
            ("simple", entry["simple_prompt"], entry["simple_look_for"]),
            ("semi_simple", entry["semi_simple_prompt"], entry["semi_simple_look_for"]),
        ]:
            filename = f"{step:02d}_{entry['agent_id']}_{prompt_type}.md"
            out_path = response_dir / filename
            write_response_file(
                out_path,
                entry["agent_id"],
                entry["display_name"],
                entry["role_card_path"],
                prompt_type,
                prompt_text,
                look_for,
            )
            print(f"Wrote {out_path.relative_to(base_dir)}")
            step += 1
    runbook_path = base_dir / "docs" / "agents" / "AGENT_APTITUDE_RUNBOOK.md"
    write_runbook(runbook_path, manifest, response_dir, base_dir)
    print(f"Wrote {runbook_path.relative_to(base_dir)}")
    print("Done. Follow the runbook to run prompts and paste responses.")


if __name__ == "__main__":
    main()
