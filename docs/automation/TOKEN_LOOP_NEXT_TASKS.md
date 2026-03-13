# Token Loop — Recommended Tasks for Next Run

**Purpose:** At the end of each token loop (Step 7), the agent organizes results and writes **recommended TODO tasks for the next token loop**. The next run reads this file during Step 0 (deep research) so it can prioritize and carry forward work. **Also append one block to [TOKEN_LOOP_RUN_LEDGER.md](./TOKEN_LOOP_RUN_LEDGER.md)** and update **[TOKEN_SAVING_PRACTICES.md](./TOKEN_SAVING_PRACTICES.md)** §3 (what worked / didn't) each run. **No human in the loop** — the agent updates this file autonomously.

---

## Run token-20260311-2305 (2026-03-11)

**Previous state (rollback):** Snapshot `docs/automation/token_loop_snapshots/token-20260311-2305.json`. Git HEAD at run start: `c1a65e637f06d6a3876f1f12bd873829fce61cda`.

**Rule output this run vs baseline:**
- **always_apply_count:** 2 (baseline 1) — self-improvement.mdc + data-separation.mdc. Target: 1.
- **always_apply_lines:** 53 (baseline 42; target &lt;50).
- **Progress report:** TOKEN_REDUCTION_LOOP §4.4 now requires every report to include **Loop #**, **proof of work**, and **how we benefit**.

**What worked:**
- Loop #5 completed; §4.4 updated so every run reports Loop #, proof of work, and benefits to the user.
- All steps 0–7 + token_loop_end; listener events recorded; ledger and NEXT_TASKS updated.

**What didn't / to fix:**
- Same as prior run: two always-apply rules; 53 lines &gt; 50. Convert data-separation to glob or description-only.

**Recommended TODO tasks for next token loop:**

- [ ] Convert data-separation.mdc to glob-scoped or agent-decided so only one rule is always-apply (self-improvement.mdc).
- [ ] Trim always-apply total to &lt;50 lines.
- [ ] Add .cursorignore for build, node_modules, generated if not present.
- [ ] **Every run:** Report to user with Loop #, proof of work, and how we benefit (now in §4.4).
- [ ] If Cursor releases new context/token features, add to TOKEN_REDUCTION_LOOP §1–2 and TOKEN_SAVING_PRACTICES §1.
- [ ] **Step 0 research (expanded scope):** Include **context compression**, **embedding/retrieval**, and **LLM optimization** in every run's research; see TOKEN_LOOP_IMPROVEMENT_PLAN §2 and TOKEN_SAVING_PRACTICES (context compression, embedding, LLM optimization sections). Our duties are not only token optimization but all levers that reduce token spend.

---

## Run token-20260311-2227 (2026-03-11)

**Previous state (rollback):** Snapshot `docs/automation/token_loop_snapshots/token-20260311-2227.json`. Git HEAD at run start: `446d0e31cb82ab6a2e8574c5ecad7c29bb8636fa`.

**Rule output this run vs baseline:**
- **always_apply_count:** 2 (baseline 1) — two rules have alwaysApply: true (self-improvement.mdc, data-separation.mdc). Target: 1.
- **always_apply_lines:** 53 (baseline 42; target &lt;50). Combined always-apply lines slightly over target.
- **Snapshot metrics:** Parsed correctly; listener step_end Step 1 sent metrics as parsed object.

**Research (Step 0):** 2025 practices — disable Memories unless needed; match model to task; audit always-apply rules; use dynamic context / let agent pull context; add .cursorignore for build/node_modules/generated; keep chunks under ~50k tokens; clear chat after milestones.

**What worked:**
- Full loop 0–7 + token_loop_end; progress report §4.4 added to TOKEN_REDUCTION_LOOP.
- Research at start and add-to-todo at end wired; Step 0 web research performed.
- Listener metrics recorded; snapshot and ledger updated.

**What didn’t / to fix:**
- Two always-apply rules (data-separation.mdc added); prefer one. Consider converting data-separation to glob or description-only.
- always_apply_lines 53 &gt; target 50; trim or split always-apply content.

**Recommended TODO tasks for next token loop:**

- [ ] Convert data-separation.mdc to glob-scoped or agent-decided (description only) so only one rule remains always-apply (self-improvement.mdc).
- [ ] Trim always-apply total to &lt;50 lines (e.g. self-improvement + any other; or merge/condense).
- [ ] Add .cursorignore for build, node_modules, generated code if not present (from 2025 research).
- [ ] Keep Step 0 research and Step 7 “add useful data to todo” + progress report each run.
- [ ] If Cursor releases new context/token features, add to TOKEN_REDUCTION_LOOP §1–2 and TOKEN_SAVING_PRACTICES §1.

---

## Before Launch 3 (next run)

- **At loop start (Step 0):** **Research popular, current, up-to-date token saving methods and best practices** (e.g. web search, Cursor/IDE docs). Read [TOKEN_SAVING_PRACTICES.md](./TOKEN_SAVING_PRACTICES.md) and [TOKEN_LOOP_IMPROVEMENT_PLAN.md](./TOKEN_LOOP_IMPROVEMENT_PLAN.md). Run `.\scripts\automation\run_token_loop.ps1` to get RunId; use same RunId for all listener events. Note if standard practices need refresh from latest research. **Optional:** Run `.\scripts\automation\run_token_loop_tests.ps1` (or `run_token_loop.ps1 -Test`) before starting the loop.
- **During run:** Steps 0–7; at Step 5 refresh TOKEN_SAVING_PRACTICES if new research; at Step 7: **take all useful data from the run and from Step 0 research and add it to the todo** — update NEXT_TASKS (this file) with a dated section and Recommended TODO list drawn from research + what worked/didn't; append TOKEN_LOOP_RUN_LEDGER; update TOKEN_SAVING_PRACTICES §3; optionally TOKEN_LOOP_IMPROVEMENT_PLAN §4 (rule output this run).
- **Ready:** Snapshot script fixed; improvement plan and practices doc wired; ledger template in place. Launch 3 = third full token loop run.

---

**Format:** Add a new dated section per run with run_id, date, and a bullet list of 3–6 concrete tasks. Example:

---

## Run token-20260311-2115 (2026-03-11)

**Previous state (rollback):** Snapshot `docs/automation/token_loop_snapshots/token-20260311-2115.json`. Run after implementing Token Loop Improvement Plan (TOKEN_LOOP_IMPROVEMENT_PLAN.md).

**Rule output this run vs baseline:**
- **always_apply_count:** 1 (baseline 1) — snapshot script fixed; count derived from rules array only.
- **always_apply_lines:** 43 (baseline 42) — within target &lt;50.
- **Snapshot metrics:** Parsed correctly in JSON; listener step_end Step 1 sent metrics as parsed object (single-quoted JSON).
- **Improvement plan:** Read at Step 0; Step 7 optional update documented.

**What worked:**
- TOKEN_LOOP_IMPROVEMENT_PLAN.md created (blindspots, context/embedding research, rule output baseline).
- token_loop_state_snapshot.ps1 fixed: always_apply_count and always_apply_lines derived only from deduped rules array; single-rule count no longer reported as 3.
- TOKEN_REDUCTION_LOOP Step 0 reads improvement plan; Step 7 optionally updates it.
- Master Plan linked to improvement plan; Phase 5 added (read improvement plan at Step 0).
- Listener -MetricsPath added; TOKEN_LOOP_LISTENER.md documents single-quote or -MetricsPath for valid metrics.
- All 8 steps (0–7) completed; token_loop_end with steps_completed=8.

**Recommended TODO tasks for next token loop:**

- [ ] Fix listener -Metrics in scripts/callers that pass double-quoted JSON so step_end consistently records parsed `metrics` (not metrics_raw).
- [ ] Deduplicate or normalize .cursor/rules paths so Glob/snapshot see one path per file (optional; snapshot now dedupes by name).
- [ ] Keep always-apply rule (self-improvement.mdc) under 50 lines; current ~43.
- [ ] At Step 7, optionally append "Rule output this run" to TOKEN_LOOP_IMPROVEMENT_PLAN §4 for trend over runs.
- [ ] If Cursor releases new context/token features, add to TOKEN_REDUCTION_LOOP §1–2 and TOKEN_LOOP_IMPROVEMENT_PLAN §2.

---

## Run token-20260311-2031 (2026-03-11)

**Previous state (rollback):** Snapshot `docs/automation/token_loop_snapshots/token-20260311-2031.json`. Git HEAD at run start: `79f3624b6b39f5ef344447624bde7d384986399a`.

**What worked:**
- State snapshot and listener fired correctly; RunId used for all events.
- One always-apply rule (self-improvement.mdc, ~42 lines); four other rules have alwaysApply: false (2-hour-loop, blue-team, kotlin-best-practices, red-team). Kotlin rule scoped to `app/**/*.kt`.
- Rules reference docs (e.g. CODEBASE_OVERVIEW, KNOWN_TRUTHS) — no large inlined blocks.
- .vscode/settings.json has files.watcherExclude and search.exclude for .gradle, build, .cursor.
- All 8 steps (0–7) completed; token_loop_end recorded.

**What didn’t / to fix:**
- Snapshot script reported `always_apply_count: 3` but only one rule has alwaysApply: true. Likely regex `alwaysApply:\s*true` matching elsewhere (e.g. in description or body). Fix: count only from rules array where always_apply === true.
- .cursor/rules has duplicate file paths (e.g. red-team.mdc and kotlin-best-practices.mdc appear under both `/` and `\`). Consider deduplicating or single path convention so snapshot and Glob don’t double-count.

**Recommended TODO tasks for next token loop:**

- [ ] Fix token_loop_state_snapshot.ps1: set always_apply_count from rules array (count where always_apply is true), not from regex match on full file content.
- [ ] Trim always-apply rule (self-improvement.mdc) to under 50 lines if it grows (currently ~57 content lines; snapshot reports 42).
- [ ] Deduplicate or normalize .cursor/rules paths (avoid same rule appearing as two files) so audit and snapshot count unique rules only.
- [ ] Add TOKEN_LOOP_NEXT_TASKS.md to Step 0 read list: next run should read this file during deep research and prioritize tasks from latest section.
- [ ] If Cursor releases new context/token features, add to TOKEN_REDUCTION_LOOP §1–2 and note in next run’s Step 5.

---

## Run token-YYYYMMdd-HHmm (YYYY-MM-DD)

**Recommended TODO tasks for next token loop:**

- [ ] Convert rule X to glob-scoped (e.g. `app/**/*.kt`).
- [ ] Trim always-apply rule (self-improvement.mdc) to under 50 lines.
- [ ] Add search.exclude for `**/generated/**` in .vscode/settings.json if present.
- [ ] Fix doc reference in rule Y: point to docs/ instead of inlining.
- [ ] Review token_loop_events.jsonl for runs that didn’t complete all 8 steps; add task to fix cause.

---

*(Sections above are filled in by each token loop run. Next run uses this list during Step 0.)*
