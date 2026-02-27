# Purple Team Protocol – Red and Blue Working Together

This document describes how to run a **Purple Team** exercise: Red Team (attackers) and Blue Team (defenders) work in the same flow so that attacks are immediately checked and gaps are fixed on the spot.

---

## What is the Purple dynamic?

Purple is not a separate team of people; it is a **collaborative exercise** where:

1. **Red Team** attacks a specific target (server, app, API, or scenario).
2. **Blue Team** checks whether their alarm went off (logs, alerts, validation, monitoring).
3. **If the alarm did not go off:** Blue proposes or implements a fix right then and there. Red can re-test after the fix to verify.

Because Red and Blue are "in the same room" (e.g. same chat or same session), you avoid long handoff delays and get immediate feedback.

---

## When to use Purple

- After defining a clear **scope** (e.g. "trip export API," "app login flow," "phishing scenario for support emails").
- When you want **proof of work**: every Red action and every Blue check/fix is logged (see `docs/agents/security-team-proof-of-work.md`).
- When you want to **link** the two agent personalities: invoke Red and Blue in one exercise and alternate or combine their outputs.
- **Board-adopted:** When **Export** or another sensitive flow ships, run a short Purple exercise: Red targets that flow; Blue defines "alarm" for that surface, checks detection, remediates if missed, and we re-test after fix. See `docs/security/SECURITY_NOTES.md` §8.

---

## Step-by-step protocol

### 1. Scope and roles

- **User or Lead** states: target (e.g. "OutOfRouteBuddy trip export"), environment (e.g. "this codebase / local only"), and any off-limits (e.g. "no real user data").
- **Red Team (Lead)** confirms scope and chooses which role(s) to use (Specialist for phishing, Technical Ninja for code/API, or both).

### 2. Red attacks

- **Red Team** performs one or more attack actions (e.g. "attempt to export trip data without proper checks," "draft a phishing email that requests credentials").
- Each action is documented in the standard Red output format (target, action, result, Blue visibility, artifacts).

### 3. Blue checks

- **Blue Team** reviews each Red action and answers:
  - **Did our alarm go off?** (Would we have seen this in logs, alerts, or code?)
  - **If yes:** What detected it? (Document for proof of work.)
  - **If no:** This is a **gap**. Blue documents what should have detected it and what is missing.

### 4. Fix if missed

- **If the alarm did not go off:** Blue proposes or implements a fix (e.g. add a log, add validation, add an alert rule).
- Fix is recorded in the proof-of-work log with artifacts (file paths, config changes).
- Optionally, **Red** re-runs the same attack to confirm the fix works (alarm now goes off).

### 5. Save and reuse

- All Red actions and Blue checks/fixes are appended to the proof-of-work log and, where useful, artifacts are saved under `docs/agents/data-sets/security-exercises/`.
- Future runs can **import** this data to avoid re-testing the same findings and to show a history of exercises.

---

## How to invoke (linking the two agents)

- **Single prompt:** "Run a Purple Team exercise: Red attacks [X], Blue checks alarms. Use docs/agents/purple-team-protocol.md and the Red/Blue agent cards."
- **Sequential:** First ask Red to attack [X] and produce the structured output; then ask Blue to review that output and answer whether the alarm went off; if not, Blue proposes a fix.
- **Proof of work:** After the exercise, add the Red and Blue blocks to `docs/agents/security-team-proof-of-work.md` (or the run log file) and save any scripts or configs to `docs/agents/data-sets/security-exercises/`.

---

## References

- **Red Team agent:** `docs/agents/roles/red-team-agent.md`
- **Blue Team agent:** `docs/agents/roles/blue-team-agent.md`
- **Proof of work and logging:** `docs/agents/security-team-proof-of-work.md`
- **Security data set:** `docs/agents/data-sets/security.md` (existing); exercises go in `docs/agents/data-sets/security-exercises/`
