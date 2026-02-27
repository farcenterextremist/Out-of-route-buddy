# Purple Team Exercise – OutOfRouteBuddy

**exercise_id:** 2025-02-22-purple-outofroutebuddy  
**date:** 2025-02-22  
**target:** OutOfRouteBuddy Android app – trip export, trip delete, FileProvider scope  
**mode:** Purple (Red + Blue run together)

---

## Scenario 1: Trip export – no security audit trail

### Red action – Technical Ninja
- **Role:** Technical Ninja
- **Target:** Trip export flow (CSV and report) – `TripHistoryViewModel.exportTrips()`, `exportToPDF()`, `TripExporter`
- **Action:** Simulated “export requested” from app: user taps Export, ViewModel uses `_trips.value` (all in-memory trips), writes to cache via `TripExporter`, shares via FileProvider. No user-controlled path; filename is app-generated (`trips_export_<timestamp>.csv`). Checked whether any **audit or security-relevant log** would fire so a defender could answer “did someone export data?”
- **Result:** Partial (code path reviewed; no live device run)
- **Blue visibility:** No – only generic `Log.d("TripHistoryViewModel", "Exported N trips to CSV")` existed; no dedicated, filterable audit event for “export requested” that could be used for alerting or compliance.
- **Artifacts:** (this file)

### Blue check – Scenario 1
- **Red action reviewed:** Export CSV and export report flows
- **Alarm went off?** No
- **If no (gap):** No dedicated audit log for “export requested” with format and count; defenders could not easily detect or alert on export events.
- **Remediation:** Added consistent audit-style logs in `TripHistoryViewModel`: before each export, `Log.w("TripExportAudit", "export_requested format=csv trip_count=N")` and `format=report` for PDF/report. Tag `TripExportAudit` is filterable for future alerting or log aggregation.
- **Artifacts:** `app/src/main/java/com/example/outofroutebuddy/presentation/ui/history/TripHistoryViewModel.kt`

---

## Scenario 2: Trip delete – no audit trail

### Red action – Technical Ninja
- **Role:** Technical Ninja
- **Target:** Trip delete flow – `TripHistoryViewModel.deleteTrip()`, `TripRepository.deleteTrip()`
- **Action:** Simulated “trip delete attempted”: ViewModel calls repository with a trip; success/failure only in `Log.d`/`Log.e` and `deleteError` SharedFlow. Checked whether a defender could answer “was a delete attempted, and did it succeed?”
- **Result:** Partial (code path reviewed)
- **Blue visibility:** No – success was logged as `Log.d("TripHistoryViewModel", "Trip deleted: id")`; failure as Log.e and deleteError. No single, filterable audit line for “delete_attempted” with trip_id and result.
- **Artifacts:** (this file)

### Blue check – Scenario 2
- **Red action reviewed:** Delete trip flow
- **Alarm went off?** No
- **If no (gap):** No dedicated audit log for “delete attempted” with trip_id and outcome.
- **Remediation:** In `TripHistoryViewModel.deleteTrip()`, added `Log.w("TripDeleteAudit", "delete_attempted trip_id=X result=true|false")` on success path and `result=exception` on catch. Enables detection and auditing of delete attempts.
- **Artifacts:** `app/src/main/java/com/example/outofroutebuddy/presentation/ui/history/TripHistoryViewModel.kt`

---

## Scenario 3: FileProvider scope – defense in depth

### Red action – Technical Ninja
- **Role:** Technical Ninja
- **Target:** FileProvider config – `res/xml/file_paths.xml`; `TripExporter` (writes to `context.cacheDir`, fixed filenames)
- **Action:** Reviewed FileProvider path scope. `cache-path name="exported_files" path="."` exposes the entire cache directory. TripExporter only creates files with fixed patterns (`trips_export_*.csv`, `trips_report_*.txt`); no user input in path or filename. Risk: future code change could introduce user-controlled path into FileProvider URI.
- **Result:** Partial (no current exploit; hardening opportunity)
- **Blue visibility:** Unclear – no “alarm” for “unexpected file shared”; scope is broad by design.
- **Artifacts:** (this file)

### Blue check – Scenario 3
- **Red action reviewed:** FileProvider scope and TripExporter usage
- **Alarm went off?** N/A (no abuse in current code)
- **If no (gap):** No documentation or code guard against future use of user-controlled paths in FileProvider.
- **Remediation:** (1) Added comment in `file_paths.xml`: do not use user-controlled paths; TripExporter uses fixed names only. (2) Added **Section 5** in `docs/security/SECURITY_NOTES.md`: FileProvider scope hardening – never use user-controlled paths; consider subfolder `cache/exports/` and narrower path for defense in depth.
- **Artifacts:** `app/src/main/res/xml/file_paths.xml`, `docs/security/SECURITY_NOTES.md`

---

## Summary

| Scenario | Red target | Alarm before? | Blue remediation |
|----------|------------|----------------|------------------|
| 1 | Export CSV/Report | No | Audit log `TripExportAudit` in ViewModel |
| 2 | Delete trip | No | Audit log `TripDeleteAudit` in ViewModel |
| 3 | FileProvider scope | N/A | Doc + comment; no user-controlled paths |

All remediations have been implemented. Proof of work: this file; code changes in TripHistoryViewModel, file_paths.xml, SECURITY_NOTES.md.
