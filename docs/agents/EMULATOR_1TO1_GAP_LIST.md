# Emulator 1:1 gap list

Single checklist of gaps between [1TO1_SPEC.md](../../phone-emulator/1TO1_SPEC.md) and [EMULATOR_PERFECTION_PLAN.md](../../phone-emulator/EMULATOR_PERFECTION_PLAN.md). Check off when implemented.

**Source:** Phase P task P-FO-1. Execution plan: [EMULATOR_1TO1_AND_EDITING_100_PLAN.md](EMULATOR_1TO1_AND_EDITING_100_PLAN.md).

---

## Visual / layout

| # | Gap | Perfection plan ref | Status |
|---|-----|---------------------|--------|
| 1 | Settings icon: SVG gear (no emoji) matching ic_settings | §1.1 | ☐ |
| 2 | Toolbar: 16dp start/end margin, 8dp gap | §1.1 | ☐ |
| 3 | Action bar height 56dp | §1.1 | ☐ |
| 4 | Statistics: arrow icon (chevron), not text ▲/▼ | §1.2 | ☐ |
| 5 | Statistics: arrow on right (iconGravity textEnd) | §1.2 | ☐ |
| 6 | Statistics: chevron down when collapsed, up when expanded | §1.2 | ☐ |
| 7 | Inputs: numberDecimal, digits 0-9 and . | §1.3 | ☐ |
| 8 | Inputs: no stroke on light (card_white_rounded) | §1.3 | ☐ |
| 9 | Inputs: 2dp elevation | §1.3 | ☐ |
| 10 | Pause button: 48dp, right of Start, only when trip active | §1.4, §2.1 | ☐ |
| 11 | Start vs End Trip button text by state | §1.4, §2.3 | ☐ |
| 12 | Progress bar below Start when loading | §1.4, §2.2 | ☐ |
| 13 | Today's Info: 10dp padding, row label left / value right | §1.5 | ☐ |
| 14 | Statistics: divider light_gray_background (#F5F5F5 / #2C2C2C) | §1.6 | ☐ |
| 15 | Statistics: labels secondary color, values primary | §1.6 | ☐ |
| 16 | Dark: card #2C2C2C, gradient #1E1E1E→#121212, toolbar #1E1E1E | §1.7 | ☐ |

---

## Behavior / modals

| # | Gap | Perfection plan ref | Status |
|---|-----|---------------------|--------|
| 17 | Settings button opens modal (Mode, Templates, Help & Info) | §3.1 | ☐ |
| 18 | Start Trip validates inputs; toggles to End + shows Pause | §3.2 | ☐ |
| 19 | End Trip opens confirmation (End / Clear / Continue) | §3.3 | ☐ |
| 20 | Pause/Resume toggles icon and state; toast | §3.4 | ☐ |
| 21 | Statistics "View" opens period picker / selector | §3.5 | ☐ |
| 22 | Input validation error state (red border) | §3.6 | ☐ |

---

## Editor / Cursor

| # | Gap | Perfection plan ref | Status |
|---|-----|---------------------|--------|
| 23 | Every visible text/control has data-edit-path, editable | §5.1 | ☐ |
| 24 | Right-click empty → "Add element here" | §5.1 | ☐ |
| 25 | Long-press 500–600 ms opens context menu on touch | §5.1 | ☐ |

---

*Update status as Phase P and Phase E tasks complete.*
