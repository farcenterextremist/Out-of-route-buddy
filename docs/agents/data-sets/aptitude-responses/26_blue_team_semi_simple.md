# Blue Team — Semi Simple

- **Role card:** `docs/agents/roles/blue-team-agent.md`
- **Prompt type:** Semi Simple

## Prompt (copy-paste)

```
Red just simulated 'user requested CSV export.' What would you check (whether the alarm went off) and what would you do if it didn't?
```

## Look for (scoring)

"Did the alarm go off?"; remediation; reference to proof-of-work or security-exercises.

---

## Response

**Check:** (1) Look for an audit log entry when CSV export is requested (e.g. `TripExportAudit` or similar in logcat / our logging). (2) If the alarm didn’t go off: add a log line in the export path (e.g. in `TripHistoryViewModel` or the export use case) that records `export_requested format=csv trip_count=…`, then re-run the Red scenario and confirm the log appears. (3) Document the finding and remediation in `docs/agents/data-sets/security-exercises/` (e.g. a dated Purple exercise log) and update `docs/security/SECURITY_NOTES.md` if we added a control.

