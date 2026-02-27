# Jarvey data quality score

Assessment of Jarvey’s **training and evaluation data** (instructions, context, scenarios, scorecard, evaluation). Use this score and the linked improvement prompt to raise the "Data quality score" and keep data files well organized.

---

## 1. Scope of “Jarvey data”

| Category | Files / locations |
|----------|--------------------|
| **Training (instructions)** | `docs/agents/coordinator-instructions.md` |
| **Training (context)** | `docs/agents/coordinator-project-context.md` |
| **Scenarios** | `docs/agents/data-sets/jarvey-scenarios/` (01–06, README, SCENARIO_RUN_RESULTS.md) |
| **Scorecard** | `docs/agents/data-sets/agent-aptitude-scorecard.md` (Email coordinator / Jarvey rows) |
| **Evaluation** | `docs/agents/data-sets/JARVEY_EVALUATION_REVIEW.md` |
| **Plans / index** | `docs/agents/JARVEY_INTELLIGENCE_PLAN.md`, `docs/agents/AGENT_APTITUDE_AND_SCORING.md` §3.1.1 |
| **Role data-set index** | `docs/agents/data-sets/README.md` — **includes Jarvey** ([jarvey.md](jarvey.md)) |
| **Jarvey data set** | `docs/agents/data-sets/jarvey.md` — consumes/produces, canonical lists, points to scenarios/scorecard/evaluation |

---

## 2. Data quality dimensions and scores (1–5)

| Dimension | Score | Notes |
|-----------|--------|--------|
| **Structure & organization** | 4 | Instructions vs context split clear; **jarvey.md** added in data-sets; **README lists Jarvey**. Training files remain under docs/agents/, scenarios/evaluation under docs/agents/data-sets/; jarvey.md provides a single index. |
| **Clarity & single-source-of-truth** | 4.5 | Single instructions and context; **canonical lists** in jarvey.md (general conversation vs roadmap-on-demand); **coordinator-project-context** refreshed for dynamic conversation (current state, recent changes, questions for user; roadmap only when user asks). |
| **Consistency** | 4 | Scenario format and naming (01–06) unchanged; **naming conventions** for future scenarios (07_, 08_, …) and section layout documented in jarvey-scenarios/README. Scorecard dimensions aligned with AGENT_APTITUDE_AND_SCORING. |
| **Completeness** | 2.5 | LLM scenario responses (1, 2, 4, 5, 6) still pending (run compose_reply when API/Ollama available); Jarvey scorecard rows still empty. **Process documented** in SCENARIO_RUN_RESULTS section 5. Improves when scenarios are run and scorecard filled. |
| **Discoverability & maintainability** | 4 | **Jarvey data and scripts (index)** in JARVEY_INTELLIGENCE_PLAN section 7; **When to re-score data quality** in section 8. data-sets/README lists Jarvey; jarvey.md points to all Jarvey assets. |

### Data access verification

**test_jarvey_data_access.py** verifies Jarvey can read data, comprehend it, and that files are in correct locations. Covers: location (10 tests), read (9 tests), comprehension (10 tests), context-loader integration (3 tests). See [JARVEY_DATA_ACCESS_TESTS.md](../JARVEY_DATA_ACCESS_TESTS.md).

### Data–model link verification

**test_data_model_link.py** verifies that quality data reaches the model's prompt. For each user query type (roadmap, recovery, entity lookup, recent changes), the test asserts the system prompt contains the expected content from ROADMAP.md, KNOWN_TRUTHS, project index, etc. Run: `python scripts/coordinator-email/test_data_model_link.py`. See [JARVEY_WIRING_PLAN.md](../JARVEY_WIRING_PLAN.md) §3.2.

### Bad data removal

Failed benchmark outputs (missing sign-off, code in no-code scenario, invented content, raw git hashes) are moved to `benchmark_output/removed/` and logged in **TRAINING_DATA_REMOVED.md**. Out-of-scope content (generic products, emulator-as-Jarvey conflation) is detected by `audit_jarvey_training_data.py` and logged. The removal log documents reasons for each exclusion; scoring considers whether the removal process is documented and consistent.

---

## 3. Overall data quality score

**Overall: 3.8 / 5** (updated after data quality plan implementation)

- **Strong:** jarvey.md and README index; canonical lists and context refreshed for dynamic conversation; Jarvey data and scripts index and re-score guidance in JARVEY_INTELLIGENCE_PLAN; scenario naming conventions documented.
- **Weak:** Scenario responses and scorecard rows still to be filled (manual step when LLM available); completeness will improve once those are run and recorded.

---

## 4. Detailed prompt for a data-quality improvement plan

Use the following prompt (e.g. in Cursor or a plan doc) to generate a concrete plan to **improve Jarvey’s Data quality score** and **keep data files well organized**.

---

**START OF PROMPT**

Create a detailed, actionable plan to improve **Jarvey’s data quality** and ensure **data files are well organized**. Use the data quality score in `docs/agents/data-sets/JARVEY_DATA_QUALITY_SCORE.md` and the existing Jarvey docs (evaluation, intelligence plan, scorecard, scenarios) as context.

**Context:**
- **Jarvey** is the email coordinator bot. Its “data” currently spans: (1) training: `docs/agents/coordinator-instructions.md`, `docs/agents/coordinator-project-context.md`; (2) evaluation/scenarios: `docs/agents/data-sets/jarvey-scenarios/`, `docs/agents/data-sets/JARVEY_EVALUATION_REVIEW.md`, `docs/agents/data-sets/agent-aptitude-scorecard.md`; (3) plans: `docs/agents/JARVEY_INTELLIGENCE_PLAN.md`, `docs/agents/AGENT_APTITUDE_AND_SCORING.md` §3.1.1.
- The **Data quality score** (see JARVEY_DATA_QUALITY_SCORE.md) is scored on: Structure & organization, Clarity & single-source-of-truth, Consistency, Completeness, Discoverability & maintainability. Current overall is 3.4/5; main gaps are: no Jarvey entry in data-sets index, training vs data-sets split, incomplete scenario responses and empty scorecard rows, no single “Jarvey data set” index.

**Requirements for the plan:**

1. **File organization and index**
   - Propose a clear, consistent place for all “Jarvey data” (training, scenarios, scorecard, evaluation). Options: (a) keep instructions/context in `docs/agents/` but add a single “Jarvey data set” file under `docs/agents/data-sets/` (e.g. `jarvey.md` or `coordinator-email.md`) that lists what Jarvey consumes and produces and points to coordinator-instructions, coordinator-project-context, jarvey-scenarios, scorecard, and evaluation; (b) or move/copy references so that data-sets is the single discoverable root for Jarvey data. Ensure `docs/agents/data-sets/README.md` is updated to include Jarvey (and coordinator-email) so the data-set index is complete.

2. **Single source of truth and clarity**
   - Propose how to avoid duplication and keep one canonical list of: key doc paths Jarvey should cite (ROADMAP, Known Truths, etc.), reply conventions (sign as Jarvey, acknowledge → answer → next steps), and where “Jarvey data” lives. Consider whether coordinator-project-context should remain the only injected context or if a short “Jarvey data set” summary is useful for delegation docs.

3. **Completeness of scenario and scorecard data**
   - Propose steps to: (a) run the five LLM scenarios (1, 2, 4, 5, 6) at least once with compose_reply (or listener), (b) paste or save the replies into the Response sections of the scenario files (or linked artifact files), (c) score each on Scope, Data set, Output, Handoff, Voice, (d) fill the “Email coordinator (Jarvey)” rows in `docs/agents/data-sets/agent-aptitude-scorecard.md`. Optionally add one “golden” example reply per scenario type (simple, semi-simple, multi-question, no-code, unclear) into coordinator-project-context or a separate examples file if within size limits.

4. **Consistency and naming**
   - Ensure scenario file names, section headers, and scorecard dimension names stay aligned with `docs/agents/AGENT_APTITUDE_AND_SCORING.md` §3.1.1 and JARVEY_INTELLIGENCE_PLAN. Propose any naming or layout conventions for future scenario files (e.g. 07_…) and for any new “Jarvey data” files.

5. **Discoverability and maintainability**
   - Propose a short “Jarvey brain” section or subsection in `docs/agents/JARVEY_INTELLIGENCE_PLAN.md` or in `docs/agents/data-sets/README.md` that lists every file that affects Jarvey (instructions, context, scenarios, scorecard, evaluation, compose_reply, listener, check_and_respond) and one-line purpose. So that “data quality” is measurable: define what “Data quality score” means operationally (e.g. re-run the five dimensions in JARVEY_DATA_QUALITY_SCORE.md after changes) and add a short “Data quality” subsection to the intelligence plan or evaluation review describing when to re-score.

6. **No regressions**
   - Ensure the plan does not break existing references: coordinator_listener and compose_reply load coordinator-instructions and coordinator-project-context from their current paths; any move or rename must update those references and tests.

**Deliverables:** The plan should list concrete tasks in implementation order, with file paths and, where relevant, function or script names. Do not implement the plan in this step—only produce the plan document so it can be reviewed and then executed. After execution, re-score data quality using the dimensions in JARVEY_DATA_QUALITY_SCORE.md and update the score in that file.

**END OF PROMPT**

---

Use the prompt above to generate a plan focused on data quality and file organization for Jarvey. After implementation, re-run the data quality dimensions and update the score in this file.
