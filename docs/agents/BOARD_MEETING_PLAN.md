# Board Meeting — Plan and call script

**Purpose:** A structured **call script program** that summons every agent one by one in a “grand symphony,” so the full team contributes opinions based on best practice and Single Source of Truth, recommends ideas, and gives updates (e.g. weekly progress). We call this program **Board Meeting**.

---

## Vision

- **One meeting, every voice.** Coordinator chairs; each role is “called” in sequence with full training context (role card, data set, Known Truths, roundtable).
- **Aligned with SSOT.** All opinions and recommendations reference `KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md` and existing technical docs so nothing contradicts the codebase.
- **Artifacts.** Each run produces a **Board Meeting transcript** (and optionally a **summary** or **weekly progress report**) that can be saved, emailed via Human-in-the-Loop, or used as input to the next meeting.

---

## Roles and order (the symphony)

Suggested order so reports flow logically and handoffs make sense:

| # | Role | Role card | Data set | Notes |
|---|------|-----------|----------|--------|
| 0 | **Coordinator** | `coordinator-instructions.md` | — | Chairs: sets agenda, calls the meeting, can inject optional agenda doc. |
| 1 | Project Design / Creative Manager | `roles/design-creative-manager.md` | `data-sets/design-creative.md` | Vision, roadmap, priorities. |
| 2 | UI/UX Specialist | `roles/ui-ux-specialist.md` | `data-sets/ui-ux.md` | Screens, flows, accessibility. |
| 3 | Front-end Engineer | `roles/frontend-engineer.md` | `data-sets/frontend.md` | Android UI, fragments, resources. |
| 4 | Back-end Engineer | `roles/backend-engineer.md` | `data-sets/backend.md` | Data, services, persistence. |
| 5 | DevOps Engineer | `roles/devops-engineer.md` | `data-sets/devops.md` | Build, CI/CD, environments. |
| 6 | QA Engineer | `roles/qa-engineer.md` | `data-sets/qa.md` | Test strategy, coverage, regression. |
| 7 | Security Specialist | `roles/security-specialist.md` | `data-sets/security.md` | Threat model, hardening, compliance. |
| 8 | Email Editor / Market Guru | `roles/email-editor-market-guru.md` | `data-sets/email-editor.md` | Messaging, outreach, copy. |
| 9 | File Organizer | `roles/file-organizer.md` | `data-sets/file-organizer.md` | Repo structure, naming, tidiness. |
| 10 | Red Team | `roles/red-team-agent.md` | — | Attack surface, recent exercises (optional in some runs). |
| 11 | Blue Team | `roles/blue-team-agent.md` | — | Defenses, alarms, remediation (optional in some runs). |
| 12 | Human-in-the-Loop Manager | `roles/human-in-the-loop-manager.md` | `data-sets/human-in-the-loop.md` | Closes: summary, recommendations for user, optional email draft. |

**Shared context for every role:** `KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md`, `data-sets/employee-roundtable-transcript.md`.

**Optional:** A **meeting agenda** or **weekly brief** (e.g. “Focus: calendar refactor and QA coverage”) that the Coordinator injects and each role can reference.

---

## What each agent is asked to contribute

When “called,” each agent should:

1. **Identify themselves** briefly (per roundtable and role card).
2. **Give a short update** relevant to their domain (e.g. “This week: …” or “Current state of …”).
3. **Offer 1–3 opinions or recommendations** based on best practice and SSOT (no contradictions to Known Truths or technical wiring).
4. **Optionally** flag handoffs (“This should go to QA”) or questions for the user.

The **Human-in-the-Loop Manager** (last) synthesizes: what to tell the user, what to ask, and optionally drafts the email (or Board Meeting summary) for the open line of communication.

---

## Call script (per-role prompt template)

Use this template when invoking each role (whether in one chat turn-by-turn or via subagent spawns). Replace `[ROLE]`, `[ROLE_CARD_PATH]`, `[DATA_SET_PATH]`, and `[PRIOR_SPEAKERS]` as needed.

```text
You are the [ROLE] for OutOfRouteBuddy in a Board Meeting. You have been given:
- Your role card (attached)
- Your data set (attached, if any)
- Known Truths and Single Source of Truth (attached)
- Employee roundtable transcript (attached)
- [Optional] Meeting agenda or weekly brief (attached)
- [Optional] What previous speakers said (below).

PRIOR SPEAKERS (for context; you may reference but do not repeat at length):
[PRIOR_SPEAKERS]

Your turn. Please:
1. Identify yourself in one sentence (scope per roundtable).
2. Give a short update relevant to your domain (current state or “this week”).
3. Give 1–3 opinions or recommendations based on best practice and SSOT; do not contradict Known Truths.
4. Optionally flag handoffs or questions for the user.

Keep your response concise (suitable for a meeting transcript). Output in a clear, scannable format (e.g. short bullets or numbered list).
```

**Coordinator-specific** (run first):

```text
You are the Master Branch Coordinator. You are chairing a Board Meeting for OutOfRouteBuddy. You have been given:
- Coordinator instructions (attached)
- Known Truths and Single Source of Truth (attached)
- Employee roundtable transcript (attached)
- [Optional] Meeting agenda or weekly brief (attached).

Open the meeting: state the purpose (e.g. weekly sync, or “gather full team opinions and progress”). Set the order of speakers (list the roles). Keep it brief. Do not speak for other roles; you are only chairing.
```

**Human-in-the-Loop** (run last):

```text
You are the Human-in-the-Loop Manager. You have been given:
- Your role card and data set (attached)
- Known Truths and Single Source of Truth (attached)
- Employee roundtable transcript (attached)
- The full Board Meeting transcript so far (below).

FULL TRANSCRIPT (all prior speakers):
[FULL_TRANSCRIPT]

Close the meeting: (1) Summarize key points and recommendations. (2) List what the user should be told or asked. (3) Optionally draft a short email to the user (subject + body) that could be sent via scripts/coordinator-email/ to report on this Board Meeting.
```

---

## Inputs and outputs

| Input | Purpose |
|-------|---------|
| **Known Truths + SSOT** | So every opinion aligns with persistence, recovery, calendar, GPS, settings. |
| **Employee roundtable** | So each role’s self-description and handoffs match the team. |
| **Role card + data set (per role)** | So each agent stays in scope and produces the right artifacts. |
| **Optional: meeting agenda / weekly brief** | Focus (e.g. “Calendar refactor and QA”; “Security review”). |
| **Optional: previous transcript** | For Human-in-the-Loop or for “continuation” meetings. |

| Output | Purpose |
|--------|---------|
| **Board Meeting transcript** | One document with Coordinator + each role’s turn in order. |
| **Board Meeting summary** | Short summary + list of recommendations (can be generated by Coordinator or Human-in-the-Loop). |
| **Optional: email draft** | Human-in-the-Loop output for sending to user via coordinator-email script. |

Suggested file naming:

- `docs/agents/data-sets/board-meeting/board-meeting-YYYY-MM-DD.md` — transcript
- `docs/agents/data-sets/board-meeting/board-meeting-YYYY-MM-DD-summary.md` — summary
- Optionally: agenda file e.g. `board-meeting-YYYY-MM-DD-agenda.md`

---

## Implementation options

### Option A: Runbook (manual or single-session AI)

- **What:** A markdown runbook (this plan + the call script) that a human or the main AI follows in **one chat session**.
- **How:** For each role in order, paste the call script, attach the right files (role card, data set, Known Truths, roundtable, and prior speakers/transcript), and run. Append each response to the transcript; pass the growing transcript to the next role. Human-in-the-Loop gets the full transcript.
- **Pros:** No code; works in any chat; full control. **Cons:** Many copy-pastes; token usage can get large if full transcript is passed every time.

### Option B: Script that invokes subagents (e.g. Cursor / MCP)

- **What:** A script (e.g. Python or shell) or an MCP tool that, for each role in order, **spawns a subagent** with the correct prompt and attachments (see SUBAGENTS_SPAWN_AND_TRAINING.md).
- **How:** Script builds `PRIOR_SPEAKERS` or `FULL_TRANSCRIPT` from previous subagent outputs; passes that plus role card, data set, Known Truths, roundtable into the next spawn. Writes transcript and summary to `docs/agents/data-sets/board-meeting/`.
- **Pros:** Repeatable, automatable, same context every time. **Cons:** Depends on subagent API (e.g. Cursor mcp_task) and attachment limits.

### Option C: Hybrid — runbook + small automation

- **What:** Runbook defines the order and prompts; a small script only (1) builds the list of roles and file paths, (2) optionally builds an agenda template, (3) writes the transcript file and appends each segment.
- **How:** Human or AI runs each “turn” using the runbook; script just assembles transcript and maybe summary from saved segments. Later, replace “human runs each turn” with subagent spawns when the API is fixed.

---

## Recommended next steps

1. **Create the runbook file** — Extract the call script and order into a single **BOARD_MEETING_RUNBOOK.md** (or add a “Runbook” section here) so anyone can run a Board Meeting in one chat by following steps.
2. **Create board-meeting output dir** — e.g. `docs/agents/data-sets/board-meeting/` with a short README (e.g. “Transcripts and summaries live here; name: board-meeting-YYYY-MM-DD.md”).
3. **Optional: agenda template** — A minimal `board-meeting-agenda-template.md` (date, focus, optional topics) that the Coordinator can use to set the meeting’s scope.
4. **Pilot one meeting** — Run one full Board Meeting (Option A) and save transcript + summary; tune prompts if some roles are too long or off-scope.
5. **Later: automate (Option B or C)** — If you use subagents, implement the spawn loop and transcript assembly so Board Meeting can be “run” with one command or MCP call.

---

## Quick runbook (run in one chat)

1. **Create today’s transcript file** — e.g. `docs/agents/data-sets/board-meeting/board-meeting-YYYY-MM-DD.md`. You will append each speaker’s response.
2. **Optional:** Create an agenda file or paste a one-line focus (e.g. “Weekly sync” or “Calendar refactor + QA”).
3. **Coordinator first** — Use the Coordinator prompt from “Call script” above; attach coordinator-instructions, Known Truths, roundtable, and optional agenda. Paste the response into the transcript file.
4. **For each role in order (1–12)** — Use the per-role prompt; attach that role’s card, data set (if any), Known Truths, roundtable. Set `[PRIOR_SPEAKERS]` to the content of the transcript so far (or last N speakers to control length). Append the response to the transcript file.
5. **Human-in-the-Loop last** — Use the Human-in-the-Loop prompt; attach role card, data set, Known Truths, roundtable. Set `[FULL_TRANSCRIPT]` to the complete transcript. Append their response (summary + optional email draft) to the transcript file.
6. **Save summary** — Copy the Human-in-the-Loop summary into `board-meeting-YYYY-MM-DD-summary.md` (or keep it at the end of the transcript).

---

## Summary

- **Board Meeting** = call script program that invokes every agent in a fixed order (Coordinator → Design → … → Human-in-the-Loop), each with full training context (role card, data set, Known Truths, roundtable).
- Each agent gives a short update, 1–3 SSOT-aligned recommendations, and optional handoffs; Human-in-the-Loop closes with summary and optional email draft.
- Outputs: transcript, summary, optional email. Implement first as a runbook (Option A), then add automation (Option B/C) when you have subagent or script support.
