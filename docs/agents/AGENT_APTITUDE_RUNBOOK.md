# Agent Aptitude Test — Runbook

Follow these 26 steps in order. For each step:
1. Invoke the agent in Cursor (with the role card).
2. Paste the prompt from the response file or from this runbook.
3. Paste the agent's reply into the **Response** section of the corresponding file in `docs/agents/data-sets/aptitude-responses/`.

---

## Step 1: Master Branch Coordinator — Simple

- **Role card:** `docs/agents/coordinator-instructions.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\01_coordinator_simple.md`

**Prompt:**

> We need to add a new 'Export trip to PDF' button on the trip history screen. Who should do what? Assign work.

---

## Step 2: Master Branch Coordinator — Semi Simple

- **Role card:** `docs/agents/coordinator-instructions.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\02_coordinator_semi_simple.md`

**Prompt:**

> The user asked: 'Should we support dark mode for the statistics calendar?' You don't know the answer. What do you do, and which role(s) do you involve?

---

## Step 3: Project Design / Creative Manager — Simple

- **Role card:** `docs/agents/roles/design-creative-manager.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\03_design_creative_simple.md`

**Prompt:**

> In one short paragraph, what is the product vision for OutOfRouteBuddy and who it's for?

---

## Step 4: Project Design / Creative Manager — Semi Simple

- **Role card:** `docs/agents/roles/design-creative-manager.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\04_design_creative_semi_simple.md`

**Prompt:**

> We have three ideas: (A) in-app reports, (B) fleet-wide dashboard, (C) better trip recovery. Prioritize them with a one-line reason each and say what you'd hand off to whom next.

---

## Step 5: UI/UX Specialist — Simple

- **Role card:** `docs/agents/roles/ui-ux-specialist.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\05_ui_ux_simple.md`

**Prompt:**

> Where should a 'Help & Info' entry point live in the app? Give one concrete recommendation and which file or screen you're referring to.

---

## Step 6: UI/UX Specialist — Semi Simple

- **Role card:** `docs/agents/roles/ui-ux-specialist.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\06_ui_ux_semi_simple.md`

**Prompt:**

> The statistics section feels cramped. Suggest two improvements (layout or copy) and say what you'd hand to the Front-end Engineer.

---

## Step 7: Front-end Engineer — Simple

- **Role card:** `docs/agents/roles/frontend-engineer.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\07_frontend_simple.md`

**Prompt:**

> Name the main layout file for the trip input screen and one Kotlin fragment that uses it.

---

## Step 8: Front-end Engineer — Semi Simple

- **Role card:** `docs/agents/roles/frontend-engineer.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\08_frontend_semi_simple.md`

**Prompt:**

> We want to add a 'Export to PDF' button on the trip history screen. What file(s) would you change and what would you need from the Back-end or ViewModel?

---

## Step 9: Back-end Engineer — Simple

- **Role card:** `docs/agents/roles/backend-engineer.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\09_backend_simple.md`

**Prompt:**

> Where is trip data persisted (e.g. which layer or component)? One sentence.

---

## Step 10: Back-end Engineer — Semi Simple

- **Role card:** `docs/agents/roles/backend-engineer.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\10_backend_semi_simple.md`

**Prompt:**

> We need to expose 'total trips this month' for the statistics screen. What would you add or change (repository, service, or domain) and which file(s)?

---

## Step 11: DevOps Engineer — Simple

- **Role card:** `docs/agents/roles/devops-engineer.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\11_devops_simple.md`

**Prompt:**

> How do we run unit tests from the command line on this project?

---

## Step 12: DevOps Engineer — Semi Simple

- **Role card:** `docs/agents/roles/devops-engineer.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\12_devops_semi_simple.md`

**Prompt:**

> We want to run unit tests in CI on every push. What would you use (e.g. Gradle task, script) and where is the config?

---

## Step 13: QA Engineer — Simple

- **Role card:** `docs/agents/roles/qa-engineer.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\13_qa_simple.md`

**Prompt:**

> Where are unit tests for the app located (directory path)? Name one test file.

---

## Step 14: QA Engineer — Semi Simple

- **Role card:** `docs/agents/roles/qa-engineer.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\14_qa_semi_simple.md`

**Prompt:**

> We added a new 'Export to PDF' button. What would you test (list 2–3 test ideas) and would you hand off anything to another role?

---

## Step 15: Security Specialist — Simple

- **Role card:** `docs/agents/roles/security-specialist.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\15_security_simple.md`

**Prompt:**

> Where are security notes or recommendations for this app documented?

---

## Step 16: Security Specialist — Semi Simple

- **Role card:** `docs/agents/roles/security-specialist.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\16_security_semi_simple.md`

**Prompt:**

> We're about to add export to PDF that writes to app cache and shares via FileProvider. What would you check and what doc would you update?

---

## Step 17: Email Editor / Market Guru — Simple

- **Role card:** `docs/agents/roles/email-editor-market-guru.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\17_email_editor_simple.md`

**Prompt:**

> Draft a one-line subject and one-sentence body for an email to the user: 'Statistics section refactor is done, please review.'

---

## Step 18: Email Editor / Market Guru — Semi Simple

- **Role card:** `docs/agents/roles/email-editor-market-guru.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\18_email_editor_semi_simple.md`

**Prompt:**

> The Coordinator wants to ask the user to choose between two feature priorities. Draft a short email (subject + 2–3 sentences) that the Human-in-the-Loop Manager could send.

---

## Step 19: File Organizer — Simple

- **Role card:** `docs/agents/roles/file-organizer.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\19_file_organizer_simple.md`

**Prompt:**

> Where do agent role definitions live in this repo (folder path)?

---

## Step 20: File Organizer — Semi Simple

- **Role card:** `docs/agents/roles/file-organizer.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\20_file_organizer_semi_simple.md`

**Prompt:**

> We have new docs for the Purple Team (Red/Blue). Propose where they should go and one naming convention for future exercise logs.

---

## Step 21: Human-in-the-Loop Manager — Simple

- **Role card:** `docs/agents/roles/human-in-the-loop-manager.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\21_human_in_the_loop_simple.md`

**Prompt:**

> What script do you use to send email to the user, and where is it?

---

## Step 22: Human-in-the-Loop Manager — Semi Simple

- **Role card:** `docs/agents/roles/human-in-the-loop-manager.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\22_human_in_the_loop_semi_simple.md`

**Prompt:**

> The Coordinator says: 'Email the user that the Help & Info dialog fix is deployed and ask them to confirm it looks good on their device.' Draft the subject and body you'd send (you don't have to run the script).

---

## Step 23: Red Team — Simple

- **Role card:** `docs/agents/roles/red-team-agent.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\23_red_team_simple.md`

**Prompt:**

> As Red Team Lead, in one sentence what is your main rule of engagement when attacking a target?

---

## Step 24: Red Team — Semi Simple

- **Role card:** `docs/agents/roles/red-team-agent.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\24_red_team_semi_simple.md`

**Prompt:**

> Simulate one attack idea for the trip export flow (e.g. who can trigger it, what data is exposed). Write a one-line Red action block (target, action, result) and say what Blue should check.

---

## Step 25: Blue Team — Simple

- **Role card:** `docs/agents/roles/blue-team-agent.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\25_blue_team_simple.md`

**Prompt:**

> After a Red Team action, what is the one question you must answer?

---

## Step 26: Blue Team — Semi Simple

- **Role card:** `docs/agents/roles/blue-team-agent.md`
- **Response file:** `docs\agents\data-sets\aptitude-responses\26_blue_team_semi_simple.md`

**Prompt:**

> Red just simulated 'user requested CSV export.' What would you check (whether the alarm went off) and what would you do if it didn't?

---

## Dated runs

To keep a history of runs and compare results over time:

1. Copy the scorecard template to a dated file:  
   `docs/agents/data-sets/agent-aptitude-scorecard-YYYY-MM-DD.md`
2. Fill scores (manually or via LLM-assisted scoring):  
   `python scripts/agent-aptitude/score_from_responses.py docs/agents/data-sets/agent-aptitude-scorecard-YYYY-MM-DD.md`  
   (Requires `OPENAI_API_KEY`. See `scripts/agent-aptitude/README.md` Step 3a.)
3. Generate a dated report:  
   `python scripts/agent-aptitude/report_from_scorecard.py docs/agents/data-sets/agent-aptitude-scorecard-YYYY-MM-DD.md -o docs/agents/data-sets/aptitude-training-priority-report-YYYY-MM-DD.md`

Each run then has a scorecard + report pair for comparison.
