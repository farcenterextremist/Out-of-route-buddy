# User Preferences & Design Intent

**Purpose:** Capture the user's subtle preferences and original design intent so the Improvement Loop stays aligned. Read this **first** at the start of every loop—"get personal with the code"—before making any changes.

**Rule:** Do not stray from the original design. When in doubt, ask.

---

## Must Not Change (Without Permission)

| Preference | Source | Note |
|------------|--------|------|
| **No unwarranted UI changes** | User rule | Do not make UI changes without explicit permission. See [SCOPE_AND_BOUNDARIES.md](../SCOPE_AND_BOUNDARIES.md). |
| **Original UI layout** | LOOP_TIERING | Stay on track; do not drift from the original layout. |
| **Statistics: monthly only** | User preference (CRUCIAL §9) | Remove weekly/yearly only after user approval. |
| **No social features, no ads** | GOAL_AND_MISSION | Never. |
| **Offline-first** | GOAL_AND_MISSION | Prefer local; no cloud-first. |

---

## Design Intent (Original)

- **Trip tracking:** Loaded, bounce, actual miles. Start → End → Clear.
- **Calendar & stat cards:** Period-based (daily, weekly, monthly). Green start, red end.
- **Settings:** Theme, units, GPS, notifications. Keep simple.
- **Recovery:** Trip state persistence; graceful recovery after force-stop.

---

## Subtle Preferences (Add Over Time)

*The user can add preferences here as they discover them. Examples:*

| Area | Preference | Example |
|------|------------|---------|
| **Colors** | Prefer X over Y | "Keep toolbar background as-is" |
| **Spacing** | Tight vs loose | "8dp grid; don't add extra padding" |
| **Typography** | Font sizes, weights | "Body text 14sp; headings 18sp" |
| **Flow** | Order of screens, buttons | "Trip input stays primary; history secondary" |
| **Wording** | Labels, strings | "Use 'End trip' not 'Finish trip'" |
| **Code style** | Kotlin, naming | "Prefer `viewModel` over `vm`" |

---

## How to Use (Improvement Loop)

1. **Phase 0.0a:** Read this doc first. Note: "Design intent: X. Must not change: Y. User preferences: Z."
2. **Before any UI change:** Check against "Must Not Change" and "Subtle Preferences." If uncertain, suggest in summary; do not implement.
3. **When researching design:** Compare proposals to this doc. Prefer options that align with original intent.
4. **Summary:** If a preference was clarified this run, add it here (Light task).

---

*Update when the user expresses new preferences. File Organizer can propose additions from summary discussions.*
