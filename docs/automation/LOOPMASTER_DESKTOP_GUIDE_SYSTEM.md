# Loopmaster desktop guide system

**Purpose:** Keep the desktop operator guide readable for humans while preserving a single source of truth inside the repository.

**Source file:** `docs/automation/LOOPMASTER_DESKTOP_GUIDE_SOURCE.txt`
**Default exported file:** `C:\Users\brand\Desktop\Loopmaster_Best_Practices_and_Design_Flow.txt`
**Exporter:** `scripts/automation/export_loopmaster_desktop_guide.ps1`
**Exporter test:** `scripts/automation/test_loop_desktop_guide_export.ps1`

---

## Why this system exists

The desktop guide is useful because it is easy to find and easy to read.

But if the desktop copy is edited independently, it will drift away from:

- loop docs
- skills
- current tab model
- current blind-spot coverage

So the repo keeps the source of truth, and the desktop copy becomes an export target.

---

## Update workflow

1. Update `docs/automation/LOOPMASTER_DESKTOP_GUIDE_SOURCE.txt`
2. Run:

```powershell
.\scripts\automation\export_loopmaster_desktop_guide.ps1
```

3. Verify:

```powershell
.\scripts\automation\test_loop_desktop_guide_export.ps1
```

4. If the content is more useful and still readable, keep it

---

## What should trigger an update

Update the desktop guide when any of these change materially:

- tab layout or spawn behavior
- health / readiness / diagnostic rules
- data bucket rules
- blind-spot coverage
- cross-project flywheel setup guidance
- design-sharpening rules

---

## Readability rule

The desktop guide should stay:

- plain text
- fast to scan
- low jargon
- focused on operation, not deep policy history

If a detail is important but too heavy for the desktop guide, keep it in repo docs and summarize it in one or two lines.

---

## Anti-drift rule

Do not update the desktop file first.

Update the source guide first, then export.

This keeps:

- one editable source
- repeatable exports
- testable guide updates

---

## Cross-project flywheel note

The desktop guide is intentionally written so another app or program can reuse the same operating pattern with:

- different repo paths
- different test commands
- different domain checks

That makes the guide a flywheel seed, not just a one-off note for this repo.
