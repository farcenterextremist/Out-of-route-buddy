# OOR View & Edge Cases — Study Session

**Purpose:** Research what "good" out-of-route % is, over/under framing, color semantics for OOR display, and edge cases (e.g. load cancelled mid-trip, new load). Captures study findings and future todos (e.g. get future context).  
**References:** [GOAL_AND_MISSION.md](../GOAL_AND_MISSION.md), [FUTURE_IDEAS.md](./FUTURE_IDEAS.md) § 8.1 (OOR goal), [CalculateTripOorUseCase](../../app/src/main/java/com/example/outofroutebuddy/domain/usecase/CalculateTripOorUseCase.kt)

---

## 1. What is "good" out-of-route %? (research)

### Industry context

- **Definition:** Out-of-route = actual miles driven minus dispatched miles (loaded + bounce). Positive OOR = drove more than dispatched; negative OOR = drove less than dispatched.
- **Typical benchmarks:**
  - Fleets often see **3–10%** out-of-route annually due to planning and driver decisions.
  - **Best-in-class** aim lower; some targets keep deadhead/empty miles below 15–18%.
  - Example: 4.34% over dispatched (16.8 OOR miles per trip) was cited as costing drivers ~$39/week; reducing OOR by 3% can save a 10-truck fleet $15k–$22.5k/year.
- **"Good" in practice:** **≤5%** is often treated as excellent; **≤10%** as good; **>10–25%** as fair to poor. So "good" OOR % is in the **low single digits or zero**; **negative** (under route) is better than dispatched.

### App alignment

- `CalculateTripOorUseCase.getEfficiencyRating()` already uses: ≤5% Excellent, ≤10% Good, ≤15% Fair, ≤25% Poor, else Very Poor.
- OOR can be **negative** when actual miles < dispatched miles (under route); the use case computes `oorMiles = actualMiles - dispatchedMiles` and allows negative. Negative = driver came in under the dispatched distance = good.

**Takeaway:** "Good" OOR % = low or zero; **negative OOR % = under route = best**. Use this for over/under framing and color semantics.

---

## 2. Over/under framing

| Concept | Meaning | Example |
|--------|--------|--------|
| **Under route** | Actual miles < dispatched (loaded + bounce). OOR miles and OOR % are **negative**. | Dispatched 200 mi, drove 195 → OOR = −5 mi, −2.5%. |
| **On route** | Actual ≈ dispatched. OOR ≈ 0. | Dispatched 200, drove 200 → OOR = 0. |
| **Over route** | Actual > dispatched. OOR **positive**. | Dispatched 200, drove 220 → OOR = +20 mi, +10%. |

**Product framing:** Expose this as **over/under** where possible (e.g. "Under by 2.5%" or "Over by 10%") so negative = under = good is obvious. Tie to color semantics below.

---

## 3. Color semantics (proposed)

User direction: **negative OOR = bright gleaming matrix green**; **from there up = glowey blue**; **excess OOR = red**.

| OOR band | Meaning | Proposed color | Use |
|----------|--------|----------------|------|
| **Negative** (actual < dispatched) | Under route; best outcome | **Matrix green** (bright, gleaming) | Day/trip/card when OOR % < 0. |
| **Zero / low positive** (e.g. 0–5%, 0–10%) | On route or excellent | **Glowey blue** | Transition from green; still good. |
| **Moderate positive** (e.g. 5–15%) | Fair; over route | **Blue → amber** (optional) | Or keep blue up to a threshold. |
| **High positive** (excess OOR) | Poor; excess mileage | **Red** | Days/trips with excess OOR (e.g. >10% or >15%). |

**Implementation notes (future):**

- Apply to **day indicators** (e.g. calendar dots or list rows), **trip cards**, or **statistics row** when showing OOR.
- Exact thresholds (e.g. green < 0, blue 0–10%, red > 10%) can be configurable or tied to the existing efficiency rating (Excellent/Good/Fair/Poor).
- Accessibility: ensure sufficient contrast and do not rely on color alone (e.g. icon or label "Under" / "Over").

**Status:** Design direction only; no UI change without user approval. See USER_PREFERENCES (no unwarranted UI changes). Can be added to FUTURE_IDEAS as a Heavy or Medium (visual polish) and linked to § 8.1 OOR goal.

---

## 4. Edge case: load cancelled halfway, new load given

### Scenario

- User starts a trip with **Load A** (e.g. 200 loaded, 50 bounce = 250 dispatched).
- Mid-trip, **load is cancelled**; dispatch gives **Load B** (different origin/destination, different miles).
- Current app model: one trip = one set of loaded + bounce + actual. No built-in "load cancelled" or "swap to new load" flow.

### Open questions

- Should the first segment (miles driven on Load A before cancel) be **saved as a partial trip** (e.g. actual miles so far, loaded/bounce for A), and a **new trip** started for Load B?
- Or one trip with **multiple segments** (e.g. segment 1: Load A partial; segment 2: Load B)?
- How does the user enter **revised** loaded/bounce when the load changes? (Manual edit? "Load cancelled" action that splits or replaces?)

### Simulation / thinking

- **Option A — Two trips:** "End trip" for Load A with actual miles so far and original loaded/bounce (or prorated); start new trip for Load B. Simple but may leave Load A trip looking odd (high OOR if actual is small vs full dispatched).
- **Option B — Segment or multi-load trip:** Data model allows multiple "legs" or load segments in one trip. Heavier change; needs schema/UX.
- **Option C — Replace in place:** User taps "Load cancelled" → dialog to enter new load (loaded/bounce for B) and optionally "miles already driven" (for A). App treats as one trip with updated loaded/bounce and same actual (or split actual). Clarifies semantics.

### Future context needed

- **Get future context:** When designing this flow, we need **real-world dispatch semantics**: how do drivers log cancelled loads today? Do they write off the first segment, or does it get paid? Does the app need to support "partial trip" or "trip with load change" explicitly? Research or user interviews could inform whether Option A, B, or C (or a variant) fits best.
- **Future todo:** Add to backlog: "**Load cancelled / new load mid-trip** — Design and simulate edge case; get future context (dispatch/driver workflow); decide split vs replace vs multi-segment; add to FUTURE_IDEAS or feature brief if Heavy."

---

## 5. Future todos (from this study)

- [ ] **OOR view — over/under + colors:** Implement or spec UI that shows OOR as over/under with color semantics (negative = matrix green, low positive = blue, excess = red). Link to FUTURE_IDEAS § 8.1 (OOR goal). Requires visual approval.
- [ ] **Load cancelled / new load mid-trip:** Study and simulate edge case; get future context (how drivers/dispatch handle it); decide data model and flow (split trip vs replace vs multi-segment); document in feature brief or FUTURE_IDEAS. Back-end + Design + UI/UX.
- [ ] **Get future context (general):** When adding features that touch real-world workflow (e.g. load changes, multi-stop, driver pay), add a step: "Get future context" — research or user input so we don’t assume semantics. Add to IMPROVEMENT_LOOP or design process as a checklist item.

---

## 6. References (sources)

- Per Diem Plus / fleet articles: out-of-route = actual − dispatched; 3–10% typical; 4.34% example; cost impact.
- Deadhead/empty miles: 20–25% industry average; best-in-class 15–18%.
- App: `CalculateTripOorUseCase`, `Trip.kt` (oorMiles, oorPercentage; negative allowed in calculation).

---

*Study session doc. Update when we have more research or user feedback on thresholds and load-cancel flow.*
