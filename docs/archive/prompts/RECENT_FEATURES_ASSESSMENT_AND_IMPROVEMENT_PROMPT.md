# Recent Features: Assessment & Improvement Plan Prompt

This document lists the three recently added features, scores their current code buildout, and provides a **detailed prompt** you can use to turn into a concrete improvement plan.

---

## 1. Feature list (recent additions)

| # | Feature | Description |
|---|--------|-------------|
| 1 | **Auto drive detect** | Classifies GPS updates as DRIVING vs WALKING_OR_STATIONARY; only accumulates distance when DRIVING so walking/stopped segments are auto-excluded without the user tapping Pause. |
| 2 | **End-trip notifications overlay** | When trip-ended is detected (miles driven are approaching, equal to, or over allotted, min duration, etc.), a floating bubble appears; tap opens app and "End trip?" dialog (Yes = end trip, No = continue). |
| 3 | **Pull-down menu (notification shade)** | When a trip is active, the system pull-down shows "Trip in progress"; when trip-ending is detected it shows "Trip ending" so the user sees status without opening the app. |

---

## 2. Code buildout summary & scores

### 2.1 Auto drive detect — **78%**

**What’s in place**

- **DriveStateClassifier** (`DriveStateClassifier.kt`): Speed-based classification with highway context (sustained high speed → treat current slow as DRIVING). Configurable thresholds in `ValidationConfig` (DRIVE_DETECT_WALKING_SPEED_MPH, DRIVE_DETECT_WALKING_MIN_DURATION_MS, DRIVE_DETECT_HIGHWAY_*). Reset on trip end.
- **TripTrackingService** uses classifier on each validated location; only accumulates distance when `!isPaused && driveState == DRIVING`; exposes `driveState` as `StateFlow<DriveState>`.
- **TripInputViewModel** observes `TripTrackingService.driveState` and shows status: "Not counting: walking/stopped detected - Distance: X mi" when WALKING_OR_STATIONARY.
- **Tests**: `DriveStateClassifierTest` (no speed, walking band, duration, highway context, reset, thresholds); `DriveStateAccumulationRuleTest` (accumulate only when not paused and DRIVING).

**Gaps**

- **lastLocation** parameter in `classify()` is unused (Detekt); no bearing/accuracy usage.
- No settings UI to tune DRIVE_DETECT_* (e.g. walking speed, min duration); all from ValidationConfig.
- No analytics or logging of drive-state transitions for tuning or support.
- Edge cases: very low speed with poor GPS (0 speed) may flip after 30s; no explicit "unknown" state when speed is missing and no recent history.

**Score rationale**: Core logic, integration, and tests are solid. Missing: configurability, observability, and a few edge-case/cleanup items.

---

### 2.2 End-trip notifications overlay — **82%**

**What’s in place**

- **TripEndedDetector**: Combines trip state + `TripTrackingService.tripMetrics`; pure logic in `shouldShowBubble()` (allotted miles, 98% / tolerance, min duration, min actual miles, cooldown after "No, continue"). Emits `showBubble` at most once per trip; cooldown after user chooses "No."
- **TripEndedOverlayService**: Foreground service; on `showBubble` → notifies TripTrackingService (ACTION_TRIP_ENDING_DETECTED), shows draggable bubble (WindowManager, TYPE_APPLICATION_OVERLAY). Tap → MainActivity with EXTRA_OPEN_TRIP_ENDED_DIALOG; "Yes" → endTrip + dismiss; "No" → notifyUserChoseNoContinue + dismiss. Hilt-injected detector.
- **ViewModel**: Starts overlay via `TripEndedOverlayService.startWhenTripActive(application)` when trip starts; dismisses on end trip.
- **TripInputFragment**: Shows "End trip?" dialog when intent has EXTRA_OPEN_TRIP_ENDED_DIALOG; reuses End Trip flow; dismissBubble / notifyUserChoseNoContinue on Yes/No.
- **Settings**: Overlay permission preference opens system overlay settings.
- **Tests**: `TripEndedOverlayScenarioTest` (shouldShowBubble: inactive, min miles, min duration, 98%, tolerance, cooldown, config); instrumented `TripEndedOverlayInstrumentedTest` (launch with extra → dialog, tap Yes/No → dismiss).

**Gaps**

- **Fallback when overlay permission denied**: Plan doc says "in-app notification only if overlay denied"; code only logs "Overlay permission not granted" and does not show a standard notification or in-app fallback.
- **Long-press / drag to bottom 'X' circle**: Plan mentioned optional long-press to dismiss without opening app; also dragging to the bottom 'X' circle.
- **Accessibility**: Content description present; focusability and dismissibility for assistive tech could be documented or improved.
- **Detector lifecycle**: `onTripEnded()` called from overlay service on dismiss; ensure it is also called when user ends trip in-app so "once per trip" resets correctly (currently wired via dismissBubble in ViewModel).

**Score rationale**: Detection, overlay UX, dialog flow, and tests are strong. Main gaps: permission-denied fallback and optional UX (long-press dismiss).

---

### 2.3 Pull-down menu (notification shade) — **72%**

**What’s in place**

- **TripTrackingService**: `createNotification()` builds the foreground notification (title "Out of Route Buddy", content text parameter). On start/resume → "Trip in progress"; on ACTION_TRIP_ENDING_DETECTED → "Trip ending". KDoc states this is the system pull-down (notification shade).
- **TripEndedOverlayService** calls `notifyTrackingServiceTripEnding()` before showing the bubble so the shade updates to "Trip ending."
- **Strings**: `trip_notification_in_progress`, `trip_notification_ending` (values in tests: "Trip in progress", "Trip ending"); need to confirm in `res/values/strings.xml`.
- **Tests**: `TripTrackingPullDownNotificationTest` — string resources exist and match expected text; ACTION_TRIP_ENDING_DETECTED handled without crash; start → pause → resume does not crash. No assertion of actual notification content (Robolectric limitations).

**Gaps**

- **Notification content**: Title is hardcoded "Out of Route Buddy" instead of a string resource (i18n/consistency).
- **Channel**: TripTrackingService uses its own NOTIFICATION_CHANNEL_ID ("TripTrackingChannel"); BuildConfig has NOTIFICATION_CHANNEL_ID unused — minor consistency/cleanup.
- **No tap action**: Tapping the shade notification could open the app to TripInputFragment (or trip-ended dialog when in "Trip ending"); currently ongoing notification may not have explicit content intent.
- **Tests**: Coverage is contract and behavior only; no test that notification manager receives the exact "Trip in progress" / "Trip ending" text (acceptable given test env limits).

**Score rationale**: Feature works and is documented; missing: string resource for title, optional tap action, and small config consistency.

---

## 3. Summary scores

| Feature | Score | Summary |
|--------|-------|--------|
| Auto drive detect | **78%** | Solid core and integration; add configurability, observability, and small cleanups. |
| End-trip overlay | **82%** | Strong detection and overlay flow; add permission-denied fallback and optional long-press dismiss. |
| Pull-down notification | **72%** | Correct behavior and docs; add i18n for title, optional tap action, and consistency. |

---

## 4. Detailed prompt for an improvement plan

Use the text below as the basis for a structured improvement plan (tasks, owners, acceptance criteria). Adjust product priorities (e.g. i18n vs. tap action) as needed.

---

**Context:** OutOfRouteBuddy has three recently shipped features: (1) **Auto drive detect** — classify location updates as driving vs walking/stationary and only count miles when driving; (2) **End-trip notifications overlay** — floating bubble when trip-ended is detected, tap opens "End trip?" dialog; (3) **Pull-down notification** — notification shade shows "Trip in progress" or "Trip ending" while a trip is active.

**Goal:** Produce a prioritized improvement plan that addresses gaps in buildout, maintainability, UX, and testing without changing the core behavior of these features. The plan should be implementable in small increments (e.g. by feature or by theme).

**Scope — Auto drive detect**

- Add a way for users or testers to tune drive-detect thresholds (e.g. walking speed MPH, min walking duration, highway lookback), either via Settings or a debug/config screen, backed by ValidationConfig or persistent preferences.
- Resolve the unused `lastLocation` parameter in DriveStateClassifier (use it for better classification or remove and document why).
- Add lightweight observability: log or emit drive-state transitions (DRIVING ↔ WALKING_OR_STATIONARY) with timestamps for debugging and support; consider optional analytics event when state flips.
- Document or handle edge case: when speed is unavailable and recent history is empty or has no speed, current behavior is "prefer DRIVING"; document this and consider an explicit "unknown" state if product wants different UX later.
- Add or extend unit tests for edge cases (e.g. exactly at threshold, reset mid-trip, history boundary).

**Scope — End-trip overlay**

- When overlay permission is denied, implement a fallback: show a standard notification (or in-app prompt) that "Trip may be complete – open app to confirm" instead of only logging; ensure tapping that notification opens the same "End trip?" flow.
- Optionally add long-press (or secondary action) on the bubble to dismiss without opening the app, with optional "Continue trip in background" copy.
- Verify and document accessibility: bubble focus order, content description, and dismissibility for screen readers; add or adjust TalkBack hints if needed.
- Ensure detector reset is correct when the user ends the trip from in-app (not only when dismissing the bubble) so the next trip can trigger the overlay again; add a test or scenario that covers this.
- Add a simple test that, when overlay permission is denied, the fallback notification is shown (or the appropriate code path is exercised).

**Scope — Pull-down notification**

- Move the notification title "Out of Route Buddy" to a string resource (e.g. `notification_title` or reuse app name) and use it in TripTrackingService so the shade is i18n-ready and consistent.
- Optionally add a content intent (PendingIntent) on the foreground notification so that tapping the shade notification opens the app (e.g. to TripInputFragment or to the trip-ended dialog when text is "Trip ending").
- Resolve duplication: either use BuildConfig.NOTIFICATION_CHANNEL_ID and a single channel ID constant for the trip-tracking notification or document why the service keeps its own constant; avoid dead constants in BuildConfig.
- Keep or add tests that verify the notification service handles start/pause/resume and ACTION_TRIP_ENDING_DETECTED without crash; document that full notification content assertion is limited in unit tests and covered by manual/UI testing.

**Out of scope for this plan (but we're doing it anyway)**

- Changing the core algorithms (e.g. drive-state formula, trip-ended thresholds) beyond making them configurable.
- Adding new features (e.g. different overlay designs, new notification types) unless they are the fallback for permission-denied overlay.
- Large refactors of TripTrackingService or TripEndedOverlayService; only targeted changes to support the above.

**Deliverables**

- A prioritized list of tasks (with "must have" vs "nice to have" or P1/P2).
- For each task: short description, files/layers to touch, acceptance criteria, and optional test strategy.
- Any new string resources, preferences, or manifest changes called out.
- Recommendation on whether to batch by feature (drive detect, overlay, notification) or by theme (configurability, fallbacks, i18n, tests).

---

**End of prompt.** Use this to generate a concrete improvement plan (e.g. in a separate doc or as tickets).
