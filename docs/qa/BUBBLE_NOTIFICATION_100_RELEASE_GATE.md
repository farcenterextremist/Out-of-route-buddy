# Bubble Notification Release Gate (100% Plan)

This checklist defines the release gate for the trip-ended bubble feature after the 100% implementation plan.

## Automated Matrix (executed)

- Build/install:
  - `:app:compileDebugKotlin` passes.
  - `:app:installDebug` passes on `SM-S911U`.
- Focused unit/Robolectric coverage:
  - `TripEndedOverlayScenarioTest` passes (cooldown and threshold scenarios).
  - `TripEndedOverlayFallbackTest` passes (fallback contract and dedupe logic).
  - `TripTrackingPullDownNotificationTest` passes (resource contract and notification-state mapping).
- Crash scan:
  - `adb logcat -d -v time AndroidRuntime:E *:S` shows no fatal crashes after validation run.

## Manual Matrix (must be completed on-device before release)

1. Active trip + dark mode:
   - Start trip.
   - Confirm pull-down text shows `Trip in progress`.
2. End-near detection:
   - Let detector trigger.
   - Confirm pull-down text becomes `Trip ending`.
   - If the app is foregrounded, confirm the in-app `Have you arrived?` prompt appears without needing the overlay bubble.
   - If the app is backgrounded, tap notification/bubble opens app and the same arrival-confirmation path.
3. Continue path:
   - Choose `Keep tracking` from any arrival-prompt entry point.
   - Confirm pull-down text returns to `Trip in progress`.
   - Confirm no duplicate/stuck `Trip ending` state.
4. Stopped-short path:
   - End a test trip noticeably below loaded+bounce miles.
   - Confirm a sustained parked stop can still reach the `Have you arrived?` prompt.
5. Re-alert path:
   - After continue cooldown, confirm detector can re-alert while trip remains active.
6. Overlay denied path:
   - Disable overlay permission.
   - Confirm fallback notification appears and opens app to the `Have you arrived?` prompt.
7. Theme switch stability:
   - With active trip, switch dark/light modes repeatedly.
   - Confirm no crash and notification text remains state-correct.
8. End/clear path:
   - End trip and clear trip.
   - Confirm services stop cleanly and no overlay/bubble remains.

## Release Gate Criteria

Mark feature as 100% only when all conditions below are true:

- No AndroidRuntime fatal crashes in the above manual matrix.
- Notification text is fully state-driven:
  - `Trip in progress` during normal/continued tracking.
  - `Trip ending` only after end-near detection.
- Continue action reliably resets ending state from all entry points (foreground prompt, bubble gestures, fallback notification path).
- Overlay add failures always degrade to fallback notification (no silent failure).
- Detector tuning analytics remain coarse/bucketed only; no raw coordinates or exact routes are logged for prompt feedback.
- Focused tests listed above remain green in CI/local runs.
