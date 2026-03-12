# Improvement Loop — Best Practices (For Other Agents Building Their Own Loops)

**Purpose:** Shareable guide for agents or teams building their own improvement loops. Follow these practices to get repeatable, safe, and measurable runs. OutOfRouteBuddy uses these; you can adopt or adapt them.

**Entry point for our loop:** [IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md](./IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md) — links here, our recorded data (run ledger), and key docs.

**Copy-paste prompts to give other agents:** [IMPROVEMENT_LOOP_PROMPT_FOR_OTHER_AGENTS.md](./IMPROVEMENT_LOOP_PROMPT_FOR_OTHER_AGENTS.md) — Option A (follow our loop), B (build your own), C (just record this run), D (one-liner).

---

## 1. Core principles

| Principle | Why |
|-----------|-----|
| **Checkpoint before any changes** | Enables "revert" if something breaks. One commit or tag; record it in the summary. |
| **Research first** | Read backlog, test health, security notes, and last run summary before picking tasks. Avoids rework and keeps scope clear. |
| **Tier tasks (e.g. Light / Medium / Heavy)** | Light + Medium can run autonomously; Heavy needs human approval. Prevents runaway scope. |
| **Reason before each change** | "What is my goal? What could go wrong? Is there a simpler option?" Reduces unintended side effects. |
| **Tests must pass** | If a change breaks tests, revert it. Do not leave tests red. |
| **Timebox per task** | e.g. ≤10 min per test fix or dead-code item. If stuck, document and defer. |
| **No unwarranted UI changes** | Respect user/design intent. When uncertain → suggest in summary, don't implement. |
| **One improvement per category per run (Kaizen)** | Avoid overload; small steps compound. |

---

## 2. Recommended structure (phases)

A simple 5-phase structure works well:

| Phase | Goal | Typical duration |
|-------|------|-------------------|
| **0** | Research + checkpoint + tiering | Before any edits |
| **1** | Quick wins (dead code, constants, security, smoothness) | 15–30 min |
| **2** | Test health + documentation | 15–30 min |
| **3** | Polish (strings, accessibility, one subtle UI or useful info) | 15–30 min |
| **4** | Lint + summary + **record run** | 10–15 min |

**Checkpoint** at the very start of Phase 0. **Record the run** at the end of Phase 4 (see § 4).

---

## 3. What to define upfront

- **Trigger** — e.g. "When user says GO" or a schedule. One clear entry point.
- **Required reads** — At least: common-sense rules, reasoning checkpoints, and user/design preferences. Read before Phase 0 research.
- **Backlog sources** — Where tasks come from (e.g. CRUCIAL, dead-code report, failing tests, security checklist). Read these in Phase 0.
- **Out of scope** — Explicit list of what the loop must not do (e.g. new features, Gradle migration, instrumented tests in headless env).
- **Summary format** — One canonical summary file per run (e.g. `IMPROVEMENT_LOOP_SUMMARY_<date>.md`) with: research note, what was done, metrics, suggested next steps, quality grade.

---

## 4. Recorded data — add every run

**Routine: append recorded data every time you run the Improvement Loop.**

### 4.1 Human-readable run ledger

Maintain a **run ledger** (e.g. `IMPROVEMENT_LOOP_RUN_LEDGER.md`) and **append one block per run** at the end of Phase 4. Each block should include:

- **Date** (and optionally time or run id)
- **Focus** (e.g. Security, Code Quality, UI/UX)
- **Variant** (e.g. Quick / Standard / Full) if you use variants
- **Summary link** (path to the full summary for this run)
- **Metrics one-liner** (e.g. tests pass/fail count, lint errors, files changed)
- **Checkpoint** (commit hash or tag for revert)
- **Grand progress bar one-liner** (if you use one: e.g. "7/10 green, 2 amber, 1 red")
- **Suggested next steps** (1–2 bullets or "see summary")

Example block:

```markdown
### Run 2025-03-15

- **Focus:** Security | **Variant:** Full
- **Summary:** [IMPROVEMENT_LOOP_SUMMARY_2025-03-15.md](./IMPROVEMENT_LOOP_SUMMARY_2025-03-15.md)
- **Metrics:** Tests 1021 passed, 6 skipped; lint 0 errors; 5 files changed; checkpoint `abc1234`
- **Progress bar:** 6/10 green, 3 amber, 1 red
- **Next:** Security PII grep; update GRAND_PROGRESS_BAR
```

### 4.2 Machine-readable events (optional)

If you have a listener script, append **events** to a JSONL file (e.g. `loop_events.jsonl`) at phase boundaries and on pulse:

- `loop_start` / `loop_end`
- `phase_start` / `phase_end` with phase number
- `pulse` with optional metrics (tests, lint)
- `metrics` snapshot

Each line = one JSON object with at least: `ts`, `event`, and optional `phase`, `note`, `metrics`, `run_id`. This allows later analysis (e.g. duration, pass rates over time).

### 4.3 Where we record

- **Run ledger:** [IMPROVEMENT_LOOP_RUN_LEDGER.md](./IMPROVEMENT_LOOP_RUN_LEDGER.md) — one section per run; append in Phase 4.3.
- **Events:** [loop_events.jsonl](./loop_events.jsonl) — appended by `loop_listener.ps1` at phase/pulse (see [LOOP_LISTENER.md](./LOOP_LISTENER.md)).

---

## 5. Best practices checklist (per run)

- [ ] Checkpoint created and recorded in summary.
- [ ] Research done (backlog, test health, security, last summary).
- [ ] Tasks classified (e.g. Light/Medium/Heavy); Heavy only with approval.
- [ ] Reasoning applied before each change; timebox respected.
- [ ] Tests pass after changes; revert if red.
- [ ] Summary written with metrics, next steps, quality grade.
- [ ] **Run ledger appended** with this run's block.
- [ ] (Optional) Grand progress bar or similar aggregate updated.

---

## 6. Links to our implementation

| Doc | What it is |
|-----|------------|
| [IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md](./IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md) | Entry point: best practices, recorded data, key docs. |
| [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md) | Our full phase routine. |
| [IMPROVEMENT_LOOP_COMMON_SENSE.md](./IMPROVEMENT_LOOP_COMMON_SENSE.md) | Non-negotiable rules we read at loop start. |
| [IMPROVEMENT_LOOP_REASONING.md](./IMPROVEMENT_LOOP_REASONING.md) | Reasoning checkpoints we use. |
| [LOOP_TIERING.md](./LOOP_TIERING.md) | How we define Light / Medium / Heavy. |
| [IMPROVEMENT_LOOP_RUN_LEDGER.md](./IMPROVEMENT_LOOP_RUN_LEDGER.md) | Our run ledger (recorded data per run). |
| [LOOP_LISTENER.md](./LOOP_LISTENER.md) | Our event recording (JSONL). |
| [docs/readiness/GRAND_PROGRESS_BAR.md](../readiness/GRAND_PROGRESS_BAR.md) | Our aggregate progress bar. |

---

*Other agents: use this as a template. Record every run in a ledger and optionally in JSONL so you can improve your loop over time.*
