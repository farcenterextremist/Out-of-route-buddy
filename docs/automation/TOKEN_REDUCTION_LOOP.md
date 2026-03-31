# Token Reduction Loop — LLM Loop Token-Audit Lane

**LLM-loop note:** This document now represents the **token-audit lane** inside the broader [LLM_LOOP.md](./LLM_LOOP.md) contract. The existing token-loop scripts, run IDs, events, and ledgers remain the compatibility surface for this lane.

**Goals (reinstated):**
- **Save token spend** — Reduce Cursor IDE token usage without sacrificing output quality.
- **Manage context squish** — Avoid context window bloat (rules, conversation history, file context) so the agent stays effective and we avoid summarization/dropping.

**Expanded scope (this loop is responsible for):** Not only token optimization but also **context compression** (how context is summarized, chunked, or selectively loaded), **embedding** (how codebase/search and retrieval affect what gets sent to the model), **LLM optimization** (prompt design, output control, model choice, caching), and **any other aspects that reduce token spend** (conversation hygiene, rule audit, workspace settings, RAG-style retrieval in Cursor). Research and document all of these; apply what we control; track the rest in the improvement plan.

**How we improve:** We use **listener data** (`token_loop_events.jsonl`) to improve the loop over time — run count, steps completed, always-apply trends, and research notes. See §8.

**Purpose:** Run as a thinking exercise and a periodic loop (e.g. monthly or when usage spikes). **No human in the loop** — the agent records state, does deep research, runs the audit steps, organizes results, and recommends TODO tasks for the next token loop without stopping for approval. Inside the broader LLM loop, this lane remains focused on token spend, context compression, prompt shape, and lightweight automation overhead.

**Consistency contract (required):** Follow [LOOP_CONSISTENCY_STANDARD.md](./LOOP_CONSISTENCY_STANDARD.md). Include `Loop Consistency Check` and `Consistency score: X/10` in the end-of-run progress report and ledger note.

**Health + diagnostics:** Token work should also use [LOOP_HEALTH_CHECKS.md](./LOOP_HEALTH_CHECKS.md) and [LOOP_DIAGNOSTIC_SWEEP.md](./LOOP_DIAGNOSTIC_SWEEP.md) when the loop changes rules, listener wiring, shared state, or token automation scripts.

**Established:** 2025-03-11  
**Trigger:** User says **"start token loop"** or "run token reduction loop" or "token audit." Inside the permanent broader local-first contract, **"start llm loop"** now uses the LLM loop as the top-level trigger and the token-audit lane remains one of its stable lanes. **No human in the loop** — run autonomously. When you start the token loop:
1. **Record current state** (for rollback and progress tracking — what works / what doesn’t). Run `.\scripts\automation\token_loop_state_snapshot.ps1 -RunId <run_id>`; snapshot goes to `docs/automation/token_loop_snapshots/<run_id>.json`. Use the **same RunId** for the listener and all step events this run (e.g. `token-YYYYMMdd-HHmm`). `.\scripts\automation\run_token_loop.ps1` generates RunId and runs snapshot + listener start.
2. **Deep research and analysis** (Step 0) — see §4.2. Analyze current structure; no human in the loop.
3. **Start the listener** (`token_loop_start`), then run steps 0–7 with step_start/step_end, then close the run with `complete_token_loop_run.ps1` (preferred) or the manual `finish_loop_run.ps1` + `token_loop_end` sequence. All agents get Golden Storage Rules and Token Boss via the always-apply rule (self-improvement.mdc). See [TOKEN_INITIATIVE_BRIEFING.md](docs/agents/TOKEN_INITIATIVE_BRIEFING.md) as full policy reference.

---

## 1. Research Summary (What Consumes Tokens)

Cursor's context budget is consumed by:

| Source | Approx. impact | Notes |
|--------|-----------------|--------|
| **Always-apply rules** | ~500–1,000 tokens per 100 lines | Every prompt; audit and minimize. |
| **Conversation history** | ~20k–40k per 10 messages | Long chats force summarization/dropping. |
| **File context** | ~3k–5k per 500-line file | @-attachments and agent exploration. |
| **Agent exploration** | 50k+ in large codebases | Dynamic Context reduces this (on-demand loading). |

**Cursor behavior:** Dynamic Context Discovery loads context on demand (tool outputs to files, MCP details on-demand). That can yield ~47% token savings when enabled. Rule overhead and conversation length are still under your control.

**References:** Developer Toolkit — [Token Management](https://developertoolkit.ai/en/cursor-ide/advanced-techniques/token-management/); Cursor blog — Dynamic Context Discovery.

---

## 2. Thinking Exercise — Strategies

### 2.1 Rule overhead

- **Audit:** List `.cursor/rules/*.mdc`; note which have `alwaysApply: true`. Only one rule should be always-apply if possible; keep it under ~50 lines.
- **Convert:** Prefer `globs` (e.g. `app/**/*.kt`) or agent-decided (`description` set, `alwaysApply: false`) for feature/style rules.
- **Rule discipline:** If a rule is safety-critical but not globally relevant, scope it to the files or folders where it matters instead of paying for it in every chat.
- **Reference, don't inline:** In rules, point to files (e.g. `@docs/agents/CODEBASE_OVERVIEW.md`) instead of pasting long examples.

### 2.2 Conversation hygiene

- **Start fresh when:** Context gauge >60%; switching area of codebase; agent repeats or uses outdated info; after finishing one logical task.
- **Front-load context:** Put relevant files and instructions in the first message instead of building context over many messages.

### 2.3 File context

- **Minimum viable context:** Attach only files the agent needs; let it open more if required.
- **Use @folder:** For directory overviews instead of attaching every file in the folder.

### 2.4 Model choice

- Use a **faster/cheaper model** for: simple renames, formatting, single-line fixes, comments.
- Use **stronger model** for: multi-file features, complex debugging, architecture decisions.
- Switch via Cmd/Ctrl+/ or model picker.

### 2.5 Skills and tooling

- Prefer a **focused skill** over a new global rule when the guidance is specialized or only relevant for a named workflow.
- Audit whether existing skills/rules already cover the job before adding a new one; duplicate guidance costs tokens and causes drift.
- Keep tool output selective: targeted searches, narrow file reads, and short progress updates beat broad dumps.

### 2.6 Workspace settings (already helping)

- `files.watcherExclude` and `search.exclude` for `.gradle`, `build`, `.cursor` reduce noise and indexing.
- Java lightweight mode and disabled auto-build reduce background work.
- Add `.cursorignore` when permissions allow it; otherwise keep the same exclusions in workspace settings so generated folders stay out of search/index paths.

---

## 3. Implementation Checklist (This Project)

Use this checklist when running the token loop. Update as you make changes.

- [ ] **Always-apply rules:** Only one rule has `alwaysApply: true` (self-improvement.mdc). Keep it under ~55 lines; token-awareness added in a few bullets.
- [ ] **Other rules:** red-team, blue-team, 2-hour-loop, kotlin-best-practices use `alwaysApply: false`; kotlin scoped to `app/**/*.kt`.
- [ ] **Safety rules are scoped:** data-separation and other policy-heavy rules use globs or description-only unless they truly apply to every chat.
- [ ] **Doc references:** Rules reference `docs/...` instead of inlining long content.
- [ ] **Context docs:** CODEBASE_OVERVIEW, KNOWN_TRUTHS, GOAL_AND_MISSION kept concise; linked from AGENTS.md.
- [ ] **Conversation hygiene:** Operator starts new chats when shifting tasks or when gauge is high; front-loads context in first message when possible.
- [ ] **Settings:** `.vscode/settings.json` excludes build/cache from watcher and search.
- [ ] **Cacheable prompt shape:** Stable instructions go first, volatile run-specific details go later so prompt caching can help on long repeated prompts.

---

## 4. The Loop — How to Run

### 4.1 When to run

- **Start token loop:** When you say **"start token loop"** — (1) **Record current state** with `token_loop_state_snapshot.ps1 -RunId <run_id>` (rollback + progress tracking). (2) Start the **listener** (`token_loop_start` with same RunId), then run **steps 0–7** (step 0 = deep research and analysis of current structure; step 7 = organize results and recommend TODO tasks for next token loop). **No human in the loop** — run autonomously. Use the same RunId for all listener events this run; `run_token_loop.ps1` generates it. All agents get Golden Storage Rules via the always-apply rule. Listener data is used to improve the loop (§8).
- **Start llm loop:** Use `run_llm_loop.ps1` as the permanent broader local-first entrypoint. The token-audit lane remains the first stable lane under that contract and keeps token-loop history intact. See [LLM_LOOP.md](./LLM_LOOP.md).
- **On demand:** Same as above; also "run token reduction loop" or "token audit."
- **Periodically:** e.g. monthly, or when Cursor usage dashboard shows higher-than-usual burn.
- **Optional in Improvement Loop:** As Phase 0.6 (see below).

### 4.2 Steps (each run)

When running the loop, invoke the **token loop listener** at start, each step (optional step_end with metrics), and end. See [TOKEN_LOOP_LISTENER.md](./TOKEN_LOOP_LISTENER.md). **No human in the loop** — run all steps autonomously.

| Step | Action | Time | Listener |
|------|--------|------|----------|
| **0. Deep research and analysis** | **At beginning.** (1) **Research popular, current, up-to-date token saving methods and best practices** (e.g. web search, Cursor/IDE docs, TOKEN_SAVING_PRACTICES); note any new or changed practices. (2) Read [TOKEN_LOOP_IMPROVEMENT_PLAN.md](./TOKEN_LOOP_IMPROVEMENT_PLAN.md) (blindspots, rule output baseline, context research), [TOKEN_SAVING_PRACTICES.md](./TOKEN_SAVING_PRACTICES.md) (standard practices), and [QUALITY_STANDARDS_AND_PROOF.md](./QUALITY_STANDARDS_AND_PROOF.md) (proof-of-quality evidence rules); at start of run, note if practices need refresh from latest research. (3) Analyze current structure with no human in the loop: read latest snapshot (`token_loop_snapshots/`), `token_loop_events.jsonl` (last run, run count, steps completed). (4) List `.cursor/rules/*.mdc` — alwaysApply, globs, line counts, doc references, and whether any safety rule should be scoped tighter. (5) Review `.vscode/settings.json` and `.cursorignore` if present (watcher/search/index exclusions). (6) Audit prompt-cache friendliness: keep stable instructions first and run-specific details later when prompts are long/repeated. (7) Note: always-apply count/lines, drift from checklist (§3), and one-line research summary for this run. Do not stop for approval. | 5 min | step_start Step=0; step_end with note (e.g. research_summary one-liner) |
| **0.5 Diagnostic sweep** | If this run changes loop docs, rules, or token scripts: run liveness, continuity tests, and a targeted problem-hunt pass per `LOOP_DIAGNOSTIC_SWEEP.md`. Record one residual risk. | 2 min | optional step note |
| 1. **Audit rules** | List `.cursor/rules/*.mdc`; count lines and `alwaysApply`; estimate token cost of always-apply. Prefer `scripts/automation/compare_token_rule_overhead.ps1` so this step compares the current rule state against the latest token snapshot instead of relying on manual counting. | 2 min | step_start Step=1; step_end with metrics always_apply_count, always_apply_lines |
| 2. **Check for new always-apply** | If any new rule is always-apply, consider converting to glob or description-only. | 2 min | step_start Step=2; step_end (optional metrics: conversions_done) |
| 3. **Doc references** | Ensure no rule inlines large examples; replace with pointers to `docs/` or `@path`. | 3 min | step_start Step=3; step_end |
| 4. **Conversation reminder** | Note: start fresh when gauge >60% or task switch; front-load context. | 1 min | step_start Step=4; step_end |
| 5. **Update this doc** | Add any new Cursor features or project-specific findings to §1–2. **Refresh [TOKEN_SAVING_PRACTICES.md](./TOKEN_SAVING_PRACTICES.md)** if new research or practices emerged (add to §1, cite source). Record whether rules/skills/settings/cache-index hygiene changed this run. If the broader LLM loop used a provider-specific API workflow, optionally record that via the relevant provider monitor. For the default local-first `Ollama` path, keep monitoring lightweight and prefer listener notes over heavy dashboards. | 2 min | step_start Step=5; step_end |
| 6. **Summary** | One-line note in IMPROVEMENT_LOOP_SUMMARY if run as part of main loop: "Token audit: always-apply 1 rule, ~X lines; no change / Y converted." | 1 min | step_start Step=6; step_end |
| **7. Organize results and recommend next tasks** | **Towards end.** Organize this run’s results (audit findings, snapshot vs previous, listener metrics). **Take all useful data from this run and from Step 0 research and add it to the todo:** Write or update [TOKEN_LOOP_NEXT_TASKS.md](./TOKEN_LOOP_NEXT_TASKS.md): add a dated section for this run (run_id, date) and a **Recommended TODO tasks for next token loop** list (3–6 concrete tasks drawn from research findings, what worked/didn't, and audit — e.g. "Convert rule X to glob", "Trim always-apply rule to &lt;50 lines", "Add search.exclude for Y", "Adopt practice Z from research"). **At the end of the run, give the user a short progress report** (see §4.4 below): rule output vs baseline, steps completed, key findings, and next TODOs. **Append one block to [TOKEN_LOOP_RUN_LEDGER.md](./TOKEN_LOOP_RUN_LEDGER.md)** (summary, rule output, snapshot link, steps completed, proof-of-quality evidence, and consistency score using [LOOP_CONSISTENCY_LEDGER_SNIPPET.md](./LOOP_CONSISTENCY_LEDGER_SNIPPET.md)). **Update [TOKEN_SAVING_PRACTICES.md](./TOKEN_SAVING_PRACTICES.md) §3:** append "What worked / What didn't" for this run (run_id, 1–2 bullets each); add any new practice to §1 if learned this run. Optionally update [TOKEN_LOOP_IMPROVEMENT_PLAN.md](./TOKEN_LOOP_IMPROVEMENT_PLAN.md) with a short "Rule output this run" vs baseline and any new blindspots. Next run reads NEXT_TASKS at Step 0. **Preferred closeout:** run `complete_token_loop_run.ps1` so shared-state writes succeed before `token_loop_end` is emitted. No human in the loop. | 5 min | step_start Step=7; step_end; `complete_token_loop_run.ps1` (preferred) or `token_loop_end` after manual closeout |

**At loop start:** `token_loop_start`. **At loop end:** prefer `complete_token_loop_run.ps1`; if closing manually, run `finish_loop_run.ps1` before `token_loop_end` with optional metrics (e.g. steps_completed: 8).

### 4.3 Integration with Improvement Loop

- **Optional Phase 0.6 (Token audit):** After Phase 0.3 (Cursor self-improvement), run steps 1–2 and 6 above (short audit + summary line). Full token loop (all steps) can run on demand or monthly.
- **Cursor self-improvement doc:** [CURSOR_SELF_IMPROVEMENT.md](./CURSOR_SELF_IMPROVEMENT.md) references this loop and optional Phase 0.6.

### 4.4 Progress report at end of run

At the end of each token loop run (after Step 7), give the user a **short progress report** so they see what was done and what's next. **Every report MUST include these three elements:**

1. **Loop #** — The run number (e.g. "Loop #5"). Compute as: count of `token_loop_start` events in `token_loop_events.jsonl` at the end of this run. Always show it first so the user knows which run they're reading.
2. **Proof of work** — What this run actually did: snapshot taken (run_id, path), steps 0–7 completed (Y/N), listener events recorded, ledger block appended, TOKEN_LOOP_NEXT_TASKS and TOKEN_SAVING_PRACTICES §3 updated. One or two lines; enough to verify the loop ran and artifacts exist.
3. **How we benefit** — Why this run matters: e.g. lower token spend from fewer/smaller always-apply rules, less context squish, rollback/comparison via snapshots, trend data in events for future runs, concrete next TODOs so the next loop can pick up where we left off.

Then include (as before):

- **Rule output this run vs baseline:** always_apply_count, always_apply_lines (target &lt;50).
- **Steps completed:** 0–7 and token_loop_end (Y/N).
- **Key findings:** One or two bullets from research (Step 0) or audit (e.g. new practice to adopt, drift from checklist).
- **Next TODOs:** 2–4 concrete tasks from the Recommended TODO list in TOKEN_LOOP_NEXT_TASKS for the next run.
- **Snapshot:** Link to this run's snapshot JSON (rollback/comparison).
- **Proof of quality:** Include liveness/readiness evidence and residual risk in one short bullet (see `QUALITY_STANDARDS_AND_PROOF.md`).
- **Loop consistency check:** Include pass/fail for the universal contract items and `Consistency score: X/10` (see `LOOP_CONSISTENCY_STANDARD.md`).

---

## 5. Copy-Paste Prompt for One-Off Audit

Use in Ask mode for a quick audit:

```text
Read all files in @.cursor/rules/ and estimate the token count for each rule. List them sorted by size, largest first. Identify which rules are set to alwaysApply: true and calculate the total token cost of always-applied rules. Suggest which rules could be changed from alwaysApply to glob-scoped or agent-decided without losing effectiveness.
```

---

## 7. Listener & Wiring

- **At every token loop start:** (1) **Record state** — run `.\scripts\automation\token_loop_state_snapshot.ps1 -RunId <run_id>`. Snapshot saved to `docs/automation/token_loop_snapshots/<run_id>.json` for rollback and progress tracking. Use the **same RunId** for all listener events this run; `run_token_loop.ps1` generates it. (2) **Listener** — `token_loop_start` then **steps 0–7** (step 0 = deep research and analysis; step 7 = organize results and recommend next tasks in TOKEN_LOOP_NEXT_TASKS.md), then `token_loop_end`. **No human in the loop.** We use this data to improve the loop.
- **Broader local-first entrypoint:** `.\scripts\automation\run_llm_loop.ps1` — permanent top-level entrypoint for the local-first LLM loop; preferred when the user says `start llm loop`. Default provider strategy lives in [LLM_LOOP.md](./LLM_LOOP.md).
- **Listener:** [TOKEN_LOOP_LISTENER.md](./TOKEN_LOOP_LISTENER.md) — script `scripts/automation/token_loop_listener.ps1`, output `docs/automation/token_loop_events.jsonl`.
- **Rule overhead compare:** `.\scripts\automation\compare_token_rule_overhead.ps1` — compares current `.cursor/rules` overhead to the latest token snapshot (or a specified snapshot path) and can write a JSON report.
- **Provider-specific API usage monitor:** `.\scripts\automation\monitor_openai_api_usage.ps1` — optional lightweight snapshot for OpenAI-backed loop work; writes compact JSON to `docs/automation/api_usage_snapshots/`. This is optional inside the broader LLM loop and is not needed for the default local `Ollama` path. See [OPENAI_API_USAGE_MONITORING.md](./OPENAI_API_USAGE_MONITORING.md).
- **Tests:** `.\scripts\automation\run_token_loop_tests.ps1` — run all token loop tests (listener + completion helper + rule-overhead compare + snapshot + events analysis). Or `.\scripts\automation\run_token_loop.ps1 -Test` to run tests before starting a loop.
- **Run script:** `.\scripts\automation\run_token_loop.ps1` — runs state snapshot + listener start (token_loop_start) and prints RunId; then run the 6 steps and close with `.\scripts\automation\complete_token_loop_run.ps1` (preferred) or the manual finish-wrapper + listener sequence.

---

## 8. Research & Analysis (token_loop_events.jsonl)

### What we record

- **Per run:** run_id (e.g. token-20250311-1900), token_loop_start, token_loop_end, optional metrics (steps_completed, always_apply_count, always_apply_lines, conversions_done). Steps 0–7 (step 0 = research; step 7 = organize and recommend next tasks).
- **Per step:** step_start / step_end with step number (0–7), note, and optional metrics.

**We use this data to improve the loop** — e.g. track whether we complete all steps, whether always-apply size trends down, when we last ran.

### Analysis ideas

| Question | How |
|----------|-----|
| How many token loops have we run? | Count lines where event == "token_loop_start" (or token_loop_end). |
| Are we completing all 8 steps? | For each run_id, count step_end events; compare to 8. |
| Is always-apply rule count/lines trending down? | From step_end metrics (step 1), plot always_apply_count / always_apply_lines over time. |
| When did we last run? | Max ts where event == "token_loop_end". |

**Known historical anomalies:** If the analysis finds old incomplete or orphan token run ids, record them in [TOKEN_LOOP_EVENT_ANOMALIES.md](./TOKEN_LOOP_EVENT_ANOMALIES.md) before considering any history repair.

### Example PowerShell one-liners

```powershell
# Count token loop runs (from repo root)
(Get-Content docs\automation\token_loop_events.jsonl | Where-Object { $_ -match '"event":"token_loop_start"' }).Count

# Last run end time
Get-Content docs\automation\token_loop_events.jsonl | ForEach-Object { ($_ | ConvertFrom-Json) } | Where-Object { $_.event -eq "token_loop_end" } | Sort-Object ts -Descending | Select-Object -First 1
```

### Research notes (for future passes)

- Cursor’s **Dynamic Context** reduces agent-exploration tokens; rule and conversation hygiene remain the main levers.
- **Always-apply rules:** ~500–1k tokens per 100 lines per request; keeping one short rule is low-cost.
- **Conversation length:** Resetting when gauge >60% or when switching tasks avoids history bloat.
- Re-run this section when Cursor changes context behavior or when we add new metrics.

---

## 9. What We Did (2025-03-11)

- Created this doc (research, strategies, checklist, loop steps).
- Added a short "Token awareness" block to the single always-apply rule (self-improvement.mdc) to remind the agent to be context-conscious without adding much length.
- Wired this loop into CURSOR_SELF_IMPROVEMENT.md as an optional Phase 0.6 and as a standalone "run token reduction loop" flow.
- **Listener:** Added token_loop_listener.ps1, test_token_loop_listener.ps1, TOKEN_LOOP_LISTENER.md, run_token_loop.ps1; events to token_loop_events.jsonl; research & analysis section (§8).

---

*Re-run the loop when Cursor changes context behavior or when usage suggests a need for another pass.*
