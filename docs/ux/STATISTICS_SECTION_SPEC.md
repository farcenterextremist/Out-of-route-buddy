# Statistics Section — UX Spec

**Audience:** Front-end Engineer, QA Engineer  
**Purpose:** Single source of truth for the statistics section behavior and accessibility.  
**Related:** CRUCIAL #9 (Statistics: monthly only), `docs/CRUCIAL_IMPROVEMENTS_TODO.md`, `docs/agents/COMPREHENSIVE_AGENT_TODOS.md`.

---

## 1. Scope: Monthly Only, One Aggregate View

- The statistics section shows **monthly** trip statistics only.
- **No weekly view.** Do not display, wire, or test a "Weekly" period or tab.
- **No yearly view.** Do not display, wire, or test a "Yearly" period or tab.
- **One aggregate view:** A single view that presents the current month’s aggregated stats (e.g. miles driven, out-of-route %, or whatever metrics the app exposes for the month).
- Period selection (if any) is **monthly** or **custom** (e.g. pick a month via calendar). No weekly/yearly options in the period picker or UI state.
- Front-end: Load and bind only monthly statistics; remove weekly/yearly from ViewModel state and layouts.
- QA: Assert only on monthly statistics; remove or adjust tests that expect weekly/yearly; mock only `getMonthlyTripStatistics()` where relevant.

---

## 2. Implementation & Test References

- **Front-end:** `TripInputViewModel.kt` (~231–244, ~1012–1014), `presentation/viewmodel/`, `res/` (e.g. `statistics_row.xml`, any period picker).
- **Back-end:** `TripRepository` / `DomainTripRepositoryAdapter` — keep `getMonthlyTripStatistics()`; remove or deprecate weekly/yearly.
- **QA:** `TripStatisticsWiringTest.kt`, `TripInputViewModelIntegrationTest.kt`, `TestConfig.kt` (statistics_button description).

---

## 2b. Copy: Current month vs custom period

When the statistics row shows data, the period label should clearly indicate the source:

- **Standard mode (current month):** Label or tooltip: "Statistics for [Month Year]" or "Current month: [Month Year]". Users should understand these numbers are for the full calendar month.
- **Custom period:** Label or tooltip: "Custom period: [Start date] – [End date]" or "Selected period: [Start] to [End]". Users should understand these numbers are for the chosen date range (e.g. Thursday–Friday in CUSTOM mode).

Add to `strings.xml` or tooltips when implementing. Example keys: `statistics_period_current_month`, `statistics_period_custom`.

---

## 3. Hierarchy and contrast (label vs value)

- **Primary vs secondary:** Use a clear visual hierarchy: statistic *values* (numbers) as primary text (e.g. larger or bolder); *labels* (e.g. "Miles this month") as secondary text so users scan values first, then labels. Document or implement in styles (e.g. `textAppearance` or theme).
- **Contrast:** Keep text and key UI elements in the statistics block at least 4.5:1 contrast against the background (WCAG AA). Check in both light and dark themes if the app supports them.

## 4. Accessibility — Statistics Section: 3 Quick Wins

- **Labels:** Ensure every statistic value and the section heading have clear, concise labels (e.g. `contentDescription` / `accessibilityLabel`) so screen readers announce what each number means (e.g. "Miles this month: 123.4").
- **Contrast:** (See §3.) Check in both light and dark themes.
- **Touch targets:** Buttons and tappable areas in the statistics section (e.g. "View" or period picker) must meet a minimum touch target size of 48×48 dp so they are easy to tap and comply with accessibility guidelines.

---

*When this spec is implemented, Front-end and QA can use it to verify behavior and update tests accordingly.*
