# Notification Features: The Three Types

OutOfRouteBuddy uses **three distinct notification-related surfaces** during trip tracking. It's important to keep them separate so we don't double-show, mislabel, or break recovery.

---

## 1. Tiny icon (status bar icon)

**What it is:** The **small icon in the top status bar** (next to time, battery, etc.) while a trip is active.

**How it appears:** When `TripTrackingService` runs as a **foreground service**, Android displays that service's notification. The **small icon** of that notification is what shows in the status bar—our "tiny icon."

**Source:**

- **Service:** [TripTrackingService](../app/src/main/java/com/example/outofroutebuddy/services/TripTrackingService.kt)
- **Drawable:** `R.drawable.ic_notification_truck`
- **Trigger:** `startForeground(NOTIFICATION_ID, createNotification(...))` (same notification as the pull-down).

**Behavior:**

- Visible whenever a trip is in progress and the service is in the foreground.
- No separate API: it's the status-bar representation of the **same** notification that appears in the pull-down.
- After a crash/restart, `handleServiceRestart()` restores the foreground notification, which restores both the tiny icon and the pull-down row.
- Foreground tracking updates are posted with `setOnlyAlertOnce(true)` and a low/silent channel so location refreshes do not beep repeatedly.

**Summary:** One notification object → status bar shows its small icon (tiny icon) and pull-down shows the full row.

---

## 2. Pull-down notification (notification shade / drawer)

**What it is:** The **full notification row** the user sees when they **pull down the notification shade** (notification drawer). Title, body text, and icon.

**Source:**

- **Service:** [TripTrackingService](../app/src/main/java/com/example/outofroutebuddy/services/TripTrackingService.kt)
- **Method:** `createNotification(text, promptState)`
- **Channel:** `BuildConfig.NOTIFICATION_CHANNEL_ID` ("Trip Tracking Channel")
- **IDs:**

  - Foreground trip: `BuildConfig.NOTIFICATION_ID` (1001)
  - Trip ended one-shot: `COMPLETION_NOTIFICATION_ID` (1002)
  - Errors: `ERROR_NOTIFICATION_ID` (1003)

**Content (from [strings.xml](../app/src/main/res/values/strings.xml)):**

- **Title:** "Out of Route" (`notification_trip_title`)
- **Body:**

  - "Trip in progress" (`trip_notification_in_progress`)
  - "Trip ending" (`trip_notification_ending`) when trip-ended is detected
  - "Trip ended" (`trip_notification_ended`) on the one-shot completion notification after stop

**Behavior:**

- **Ongoing** while trip is active: tap opens MainActivity; when state is "Trip ending," tap can open directly to the "End trip?" dialog (`EXTRA_OPEN_TRIP_ENDED_DIALOG`).
- **One-shot** "Trip ended" after user stops the trip (`postTripEndedCompletionNotification()`), auto-cancel.
- Error notifications (e.g. "Trip Tracking Error") use `ERROR_NOTIFICATION_ID` and are separate from the main trip notification.

**Tests:** [TripTrackingPullDownNotificationTest](../app/src/androidTest/java/com/example/outofroutebuddy/services/TripTrackingPullDownNotificationTest.kt) checks strings and state transitions for this notification.

**Summary:** This is the main "Trip in progress" / "Trip ending" / "Trip ended" row in the shade; same notification that provides the tiny icon.

---

## 3. Bubble notification (floating overlay + fallback)

**What it is:**

- **Primary:** A **floating, draggable bubble** drawn on top of other apps when the app thinks the trip may be complete ("trip ended" detected).
- **Foreground route:** If the app is already visible, the detector can skip the bubble and open the same in-app `Have you arrived?` confirmation directly.
- **Fallback:** If the app **cannot draw over other apps** (no overlay permission), a **one-shot notification** in the shade instead ("Have you arrived?").

**Source:**

- **Service:** [TripEndedOverlayService](../app/src/main/java/com/example/outofroutebuddy/services/TripEndedOverlayService.kt)
- **Bubble:** `WindowManager` + `TYPE_APPLICATION_OVERLAY`, view with **app icon** (`R.mipmap.ic_launcher`), sized ~120dp.
- **Fallback notification:** Same service, `FALLBACK_NOTIFICATION_ID` (9002), channel `trip_ended_overlay_v2`.

**When it appears:**

- When [TripEndedDetector](../app/src/main/java/com/example/outofroutebuddy/services/TripEndedDetector.kt) decides "trip may be complete" and emits either:

  - `showArrivalPrompt` for the earlier foreground prompt, or
  - `showBubble` for the stronger background bubble/fallback path.
- `TripEndedOverlayService.startWhenTripActive()` is started by TripTrackingService when a trip starts so it can show the bubble when the detector fires.

**Bubble behavior:**

- **Tap:** Open app and `Have you arrived?` dialog; bubble is dismissed.
- **Long-press or drag to bottom zone:** Dismiss without opening app; trip continues ("user chose no / continue").
- **Accessibility:** Bubble is focusable and has a content description for TalkBack.

**Fallback behavior:**

- One-shot notification: title "Have you arrived?", body "Open the app to end the trip or keep tracking."
- Tap opens MainActivity with `EXTRA_OPEN_TRIP_ENDED_DIALOG`.
- Deduplicated (e.g. min interval 60s) to avoid spam if overlay is denied.

**Foreground behavior:**

- If the detector reaches prompt confidence while `MainActivity` is already visible, `TripEndedOverlayService` reuses the same app-entry intent and opens the in-app confirmation without showing a bubble.
- Prompt taps/actions are logged with coarse buckets only (`foreground`, `overlay`, `fallback`) so tuning can improve without storing raw location traces.

**Cleanup:**

- When a **new trip starts**, TripTrackingService calls `TripEndedOverlayService.cancelStaleNotifications()` so the pull-down shows only "Trip in progress" and no leftover bubble/fallback.

**Summary:** Bubble = overlay on top of other apps; fallback = normal notification in the shade when overlay isn't allowed. Both are "trip may be complete, confirm in app."

---

## Quick comparison

| Feature | Where it appears | Purpose | Source |
| --- | --- | --- | --- |
| **Tiny icon** | Status bar (top) | Indicate trip is active | TripTrackingService (same notification as pull-down) |
| **Pull-down** | Notification shade | Show trip state and open app | TripTrackingService |
| **Bubble** | Floating overlay (or fallback notification) | Prompt arrival confirmation | TripEndedOverlayService |

---

## Implementation notes

- **Single foreground notification:** Only TripTrackingService runs as foreground and shows one ongoing notification. That one notification provides both the **tiny icon** and the **pull-down** row. TripEndedOverlayService does **not** run as foreground when only monitoring (avoids double notification in shade).
- **Icon:** The truck icon `ic_notification_truck` is used for the foreground trip notification (tiny icon + pull-down), error notifications, completion notification, and the trip-ended fallback notification. The **bubble** uses the **app icon** (`ic_launcher`) so it’s immediately recognizable.
- **Recovery:** After a process kill, `TripTrackingService.handleServiceRestart()` restores the foreground notification (and thus the tiny icon and pull-down) and restarts overlay monitoring so the bubble can appear again when trip-ended is detected.

---

*See also: [ARCHITECTURE.md](ARCHITECTURE.md) (configuration), [TRIP_ENDED_OVERLAY_PLAN_PROMPT.md](TRIP_ENDED_OVERLAY_PLAN_PROMPT.md) (overlay design).*
