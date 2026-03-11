# Security Loop Checklist — Security-Focus Runs

**Purpose:** When Security is this run's focus, use this checklist. Aligns with DevSecOps: shift-left, weekly cadence, route findings to owners.

**References:** [IMPROVEMENT_LOOP_RESEARCH_2025-03.md](./IMPROVEMENT_LOOP_RESEARCH_2025-03.md), [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md), [SECURITY_NOTES.md](../security/SECURITY_NOTES.md)

---

## Security-Focus Run Checklist

| # | Item | Action |
|---|------|--------|
| 1 | **PII in logs** | Grep for `Log.*lat\|lon\|coordinates\|tripId`; remove or redact. |
| 2 | **FileProvider scope** | Verify export/share flows use app-private or safe paths only. |
| 3 | **Keystore / StandaloneOfflineService** | KDoc note: "TODO: Migrate to Keystore + EncryptedSharedPreferences" if not present. |
| 4 | **Secrets** | Confirm no new secrets in logs; `.env`, `last_reply.txt` in `.gitignore`. |
| 5 | **Dependency audit** | Run `./gradlew dependencyUpdates` (if available) or document "last dependency audit" in summary. |
| 6 | **SECURITY_CHECKLIST** | One-line verification per [SECURITY_CHECKLIST.md](../security/SECURITY_CHECKLIST.md). |

---

## When to Run

- **Security focus** — When this run's focus is Security (see [LOOP_FOCUS_ROTATION.md](./LOOP_FOCUS_ROTATION.md)).
- **Phase 1.2** — Extend to run full checklist when Security focus. Optionally run `./gradlew dependencyUpdates` or document audit date.

---

## Phase 1.2 Extension (Security Focus)

When Security is focus, run all 6 items above in one pass. Document "Last dependency audit: YYYY-MM-DD" in summary if not running dependencyUpdates.
