# Jarvey Workflow Scoring Chart

A grading framework for the Jarvey email coordinator workflow. Use this to assess pipeline health, reply quality, and overall system performance.

**Related:** [JARVEY_ARCHITECTURE.md](JARVEY_ARCHITECTURE.md), [agent-aptitude-scorecard.md](data-sets/agent-aptitude-scorecard.md), [AGENT_APTITUDE_AND_SCORING.md](AGENT_APTITUDE_AND_SCORING.md).

---

## 1. Pipeline Stage Grades (1–5)

Each stage in the flow can be graded. Score 1–5 per stage; average for **Pipeline overall**.

| Stage | 1 (Poor) | 3 (Adequate) | 5 (Strong) | How to verify |
|-------|----------|---------------|------------|---------------|
| **Read** | IMAP fails; no messages read | Reads but misses some | Reads reliably; filters correctly | `health_check.py`; `read_replies.py` |
| **Strip** | Quoted/forwarded content included in reply | Partial strip; some leakage | Clean user text only; no "On ... wrote:" | Inspect `_strip_quoted_content` output |
| **Dedupe** | Duplicate replies sent | Occasional duplicates | No duplicates; state persisted | `diagnose_jarvey.py`; last_responded_state |
| **Cooldown** | No rate limit; spam risk | Partial cooldown | 2+ min between sends; configurable | Check cooldown logic in listener |
| **Template** | Wrong template; no match when expected | Some matches; confidence drift | Correct template for thanks/digest/priority | `test_check_and_respond.py` |
| **LLM** | Timeout; circuit open; no reply | Slow but completes | Completes; signs as Jarvey; grounded | `run_jarvey_benchmark.py`; latency logs |
| **Send** | SMTP fails; no delivery | Partial delivery | Reliable send; no bounces | `health_check.py`; test send |
| **State** | State lost; double-reply | Occasional state issues | State persisted; no regressions | last_responded_state.txt; dedupe |

---

## 2. Reply Quality Grades (1–5)

Score Jarvey's **output** using the five dimensions from [AGENT_APTITUDE_AND_SCORING.md](AGENT_APTITUDE_AND_SCORING.md). Apply to each scenario reply.

| Dimension | 1 (Poor) | 3 (Adequate) | 5 (Strong) |
|-----------|----------|---------------|------------|
| **Scope** | Wrote code; did other roles' work | Mostly in scope; minor drift | Strictly coordinator; assigns, delegates |
| **Data set** | No project context cited | Mentioned one doc | Cited ROADMAP, Known Truths, paths |
| **Output** | Vague, wrong, or unusable | Partially actionable | Concrete; answers each question |
| **Handoff** | No handoff when needed | Unclear handoff | Clear handoff to correct role |
| **Voice** | Generic; no sign-off | Some Jarvey tone | Clearly Jarvey; signs "— Jarvey" |

**Reply overall (per scenario):** Average of the 5 dimensions.

---

## 3. Benchmark Pass Rate

| Metric | Grade | Criteria |
|--------|-------|----------|
| **A** | 90–100% | 9–10 of 10 scenarios pass |
| **B** | 80–89% | 8 scenarios pass |
| **C** | 70–79% | 7 scenarios pass |
| **D** | 60–69% | 6 scenarios pass |
| **F** | &lt; 60% | 5 or fewer pass |

**Run:** `python scripts/coordinator-email/run_jarvey_benchmark.py`

---

## 4. Workflow Overall Grade

Combine pipeline, reply quality, and benchmark into a single grade:

| Component | Weight | Source |
|-----------|--------|--------|
| Pipeline overall | 25% | Average of 8 stage grades |
| Reply quality (avg of scenarios) | 35% | Score 2–3 test prompts (Simple, Semi-simple, Recent) |
| Benchmark pass rate | 40% | `run_jarvey_benchmark.py` result |

**Formula:**  
`Workflow grade = (Pipeline × 0.25) + (Reply_quality × 0.35) + (Benchmark_score × 0.40)`  
where Benchmark_score = pass_rate as 0–5 scale (e.g. 100% → 5, 80% → 4).

**Letter grade:**

| Numeric | Letter |
|---------|--------|
| 4.5–5.0 | A |
| 4.0–4.4 | B+ |
| 3.5–3.9 | B |
| 3.0–3.4 | C |
| 2.5–2.9 | D |
| &lt; 2.5 | F |

---

## 5. Scorecard Template

Copy and fill for each grading run.

### Run: _______________ (date)

**Pipeline stages (1–5 each):**

| Stage | Score | Notes |
|-------|-------|-------|
| Read | | |
| Strip | | |
| Dedupe | | |
| Cooldown | | |
| Template | | |
| LLM | | |
| Send | | |
| State | | |
| **Pipeline avg** | | |

**Reply quality (Simple + Semi-simple + Recent):**

| Prompt | Scope | Data set | Output | Handoff | Voice | Avg |
|--------|-------|----------|--------|---------|-------|-----|
| Simple | | | | | | |
| Semi-simple | | | | | | |
| Recent | | | | | | |
| **Reply quality avg** | | | | | | |

**Benchmark:** _____ / 10 pass → _____% → Grade _____

**Workflow overall:** _____ (numeric) → _____ (letter)

---

## 6. How to Run

| Task | Command |
|------|---------|
| Unit tests (template path) | `python scripts/coordinator-email/test_check_and_respond.py -v` |
| Full test suite | `python scripts/coordinator-email/run_all_jarvey_tests.py` |
| Benchmark (LLM required) | `python scripts/coordinator-email/run_jarvey_benchmark.py` |
| Benchmark (simulate, no LLM) | `python scripts/coordinator-email/run_jarvey_benchmark.py --simulate` |
| Health check | `python scripts/coordinator-email/health_check.py` |
| Diagnose (inbox, dedupe, cooldown) | `python scripts/coordinator-email/diagnose_jarvey.py` |

---

## 7. Interpretation

| Signal | Action |
|--------|--------|
| **Low Pipeline (Read/Send)** | Check IMAP/SMTP config; run `health_check.py` |
| **Low Pipeline (Strip/Dedupe)** | Review `_strip_quoted_content`; verify last_responded_state |
| **Low Pipeline (Cooldown)** | Set `JARVEY_COOLDOWN_SECONDS` in `.env`; see [WORKFLOW_IMPROVEMENT_PROOF.md](WORKFLOW_IMPROVEMENT_PROOF.md) |
| **Low Pipeline (LLM)** | Increase timeout; check circuit breaker; see [JARVEY_RESPONSE_TIME.md](JARVEY_RESPONSE_TIME.md) |
| **Low Reply quality (Scope)** | Tighten coordinator instructions; add no-code examples |
| **Low Reply quality (Data set)** | Add/update intent sources; inject project context |
| **Low Benchmark** | Run `--remove-failures`; review TRAINING_DATA_REMOVED.md; retrain |

**Proof of work:** See [WORKFLOW_IMPROVEMENT_PROOF.md](WORKFLOW_IMPROVEMENT_PROOF.md) for improvements applied, test results, and what worked.
