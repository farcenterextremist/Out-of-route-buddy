# Automation — 8-hour improvement run and pulse

This folder backs the **8-hour slow improvement** run and the **ship instructions** for OutOfRouteBuddy.

## Files

| File | Purpose |
|------|--------|
| **8_HOUR_IMPROVEMENT_PLAN.md** | Improvement objectives (backend-heavy, ~2% frontend), pulse checklist, and how to run automation. |
| **IMPROVEMENT_LOOP_ROUTINE.md** | **Routine** Improvement Loop: research first, security + smoothness, suggested next steps. Run when user says **GO**. |
| **IMPROVEMENT_LOOP_COMMON_SENSE.md** | Non-negotiable parameters: checkpoint first, tests green, full autonomy for Light+Medium. Read at loop start. |
| **IMPROVEMENT_LOOP_REASONING.md** | Logic and reasoning: decision checkpoints, reasoning framework, traceable rationale in summaries. Think before you act. |
| **HEAVY_TIER_IDEAS.md** | Heavy tier ideas list (cap 50) with state and true completion %. Light and Medium add 1–2 per run. |
| **SANDBOX_COMPLETION_PERCENTAGE.md** | True completion % for sandboxed ideas; merging should not be taken lightly. |
| **IMPROVEMENT_LOOP_AUDIT.md** | Full systems audit: safety, durability, accuracy, readiness. Re-run after major changes. |
| **IMPROVEMENT_LOOP_TEAMS.md** | **Teams** Front: Researchers + Meta-Researchers. Back: File Organizer (recommends new ideas; Heavy needs human approval). |
| **AUTONOMOUS_LOOP_SETUP.md** | One-time setup for no human intervention (Run Everything or allowlist). |
| **LOOP_MASTER_ALLOWLIST.md** | Commands for allowlist; use prefix `cd c:\...\OutofRoutebuddy` for minimal entries. |
| **120_MINUTE_IMPROVEMENT_LOOP.md** | Legacy plan; superseded by IMPROVEMENT_LOOP_ROUTINE. |
| **pulse_log.txt** | Appended every 30 min by the pulse script (tests, lint, one-line note). |
| **loop_events.jsonl** | Structured event log (JSONL) from loop listener; used for loop improvement. |
| **OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt** | Generated in the last 2 hours (or manually). **Copy this to your Desktop** if you want it there: `copy OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt %USERPROFILE%\Desktop\` |

## Scripts (run from repo root)

| Script | Purpose |
|--------|--------|
| **scripts\automation\pulse_check.ps1** | Run every 30 min. Runs unit tests (optional skip with `-Quick`), lint, appends to `pulse_log.txt`. |
| **scripts\automation\run_8hr_automation.ps1** | Runs for 8 hours: pulse every 30 min; in the last 2 hours calls `write_ship_instructions.ps1` to generate ship instructions. |
| **scripts\automation\run_120min_loop.ps1** | Runs for 2 hours: pulse every 30 min. Use with IMPROVEMENT_LOOP_ROUTINE.md when user says GO. |
| **scripts\automation\loop_listener.ps1** | Records loop events (loop_start, phase, pulse, loop_end) to loop_events.jsonl for data and improvement. |
| **scripts\automation\test_loop_listener.ps1** | Test/simulation: verifies listener is wired and functioning. |
| **scripts\automation\write_ship_instructions.ps1** | Writes `OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt` (default: Desktop; use `-OutFile` to override). Creates output directory if missing. |

## Quick start

1. **2-hour improvement loop (routine)** (when user says GO):
   - **One-time:** Follow `docs/automation/AUTONOMOUS_LOOP_SETUP.md` for no-human-intervention (Run Everything or allowlist).
   - **Phase 0:** Research: read latest loop summary, CRUCIAL_IMPROVEMENTS, SECURITY_NOTES, FAILING_OR_IGNORED_TESTS.
   - **Run:** `.\scripts\automation\run_120min_loop.ps1`
   - **Follow:** `docs/automation/IMPROVEMENT_LOOP_ROUTINE.md` — the agent orchestrates phases and spawns subagents.

2. **Start the 8-hour run** (pulse every 30 min, ship doc in last 2 hours):

   **Option A — From repo root** (ensure your current directory is the repo root, e.g. `C:\Users\brand\OutofRoutebuddy`):
   ```powershell
   cd c:\Users\brand\OutofRoutebuddy
   .\scripts\automation\run_8hr_automation.ps1
   ```

   **Option B — Batch launcher** (works from any folder; run from repo root or double-click):
   ```cmd
   c:\Users\brand\OutofRoutebuddy\run_8hr_automation.bat
   ```

   **Option C — Full path** (no need to cd first):
   ```powershell
   & "c:\Users\brand\OutofRoutebuddy\scripts\automation\run_8hr_automation.ps1"
   ```
3. **Or** run the pulse manually every 30 min:
   ```powershell
   .\scripts\automation\pulse_check.ps1
   .\scripts\automation\pulse_check.ps1 -Note "description of what you improved"
   ```
4. **Generate ship instructions now** (e.g. after the run or anytime):
   ```powershell
   .\scripts\automation\write_ship_instructions.ps1
   ```
   To save to Desktop if the default path fails:
   ```powershell
   .\scripts\automation\write_ship_instructions.ps1 -OutFile "$env:USERPROFILE\Desktop\OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt"
   ```

Ship instructions are also written here as **OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt**; copy to Desktop if desired.
