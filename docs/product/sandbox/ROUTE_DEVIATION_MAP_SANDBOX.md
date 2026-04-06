# Sandbox: Route deviation map (instant replay)

**Heavy tier · FUTURE_IDEAS §3.1**  
**Status:** Sandbox in progress — design brief only; **no production code** until visual approval and **"approve 100% implement"**.  
**References:** [FUTURE_IDEAS.md](../FUTURE_IDEAS.md) §3.1, [SANDBOX_TESTING.md](../../automation/SANDBOX_TESTING.md) §5, [LOOP_TIERING.md](../../automation/LOOP_TIERING.md).

---

## 1. Product intent

- **What:** A small map (or expandable block) that shows **actual driven path** vs **expected / reference path**, with **clear visual emphasis on deviations** (e.g. red segments or overlay where the driver left the reference corridor).
- **Why:** Instant visual feedback on *where* miles went off planned route (e.g. Walmart detour), not only aggregate OOR numbers.
- **Where (UX):** Prefer **expandable** section on trip detail or history stat card — default collapsed to avoid clutter and map SDK cost on every scroll.

---

## 2. Current codebase reality (gap analysis)

| Capability today | Notes |
|------------------|--------|
| **Trip + GPS metadata** | `Trip` / `TripEntity` store aggregates (`totalGpsPoints`, `validGpsPoints`, accuracy, speed stats, etc.) and **last** lat/lng in metadata — **not** a full time-ordered polyline. |
| **Live tracking** | `TripTrackingService` receives locations and accumulates distance; it does **not** persist every fix to Room for completed trips. |
| **Expected route** | User enters loaded/bounce miles and optional addresses; there is **no** stored “official” route geometry from a directions API for the trip. |
| **Geocoding** | Photon forward geocode exists for pickup/dropoff **points**, not corridor polylines. |

**Conclusion:** Implementing a true “replay” requires **new persistence** (and likely **new permissions / privacy copy**) for sampled GPS trail, plus a defined **reference path** source. This is **Heavy** scope: schema or sidecar storage, migration, retention, and UI.

---

## 3. Proposed phased approach (sandbox plan)

| Phase | Goal | Sandbox outcome |
|-------|------|-------------------|
| **A — Trail capture** | Define **what** to store (e.g. lat, lng, time, accuracy; sampling interval; max points per trip; compression). | Schema sketch + retention policy; no UI. |
| **B — Reference path** | Choose **one** primary expected-path source for v1 (see §4). | Written decision + fallback behavior when missing. |
| **C — Deviation logic** | Define how “off route” is computed (buffer distance from polyline, time in corridor, etc.). | Algorithm spec + edge cases (GPS drift, tunnels, bad first fix). |
| **D — Map UI** | Pick SDK, screen placement, loading/error states, TalkBack. | Mockup / wireframe → visual approval gate. |

Phases can be validated in a **feature branch** or **debug-only preview** (see §13.1 lightweight preview container in FUTURE_IDEAS) before merging to production.

---

## 4. Expected-path source (decision matrix — v1 candidates)

| Option | Pros | Cons |
|--------|------|------|
| **A. Straight line pickup → dropoff** | Cheap; uses existing geocoded points when both exist. | Misleading for multi-leg or non-great-circle driving. |
| **B. Directions API polyline** (Google / OSRM / similar) | Matches road network. | API key, cost, ToS, offline behavior, network dependency. |
| **C. User “planned” polyline (future)** | Accurate to dispatch. | No data model today; heavy UX. |
| **D. Corridor from loaded miles only** | No external API. | Weak geometry — only distance scalar, not shape. |

**Sandbox recommendation:** Start design assuming **A as fallback** and **B as optional enhancement** when both addresses geocode and network is available; document **graceful hide** of map when reference cannot be built.

---

## 5. Map SDK (sandbox shortlist)

| SDK | Notes |
|-----|--------|
| **Google Maps SDK** | Familiar; requires API key, billing, Play services; strong Android docs. |
| **MapLibre** | Open-source style; more setup; good for avoiding Google dependency (aligns with “no ads / solo driver” ethos — **user/product choice**). |
| **OSMDroid** | Fully offline-capable tiles; different UX polish tradeoffs. |

**Checklist item:** Pick one for v1 and record **key + ProGuard + min SDK** implications in `app/build.gradle.kts` notes when approaching implementation.

---

## 6. Privacy, safety, retention

- Trail is **sensitive** (reveals stops, home terminal patterns). Sandbox must specify:
  - **Retention:** delete with trip? export rules?
  - **Settings:** optional “Save route map data” toggle if legally/UX preferable.
  - **Help copy:** plain-language explanation before first use.
- **Synthetic / GOLD:** Deviation map for **sandbox/virtual** trips must follow same tier separation as virtual fleet (no GOLD contamination).

---

## 7. Non-goals for first sandbox iteration

- 3D flyover, full historical replay across all trips on one map.
- Real-time sharing of route maps (social) — out of mission scope unless product direction changes.

---

## 8. Validation checklist (expandable)

Use this list to advance sandbox % toward “ready for approve 100% implement.”

- [ ] **Trail model** — Sample rate, max points, storage (Room table vs file blob), migration story documented.
- [ ] **Reference path** — v1 source chosen (§4); behavior when only one address or none.
- [ ] **Deviation rule** — Buffer meters, min dwell to count as deviation, handling poor accuracy.
- [ ] **Map SDK** — Selected; licensing and build impact noted.
- [ ] **UI** — Placement (expandable on trip detail vs stat card); empty/error states; a11y outline.
- [ ] **Performance** — Long trip (e.g. 12h) point count cap and decimation strategy.
- [ ] **Privacy** — Toggle + retention + Help text drafted.
- [ ] **Visual approval** — Mockup or screenshot approved by user (LOOP_TIERING).
- [ ] **Explicit implement phrase** — User says **"approve 100% implement"** before any production merge.

---

## 9. Open questions (for you)

1. Is **v1** acceptable if the map only appears when **both** pickup and dropoff geocode (straight line or directions polyline)?
2. Preference: **Google Maps** vs **MapLibre** / offline-first?
3. Should historical trips **without** stored trails show a **“No route replay for this trip”** message, or hide the section entirely?

---

## 10. Next sandbox steps (documentation / design only)

1. Answer §9 (can be inline in this file or in chat).
2. Add a **wireframe** (image or HTML mock) to `.cursor/` or `docs/mockups/` and link here.
3. Optionally add a **Phase A data-class sketch** (Kotlin in a `docs/` snippet or gist) — still not app code until approved.

---

*Created to formalize sandboxing for Heavy item #5 (route deviation map). Update this file as decisions land; keep FUTURE_IDEAS §3.1 in sync with one-line status.*
