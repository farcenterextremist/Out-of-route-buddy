# Screenshot review workflow

**Purpose:** Review UI quality using real screenshots so frontend judgments are based on visual evidence instead of memory or opinion.

**Related:** `docs/ux/PLEASANTNESS_AND_FLOW_STANDARD.md`, `docs/ux/UI_CONSISTENCY.md`, `docs/ux/ACCESSIBILITY_CHECKLIST.md`, `app/src/test/java/com/example/outofroutebuddy/screenshots/ThemeScreenshotTest.kt`.

---

## What this workflow is for

Use this workflow when:

- reviewing whether a screen looks pleasant or rough
- comparing before/after UI polish
- checking light/dark theme clarity
- validating user flow states such as empty/loading/error/expanded

This is a **manual evidence workflow**, not a full screenshot regression test suite.

It also serves as the visual-evidence input for the sandboxed [LOOP_COUNCIL_SANDBOX.md](../automation/LOOP_COUNCIL_SANDBOX.md) model when a connected device or emulator is available.

---

## Recommended evidence pack

For one screen or flow, try to gather:

1. **Primary state**
   - The main/default screen state
2. **Alternate theme**
   - The same state in light and dark when relevant
3. **Critical interaction state**
   - Expanded, selected, error, loading, empty, or confirmation state
4. **Before/after pair**
   - If reviewing a UI change, collect both

---

## Capture methods

Preferred sources:

- User-provided screenshots
- Agent-captured device screenshots using `adb exec-out screencap -p`
- Existing screenshot artifacts from tests or release checks if available later

If using a connected Android device, the usual capture pattern is:

```powershell
adb exec-out screencap -p > screenshot.png
```

Then review the image directly.

---

## Review steps

1. **Name the screen and task**
   - Example: "Trip Input screen while active trip is running"

2. **Check the pleasantness rubric**
   - Use `PLEASANTNESS_AND_FLOW_STANDARD.md`

3. **Check accessibility basics**
   - Contrast, readability, tap target plausibility, clutter, state clarity

4. **Check flow cues**
   - Is the next action obvious from the screenshot?
   - Are primary and secondary actions visually distinct?

5. **Compare before/after**
   - What improved?
   - What still feels weak?

---

## Things to look for

- Competing focal points
- Crowded spacing
- Inconsistent padding or icon treatment
- Weak contrast in dark mode
- Destructive actions blending with safe actions
- Sections that look visually heavy or noisy
- States that are technically correct but emotionally rough

---

## Output block

```markdown
## Screenshot Review

- Screen: [name]
- Evidence set: [before/after, light/dark, state names]
- Pleasantness score: X/20
- Strongest visual trait: [one line]
- Weakest visual trait: [one line]
- Flow clarity: [clear | mostly clear | confusing]
- Recommendation: [acceptable | polish next | structural proposal only]
```

For council-style review, this block should be paired with durability/build evidence rather than used by itself.

---

## Current limitation

The project does **not** yet have an enabled screenshot-regression system. `ThemeScreenshotTest.kt` is still deferred pending Paparazzi setup.

So for now:

- use screenshots as review evidence,
- not as a strict automated golden-baseline system.
