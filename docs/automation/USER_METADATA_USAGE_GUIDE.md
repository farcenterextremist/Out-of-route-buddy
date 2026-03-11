# User Metadata — Collection, Display, and Improvement Use

**Purpose:** Research-backed methods for collecting intricate metadata from users (including the developer as app user), creative suggestions for metadata display, and ways to use metadata to improve the app. Integrates with the Improvement Loop when Data/Metrics focus.

**References:** [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md), [LOOP_FOCUS_ROTATION.md](./LOOP_FOCUS_ROTATION.md), [docs/security/SECURITY_NOTES.md](../security/SECURITY_NOTES.md)

---

## 1. Collection Methods

### Privacy-First Principles

- **Opt-in by default** — User explicitly enables metadata collection; no collection without consent.
- **Lightweight by default** — Start with minimal fields; add more only when user opts in.
- **On-device first** — Store metadata locally; only send aggregated/anonymized data if user consents and Firebase is used.
- **No PII** — No coordinates, trip IDs linked to identity, or user-identifying content. Per [SECURITY_NOTES.md](../security/SECURITY_NOTES.md).

### Automatic (Opt-In)

| Method | What to collect | Use |
|--------|-----------------|-----|
| **Session context** | Screen name, timestamp, app version, timezone | Friction points, flow analysis |
| **Event timing** | Time from trip start → first mile added; trip end → history view | UX bottlenecks |
| **UI interaction counts** | Taps per screen, back-navigation frequency | Flow optimization |
| **Error/retry counts** | Validation failures, recovery dialog shown | Reliability improvement |

### In-App Feedback (Explicit)

| Method | What to collect | Use |
|--------|-----------------|-----|
| **Quick rating** | Thumbs up/down or 1–5 after trip end | Satisfaction signal |
| **Optional note** | Free-text (sanitized, no PII) at trip end | Qualitative insight |
| **Friction flag** | "Something was confusing" button with screen context | Pinpoint UX issues |
| **Feature request** | One-line suggestion with screen + timestamp | Prioritization |

### Trip-Level Metadata (Aggregated, No PII)

| Field | Example | Use |
|-------|---------|-----|
| Trip duration bucket | &lt;15 min, 15–60 min, 1–4 hr, 4+ hr | UI defaults, notifications |
| Miles bucket | &lt;10, 10–50, 50–200, 200+ | Stat card layout, export format |
| Time-of-day bucket | Morning, midday, evening, night | Notification timing |
| Day-of-week | Mon–Sun | Usage patterns |
| Period type | Daily, weekly, monthly | Calendar UX |
| Recovery used | Yes/No | Trip recovery UX improvement |

### Device/Environment (Anonymized)

| Field | Example | Use |
|-------|---------|-----|
| App version | 1.1 | Regression correlation |
| Android API level | 34 | Compatibility |
| Screen size bucket | Phone, tablet | Layout decisions |
| Locale | en-US | i18n priority |
| Dark/light mode | System preference | Theme defaults |

---

## 2. Creative Metadata Suggestions

### For OutOfRouteBuddy (Trip-Tracking, Fleet/Driver Context)

| Suggestion | Description | Improvement use |
|------------|-------------|-----------------|
| **Trip cadence** | Avg trips per week; streak (consecutive days with trips) | Gamification, reminders |
| **Peak usage hour** | When user most often starts trips | Suggest "Start trip?" at that time |
| **Miles distribution** | Histogram of trip lengths | Stat card defaults, export presets |
| **Recovery rate** | % of trips that used recovery flow | Improve recovery UX |
| **Period switch frequency** | How often user changes daily/weekly/monthly | Simplify period picker |
| **Calendar open count** | How many times calendar opened per session | Calendar placement, shortcuts |
| **Settings change frequency** | When user last changed a setting | Reduce settings clutter |
| **Notification interaction** | Did user tap notification to return to app? | Notification copy, timing |
| **Trip-end flow time** | Seconds from "End trip" tap to confirmation | Streamline end flow |
| **Validation failure screen** | Which screen had most validation errors | Fix validation UX |

### Developer-as-User Metadata

| Suggestion | Description | Use |
|-----------|-------------|-----|
| **Dogfooding log** | Developer notes: "Used app today; X was clunky" | Direct improvement input |
| **Session replay summary** | Screen sequence + duration (no PII) | Flow optimization |
| **Feature usage heatmap** | Which features used most/least | Prioritize polish |
| **Crash-free session length** | Sessions between crashes | Stability focus |

---

## 3. Metadata Display Suggestions

### In-App

| Location | Display | Purpose |
|----------|---------|---------|
| **Settings** | "Usage insights" toggle + summary (trips this week, avg trip length) | User sees their own patterns |
| **Trip history** | "Your patterns" card: peak day, avg miles | Engagement, transparency |
| **After trip end** | Optional "How was this trip?" (1–5 or emoji) | Satisfaction signal |
| **Stats/calendar** | "Most active day: Tuesday" | Personalization |
| **Debug/dev build** | Metadata export (JSON) for developer | Dogfooding analysis |

### Export / Offline

| Format | Content | Use |
|--------|---------|-----|
| **JSON export** | Aggregated metadata (no PII) | Import to spreadsheet, analysis |
| **CSV** | Trip counts, miles by period | Reporting |
| **Share** | Anonymized "My trip stats" summary | Optional social/share |

### Improvement Loop Summary

| Location | Content | Use |
|----------|---------|-----|
| **Phase 4.3 summary** | "Metadata available: Y/N. Key signals: X." | Data/Metrics focus runs |

---

## 4. Ways to Use Metadata to Improve the App

| Use | How |
|-----|-----|
| **Prioritize features** | High-usage features get polish; low-usage get deprioritized or simplified |
| **Fix friction** | Screens with high validation failures or back-navigation get UX review |
| **Improve defaults** | Most-used period type, time-of-day → set as default |
| **Notification timing** | Peak usage hour → suggest "Start trip?" at that time |
| **Recovery flow** | High recovery rate → simplify recovery dialog, add tips |
| **Calendar UX** | High calendar open count → add shortcut, improve placement |
| **Settings** | Rarely changed settings → move to "Advanced" or hide |
| **Trip-end flow** | Long end-flow time → reduce steps, add "End & clear" shortcut |
| **A/B decisions** | Metadata informs which variant to ship (when A/B is added) |
| **Regression detection** | New version has different usage pattern → investigate |

---

## 5. Implementation Tiers (Improvement Loop)

| Tier | Task | When |
|------|------|------|
| **Light** | Research metadata methods; document one new suggestion in this guide | Data/Metrics focus |
| **Medium** | Add one opt-in metadata collection point (e.g., trip-end rating); store on-device | Data/Metrics focus |
| **Medium** | Add one metadata display (e.g., "Trips this week" in Settings) | Data/Metrics focus |
| **Heavy** | Full metadata dashboard; Firebase custom events (opt-in); export | FUTURE_IDEAS |

---

## 6. Privacy and Compliance

- **Declare in Data safety** — If any metadata is sent off-device, update Play Console Data safety form.
- **No coordinates in metadata** — Trip metadata is aggregated (counts, buckets); no lat/lng.
- **User control** — Settings toggle: "Help improve the app (usage insights)" — off by default.
- **Transparency** — In-app text: "We collect anonymous usage patterns to improve the app. No trip details or location data are sent."

---

## 7. Integration with Improvement Loop

- **Phase 0.1:** When Data/Metrics focus, read this guide; consider one metadata task.
- **Phase 1–3:** When Data/Metrics focus, add one Light or Medium metadata task (research, one collection point, or one display).
- **Phase 4.3:** If metadata was collected/displayed, note in summary: "Metadata: [what was added]. Next: [suggestion]."
- **File Organizer:** When recommending next focus, use metadata signals if available (e.g., "Recovery rate high → consider UI/UX focus for recovery flow").
