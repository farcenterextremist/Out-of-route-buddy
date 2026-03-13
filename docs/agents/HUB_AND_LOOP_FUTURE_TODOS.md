# Hub, Loop Master, and Universal Loop Prompt — Future TODOs

**Purpose:** Tasks derived from what was built for the Hub, Loop Master “Read the Hub,” Universal Loop Prompt, sandboxing fixes, and full axis compilation. Use for Improvement Loop suggested next steps, File Organizer backlog, or agent onboarding.

**Source:** Sandboxing update, Hub creation, “send to hub” (text command), full axis doc, Universal Loop Prompt, LOOP_MASTER Step 0.M Hub read, IMPROVEMENT_LOOP_FOR_OTHER_AGENTS Hub-at-start.  
**Last updated:** 2026-03-11

---

## Hub (docs/agents/data-sets/hub/)

- [ ] Verify every agent/role that runs a loop has UNIVERSAL_LOOP_PROMPT in context or linked from their entry doc.
- [ ] Add “Hub consulted” and “Advice/rules applied” to LOOP_METRICS_TEMPLATE or IMPROVEMENT_LOOP_SUMMARY template so every run records them.
- [ ] Create HUB_INDEX.md (or equivalent) if hub/README.md index table exceeds ~25 rows for easier scanning.
- [ ] Run a “Hub health” check: ensure every row in hub/README index has a corresponding file in hub/; remove orphan rows or add missing files.
- [ ] Document Hub naming convention (`YYYY-MM-DD_<role-or-topic>_<short-description>.<ext>`) in DATA_SETS_AND_DELEGATION_PLAN or data-sets README.
- [ ] Add Hub path and “send to hub” to Help & Info or in-app docs when the text-command feature is implemented (FUTURE_IDEAS § 12.1).
- [ ] Cross-link UNIVERSAL_LOOP_PROMPT from .cursor/rules (e.g. 2-hour-loop.mdc or self-improvement.mdc) so GO / loop runs see it.
- [ ] Add a “last Hub sync” or “last Loop Master run” date to hub/README or to Loop Master findings in the summary.
- [ ] Ensure SEND_TO_HUB_PROMPT is linked from every agent-facing doc that mentions “send to hub” (data-sets README, UNIVERSAL_LOOP_PROMPT, LOOP_MASTER).
- [ ] Add optional “deposit to hub” step to Token Loop Step 7 (loop report) and Cyber Security Loop Phase 3 (proof of work / data summary) so they routinely land in hub/.

---

## Loop Master (Step 0.M, LOOP_MASTER_ROLE.md)

- [ ] Add “Read the Hub” to IMPROVEMENT_LOOP_ROUTINE trigger description when trigger is “start master loop” (so agents see it in the main routine).
- [ ] Run one full Master Loop and verify Step 0.M includes: Read Hub → Research loops → Compare/analyze → Set standard → Update universal files → Run Improvement Loop.
- [ ] Add “Hub consulted” and “Advice/rules applied” to the optional “Loop Master findings” subsection in the run summary template.
- [ ] Ensure LOOP_MASTER_ROLE “Universal files” table includes a row for Hub / UNIVERSAL_LOOP_PROMPT if we want Loop Master to keep that doc current.
- [ ] If Synthetic Data Loop has a “master” or research step, add “Read the Hub” there and link to UNIVERSAL_LOOP_PROMPT.

---

## Universal Loop Prompt & anti-slop

- [ ] Add “Minimize slop” and “Critique before deposit” to IMPROVEMENT_LOOP_BEST_PRACTICES.md so it’s in the shared best-practices checklist.
- [ ] Add one bullet to IMPROVEMENT_LOOP_FOR_OTHER_AGENTS “Best practices”: output must be specific, traceable, actionable; avoid generic filler; use critiquing skills if available.
- [ ] When user installs critiquing/slop-minimization skills, add their names or triggers to UNIVERSAL_LOOP_PROMPT § 3 (“Use your critiquing skills”).
- [ ] Document in hub/README or UNIVERSAL_LOOP_PROMPT which critiquing/slop-minimization skills or tools are available and when to invoke them.
- [ ] Add a one-line “Anti-slop checklist” to the summary template: e.g. “Output actionable? References real artifacts? Reusable without guessing?”

---

## Sandboxing (SANDBOX_*, HEAVY_IDEAS_FAVORITES)

- [ ] Fix IMPROVEMENT_LOOP_INDEX § 4: change HEAVY_TIER_IDEAS.md to HEAVY_IDEAS_FAVORITES.md (correct file name).
- [ ] Audit LOOP_TIERING.md for any remaining HEAVY_TIER_IDEAS or HEAVY_TIER_IDEAS.md references; replace with HEAVY_IDEAS_FAVORITES.
- [ ] Run one Medium-tier Improvement Loop and perform one sandbox action that uses SANDBOX_COMPLETION_PERCENTAGE (advance % for 1–2 Heavy ideas).
- [ ] Track completion % for 1–2 Heavy ideas in HEAVY_IDEAS_FAVORITES or a separate tracker (e.g. a small table in SANDBOX_COMPLETION_PERCENTAGE or a dated file in hub/).
- [ ] Ensure Phase 2.3 “improve 1–2 sandboxed ideas” is reported in the summary as “improved N ideas” or “advanced § X.Y to ~N%.”

---

## Full axis & cross-loop

- [ ] Add Hub and “send to hub” to IMPROVEMENT_LOOP_INDEX § 9 “Related outside docs/automation/” or a new “Hub & data” row.
- [ ] Add LOOPS_AND_IMPROVEMENT_FULL_AXIS.md to IMPROVEMENT_LOOP_INDEX as a “Full axis / all loops” reference.
- [ ] In LOOPS_AND_IMPROVEMENT_FULL_AXIS § 7 “Cross-loop dependencies,” add: “All loops → Hub (deposit polished output when applicable).”
- [ ] Add “Before any loop: (1) LOOP_MASTER_ROLE (2) Hub README + index (3) Note Hub consulted & advice applied” to IMPROVEMENT_LOOP_FOR_OTHER_AGENTS or UNIVERSAL_LOOP_PROMPT short form.
- [ ] Ensure Token Loop Step 0 or “at start” references reading Hub (hub/README + relevant reports) and noting advice applied; add to TOKEN_REDUCTION_LOOP if missing.
- [ ] Ensure Cyber Security Loop Phase 0 or “at start” references reading Hub (e.g. cyber-security proof of work, data-summary) and noting advice applied; add to CYBER_SECURITY_LOOP_ROUTINE if missing.
- [ ] Ensure Synthetic Data Loop “at start” references reading Hub and UNIVERSAL_LOOP_PROMPT; add to SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS if missing.
- [ ] Add Hub to BOARD_MEETING_PLAN or team-parameters so roles know they can deposit meeting summaries or one-pagers to hub/.

---

## Improvement Loop & ledger

- [ ] Run one Improvement Loop (GO or Light+Medium) and verify “Hub consulted” and “Advice/rules applied” appear in research note or summary.
- [ ] Append one run to IMPROVEMENT_LOOP_RUN_LEDGER and include one line: “Hub: [files or roles consulted]; advice: [1–2 bullets].”
- [ ] Add “Hub consulted” and “Advice/rules applied” to the ledger block template in IMPROVEMENT_LOOP_RUN_LEDGER.md (optional fields).
- [ ] Ensure IMPROVEMENT_LOOP_BEST_PRACTICES “what to record every run” includes Hub read and advice applied when applicable.

---

## Documentation & discoverability

- [ ] Add docs/agents/HUB_AND_LOOP_FUTURE_TODOS.md to TASKS_INDEX.md “Where tasks live” table.
- [ ] Add docs/agents/HUB_AND_LOOP_FUTURE_TODOS.md to docs/README.md or docs/agents/README.md if present.
- [ ] Link UNIVERSAL_LOOP_PROMPT from AGENTS.md “Improvement Loop” or “Agent entry” section.
- [ ] Add a “Hub & Loop system” subsection to COMPREHENSIVE_AGENT_TODOS “Quick reference” or a new section that points to this file and UNIVERSAL_LOOP_PROMPT.
- [ ] Ensure CRUCIAL_IMPROVEMENTS_TODO summary table row for “Hub & Loop system” points to this file (HUB_AND_LOOP_FUTURE_TODOS.md) and notes ~50 TODOs.

---

## Optional / later

- [ ] **From today's build (2026-03-11):** See [HUB_AND_LOOP_TODOS_FROM_2026-03-11.md](HUB_AND_LOOP_TODOS_FROM_2026-03-11.md) for 37 additional TODOs (send-to-hub wiring, data-sets index, Universal Loop Prompt, scripts, maturity).
- [ ] Add .cursorignore rules for hub/ if we ever want to exclude large or transient artifacts from context. if we ever want to exclude large or transient artifacts from context.
- [ ] Create a one-page “Loop Master + Hub cheat sheet” for quick paste into new chats (trigger, Step 0.M, Hub path, UNIVERSAL_LOOP_PROMPT short form).
- [ ] Consider a small script or checklist that validates: LOOP_MASTER Step 0.M includes Hub; IMPROVEMENT_LOOP_FOR_OTHER_AGENTS “upon initiation” includes Hub; UNIVERSAL_LOOP_PROMPT exists and is linked from hub/README.
- [ ] When in-app “send to hub” (FUTURE_IDEAS § 12.1) is implemented, add a todo: “Document in-app text command in Help & Info and link to hub path.”
- [ ] Add “Hub” to the list of “other loops in this project” in IMPROVEMENT_LOOP_FOR_OTHER_AGENTS if we treat Hub as a shared output loop.
- [ ] Verify docs/automation/README.md “Start here” includes LOOPS_AND_IMPROVEMENT_FULL_AXIS and that it mentions Hub.
- [ ] Add one “Hub & Universal Loop Prompt” bullet to IMPROVEMENT_LOOP_WIZARD or 2-hour-loop rule so GO runs see “read Hub at start.”
- [ ] Ensure DATA_SETS_AND_DELEGATION_PLAN or data-sets README mentions that finished outputs go to hub/ and that UNIVERSAL_LOOP_PROMPT applies to all loop runs.

---

*Use this list for Improvement Loop suggested next steps, File Organizer backlog, or onboarding. Check off as done and update source docs (e.g. IMPROVEMENT_LOOP_INDEX, LOOP_TIERING) when references are fixed.*
