# Worker todo lists and one idea each

Assigned by the Coordinator. Each role has a short todo list and proposes **one idea** for the roadmap.

---

## Project Design / Creative Manager

**Todos:**
- [ ] Draft a short product brief for "Auto drive detected" mode (value, when to use it).
- [ ] Prioritize next 3 features after emulator polish and IMAP (e.g. Auto drive, Reports screen, History improvements).
- [ ] Align workdays (Sunday 3–4 hr) with a biweekly "sprint" note so the team knows what to batch.

**One idea:** **"Driver check-in prompts"** — After a trip ends, optionally prompt the user once: "How was the route?" (thumbs up/down or optional note). One tap, no forms. Use it later for simple analytics or to flag bad routes.

---

## UI/UX Specialist

**Todos:**
- [ ] Propose wireframe for **Auto drive detected** mode/button: where it lives (toolbar vs trip screen), default state (auto-on vs opt-in), and one-tap override.
- [ ] Add long-press for edit in the emulator (touch devices) so right-click isn’t the only way.
- [ ] Review statistics section accessibility (labels, contrast, touch targets) and list 3 quick wins.

**One idea:** **"Trip summary card"** — When the user ends a trip, show a single card: "X.X mi driven, Y.Y% OOR" with a Share or Save option. Reduces cognitive load and gives a clear "done" moment.

---

## Front-end Engineer

**Todos:**
- [ ] Implement **Auto drive detected** UI: button or indicator + optional settings toggle (from UI/UX spec).
- [ ] Emulator: Settings dialog (Mode, Templates, Help) and End Trip confirmation modal (End / Clear / Continue).
- [ ] Statistics "View" button: wire to a simple period picker (This week / This month / Custom) in the emulator.

**One idea:** **"Haptic on trip start/end"** — Light vibration when trip starts and when it ends so the user gets tactile confirmation without looking (e.g. phone in pocket).

---

## Back-end Engineer

**Todos:**
- [ ] Define **Auto drive detected** logic: what triggers it (e.g. movement + speed threshold, or geofence), and how it starts a trip or shows the button.
- [ ] Implement trip state persistence so an active trip survives app kill (already partially there; verify and document).
- [ ] Optional: persist "Driver check-in" (thumbs up/down) if Design approves the feature.

**One idea:** **"Smart default for bounce miles"** — Remember last used bounce miles per user (or per region) and prefill; still editable. Reduces repeated entry for regular routes.

---

## DevOps Engineer

**Todos:**
- [ ] Document how to run the emulator in CI (e.g. static build or smoke test) if we want a "no regressions" gate.
- [ ] Add a one-line "health" script or target that checks: build, unit tests, and (optional) coordinator email .env present.
- [ ] Keep Gradle and JDK versions in README or DEPLOYMENT.md up to date.

**One idea:** **"Nightly digest"** — Optional scheduled job (e.g. Sunday evening) that runs a small script to summarize the week’s code changes or test results and emails you a 3-line digest. Off by default; enable via a flag or cron.

---

## QA Engineer

**Todos:**
- [ ] Add test cases for **Auto drive detected** once the flow is defined (e.g. "when movement detected, button appears" / "tapping starts trip").
- [ ] Cover read_replies.py and send_email.py with a simple smoke test (e.g. mock IMAP/SMTP or dry-run) so we don’t break the open line.
- [ ] Fix or document the known ignored/failing tests (e.g. TripInputViewModelIntegrationTest dispatcher, TripHistoryByDateViewModelTest context).

**One idea:** **"Screenshot on trip end"** — In debug builds, optionally capture a single screenshot when the user ends a trip (with no PII). Gives a visual regression corpus for "trip end" screen over time.

---

## Security Specialist

**Todos:**
- [ ] Review **Auto drive detected** for privacy: location/movement data usage, and whether anything is stored or sent beyond the device.
- [ ] Confirm coordinator email .env and last_reply.txt are never committed and document one-time checklist for new machines.
- [ ] Short threat note: "What if the device is lost while a trip is active?" (lock screen, optional remote wipe, etc.).

**One idea:** **"App PIN or biometric to end trip"** — Optional: require PIN or fingerprint to end a trip. Reduces risk of someone else ending the driver’s trip if the phone is unlocked.

---

## Email Editor / Market Guru

**Todos:**
- [ ] Draft 2–3 subject-line templates for "we need your decision" vs "here’s an update" vs "please confirm" so the Human-in-the-Loop Manager can pick quickly.
- [ ] One-sentence value prop for OutOfRouteBuddy (for future app store or outreach).
- [ ] Suggest a short sign-off for coordinator emails (e.g. "— OutOfRouteBuddy Team" vs "— Coordinator").

**One idea:** **"Weekly one-liner"** — Every Sunday (or when you’re on), the Human-in-the-Loop sends one line: "This week: [X]. Next: [Y]. Reply with any changes." Keeps the open line warm without long reads.

---

## File Organizer

**Todos:**
- [ ] Propose a small reorg: e.g. `docs/agents/` vs `docs/product/` for roadmaps vs agent instructions, and where "future plans" live.
- [ ] Ensure EMULATOR_PERFECTION_PLAN, WORKER_TODOS, and OPEN_LINE doc are linked from README or a single "docs index."
- [ ] Naming: decide whether worker todos are `WORKER_TODOS_*.md` by date or a single `WORKER_TODOS.md` that gets updated.

**One idea:** **"Changelog for you"** — A single CHANGELOG_USER.md (or section) in plain language: "What we did this week / What’s next," updated when we email you. One place to look instead of digging through commits.

---

## Human-in-the-Loop Manager

**Todos:**
- [ ] Send the summarized "future plans + worker todos + one idea each" email to the user (done when this doc is used).
- [ ] After user replies or next session: run read_replies if they said they replied, and update team-parameters or backlog from their response.
- [ ] Use the Email Editor’s subject-line templates once they’re drafted.

**One idea:** **"Reply with a number"** — When we need a quick priority (e.g. "What should we do first? 1=Auto drive, 2=Reports, 3=Emulator polish"), ask the user to reply with just the number. Makes parsing last_reply.txt easy and keeps emails short.

---

## Future plans (summary for email)

- **Auto drive detected mode/button** — Detect when the driver is likely on the road (movement/speed or similar) and show a clear "Auto drive detected" state with a one-tap button to start a trip (or confirm). Reduces friction for drivers who forget to tap Start. Design + UI/UX + Front-end + Back-end will own their parts; Security will review privacy.
- **Emulator** — Finish Settings dialog, End Trip confirmation, period picker for Statistics, long-press for edit. Then treat emulator as the visual spec for the real app.
- **Open line of communication** — Keep using email to ask you questions and read your replies; optional weekly one-liner and "reply with a number" for priorities.
- **Worker ideas** — Each role’s one idea above is on the backlog for you to approve, defer, or combine (e.g. Driver check-in, Trip summary card, Haptic, Smart bounce default, Nightly digest, Screenshot on trip end, App PIN to end trip, Weekly one-liner, Changelog for you, Reply with a number).
