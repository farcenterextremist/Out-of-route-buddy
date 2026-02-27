# Security Specialist — Semi Simple

- **Role card:** `docs/agents/roles/security-specialist.md`
- **Prompt type:** Semi Simple

## Prompt (copy-paste)

```
We're about to add export to PDF that writes to app cache and shares via FileProvider. What would you check and what doc would you update?
```

## Look for (scoring)

docs/security/, manifest, no implementation; recommendations only.

---

## Response

**Check:** (1) FileProvider paths in `res/xml/file_paths.xml`—only expose fixed cache subdirs, no user-controlled paths. (2) Export file names—sanitized, no path traversal (e.g. fixed prefix like `trips_export_*.pdf`). (3) Share intent—use `Intent.FLAG_GRANT_READ_URI_PERMISSION` and limit to the single file. **Doc to update:** `docs/security/SECURITY_NOTES.md` with a short "Export to PDF" subsection: FileProvider scope, naming, and share-scope. I don’t write the implementation.

