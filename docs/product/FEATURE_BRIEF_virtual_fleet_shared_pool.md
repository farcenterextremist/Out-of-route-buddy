# Feature brief: Virtual fleet and shared pool

**Owner:** Project Design / Creative Manager  
**Created:** 2026-03-15  
**Status:** Approved for implementation — hand off to Back-end (shared-pool/export wiring), QA (tier contamination tests), Security (local-data review), and future UI/UX only if user-facing controls are added.  
**Related:** [ROADMAP.md](ROADMAP.md), [CRUCIAL_IMPROVEMENTS_TODO.md](../CRUCIAL_IMPROVEMENTS_TODO.md), [KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md](../agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md), [DATA_TIERS.md](../technical/DATA_TIERS.md).

---

## Problem

OutOfRouteBuddy needs a safe way to study larger-scale trucking behavior and compare it against real driving data without contaminating production trip history, statistics, or retention rules. At the same time, the owner's real GOLD-tier driving data needs to be visible in a shared local data pool alongside synthetic and MyTruckingBot intelligence.

---

## Value

- **Sandboxed experimentation** — Virtual fleet batches let us test analytics, edge cases, and future fleet-oriented ideas without touching real trip storage.
- **Human-data visibility** — GOLD trips from the owner's driving can be exported into a shared local pool for analysis and viewing.
- **Cross-project learning** — OutOfRouteBuddy and MyTruckingBot can contribute to the same reporting layer without either app surrendering its own source of truth.
- **Traceable research** — Official dataset references can improve realism and analytics context while remaining clearly separate from firsthand trip data.

---

## When to use it

- **Ideal:** We want to compare real driving patterns with synthetic fleet batches, or combine OutOfRouteBuddy trip exports with MyTruckingBot road/logistics intelligence in one local reporting database.
- **Override:** If the user wants live/two-way sync later, that should be a future phase with a bridge service and new approval.
- **Not for:** Writing synthetic rows into the app's Room `trips` table, reclassifying synthetic data as GOLD, or replacing OutOfRouteBuddy's primary trip persistence.

---

## High-level behavior

1. **OutOfRouteBuddy remains the source of truth** for human trip data in Room; only End trip writes to the store.
2. **GOLD export is additive** — approved human-driving trip data is exported outward as a shared-pool bundle without changing the primary trip store.
3. **Virtual fleet generation is separate** — synthetic personas and trip batches are generated in a sandbox service and exported as PLATINUM/SILVER only.
4. **Desktop shared pool is the meeting point** — MyTruckingBot scripts import OutOfRouteBuddy exports, virtual-fleet batches, and selected road/logistics data into a local SQLite reporting database.
5. **Human-readable inspection stays local** — tools such as Datasette and DB Browser for SQLite read the shared pool without changing app ownership or tier semantics.

---

## Out of scope (for this brief)

- New frontend screens or controls for the shared pool unless separately approved.
- Live/two-way sync between Android runtime and desktop services.
- Cloud backend storage or cloud-first analytics architecture.
- Replacing MyTruckingBot's existing database or OutOfRouteBuddy's Room database.

---

## Handoffs

- **Back-end:** Implement shared-pool export bundles, virtual-fleet generator, and MyTruckingBot import/publish scripts.
- **QA:** Add tests proving GOLD exports are visible in the shared pool and synthetic data never contaminates OutOfRouteBuddy production stats/history.
- **Security:** Review local export paths, PII/logging, provenance fields, and sandbox separation.
- **UI/UX:** Only needed later if the user wants in-app controls for export status, shared-pool actions, or virtual-fleet previews.

---

*Next: implement the additive export pipeline, shared-pool schema, synthetic virtual-fleet engine, and verification tests while preserving SSOT and tier separation.*
