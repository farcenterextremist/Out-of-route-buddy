# Plan: Trip Ended Notification Overlay (Messenger-Style Bubble)

**Purpose:** Introduce a “Trip ended” notification overlay in OutOfRouteBuddy that appears as a floating bubble (similar to Facebook Messenger’s chat head). The bubble uses the provided truck silhouette image, brings the user into the app on tap, and shows a confirmation dialog to either complete the trip or continue it. Trip-ended detection uses several signals (dynamic tracking, miles driven vs allotted, and optional heuristics) so the alert is accurate and not annoying.

**Related:** [TripInputFragment](app/src/main/java/com/example/outofroutebuddy/presentation/ui/trip/TripInputFragment.kt) (End Trip dialog, `showEndTripConfirmation()`, `viewModel.endTrip()`), [TripInputViewModel.endTrip()](app/src/main/java/com/example/outofroutebuddy/presentation/viewmodel/TripInputViewModel.kt), [UnifiedTripService](app/src/main/java/com/example/outofroutebuddy/services/UnifiedTripService.kt) (trip state, `actualMiles`, `loadedMiles`, `bounceMiles`), [TripTrackingService](app/src/main/java/com/example/outofroutebuddy/services/TripTrackingService.kt).

---

## 1. Bubble asset and overlay UX

- **Bubble icon:** Use the attached truck silhouette image (white semi-truck on black) as the overlay bubble. Copy or reference it from:
  - Workspace asset: `assets/c__Users_brand_AppData_Roaming_Cursor_User_workspaceStorage_.../Gemini_Generated_Image_akeh34akeh34akeh-63582611-5830-46c4-b014-4c7327b40826.png`
  - Add to the app as a drawable (e.g. `app/src/main/res/drawable/ic_trip_ended_bubble.png`) so it can be used in an overlay view. Prefer a format that supports transparency (e.g. PNG with transparent background) so the bubble can float on any screen; if the source is white-on-black, consider using it as a mask or extracting the truck shape for a circular/rounded bubble background.
- **Overlay behavior (Messenger-style):**
  - When “trip ended” is detected (see §3), show a small floating view (bubble) on top of other apps, draggable so the user can move it (e.g. to the edge). It should not block critical UI; size and position should follow platform best practices for overlay/bubble UIs.
  - Tapping the bubble: bring the app to the foreground and open the **Trip ended?** confirmation flow (see §2). Do not end the trip automatically on tap; always show the dialog.
  - Optionally: long-press or secondary action to dismiss the bubble without opening the app (e.g. “Continue trip in background” or “Dismiss for now”). If dismissed, the user must end the trip manually later from inside the app.
- **Permissions:** Overlay/bubble UI on Android typically requires `SYSTEM_ALERT_WINDOW` (draw over other apps). Document the permission request and fallback (e.g. in-app notification only if overlay is denied).
- **Accessibility:** Provide content description for the bubble (e.g. “Trip ended – tap to open and confirm”) and ensure the overlay is focusable and dismissible for accessibility.(the plan is to incorporate notofication alerts after this feature is complete)

---

## 2. In-app flow when user taps the bubble

- **Entry:** User taps the floating bubble → app comes to foreground; navigate to the trip screen (e.g. `TripInputFragment` or the tab/fragment that shows the current trip and End Trip).
- **Dialog – “End trip?”:** Show a dialog similar to the existing **End Trip** confirmation, but with distinct copy and two primary actions:
  - **Title:** “End trip?” (or use string resource `end_trip_overlay_dialog_title`).
  - **Message:** Short line such as “Your trip may be complete. Confirm to save and end, or continue tracking.” (or `end_trip_overlay_dialog_message`).
  - **“Yes, complete trip”**  
    - Action: Call the **same logic as the existing End Trip button** (e.g. `TripInputViewModel.endTrip()`).  
    - This saves the trip, calculates OOR, stops `TripTrackingService`, clears persistence, and emits `TripEvent.TripEnded`.  
    - Then dismiss the overlay bubble (if still showing) and close the dialog.
  - **“No, continue trip”**  
    - Action: Dismiss the dialog only. Do **not** call `endTrip()`.  
    - The trip remains active; user continues tracking and must end the trip manually later (e.g. via the in-app “End Trip” button).
- **Reuse:** Where possible, reuse the existing End Trip confirmation building blocks (e.g. `TripInputFragment.showEndTripConfirmation()`-style layout and `viewModel.endTrip()`) so “Yes, complete trip” is a single code path with the existing end-trip behavior. Introduce a parameter or entry point (e.g. “from overlay”) only if needed for analytics or for closing the overlay after confirm.

---

## 3. When to show the “Trip ended” alert (detection)

Use **multiple signals** so the overlay appears when the trip is likely complete, without relying on a single heuristic.

- **3.1 Dynamic trip state (authoritative)**  
  - Use existing trip state from `UnifiedTripService` / `TripTrackingService` / `TripStateManager`: only show the overlay when a trip is **active** (e.g. `TripStatus.ACTIVE` or equivalent “trip in progress” flag). Never show it when there is no active trip or when the user has already ended the trip.

- **3.2 Miles driven vs allotted (out-of-route) miles**  (this may not be 100% accurate)
  - **Allotted miles** = loaded miles + bounce miles (dispatched miles) for the current trip.  
  - **Miles driven** = current `actualMiles` from live GPS/trip state.  
  - Trigger a “trip possibly ended” signal when:  
    - `actualMiles >= (loadedMiles + bounceMiles)` within a small tolerance (e.g. 0.1–0.5 mi), **or**  
    - `actualMiles` is within a configurable percentage of allotted (e.g. ≥ 98% of `loadedMiles + bounceMiles`).  
  - This indicates the driver has roughly completed the dispatched distance; combine with other signals before showing the bubble.

- **3.3 Optional: Movement / idle heuristics**  
  - If available from `UnifiedLocationService` or similar: consider “trip ended” when the vehicle has been **stationary** (e.g. speed below threshold) for a minimum duration (e.g. 5–10 minutes), **and** the miles-driven vs allotted condition above is satisfied. This reduces false positives when the driver is still moving toward the destination.

- **3.4 Optional: Time / distance thresholds**  
  - Do not show the bubble in the first N minutes of a trip (e.g. 5–10 minutes) or before a minimum `actualMiles` (e.g. 0.5 mi) to avoid showing “trip ended” during initial GPS warm-up or short stops.

- **3.5 Combining signals**  
  - Require at least:  
    - Trip is active (3.1), **and**  
    - Miles-driven vs allotted condition (3.2) is true.  
  - Optionally add: idle/movement (3.3) and time/distance (3.4) to reduce false triggers.  
  - Do not show the bubble again for the same trip after the user has chosen “No, continue trip” until the combined signals again indicate “possibly ended” (e.g. re-evaluate after more distance or after a cooldown period).

- **3.6 Rate limiting and one-shot per trip**  
  - Show the overlay at most once per active trip (or at most once per N minutes) until the user taps and chooses “Yes” or “No.” If “No, continue trip” is chosen, allow re-triggering only when conditions again hold (and optionally after a cooldown so the same stop doesn’t re-trigger immediately).

---

## 4. Implementation notes (for the implementer)

- **Overlay implementation:** On Android, implement the bubble with a `Service` that uses `WindowManager` and `TYPE_APPLICATION_OVERLAY` (or the appropriate type for a draggable bubble). The view can be a small circular view with the truck drawable as icon. Reuse or mirror patterns from open-source “bubble” or “chat head” implementations if helpful; keep dependencies minimal and consistent with the rest of the app.
- **Navigation:** When the user taps the bubble, use an `Intent` with the appropriate flags to bring the app to the foreground and open the fragment/screen that hosts the End Trip dialog (e.g. deep link or explicit fragment transaction to `TripInputFragment` and then show the “End trip?” dialog).
- **Strings:** Add all new copy to `app/src/main/res/values/strings.xml` (and optional `strings.xml` for other locales): e.g. `end_trip_overlay_dialog_title`, `end_trip_overlay_dialog_message`, `yes_complete_trip`, `no_continue_trip`, and any accessibility labels for the bubble.
- **Testing:** Manually test: (1) trip active + miles driven ≥ allotted → bubble appears; (2) tap bubble → app opens and “End trip?” dialog shows; (3) “Yes, complete trip” → same outcome as in-app End Trip; (4) “No, continue trip” → dialog closes, trip continues, bubble can be dismissed; (5) overlay permission denied → fallback behavior (e.g. standard notification only).
- **Out of scope for this plan:** Changing the core End Trip logic (OOR calculation, save, service stop); only the trigger (overlay + dialog entry point) and the “Yes”/“No” wiring are in scope.

---

## 5. Summary checklist

- [ ] Add truck silhouette drawable (e.g. `ic_trip_ended_bubble.png`) and use it for the overlay bubble.
- [ ] Implement overlay service + draggable bubble view; request draw-over-apps permission with fallback.
- [ ] Implement “trip ended” detection: active trip + miles driven vs allotted (and optionally idle/time rules); rate limit and one-shot per trip behavior.
- [ ] On bubble tap: bring app to foreground and show “End trip?” dialog with “Yes, complete trip” (→ `viewModel.endTrip()`) and “No, continue trip” (dismiss only).
- [ ] Reuse existing End Trip flow for “Yes, complete trip”; dismiss overlay after trip is ended.
- [ ] Add string resources and accessibility for the bubble and dialog.
- [ ] Test overlay permission granted/denied, tap flows, and “No, continue trip” path.
