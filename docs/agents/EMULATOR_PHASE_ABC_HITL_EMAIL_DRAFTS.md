# Emulator Phase A/B/C – HITL email drafts

Drafts for Human-in-the-Loop to send to the user after each phase. Send manually (no `send_email.py` in repo). See [EMAIL_AT_END_OF_BIG_CHANGES.md](EMAIL_AT_END_OF_BIG_CHANGES.md).

---

## Phase A completion (C-HITL-1)

**Subject:** Emulator: Undo/Redo and clear Save vs Sync

**Body:**

The emulator now has:

- **Undo** and **Redo** (toolbar buttons and Ctrl+Z / Ctrl+Y) so you can revert or reapply edits.
- A clear split: **Save** = update only in the emulator; **Sync to project** = write to the repo (e.g. strings.xml). Optional **Auto-sync to project (after each Save)** so Cursor sees changes soon after you Save.

Try it: edit a field → Save → Undo to revert → edit again → Sync to project to push to the repo.

---

## Phase B completion (C-HITL-2)

**Subject:** Emulator: 1:1 spec and Load from project

The emulator now matches the app more closely:

- **1TO1_SPEC.md** in the phone-emulator folder documents screens in scope, spacing (toolbar 56dp, margins, card padding), string-key parity (every strings.xml key has an editable), and icon sources.
- **Load from project** — Click this button to pull the current strings from the repo into the emulator so the phone shows what’s in the codebase. Use **Sync to project** to push your edits the other way.

Please try side-by-side: open the app and the emulator and confirm labels and layout feel aligned.

---

## Phase C summary (C-HITL-3)

**Subject:** Emulator Phase A/B/C complete – checklist and links

Phase C is done:

- **QA checklist** — `docs/qa/EMULATOR_PHASE_ABC_CHECKLIST.md` combines string-key parity and editing-flow test cases (Edit/Save, Undo, Redo, Sync, Auto-sync, Load from project, Copy for Cursor).
- **Links** — The brainstorm doc, Phase ABC todo doc, and docs index are cross-linked. 1TO1_SPEC is linked from the emulator README.
- **Editing flow** — Undo/Redo, Save vs Sync, Load from project, and 1:1 spec are in place.

You can run the Phase C checklist anytime to verify everything end-to-end.
