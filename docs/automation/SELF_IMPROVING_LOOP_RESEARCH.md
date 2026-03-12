# Self-Improving Loop — Research & Methods

**Purpose:** High-level research on agent loop ideas, setups, and optimizations; concrete methods to make the Improvement Loop **self-improving**.  
**References:** [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md), [IMPROVEMENT_LOOP_REASONING.md](./IMPROVEMENT_LOOP_REASONING.md), [IMPROVEMENT_LOOP_RUN_LEDGER.md](./IMPROVEMENT_LOOP_RUN_LEDGER.md)

---

## 1. Research Summary: How Agent Loops Self-Improve

### 1.1 Retrospective & feedback loops

- **RetroAgent-style:** Hindsight self-reflection produces two kinds of feedback: (1) **numerical** (incremental progress vs prior attempts), (2) **language** (reusable lessons in memory). Retrieval balances relevance and exploration so past runs inform the next.
- **Reflexion:** Agents learn via **linguistic feedback** (no weight updates): reflect on task outcomes, store reflective text in episodic memory, use it in later trials.
- **Apply here:** Our loop already has “Reasoning (this run)” and “Suggested next steps” in the summary. Self-improvement = **explicitly read last run’s summary and reasoning** at Phase 0 and **update a small “lessons learned” store** (e.g. one file or a section in the summary template) so the next run avoids repeated mistakes and doubles down on what worked.

### 1.2 Single-improvement / one-fix-per-cycle rule

- **Nightly loop pattern:** Review logs → **pick exactly one high-impact improvement** → implement it → run quality audit → report. **One fix per night** avoids cascading regressions and makes cause-effect clear.
- **Compound effect:** 1% nightly improvement ≈ 37× annually; 2.5× in Q1. Constraint: fix must be specific, actionable, implementable in one session, and verifiable.
- **Apply here:** We already have **Kaizen rule (one improvement per category per loop)**. Strengthen by: (1) requiring “single highest-impact change” to be named in the summary, and (2) optionally a **one-fix-per-run** variant (e.g. “Quick” loop = one fix only) for maximum safety and traceability.

### 1.3 Generate → evaluate → revise

- **Agent loops:** Generate candidates → **evaluate** with feedback (tests, lint, human/LLM judge) → **revise** using that feedback → repeat. Test-time search replaces single-shot decisions.
- **Apply here:** We already “Do” then “Check” (tests, lint). Self-improvement = **formalize “evaluate” outputs** (e.g. test pass/fail, lint delta, “what broke”) and **require “revise”** in the same run when possible (revert + document), and **always** feed “what we learned from evaluate” into the next run’s research (last summary + reasoning).

### 1.4 Memory and context files

- **Compound learning:** Agents improve via **persistent memory**: AGENTS.md, SOUL.md, MEMORY.md, improvement-log.md. Conventions, style, and lessons from past runs are stored and read at start so the agent stops repeating mistakes.
- **Apply here:** We have USER_PREFERENCES_AND_DESIGN_INTENT, KNOWN_TRUTHS, HEAVY_IDEAS_FAVORITES, RUN_LEDGER. Self-improvement = add a **LOOP_LESSONS_LEARNED.md** (or a section in an existing doc) that each run **reads in Phase 0** and **appends to in Phase 4** with 1–3 bullets: “This run: we learned X; avoid Y; next run try Z.”

### 1.5 AI-augmented PDCA

- **Plan:** AI surfaces anomalies and improvement opportunities; humans validate strategy and ethics.
- **Do:** Agents execute within policies and risk boundaries.
- **Check:** Real-time monitoring + explainable “why it worked/failed”; side-effects flagged.
- **Act:** Living standards update via guardrails; improvements propagate.
- **Apply here:** Our loop is already PDCA (Phase 0 = Plan, 1–3 = Do, 4 = Check/Act). Self-improvement = (1) **explicit “Act” output**: update one shared artifact (e.g. LOOP_LESSONS_LEARNED, or “Next run focus” in ledger), (2) **reasoning section** explains “why it worked/failed” so Check is interpretable.

### 1.6 Human-guided, machine-executed

- **Principle:** Detection, experimentation, and iteration are machine-led; **governance, safety, and strategy** stay human-led.
- **Apply here:** We already gate Heavy tasks and UI changes. Self-improvement = keep and document this split: loop can self-improve **within** Light/Medium and within “suggest, don’t implement” for Heavy; any change to loop **structure** (e.g. new phases, new required files) should be proposed in the summary for user approval.

---

## 2. High-Level Setups That Support Self-Improvement

| Setup | What it is | How we use it / could use it |
|-------|------------|------------------------------|
| **Structured run ledger** | Chronological log of every run with focus, metrics, next steps | We have IMPROVEMENT_LOOP_RUN_LEDGER; each run appends. **Use:** Phase 0 reads last 1–2 ledger entries + last summary so “last run” drives “this run.” |
| **Last summary as input** | Next run’s Plan phase reads previous run’s summary and suggested next steps | Already in routine (Phase 0.1). **Strengthen:** Require one-line “Last run conclusion: …” in research note and one “We will not repeat: …” from reasoning. |
| **Lessons-learned file** | Single file or section updated every run with do’s/don’ts | **Add:** LOOP_LESSONS_LEARNED.md or a section in IMPROVEMENT_LOOP_REASONING.md. Phase 0 read; Phase 4 append (1–3 bullets). |
| **Single-improvement variant** | One fix per run; name it in summary | **Add:** “Quick” or “Single-fix” variant: one Light or one Medium task only; summary names “The one improvement this run: …” |
| **Quality grade + next improvement** | Every summary ends with grade and “one improvement for next run” | We have Quality Grade and “Next run improvement.” **Use:** Next run must read that line and optionally address it in research or task choice. |
| **Run metrics as feedback** | Tests, lint, files changed feed into “did we get better?” | We have metrics. **Add:** Optional “Metrics vs last run” (delta tests, delta lint) in summary so the loop can see trend. |

---

## 3. Concrete Methods to Make This Loop Self-Improving

### Method A: Lessons-learned file (low effort)

- **Add:** `docs/automation/LOOP_LESSONS_LEARNED.md` (or a section in IMPROVEMENT_LOOP_REASONING.md).
- **Phase 0:** Read it during Research. Note in research output: “Lessons applied: …”.
- **Phase 4:** Append 1–3 bullets: “YYYY-MM-DD: Learned …; Avoid …; Next run try ….”
- **Effect:** Next run avoids repeated mistakes and reinforces good patterns.

### Method B: “Last run” drives “this run” (already partial; tighten)

- **Phase 0:** Require explicit use of last summary: (1) “Last run did: …; suggested next: …”, (2) “This run we will not repeat: …” (from last run’s reasoning or failure).
- **Phase 4:** “Next run focus” and “Suggested next steps” stay; add “One thing next run must consider: …” (from this run’s reasoning).
- **Effect:** Continuity and fewer repeated skips or redundant work.

### Method C: Single highest-impact change (Kaizen++)

- **In summary:** Add a required line: “**Single highest-impact change this run:** [one sentence].” Even when multiple tasks were done, name the one that mattered most.
- **Optional “Single-fix” variant:** When user says “GO single-fix” or “GO quick,” run only one Light or one Medium task; summary centers on that one change and its verification.
- **Effect:** Clear cause-effect; compound learning; safe for nightly or high-cadence runs.

### Method D: Metrics delta (optional)

- **In summary:** If last run’s metrics are available, add “**Metrics vs last run:** Tests …; Lint …; Files ….” (e.g. “+2 tests, −1 lint warning”).
- **Effect:** Loop (and user) see trend; supports “are we getting better?”.

### Method E: Loop-routine improvement suggestions (meta)

- **In summary:** File Organizer or reasoning can add: “**Loop improvement suggestion:** …” (e.g. “Add Phase 0 read of LOOP_LESSONS_LEARNED” or “Reduce Phase 2 sandbox options to 1 per run”). No auto-change to routine; user approves.
- **Effect:** The loop itself can propose structural improvements safely.

### Method F: Run ledger as memory

- **Phase 0:** Read last 1–2 entries of IMPROVEMENT_LOOP_RUN_LEDGER + linked summary. Use “Next” bullets from last run as required input to task classification.
- **Effect:** Ledger becomes real memory for “what we said we’d do next.”

---

## 4. Recommended Order of Adoption

| Priority | Method | Effort | Impact |
|----------|--------|--------|--------|
| 1 | **B** — Last run drives this run (tighten) | Low | High |
| 2 | **A** — Lessons-learned file | Low | High |
| 3 | **F** — Ledger as memory (Phase 0 read last “Next”) | Low | Medium |
| 4 | **C** — Single highest-impact change in summary | Low | Medium |
| 5 | **D** — Metrics delta (optional) | Medium | Medium |
| 6 | **E** — Loop improvement suggestions (meta) | Low | Medium (long-term) |
| 7 | **Single-fix variant** (e.g. “GO quick” = one task) | Medium | High for safety/cadence |

---

## 5. Integration With Existing Docs

- **IMPROVEMENT_LOOP_ROUTINE.md:** Phase 0.1 “Research” can require: read last summary + last ledger “Next”; output “Last run: …; This run will not repeat: …”. Phase 4.3 summary template can require “Single highest-impact change this run” and “One thing next run must consider.”
- **IMPROVEMENT_LOOP_REASONING.md:** Add checkpoint “After research: What did last run miss? What will we do differently?” Optionally host “LOOP_LESSONS_LEARNED” as a section here instead of a new file.
- **IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md:** Add best practice: “Read last run summary and ledger ‘Next’ at start; append to lessons-learned at end.”
- **LOOP_TIERING.md / LOOP_VARIANTS.md:** Document “Single-fix” or “Quick” variant if adopted.

---

## 6. Sources (High-Level)

- RetroAgent / Reflexion: retrospective and linguistic feedback; memory buffers.
- Nightly loop / single-improvement rule: one fix per cycle; quality audit; compound effect.
- Generate → evaluate → revise: test-time search; feedback into next iteration.
- Kaizen Agent / AI-augmented PDCA: automate test/evaluate/improve; human governance.
- Compound learning: persistent context files (AGENTS.md, MEMORY.md) updated each run.
- Human-guided, machine-executed: loop improves within guardrails; structural changes proposed, not auto-applied.

---

*Use this doc to choose and implement self-improving methods; update as we adopt them.*
