# Autonomous 2-Hour Loop — No Human Intervention

**Goal:** Run the 2-hour improvement loop without approval prompts. Fully automated = zero human intervention.

---

## Fully Automated (Zero Human Intervention)

For **scheduled runs with no one at the keyboard**, use **Cursor Automations** (cloud):

### Setup at cursor.com/automations

1. Go to [cursor.com/automations](https://cursor.com/automations)
2. **Create** a new automation
3. **Trigger:** Choose **Scheduled** (e.g. every 2 weeks, Monday 9am) or **Webhook** (POST to run on demand from CI/cron)
4. **Repository:** Select your OutOfRouteBuddy repo and branch
5. **Tools:** Enable **Open pull request** if the automation should make changes and open a PR for review
6. **Prompt:** Paste the 2-hour loop instructions (see [Automation prompt template](#automation-prompt-template) below)
7. **Environment:** Enable if the loop needs to run Gradle (tests, lint)
8. **Save** — webhook URL and API key are generated for webhook triggers

### Automation prompt template

```
Run the 2-hour improvement loop for OutOfRouteBuddy.

1. Read docs/automation/IMPROVEMENT_LOOP_ROUTINE.md
2. Execute Phase 0 (Research), Phase 0.3 (Self-improvement), Phases 1–4
3. Write docs/automation/IMPROVEMENT_LOOP_SUMMARY_<date>.md with what was done and suggested next steps
4. Open a pull request with your changes, or comment on the repo with the summary if no code changes

Scope: Low-risk fixes only. No new features, architecture refactors, or unwarranted UI changes. See docs/GOAL_AND_MISSION.md and docs/CRUCIAL_IMPROVEMENTS_TODO.md.
```

### Webhook for on-demand runs

- Use **Webhook** trigger to run the loop when you want (e.g. from a script, CI, or manual POST)
- Store the webhook URL and API key securely
- POST to the URL with the API key in headers to start a run

### Billing

Automations use cloud agents; billing is per usage. See [Cursor cloud agent pricing](https://cursor.com/docs/models-and-pricing).

---

## Option A: Run Everything (Simplest, Local)

**One setting enables full autonomy.**

1. Open **Cursor Settings** (Ctrl+,)
2. Go to **Agents** → **Auto-Run**
3. Set **Auto-run mode** to **Run Everything**

**Effect:** All terminal commands, file writes, and MCP tools (including subagent spawns) run automatically. No prompts. When you say **GO**, the agent runs the full loop without stopping for approval.

**Trade-off:** Applies to all agent sessions, not just the loop. Use only if you're comfortable with the agent having full autonomy in this workspace.

---

## Option B: Allowlist + Run in Sandbox (More Secure)

Keep **Run in Sandbox** or **Ask Every Time**, but add commands so the loop’s terminal calls are pre-approved.

### Allowlist uses prefix matching

Cursor matches the **start** of the command. One entry can cover many variants.

### Minimal allowlist (1–2 entries)

Add these to **Command Allowlist** (Settings → Agents → Auto-Run → Protection → Command Allowlist):

| Entry | Covers |
|-------|--------|
| `cd c:\Users\brand\OutofRoutebuddy` | All commands that start with this (gradlew, powershell, pulse, etc.) |

That single prefix matches:

- `cd c:\Users\brand\OutofRoutebuddy; .\gradlew.bat :app:testDebugUnitTest --no-daemon`
- `cd c:\Users\brand\OutofRoutebuddy; .\gradlew.bat :app:lintDebug --no-daemon`
- `cd c:\Users\brand\OutofRoutebuddy; powershell -File .\scripts\automation\pulse_check.ps1`
- Any other command that begins with `cd c:\Users\brand\OutofRoutebuddy`

### Optional: Broader Gradle access

If the agent runs Gradle from other working directories, add:

| Entry | Covers |
|-------|--------|
| `.\gradlew.bat` | All Gradle tasks from repo root |

### Protection settings to check

- **File-Deletion Protection:** Leave ON if you don’t want the agent to delete files.
- **External-File Protection:** Leave ON to avoid edits outside the workspace.
- **Dotfile Protection:** Leave ON to avoid edits to `.gitignore`, etc.

Workspace file edits (Kotlin, XML, etc.) are allowed in the sandbox by default.

---

## Option C: Cursor Automations (Scheduled, Cloud)

For scheduled runs without opening Cursor:

1. Go to [cursor.com/automations](https://cursor.com/automations)
2. Create an automation triggered by schedule (e.g. every 2 weeks) or a webhook
3. Configure the agent with the 2-hour loop routine and your repo

**Note:** Automations run in Cursor’s cloud; they don’t use your local Cursor settings.

---

## Verification

After setup:

1. Open a new chat
2. Say **GO**
3. Confirm the agent runs commands (e.g. `gradlew testDebugUnitTest`) without asking for approval

---

## Summary

| Option | Setup | Autonomy | Human intervention |
|--------|-------|----------|--------------------|
| **Automations (cloud)** | cursor.com/automations | Scheduled or webhook | **Zero** — runs in cloud |
| **A: Run Everything** | 1 setting | Full (local) | Say "GO" once |
| **B: Allowlist** | 1–2 allowlist entries | Loop commands (local) | Say "GO" once |
| **C: Automations** | Same as top row | Scheduled | **Zero** |

**For zero human intervention:** Use **Cursor Automations** with a scheduled or webhook trigger.

**For local runs with minimal prompts:** Use **Option B** (allowlist); say "GO" to start.

---

## Sandbox Testing

For testing new features before they reach the main project, see [SANDBOX_TESTING.md](./SANDBOX_TESTING.md).
