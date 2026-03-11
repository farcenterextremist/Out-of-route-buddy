# Agent Aptitude Test — Automation

This folder contains scripts and the prompt manifest for running the **agent aptitude test** for all 12 agents (24 prompts), collecting responses so you can **read the output**, scoring via the markdown scorecard, and generating a **training priority report** to see which agents need more training or data injection.

## Purpose

- Run all 24 prompts (12 agents × 2 each: Simple and Semi-simple).
- Save every agent response in readable markdown files under `docs/agents/data-sets/aptitude-responses/`.
- Score each response (1–5 on Scope, Data set, Output, Handoff, Voice) in the markdown scorecard.
- Produce a **training priority report** that ranks agents and recommends training vs data injection per agent.

## Prerequisites

- Python 3.7+
- No API keys required for the standard flow (you paste responses from Cursor chat).
- **LLM-assisted scoring (Step 3a):** Set `OPENAI_API_KEY` in the environment and install: `pip install openai` (or `pip install -r requirements.txt`).

## Step 1 — Generate response collection and runbook

From the repo root:

```bash
python scripts/agent-aptitude/generate_response_collection.py
```

Or from this directory:

```bash
python generate_response_collection.py
```

**Output:**

- **24 response files** in `docs/agents/data-sets/aptitude-responses/`:  
  `01_coordinator_simple.md` … `24_blue_team_semi_simple.md`.  
  Each file contains the prompt (copy-paste ready), “Look for” criteria for scoring, and an empty **Response** section.
- **Runbook** at `docs/agents/AGENT_APTITUDE_RUNBOOK.md`: 24 steps in order; each step says which agent to invoke, which role card to use, the prompt, and the path to the response file.

Optional: `--base-dir /path/to/repo` to override the repo root.

## Step 2 — Run prompts and collect responses

1. Open `docs/agents/AGENT_APTITUDE_RUNBOOK.md`.
2. For each step:
   - In Cursor, invoke the agent (e.g. “Act as the Back-end Engineer; use docs/agents/roles/backend-engineer.md”).
   - Paste the prompt from the runbook or from the corresponding file in `docs/agents/data-sets/aptitude-responses/`.
   - Copy the agent’s reply and paste it into the **Response** section of that same response file.
3. You **read the output** in those response files to score and to review quality.

**Future API hook:** A later script or integration could call an LLM API (e.g. OpenAI, Anthropic) with:
- Role card content (read from the path in `prompt_manifest.json`),
- Prompt text from the manifest for that agent and prompt type,

then write the API response into the **Response** section of the matching file in `aptitude-responses/`. The contract is: **manifest** (`prompt_manifest.json`) and **file naming** (`NN_agent_id_simple.md` / `NN_agent_id_semi_simple.md`). No implementation is provided here; this is documented for future use.

## Step 3 — Score

1. Open each response file in `docs/agents/data-sets/aptitude-responses/` and read the agent’s reply.
2. Score 1–5 on each dimension: **Scope**, **Data set**, **Output**, **Handoff**, **Voice** (see `docs/agents/AGENT_APTITUDE_AND_SCORING.md` for the rubric).
3. Fill the first scorecard table in `docs/agents/data-sets/agent-aptitude-scorecard.md` (or a dated copy, e.g. `agent-aptitude-scorecard-2025-02-22.md`). One row per (Agent, Prompt); 26 rows. Optionally fill the **Overall** column (average of the five); the report script can compute it if left blank.

## Step 3a — LLM-assisted scoring (optional)

To get suggested scores without grading all 26 responses by hand:

1. Copy the scorecard template to a dated file:  
   `docs/agents/data-sets/agent-aptitude-scorecard-YYYY-MM-DD.md`
2. Run the scoring script (requires `OPENAI_API_KEY` and `pip install openai`):

   ```bash
   python scripts/agent-aptitude/score_from_responses.py docs/agents/data-sets/agent-aptitude-scorecard-YYYY-MM-DD.md
   ```

   Optional: `--date YYYY-MM-DD` to set the run date; `--dry-run` to print suggested scores without writing the scorecard.
3. Open the dated scorecard, review the suggested scores, and edit any you disagree with.

**Dated runs:** Use a dated scorecard per run so you can compare over time. Then generate a dated report with `-o` (see Step 4).

## Step 4 — Generate training priority report

From the repo root:

```bash
python scripts/agent-aptitude/report_from_scorecard.py docs/agents/data-sets/agent-aptitude-scorecard.md
```

Or with a dated scorecard:

```bash
python scripts/agent-aptitude/report_from_scorecard.py docs/agents/data-sets/agent-aptitude-scorecard-2025-02-22.md
```

**Output:** `docs/agents/data-sets/aptitude-training-priority-report.md` by default. For dated runs, pass `-o` to write a dated report so each run has a scorecard + report pair:

```bash
python scripts/agent-aptitude/report_from_scorecard.py docs/agents/data-sets/agent-aptitude-scorecard-2025-02-22.md -o docs/agents/data-sets/aptitude-training-priority-report-2025-02-22.md
```

Open the report to see:
- Which agents need more training or data injection, ranked by priority.
- Per agent: agent overall, lowest dimension, and recommended action (training vs data injection).

## File reference

| File | Description |
|------|--------------|
| `scripts/agent-aptitude/prompt_manifest.json` | Source of truth for all 12 agents and their two prompts + “look for” text. |
| `scripts/agent-aptitude/generate_response_collection.py` | Generates the 26 response files and the runbook. |
| `scripts/agent-aptitude/score_from_responses.py` | LLM-assisted scoring: reads response files, suggests 1–5 scores, fills a scorecard (requires `OPENAI_API_KEY`). |
| `scripts/agent-aptitude/report_from_scorecard.py` | Parses the filled scorecard and writes the training priority report (use `-o path` for dated reports). |
| `scripts/agent-aptitude/README.md` | This file. |
| `docs/agents/data-sets/aptitude-responses/*.md` | One file per (agent, prompt); you paste or write the agent’s reply in the Response section. |
| `docs/agents/AGENT_APTITUDE_RUNBOOK.md` | 26-step runbook; generated by `generate_response_collection.py`. |
| `docs/agents/data-sets/agent-aptitude-scorecard.md` | Markdown scorecard table; you fill scores here (or in a dated copy). |
| `docs/agents/data-sets/aptitude-training-priority-report.md` | Training priority report; written by `report_from_scorecard.py`. |
| `docs/agents/AGENT_APTITUDE_AND_SCORING.md` | Full test design, scoring dimensions, and interpretation rules. |

## Re-running the process (dated runs)

1. **New run:** Copy the scorecard template to a dated file (e.g. `agent-aptitude-scorecard-YYYY-MM-DD.md`). Fill it manually (Step 3) or run `score_from_responses.py` with that path (Step 3a), then review and edit.
2. **Dated report:** Run `report_from_scorecard.py <scorecard-path> -o docs/agents/data-sets/aptitude-training-priority-report-YYYY-MM-DD.md` so each run has a scorecard + report pair for comparison.
3. **Regenerate response files:** Run `generate_response_collection.py` again only if you want fresh empty response files; it overwrites the 26 files. Back up `aptitude-responses/` first if you want to keep existing responses.

This makes the process repeatable for **future training** rounds: copy scorecard → run prompts / paste responses → score (manual or LLM-assisted) → report → improve weak agents → re-test.
