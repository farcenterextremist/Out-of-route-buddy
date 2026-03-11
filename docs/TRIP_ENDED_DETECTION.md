# Trip-ended detection (how the bubble is triggered)

The app decides "trip may be complete" in [TripEndedDetector](../app/src/main/java/com/example/outofroutebuddy/services/TripEndedDetector.kt), which combines **trip metrics**, **GPS/movement signals**, and **activity recognition**, then runs a small state machine. When the detector fires, [TripEndedOverlayService](../app/src/main/java/com/example/outofroutebuddy/services/TripEndedOverlayService.kt) shows the bubble (or fallback notification).

---

## Inputs (from TripTrackingService and TripStateManager)

| Signal | Source | Meaning |
| --- | --- | --- |
| **Trip state** | TripStateManager | Active trip, loaded/bounce miles, start time |
| **Trip metrics** | TripTrackingService.tripMetrics | totalMiles, oorMiles (live from GPS) |
| **Drive state** | TripTrackingService.driveState | DRIVING vs WALKING_OR_STATIONARY (from DriveStateClassifier) |
| **TripEndingSignalSnapshot** | TripTrackingService | effective speed, rollingDistanceMeters (last 2 min), headingVarianceDeg, gpsAccuracyMeters, direct-speed flag, low-motion sample count, sustained stop dwell |
| **ActivityTransitionSignal** | Activity Recognition API | stillConfidence, inVehicleConfidence (0–100) |
| **GeofenceContextSignal** | TripTrackingService | isNearOrigin (within 250 m of trip start), dwellInOriginMillis |

Trip "origin" is the **first GPS point with accuracy ≤ 40 m** after the trip starts. So "near origin" means back near where the trip started (e.g. depot); trips that end elsewhere won’t get this signal.

---

## State machine

1. **IN_PROGRESS** – Trip active, not enough signals that trip is ending.
2. **CANDIDATE_ENDING** – Confidence crossed `candidateConfidenceThreshold` (0.6); we’re watching for sustained “trip ended” signals.
3. **Foreground prompt** – Once candidate confidence stays high long enough, the detector can emit an earlier `showArrivalPrompt` signal. If the app is already foregrounded, this routes directly to the in-app `Have you arrived?` prompt.
4. **ENDING_DETECTED** – We’ve stayed in CANDIDATE_ENDING long enough with high enough confidence → **show bubble or fallback notification** when the app is backgrounded.
5. **RESUMED_CONTINUE_COOLDOWN** – User recently chose “Keep tracking”; we suppress re-trigger for `cooldownAfterNoContinueMs`.
6. **ENDED_CONFIRMED** – Trip was ended from the app.

**Transitions:**

- **IN_PROGRESS → CANDIDATE_ENDING:** `confidence >= candidateConfidenceThreshold` (default 0.6).
- **CANDIDATE_ENDING → IN_PROGRESS:** “Movement resumed”: `confidence < leaveCandidateThreshold` (0.45) **or** `speedMph > 8` **or** `inVehicleConfidence >= config.movementResumedInVehicleConfidence` (default 85). So one strong “still driving” signal cancels the candidate.
- **CANDIDATE_ENDING → foreground prompt:** After `candidateDebounceMs` (e.g. 20 s) if confidence is still ≥ `candidateConfidenceThreshold`, emit the lightweight arrival prompt. This is only surfaced when the app is already visible.
- **CANDIDATE_ENDING → ENDING_DETECTED:** After **both** debounce times, and confidence still high:
  - Time in CANDIDATE_ENDING ≥ `candidateDebounceMs` (e.g. 20 s),
  - Time in CANDIDATE_ENDING ≥ `endingDebounceMs` (e.g. 50 s),
  - `confidence >= endingConfidenceThreshold` (e.g. 0.72).
- **ENDING_DETECTED → IN_PROGRESS:** Same “movement resumed” condition (user kept driving or activity says in vehicle).

---

## Confidence score (0.0–1.0)

`computeEndConfidence()` adds/subtracts from a score; result is clamped to [0, 1]. **Higher = more “trip has ended”.**

- **Legacy gate met** (miles ≥ 98% of allotted or within tolerance): **+0.32**
- **Completion ratio** (totalMiles / allotted): ≥ 98% **+0.14**, ≥ 90% **+0.08**, ≥ 75% **+0.04**; ≥ 100% **+0.04** (over allotted)
- **Speed (mph):** ≤ 1 **+0.12**, ≤ 3 **+0.08**
- **Rolling distance (m, last 2 min):** ≤ 80 **+0.10**
- **Heading variance (deg):** ≤ 10 **+0.05**
- **Sustained stop dwell:** ≥ 30 s **+0.06**, ≥ 60 s **+0.12**, ≥ 120 s **+0.18**
- **Low-motion sample count:** ≥ 3 **+0.06**
- **Near trip origin:** **+0.08**
- **Dwell at origin:** ≥ 2 min **+0.10**
- **Drive state = WALKING_OR_STATIONARY:** **+0.08**
- **Still confidence (activity API):** ≥ 70 **+0.12**
- **In-vehicle confidence (activity API):** ≥ 80 **−0.20** (only penalize when strongly “still driving” API often lags)
- **GPS accuracy:** > 60 m **−0.08** (only penalize poor GPS; 50–60 m is common)
- **Estimated speed with high rolling distance:** **−0.08** (prevents missing-speed samples from behaving too much like true 0 mph parking)
- **Below the soft completion-ratio gate without legacy confirmation:** **−0.10** instead of hard-blocking detection
- **Speed ≥ 12 mph:** score forced to **0** (clearly still driving)

This means legitimate parked trips can now reach prompt-worthy confidence even when they end short of estimated miles or far from origin, as long as stop/dwell evidence is strong enough.

---

## Config parameters (TripEndedDetector.Config)

| Parameter | Default (tuned for real-world) | Purpose |
| --- | --- | --- |
| milesToleranceMi | 0.3 | Legacy gate: totalMiles ≥ allotted − this |
| minTripDurationMs | 5 min | No bubble before trip has been this long |
| minActualMiles | 0.5 | No bubble before at least this many miles driven |
| cooldownAfterNoContinueMs | 15 min | After “No, continue”, don’t re-trigger for this long |
| percentThreshold | 98 | Legacy: show when totalMiles/allotted ≥ 98% |
| minCompletionRatioForEnding | 0.90 | Soft gate: score penalty if ratio is below this and legacy completion has not fired |
| minCompletionRatioForEarlyPrompt | 0.55 | Hard floor for very early prompts unless strong stop evidence exists |
| candidateConfidenceThreshold | 0.6 | Enter CANDIDATE_ENDING above this |
| endingConfidenceThreshold | 0.72 | Fire bubble only if confidence stays >= this (lowered from 0.8 so noisy GPS/activity don't block) |
| leaveCandidateThreshold | 0.45 | Drop back to IN_PROGRESS if confidence drops below this |
| candidateDebounceMs | 20 s | Min time in CANDIDATE before we can transition to ENDING_DETECTED |
| endingDebounceMs | 50 s | **Total** time we must stay in CANDIDATE with high confidence before firing |
| movementResumedInVehicleConfidence | 85 | In-vehicle above this = "movement resumed" (raised from 75 so delayed "still" doesn't cancel) |

These defaults were tuned for real-world use: shorter debounce (50 s), slightly lower confidence threshold (0.72), and higher in-vehicle threshold (85) so the bubble can trigger more reliably when the driver has parked.

---

## Strengthened parameters (real-world)

To make the bubble trigger more reliably in real-world testing:

- **endingDebounceMs:** 90 s → **50 s** – Fire sooner after sustained “trip ended” signals; user often leaves within 1–2 minutes.
- **candidateDebounceMs:** 30 s → **20 s** – Slightly faster entry into “candidate” state.
- **endingConfidenceThreshold:** 0.8 → **0.72** – Don’t require every signal; activity API and GPS are noisy.
- **Movement-resumed in-vehicle threshold:** 75 → **85** – Only reset candidate when device is *strongly* “in vehicle”; reduces resets from delayed “still” transition.
- **Confidence formula:** Slightly more weight for completion ratio ≥ 98%, only penalize in-vehicle when ≥ 80, and only penalize GPS when > 60 m, so marginal conditions still allow a trigger.

These are applied in TripEndedDetector’s `Config` and `computeEndConfidence`; see the class and its tests for exact values.

---

## Where to tune

- **TripEndedDetector.kt:** `Config` defaults, `evaluateStateMachine()` (movement-resumed thresholds), `computeEndConfidence()` weights and thresholds.
- **TripTrackingService.kt:** `SIGNAL_WINDOW_MS` (2 min), `ORIGIN_GEOFENCE_RADIUS_METERS` (250 m), `ORIGIN_DWELL_MIN_SPEED_MPH` (3 mph), and the stop-dwell thresholds (`STOP_DWELL_MAX_*`). These affect rolling distance and the sustained-stop signal.

Tests: [TripEndedDetectorFlowTest](../app/src/test/java/com/example/outofroutebuddy/services/TripEndedDetectorFlowTest.kt), [TripEndedOverlayScenarioTest](../app/src/test/java/com/example/outofroutebuddy/services/TripEndedOverlayScenarioTest.kt).

---

## Privacy-safe feedback loop

The trip-ended flow now logs **bucketed** analytics for tuning instead of raw trip traces:

- `trip_end_prompt_shown`
- `trip_end_prompt_action`

Allowed fields are coarse only:

- confidence bucket
- completion bucket
- speed bucket
- rolling-distance bucket
- dwell bucket
- activity bucket
- prompt surface (`foreground`, `overlay`, `fallback`)
- booleans such as `near_origin` and `direct_speed`

Do **not** add raw coordinates, exact addresses, route geometry, or stable identifiers to these events.
