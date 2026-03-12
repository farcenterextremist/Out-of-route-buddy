# Data Tiers — Reliability and Retention

**Purpose:** Define the three upper-echelon data tiers used to separate trip (and future) data by reliability and retention policy. Data may be **promoted** or **terminated** according to tier rules.

**Owner:** Product / Data  
**Related:** `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md`, domain `DataTier` enum, `TripEntity.dataTier`.

---

## Rule: Verifiable separation

- **Synthetic data must stay verifiably separate from human data.** Human data lives in **GOLD** only; synthetic/simulated data lives in **PLATINUM** or **SILVER**. Do not mix tiers: never store synthetic data as GOLD, and never re-tag human data as synthetic without explicit user action. Queries, exports, and retention must be able to distinguish by tier (e.g. `getTripsByTier`, `deleteTripsOlderThan(cutoff, maxTier)`).
- **We may use synthetic data to suggest improvements to how we gather human data.** For example: synthetic runs can reveal gaps in validation, UX flows, or export shape; quality reports can recommend changes to trip capture, GPS handling, or prompts—so that human data gathering improves. Suggestions only; do not auto-apply to GOLD or to production capture without approval.

---

## Tiers

| Tier | Meaning | Policy |
|------|--------|--------|
| **SILVER** | Unsure / very unsure / may delete | Candidate for **termination**. Low confidence. Synthetic/simulated data may live at this level and below. |
| **PLATINUM** | Maybe useful in future; subject to demotion or promotion | Synthetic and simulated data stored at this level and below. Can be **promoted** to GOLD (e.g. after human verification) or **demoted** to SILVER. |
| **GOLD** | Purely human data; digital gold | **No** synthetic/simulated data at this tier. Treat with care: **parse carefully**, **copy when needed** instead of mutating. |

---

## Current behavior

- **Human-ended trips** (user taps "End trip") are stored as **GOLD** (set in `TripStatePersistence.saveCompletedTrip`).
- **Existing rows** (before tier column) default to **GOLD** via migration 4→5.
- **Statistics and calendar** include all tiers; filtering by tier (e.g. "only GOLD counts") can be added later if needed.
- When **copying** GOLD data, prefer copy-and-modify over in-place mutation so the original remains the single source of truth.
- For GOLD trips, use `trip.copyForEdit()` when building an edited version; persist the result via `updateTrip` so the original row is replaced only on explicit save.

---

## Promotion / termination (future)

- **Promotion:** PLATINUM → GOLD when data is verified as human or high-confidence (e.g. manual review, import confirmation).
- **Demotion:** GOLD/PLATINUM → PLATINUM/SILVER when data is deemed synthetic, low-confidence, or candidate for deletion.
- **Termination:** SILVER (and optionally PLATINUM) data may be deleted by retention/cleanup policies; GOLD should not be auto-terminated.

**API:** `setTripTier(tripId, tier)` changes a trip's tier; all transitions are allowed (promotion and demotion). GOLD remains "copy when needed" at use sites.

**Retention by tier:** Deletion applies only to tiers at or below `maxTier` when `deleteTripsOlderThan(cutoffDate, maxTier)` is used with a non-null `maxTier`. GOLD is never auto-terminated. Existing one-arg `deleteTripsOlderThan(cutoff)` continues to delete all tiers before the cutoff (unchanged behavior).
