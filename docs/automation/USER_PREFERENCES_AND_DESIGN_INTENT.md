# User Preferences and Design Intent

**Purpose:** Single place for design intent, must-not-change list, and subtle preferences. The Improvement Loop reads this **first** (Phase 0.0a) and must not drift from it.  
**References:** [GOAL_AND_MISSION.md](../GOAL_AND_MISSION.md), [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md)

---

## Design Intent

- OutOfRouteBuddy is for **solo drivers** first: advanced analytics and tracking for out-of-route miles.
- **No social features, no ads, no cloud-first** — see GOAL_AND_MISSION.
- UI should stay focused: trip input, history, statistics, settings. No unwarranted layout or flow changes.

---

## Must Not Change (without explicit permission)

- **UI layout and flow** — User rule: *"DO NOT MAKE ANY UNWARRANTED CHANGES TO THE UI WITHOUT MY PERMISSION."* When uncertain → suggest, don't implement.
- **Statistics scope** — Monthly-only change (CRUCIAL §9) is deferred until user approves; do not remove weekly/yearly without approval.
- **Persistence and recovery** — Follow KNOWN_TRUTHS; no new persistence paths or schema changes beyond planned migrations without approval.

---

## Subtle Preferences

*(Add here any specific preferences you discover or the user states — e.g. "prefer dark mode," "keep version visible in Settings," "no animation on stat cards.")*

- Version or useful info in Settings is acceptable (e.g. "Version 1.0.2" in About).
- Accessibility (contentDescription, labels) is always in scope as small improvements.
- One subtle improvement per category per loop (Kaizen); avoid overload.

---

*Read this before any research or changes in the Improvement Loop. Update when design intent or preferences change.*
