# Security Team Proof of Work – Red, Blue, and Purple

This document defines how to **import data** and **save data** for Red Team, Blue Team, and Purple Team exercises so you have **proof of work** for future use.

---

## Purpose

- **Log** every Red action and every Blue check/fix in a consistent format.
- **Save** artifacts (scripts, payloads, config snippets) so they can be re-run or audited.
- **Reuse** past exercises: import this data in future sessions to show what was tested and what was fixed.

---

## Where to save data

| What | Where |
|------|--------|
| Run logs (append-only) | This file **or** timestamped files under `docs/agents/data-sets/security-exercises/` (e.g. `2025-02-22-purple-export-api.md`) |
| Scripts, payloads, PoC code | `docs/agents/data-sets/security-exercises/artifacts/` (one folder per exercise or date if needed) |
| Red/Blue structured blocks | Appended below in **Exercise log** or in the timestamped file |

---

## Exercise log (append here or in dated file)

*(Below: example placeholder. Replace or append with real exercises.)*

---
exercise_id: "placeholder-import-example"
date: "2025-02-22"
target: "OutOfRouteBuddy – trip export and file access"
mode: Purple
---

### Red action – (example)
- **Role:** Technical Ninja
- **Target:** Trip export CSV and share flow
- **Action:** Review code path for export without ownership/context check
- **Result:** Partial
- **Blue visibility:** Unclear
- **Artifacts:** (none yet – add path when PoC is saved)

### Blue check – (example)
- **Red action reviewed:** Export flow
- **Alarm went off?** No
- **If no (gap):** No dedicated log/alert for export requested
- **Remediation:** Add logging in TripExporter when export is generated
- **Artifacts:** (file paths when implemented)

---

*(End of example. Append new exercises below or in docs/agents/data-sets/security-exercises/*.md.)*

---

Use the following template for each exercise. Copy and fill it, then append to this section or create a new file under `security-exercises/`.

```markdown
---
exercise_id: "[short id, e.g. 2025-02-22-export-api]"
date: "YYYY-MM-DD"
target: "[e.g. trip export API, phishing scenario]"
mode: Red only | Blue only | Purple
---

### Red action – [phase or time]
- **Role:** Lead | Specialist | Technical Ninja
- **Target:** 
- **Action:** 
- **Result:** Success | Failed | Partial
- **Blue visibility:** Yes | No | Unclear
- **Artifacts:** 

### Blue check – [phase or time]
- **Red action reviewed:** 
- **Alarm went off?** Yes | No
- **If yes:** What detected it?
- **If no (gap):** What should have detected it?
- **Remediation:** 
- **Artifacts:** 
```

---

## Example (proof of work)

```markdown
---
exercise_id: "2025-02-22-export-api"
date: "2025-02-22"
target: "OutOfRouteBuddy trip export and file access"
mode: Purple
---

### Red action – Technical Ninja, export path
- **Role:** Technical Ninja
- **Target:** Trip export CSV generation and share
- **Action:** Simulated access to export without checking trip ownership / app context
- **Result:** Partial (code path exists; no explicit abuse PoC run)
- **Blue visibility:** Unclear
- **Artifacts:** docs/agents/data-sets/security-exercises/artifacts/2025-02-22-export-review.md

### Blue check – export path
- **Red action reviewed:** Export flow and file access
- **Alarm went off?** No
- **If no (gap):** No dedicated log or alert for "export requested" or "export file created"
- **Remediation:** Add logging in TripExporter when export is generated; consider audit event for future alerting
- **Artifacts:** (file paths if code changed)
```

---

## Recent runs

- **2025-02-20 – Security Plan Comprehensive (2 scenarios):** [2025-02-20-security-plan.md](./data-sets/security-exercises/2025-02-20-security-plan.md) – Sync API hardening (key allowlist, 64KB limit, SyncServiceAudit), TripInsertAudit in TripRepository. All remediations implemented. See SECURITY_NOTES.md Sections 6 and 7.
- **2025-02-22 – OutOfRouteBuddy Purple (3 scenarios):** [2025-02-22-purple-outofroutebuddy.md](./data-sets/security-exercises/2025-02-22-purple-outofroutebuddy.md) – Export audit, delete audit, FileProvider scope. All three Blue remediations implemented (TripExportAudit, TripDeleteAudit, SECURITY_NOTES + file_paths comment).
- **Automated Purple simulations:** Run `python scripts/purple-team/run_purple_simulations.py` to generate new exercise logs and training data. Outputs: `docs/agents/data-sets/security-exercises/YYYY-MM-DD-purple-simulations.md` and `artifacts/YYYY-MM-DD-purple-training.json`. Use `--with-http` when sync service is running for HTTP attack simulations.

---

## Import and reuse

- **To show proof of work:** Open this file or any `security-exercises/*.md` and point to the exercise log and artifacts.
- **To run a follow-up:** "Using the last Purple exercise in security-team-proof-of-work.md, Red re-run the same attack; Blue confirm the new log/check would have fired."
- **To onboard:** New sessions can read `docs/agents/roles/red-team-agent.md`, `docs/agents/roles/blue-team-agent.md`, and this file to continue logging in the same format.

---

## Directory layout

Ensure these exist (create if missing):

- `docs/agents/data-sets/security-exercises/` – directory for exercise logs and metadata
- `docs/agents/data-sets/security-exercises/artifacts/` – directory for scripts, payloads, and PoC files

You can add a short `README.md` in `security-exercises/` explaining that this folder holds Red/Blue/Purple run logs and artifacts for proof of work.
