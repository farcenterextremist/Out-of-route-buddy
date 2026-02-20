# Feature brief: Auto drive detected

**Owner:** Project Design / Creative Manager  
**Created:** 2025-02-19  
**Status:** Brief only — hand off to UI/UX (wireframe), Back-end (trigger logic), Front-end (UI), QA (test cases), Security (privacy review).

---

## Problem

Drivers sometimes forget to tap **Start trip** when they begin driving. They only notice later, so the trip is incomplete or they start it manually mid-route. That adds friction and can skew out-of-route and distance data.

---

## Value

- **Less friction** — One tap (or auto-start with confirm) when the app detects that the user is likely driving, instead of remembering to open the app and start a trip.
- **More complete trips** — Fewer missed or late starts, so statistics and history better reflect actual driving.
- **Clear mental model** — "The app can tell I'm driving and offers to start; I can accept or dismiss."

---

## When to use it

- **Ideal:** User has the app installed and (optionally) in the background; they begin driving. After detection (e.g. movement + speed threshold or similar), the app surfaces "Auto drive detected" and a one-tap way to start the trip (or confirm if auto-start is enabled).
- **Override:** User can dismiss or turn off auto-detection in settings; manual Start/End remains always available.
- **Not for:** Replacing manual trip start/end; this is an optional convenience. Privacy must be respected: detection should be explainable and data kept on-device unless otherwise specified.

---

## High-level behavior

1. **Detection** — Back-end (or domain) defines what "driving" means (e.g. sustained movement above a speed threshold, or geofence exit). No implementation detail here; see Back-end brief/spec.
2. **Surface** — When driving is detected, the app shows a clear "Auto drive detected" state (e.g. toolbar or trip screen) and a single action: e.g. **Start trip** or **Yes, start** (if auto-start is opt-in and we only suggest).
3. **One-tap** — User taps once to start the trip; no extra steps. If the user does nothing, the state can persist or timeout per UI/UX spec.
4. **Settings** — Optional toggle: auto-drive on/off (or "suggest only" vs "auto-start with confirm"). Default can be opt-in to avoid surprising the user; UI/UX to propose.
5. **Privacy** — Location/movement used only for detection on-device unless we explicitly document otherwise. Security Specialist to review.

---

## Out of scope (for this brief)

- Exact speed/threshold or geofence logic → Back-end.
- Pixel-level placement, default on/off, and long-press/accessibility → UI/UX Specialist.
- Implementation of UI and trigger logic → Front-end and Back-end.
- Test cases and privacy checklist → QA and Security Specialist.

---

*Next: UI/UX proposes wireframe (where it lives, default state, one-tap override); Back-end defines trigger; then Front-end and QA implement and test.*
