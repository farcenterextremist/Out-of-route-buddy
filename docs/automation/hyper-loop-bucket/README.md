# Hyper-loop bucket

**Purpose:** Local artifact bucket for `Loopmaster` and future `Hyper` runs.

Use this bucket to store reusable run evidence so larger loop runs can be reviewed, compared, and selectively reverted with less guesswork.

---

## What belongs here

- run manifests
- checkpoint notes
- screenshot evidence
- summary exports
- shared-state snapshots
- touched-file lists
- rollback decision notes

---

## Why this exists

Without a bucket, large automation runs are harder to trust because:

- change intent is scattered
- screenshot evidence is easy to lose
- selective rollback becomes guesswork
- future runs cannot compare against a stable artifact pack

This bucket turns loop runs into traceable evidence sets instead of one-off edits.

---

## Suggested structure

Use one folder per run:

`YYYY-MM-DD_<mode>_<run-id>/`

Suggested contents:

- `manifest.md`
- `summary.md`
- `shared-state-snapshot.json`
- `touched-files.txt`
- `design-review.md`
- `screenshots/`

---

## Guardrails

- Keep only artifacts that are reusable or decision-critical.
- Name files clearly.
- Do not dump raw clutter here.
- If a run had no screenshot/design work, omit those files rather than leaving empty placeholders.

---

## Relationship to rollback

This bucket does **not** automatically revert code.

It supports safer human-controlled decisions by preserving:

- what changed
- why it changed
- what evidence supported keeping it
- what could be reverted later
