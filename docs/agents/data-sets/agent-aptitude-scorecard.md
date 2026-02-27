# Agent Aptitude Scorecard

Use this scorecard to record results when running the **Agent Aptitude Test** (see `docs/agents/AGENT_APTITUDE_AND_SCORING.md`). Copy the table below for each run and fill in the date and scores (1–5 per dimension). Then compute **Overall (prompt)** and **Agent overall** to see which agents need more training or data injection.

---

## Scoring reminder (1–5)

| Dimension | What to score |
|-----------|----------------|
| **Scope** | Stayed in role vs did other roles’ work |
| **Data set** | Did not use vs cited data-set paths/artifacts |
| **Output** | Vague/wrong vs concrete and actionable |
| **Handoff** | Missing/wrong vs correct handoff when needed |
| **Voice** | Generic vs clearly in role |

---

## Run: 2025-03-01 (date)

Fill in one row per agent. For each agent you run **two prompts** (Simple, Semi-simple). Record each prompt as a separate row or average the two into one row.

| Agent | Prompt | Scope | Data set | Output | Handoff | Voice | **Overall (avg)** |
|-------|--------|-------|----------|--------|---------|-------|-------------------|
| Coordinator | Simple | 5 | 5 | 5 | 4 | 5 | 4.80 |
| Coordinator | Semi-simple | 5 | 5 | 5 | 5 | 5 | 5.00 |
| Design/Creative | Simple | 5 | 5 | 5 | 4 | 5 | 4.80 |
| Design/Creative | Semi-simple | 5 | 5 | 5 | 5 | 5 | 5.00 |
| UI/UX Specialist | Simple | 5 | 5 | 5 | 5 | 5 | 5.00 |
| UI/UX Specialist | Semi-simple | 5 | 5 | 5 | 5 | 5 | 5.00 |
| Front-end Engineer | Simple | 5 | 5 | 5 | 4 | 5 | 4.80 |
| Front-end Engineer | Semi-simple | 5 | 5 | 5 | 5 | 5 | 5.00 |
| Back-end Engineer | Simple | 5 | 5 | 5 | 5 | 5 | 5.00 |
| Back-end Engineer | Semi-simple | 5 | 5 | 5 | 5 | 5 | 5.00 |
| DevOps Engineer | Simple | 5 | 5 | 5 | 5 | 5 | 5.00 |
| DevOps Engineer | Semi-simple | 5 | 5 | 5 | 5 | 5 | 5.00 |
| QA Engineer | Simple | 5 | 5 | 5 | 5 | 5 | 5.00 |
| QA Engineer | Semi-simple | 5 | 5 | 5 | 5 | 5 | 5.00 |
| Security Specialist | Simple | 5 | 5 | 5 | 4 | 5 | 4.80 |
| Security Specialist | Semi-simple | 5 | 5 | 5 | 4 | 5 | 4.80 |
| Email Editor / Market Guru | Simple | 5 | 4 | 5 | 5 | 5 | 4.80 |
| Email Editor / Market Guru | Semi-simple | 5 | 4 | 5 | 5 | 5 | 4.80 |
| File Organizer | Simple | 5 | 5 | 5 | 5 | 5 | 5.00 |
| File Organizer | Semi-simple | 5 | 5 | 5 | 5 | 5 | 5.00 |
| Human-in-the-Loop Manager | Simple | 5 | 5 | 5 | 4 | 5 | 4.80 |
| Human-in-the-Loop Manager | Semi-simple | 5 | 5 | 5 | 4 | 5 | 4.80 |
| Red Team | Simple | 5 | 5 | 5 | 4 | 5 | 4.80 |
| Red Team | Semi-simple | 5 | 5 | 5 | 5 | 5 | 5.00 |
| Blue Team | Simple | 5 | 5 | 5 | 4 | 5 | 4.80 |
| Blue Team | Semi-simple | 5 | 5 | 5 | 5 | 5 | 5.00 |

---

## Email coordinator (Jarvey) — email reply scoring

Score **Jarvey’s email replies** using the same 1–5 dimensions. Test by sending (or simulating) a user email, then run the listener (or one-off compose) and score the reply.

| Dimension | What to score for email replies |
|-----------|----------------------------------|
| **Scope** | Stays in coordinator role (assigns work, delegates, consults user); does not write code or do other roles’ work. |
| **Data set** | Cites project context / doc paths (ROADMAP, Known Truths, delegation) when relevant. |
| **Output** | Concrete, actionable; directly answers each question; next steps or owner clear. |
| **Handoff** | When the user must decide or another role must act, handoff is clear (e.g. "Human-in-the-Loop will email you"). |
| **Voice** | Clearly Jarvey: project-aware, consistent tone, signs as Jarvey. |

**Test prompts (user email body):**

- **Simple:** "What's next?"
- **Semi-simple:** "Can we prioritize the reports screen and when will it be done?"
- **Recent:** "Tell me recent project changes" (benchmark scenario 10)

| Agent | Prompt | Scope | Data set | Output | Handoff | Voice | Grounding | Hallucination | **Overall (avg)** |
|-------|--------|-------|----------|--------|---------|-------|-----------|---------------|-------------------|
| Email coordinator (Jarvey) | Simple | 5 | 5 | 5 | 5 | 5 | — | — | 5.0 |
| Email coordinator (Jarvey) | Semi-simple | 5 | 5 | 5 | 5 | 5 | — | — | 5.0 |
| Email coordinator (Jarvey) | Recent | — | — | — | — | — | — | — | _Re-score after grounding/hallucination fix_ |

---

## Agent overall (optional)

For each agent, average the two prompt overalls to get **Agent overall**. Use this to rank agents and pick which to improve first.

| Agent | Agent overall (avg of 2 prompts) | Lowest dimension | Interpretation (training vs data) |
|-------|----------------------------------|------------------|------------------------------------|
| Coordinator | 4.90 | Handoff (4) | Optional refinement. |
| Design/Creative | 4.90 | Handoff (4) | Optional refinement. |
| UI/UX Specialist | 5.00 | — | No critical gap; optional refinement. |
| Front-end Engineer | 4.90 | Handoff (4) | Optional refinement. |
| Back-end Engineer | 5.00 | — | No critical gap; optional refinement. |
| DevOps Engineer | 5.00 | — | No critical gap; optional refinement. |
| QA Engineer | 5.00 | — | No critical gap; optional refinement. |
| Security Specialist | 4.80 | Handoff (4) | Optional refinement. |
| Email Editor / Market Guru | 4.80 | Data set (4) | Optional refinement. |
| File Organizer | 5.00 | — | No critical gap; optional refinement. |
| Human-in-the-Loop Manager | 4.80 | Handoff (4) | Optional refinement. |
| Red Team | 4.90 | Handoff (4) | Optional refinement. |
| Blue Team | 4.90 | Handoff (4) | Optional refinement. |

---

## Interpretation (from AGENT_APTITUDE_AND_SCORING.md)

- **Scope 1–2** → Training: tighten role card, out-of-scope list.
- **Data set 1–2** → Data injection: add/update data-set file, concrete paths.
- **Output 1–2** → Training + data: clarify artifact format, example output.
- **Handoff 1–2** → Training: “When to hand off to X” + example in role card.
- **Voice 1–2** → Training: role-specific phrases in agent card.
- **Overall < 3.5 or any ≤ 2** → Improve lowest dimension first, then re-test.
