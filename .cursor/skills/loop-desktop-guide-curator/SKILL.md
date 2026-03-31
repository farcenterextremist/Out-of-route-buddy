---
name: loop-desktop-guide-curator
description: Maintains the Loopmaster desktop guide from a repo source of truth and keeps it readable, current, and export-tested. Use when the user mentions the desktop guide, operator guide, flywheel guide, readable loop instructions, or updating loop documentation for humans.
---

# Loop Desktop Guide Curator

## Purpose

Keep the desktop Loopmaster guide useful for humans without letting it drift away from repo truth.

Primary references:

- `docs/automation/LOOPMASTER_DESKTOP_GUIDE_SOURCE.txt`
- `docs/automation/LOOPMASTER_DESKTOP_GUIDE_SYSTEM.md`
- `scripts/automation/export_loopmaster_desktop_guide.ps1`
- `scripts/automation/test_loop_desktop_guide_export.ps1`
- `.cursor/skills/critique-data-minimize-slop/SKILL.md`

## Trigger

Use this skill when requests mention:

- "desktop guide"
- "operator guide"
- "flywheel guide"
- "readable loop instructions"
- "update the desktop doc"

## Workflow

1. Update the repo source guide first
2. Keep language plain, concise, and low-jargon
3. Export the desktop copy
4. Run the export test
5. Check whether the guide improved in readability and usefulness

## Guardrails

- Do not edit the desktop file first when the source file should change
- Keep repo truth and desktop readability aligned
- Prefer operational guidance over policy history
