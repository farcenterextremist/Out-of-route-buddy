# Token Loop — Improvement Plan

**Purpose:** Single place for token-loop blindspot audit, context compression/embedding research, and **rule output baseline** (what it was, what it should be, how to compare to future runs). Read at Step 0; optionally update at Step 7 with "Rule output this run" vs baseline.

**References:** [TOKEN_REDUCTION_LOOP.md](./TOKEN_REDUCTION_LOOP.md), [TOKEN_LOOP_MASTER_PLAN.md](./TOKEN_LOOP_MASTER_PLAN.md), [TOKEN_LOOP_NEXT_TASKS.md](./TOKEN_LOOP_NEXT_TASKS.md), [TOKEN_SAVING_PRACTICES.md](./TOKEN_SAVING_PRACTICES.md), [IMPROVEMENT_LOOP_AUDIT.md](./IMPROVEMENT_LOOP_AUDIT.md)

**Last updated:** 2026-03-11

---

## 1. Blindspot audit (token-loop specific)

### Resolved / open

| Item | Status | Notes |
|------|--------|--------|
| Snapshot `always_apply_count` wrong | **Resolved** | Count and lines now derived only from deduped rules array; single-rule case fixed (was 3 due to hashtable .Count). |
| Listener `metrics_raw` | **Open** | Step_end metrics sometimes stored as escaped JSON in metrics_raw (PowerShell -Metrics). Fix: use single-quoted JSON or helper so listener parses `metrics` object. |
| Duplicate .cursor/rules paths | **Open** | Same .mdc can appear under `/` and `\`. Snapshot/Glob may double-count. Fix: dedupe by file name when building rules array. |

### Blind spots (watch for)

| Blind spot | Risk | Mitigation |
|------------|------|------------|
| Step 0 doesn't update Master Plan % | Completion % drifts from reality | At Step 7, optionally recalc and update TOKEN_LOOP_MASTER_PLAN completion %. |
| No token estimate per rule | Hard to trend "always-apply token cost" | Optional: add estimated_tokens (e.g. line_count * 10) to snapshot; target &lt;500 for always-apply. |
| Rule output not compared across runs | Can't see if we're improving | At Step 7, add "Rule output this run vs baseline" to NEXT_TASKS; optionally append to this doc. |
| Context compression/embedding not documented | Miss Cursor features that affect tokens | This doc §2 summarizes research; update when Cursor releases change behavior. |

### Referenced docs — existence check

| Doc / script | Exists? | Fallback if missing |
|--------------|---------|----------------------|
| TOKEN_REDUCTION_LOOP.md | Yes | Required |
| TOKEN_LOOP_NEXT_TASKS.md | Yes | Create stub; Step 7 appends here |
| TOKEN_SAVING_PRACTICES.md | Yes | Step 0 read; Step 7 update §3 (what worked/didn't) |
| TOKEN_LOOP_MASTER_PLAN.md | Yes | — |
| token_loop_state_snapshot.ps1 | Yes | Skip state record; note in NEXT_TASKS |
| token_loop_listener.ps1 | Yes | Skip events; run still proceeds |
| run_token_loop.ps1 | Yes | Invoke snapshot + listener manually with same RunId |

---

## 2. Research: context compression and embedding

### Context compression (Cursor)

- **Dynamic context discovery** (2025): Cursor loads less context upfront; the agent pulls what it needs. Long tool/MCP outputs are written to files so the agent can read selectively. When the context window fills, chat history can be summarized and referenced from files to recover detail. ([Cursor blog](https://cursor.com/blog/dynamic-context-discovery), [Developer Toolkit](https://developertoolkit.ai/en/cursor-ide/advanced-techniques/token-management/))
- **Practical limit:** Keep context under ~50k tokens per chunk where possible to maintain quality. ([Dre Dyson](https://dredyson.com/7-costly-context-limit-mistakes-in-cursor-ide-and-how-to-fix-them/))
- **Implication for our loop:** We control rule size and conversation hygiene; we can't control Cursor's internal compression, but we can minimize always-apply rules and avoid piling history.

### Embeddings

- **Cursor 1.2:** Improved embeddings for codebase search; semantic search uses them. ([Cursor.fan](https://cursor.fan/blog/2025/07/03/cursor-1-2-agent-planning-better-context-faster-tab))
- **Implication:** Better @-mention and codebase search reduce the need for huge static context. Rules and conversation still dominate controllable token cost; our audit (always-apply count/lines) remains the main lever.

### Actionable levers for our loop

- Minimize always-apply rule size (target &lt;50 lines; ~500 tokens).
- Use glob/description for all other rules.
- New chat when gauge &gt;60%; front-load only what's needed.
- **Update [TOKEN_SAVING_PRACTICES.md](./TOKEN_SAVING_PRACTICES.md) each run:** at start (read/refresh from research), at end (record what worked/didn't in §3; add new practices to §1 if learned).
- Optional: add estimated tokens per rule in snapshot (e.g. line_count × 10) for trend over runs.

---

## 3. Rule output: baseline, target, and future comparison

### Table

| Aspect | What it was (baseline) | What it should be (target) | How to compare (future runs) |
|--------|------------------------|----------------------------|------------------------------|
| **Snapshot** | `rules[]` with name, line_count, always_apply; top-level always_apply_count wrong (3), always_apply_lines 42 | always_apply_count from rules array only (count where always_apply === true); always_apply_lines &lt; 50; optional estimated_tokens_per_rule | Each run: snapshot JSON; diff vs previous or vs this baseline; note in NEXT_TASKS "Rule output this run vs baseline". |
| **Listener step_end** | Step 1/2 sometimes metrics in metrics_raw (escaped JSON) | Metrics as parsed `metrics` object (always_apply_count, always_apply_lines, conversions_done, steps_completed) | Parse token_loop_events.jsonl; check metrics vs metrics_raw; trend steps_completed and always_apply_lines. |
| **Rule set** | 1 always-apply (self-improvement.mdc, ~42–57 lines), 4 others glob/description | Same; no new always-apply; always-apply kept under 50 lines | Step 1 audit each run; record in Step 7 NEXT_TASKS; optional one-line "Rule output" in run block. |

### Fields to record each run (for comparison)

- **From snapshot (after fix):** `always_apply_count`, `always_apply_lines`, `rules[].name`, `rules[].line_count`, `rules[].always_apply`.
- **From listener step_end Step 1:** same metrics when parsed.
- **Optional:** Rough token estimate (e.g. always_apply_lines × 10); target &lt;500 tokens for always-apply.

### Baseline (run token-20260311-2031)

- always_apply_count (intended): **1**
- always_apply_lines: **42**
- always_apply rule: **self-improvement.mdc**
- Other rules: 2-hour-loop, blue-team, kotlin-best-practices, red-team (all alwaysApply: false).

---

## 4. Rule output this run (fill after each run)

*(Optional: at Step 7, append a short block below with this run's snapshot vs baseline.)*

**Run token-20260311-2115 (2026-03-11):**

- always_apply_count: **1** (baseline 1)
- always_apply_lines: **43** (baseline 42)
- Snapshot metrics parsed: **Y**
- Notes: Snapshot fix applied (count from rules array); listener step_end Step 1 used single-quoted metrics; improvement plan read at Step 0.

---

**Run ________ (date):**

- always_apply_count: ___ (baseline 1)
- always_apply_lines: ___ (baseline 42)
- Snapshot metrics parsed: Y/N
- Notes: ___

---

*Read at Step 0; update at Step 7 with "Rule output this run" when completed.*
