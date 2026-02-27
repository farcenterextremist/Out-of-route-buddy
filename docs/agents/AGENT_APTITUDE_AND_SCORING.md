# Agent Aptitude Test & Scoring System

This document defines **simple to semi-simple tests** for every agent (Coordinator + all roles + Red/Blue Team) so you can see which agents need **more training** (prompt/instructions) or **better data injection** (data-set files, paths, examples). Use the **scoring system** to record results and decide where to improve.

---

## 1. How to run the test

1. **Summon one agent at a time** (e.g. “Act as the Back-end Engineer; use docs/agents/roles/backend-engineer.md”).
2. **Paste the test prompt** for that agent (Simple, then in a separate run Semi-simple).
3. **Evaluate the response** using the five dimensions below; score each 1–5.
4. **Record scores** in the scorecard: `docs/agents/data-sets/agent-aptitude-scorecard.md` (or a copy).
5. **Interpret** using the guide in §4 to decide: training vs data injection.

You can run all agents in one session (rotate through prompts) or one agent per session. Keep the agent card and data set in context when scoring.

---

## 2. Scoring dimensions (1–5 each)

| Dimension | 1 (Poor) | 3 (Adequate) | 5 (Strong) |
|-----------|----------|----------------|------------|
| **Scope** | Did other roles’ work; ignored boundaries | Mostly in scope; minor drift | Strictly stayed in role; cited “out of scope” when needed |
| **Data set** | Did not reference data set or paths | Mentioned data set or one path | Cited data-set file and concrete paths/artifacts |
| **Output** | Vague, wrong, or unusable | Partially actionable | Concrete, actionable, correct for the role |
| **Handoff** | No handoff when needed; or wrong role | Handed off but unclear | Clear handoff to correct role with brief |
| **Voice** | Generic; could be any agent | Some role-specific language | Clearly in role (artifacts, terminology, constraints) |

**Overall score (per prompt):** Average of the 5 dimensions.  
**Agent overall:** Average of the two prompts (Simple + Semi-simple), or use the lower of the two to flag risk.

---

## 3. Test prompts by agent

### 3.1 Master Branch Coordinator

- **Simple:**  
  “We need to add a new ‘Export trip to PDF’ button on the trip history screen. Who should do what? Assign work.”
- **Semi-simple:**  
  “The user asked: ‘Should we support dark mode for the statistics calendar?’ You don’t know the answer. What do you do, and which role(s) do you involve?”

*Look for: correct assignment to Front-end/Back-end/UI/UX, handoff to Human-in-the-Loop when the user must decide.*

---

### 3.1.1 Email coordinator (Jarvey)

Score **Jarvey’s email replies** (the bot that reads inbox and replies as the coordinator). Use the same five dimensions; apply them to the *email reply* (Scope: stayed in coordinator role; Data set: cited project context/doc paths; Output: concrete, answered each question; Handoff: clear when HITL will email; Voice: Jarvey persona, signs as Jarvey).

- **Simple:** User email body: "What's next?"
- **Semi-simple:** User email body: "Can we prioritize the reports screen and when will it be done?"

**How to run:** Use the same system prompt and reference file as the listener. Either (1) run the listener, send the test user email to the coordinator inbox, wait for Jarvey’s reply, then score it; or (2) in a session, paste the user email as subject+body into a one-off compose (same prompt as `coordinator_listener.py`) and score the generated reply. Record scores in `docs/agents/data-sets/agent-aptitude-scorecard.md` in the "Email coordinator (Jarvey)" table.

---

### 3.2 Project Design / Creative Manager

- **Simple:**  
  “In one short paragraph, what is the product vision for OutOfRouteBuddy and who it’s for?”
- **Semi-simple:**  
  “We have three ideas: (A) in-app reports, (B) fleet-wide dashboard, (C) better trip recovery. Prioritize them with a one-line reason each and say what you’d hand off to whom next.”

*Look for: no code/UI detail; handoff to UI/UX or Coordinator; reference to roadmap/briefs.*

---

### 3.3 UI/UX Specialist

- **Simple:**  
  “Where should a ‘Help & Info’ entry point live in the app? Give one concrete recommendation and which file or screen you’re referring to.”
- **Semi-simple:**  
  “The statistics section feels cramped. Suggest two improvements (layout or copy) and say what you’d hand to the Front-end Engineer.”

*Look for: references to layouts/strings/docs/ux; no Kotlin; handoff to Front-end.*

---

### 3.4 Front-end Engineer

- **Simple:**  
  “Name the main layout file for the trip input screen and one Kotlin fragment that uses it.”
- **Semi-simple:**  
  “We want to add a ‘Export to PDF’ button on the trip history screen. What file(s) would you change and what would you need from the Back-end or ViewModel?”

*Look for: concrete paths (res/layout, presentation/); no repository or service logic; handoff to Back-end for data.*

---

### 3.5 Back-end Engineer

- **Simple:**  
  “Where is trip data persisted (e.g. which layer or component)? One sentence.”
- **Semi-simple:**  
  “We need to expose ‘total trips this month’ for the statistics screen. What would you add or change (repository, service, or domain) and which file(s)?”

*Look for: data/domain/services paths; no UI; handoff to Front-end for UI contract.*

---

### 3.6 DevOps Engineer

- **Simple:**  
  “How do we run unit tests from the command line on this project?”
- **Semi-simple:**  
  “We want to run unit tests in CI on every push. What would you use (e.g. Gradle task, script) and where is the config?”

*Look for: Gradle/scripts/CI paths; no test case design; handoff to QA for strategy.*

---

### 3.7 QA Engineer

- **Simple:**  
  “Where are unit tests for the app located (directory path)? Name one test file."
- **Semi-simple:**  
  “We added a new ‘Export to PDF’ button. What would you test (list 2–3 test ideas) and would you hand off anything to another role?”

*Look for: app/src/test/ or androidTest/; test strategy not implementation; handoff to Front-end/Back-end for bugs.*

---

### 3.8 Security Specialist

- **Simple:**  
  “Where are security notes or recommendations for this app documented?”
- **Semi-simple:**  
  “We’re about to add export to PDF that writes to app cache and shares via FileProvider. What would you check and what doc would you update?”

*Look for: docs/security/, manifest, no implementation; recommendations only.*

---

### 3.9 Email Editor / Market Guru

- **Simple:**  
  “Draft a one-line subject and one-sentence body for an email to the user: ‘Statistics section refactor is done, please review.’”
- **Semi-simple:**  
  “The Coordinator wants to ask the user to choose between two feature priorities. Draft a short email (subject + 2–3 sentences) that the Human-in-the-Loop Manager could send.”

*Look for: no send logic; clear copy; handoff to HITL for sending.*

---

### 3.10 File Organizer

- **Simple:**  
  “Where do agent role definitions live in this repo (folder path)?"
- **Semi-simple:**  
  “We have new docs for the Purple Team (Red/Blue). Propose where they should go and one naming convention for future exercise logs.”

*Look for: docs/ structure; no content authoring; handoff to Coordinator or user for approval.*

---

### 3.11 Human-in-the-Loop Manager

- **Simple:**  
  “What script do you use to send email to the user, and where is it?"
- **Semi-simple:**  
  “The Coordinator says: ‘Email the user that the Help & Info dialog fix is deployed and ask them to confirm it looks good on their device.’ Draft the subject and body you’d send (you don’t have to run the script).”

*Look for: scripts/coordinator-email/, send_email.py or README; draft only; no product decisions.*

---

### 3.12 Red Team

- **Simple:**  
  “As Red Team Lead, in one sentence what is your main rule of engagement when attacking a target?”
- **Semi-simple:**  
  “Simulate one attack idea for the trip export flow (e.g. who can trigger it, what data is exposed). Write a one-line Red action block (target, action, result) and say what Blue should check.”

*Look for: scope/no destruction; proof-of-work format; reference to purple-team-protocol or proof-of-work doc.*

---

### 3.13 Blue Team

- **Simple:**  
  “After a Red Team action, what is the one question you must answer?”
- **Semi-simple:**  
  “Red just simulated ‘user requested CSV export.’ What would you check (whether the alarm went off) and what would you do if it didn’t?”

*Look for: “Did the alarm go off?”; remediation; reference to proof-of-work or security-exercises.*

---

## 4. Interpretation: training vs data injection

Use the scorecard and the table below to decide next steps.

| Signal | Likely cause | Action |
|--------|----------------|--------|
| **Low Scope (1–2)** | Agent doesn’t know boundaries | **Training:** Tighten role card (out-of-scope list, handoffs). Add 1–2 negative examples in the agent card. |
| **Low Data set (1–2)** | Agent doesn’t know where to read/write | **Data injection:** Ensure data-set file exists and is linked from the role card. Add concrete paths and 1–2 example artifacts to the data set. |
| **Low Output (1–2)** | Vague or wrong answers | **Training + data:** Clarify expected artifact format in the role card; add a short “example output” in the data set. |
| **Low Handoff (1–2)** | Doesn’t hand off or wrong role | **Training:** In the role card, list “When to hand off to X” and one example handoff phrase. |
| **Low Voice (1–2)** | Sounds generic | **Training:** Add 2–3 role-specific phrases or artifact names to the agent card. |
| **Overall &lt; 3.5** or **any dimension ≤ 2** | Multiple gaps | Run the test again after improving the **lowest** dimension first (training or data injection), then re-score. |

**Data injection checklist (per role):**

- [ ] Role card references a data-set file (e.g. `docs/agents/data-sets/<role>.md`).
- [ ] Data-set file exists and lists **consumes** (paths, docs) and **produces** (artifacts, paths).
- [ ] At least one concrete path or filename is in the data set (so the agent can “cite” it).
- [ ] If the role is weak on “output format,” add one **example output** (e.g. a short Red action block, or an email subject/body template).

---

## 5. Scorecard template

Record scores in **`docs/agents/data-sets/agent-aptitude-scorecard.md`** (see that file). Copy the table for each run and fill in date and scores. Use “Agent overall” to rank agents and “Interpretation” to decide training vs data injection.

---

## Automation

Scripts and a runbook automate the flow so you can run all 26 prompts, save responses for reading, and produce a training priority report from the filled scorecard.

- **Runbook and response collection:** Run `python scripts/agent-aptitude/generate_response_collection.py` to generate 26 response files under `docs/agents/data-sets/aptitude-responses/` and the runbook at `docs/agents/AGENT_APTITUDE_RUNBOOK.md`. Follow the runbook to invoke each agent, paste the prompt, and paste the reply into the corresponding file.
- **Training priority report:** After filling the scorecard table, run `python scripts/agent-aptitude/report_from_scorecard.py [path_to_scorecard]` to write `docs/agents/data-sets/aptitude-training-priority-report.md` with ranked agents and recommended actions (training vs data injection).
- **Full workflow and file reference:** See **`scripts/agent-aptitude/README.md`** for step-by-step instructions, prerequisites, and where a future LLM API could plug in to auto-fill responses.

---

## 6. Quick reference: all agents

| Agent | Role card | Simple prompt (summary) | Semi-simple (summary) |
|-------|-----------|-------------------------|------------------------|
| Coordinator | coordinator-instructions.md | Assign “Export to PDF” button work | Handle “dark mode?” user question |
| Design/Creative | design-creative-manager.md | Product vision, one paragraph | Prioritize 3 features; handoffs |
| UI/UX | ui-ux-specialist.md | Where should Help & Info live? | Statistics section improvements |
| Front-end | frontend-engineer.md | Trip input layout + fragment name | Export PDF button: files + handoff |
| Back-end | backend-engineer.md | Where is trip data persisted? | “Total trips this month” – what to add |
| DevOps | devops-engineer.md | How to run unit tests | CI on every push |
| QA | qa-engineer.md | Where are unit tests? One file | Export PDF – test ideas + handoff |
| Security | security-specialist.md | Where are security notes? | FileProvider/export review |
| Email Editor | email-editor-market-guru.md | Draft email: statistics refactor done | Draft email: choose feature priority |
| File Organizer | file-organizer.md | Where do agent roles live? | Purple docs: where + naming |
| Human-in-the-Loop | human-in-the-loop-manager.md | Email script location | Draft email: Help & Info dialog fix |
| Red Team | red-team-agent.md | Rule of engagement, one sentence | One attack idea + Red block |
| Blue Team | blue-team-agent.md | One question after Red action | Check export “alarm”; if missed, what to do |

---

*To summon all agents and run the test: invoke each agent by name with its role card, paste the prompt from §3, then score using §2 and record in the scorecard.*
