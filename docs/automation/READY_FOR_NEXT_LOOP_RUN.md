# Ready for next loop run

**Updated:** 2026-03-12 (after fixing PerformanceTestSuite memory test)

---

## Research at loop start (self-improving)

- **Synthetic Data Loop:** At the start of each run (Phase 0), agents **research** current/popular data-loop and synthetic-data best practices (web search, 2024–2025); update [SYNTHETIC_DATA_LOOP_RESEARCH.md](./SYNTHETIC_DATA_LOOP_RESEARCH.md) with findings and 1–2 suggested improvements. This makes the loop **self-improving**. See [SYNTHETIC_DATA_LOOP_ROUTINE.md](./SYNTHETIC_DATA_LOOP_ROUTINE.md) Phase 0.

---

## Test status

- **PerformanceTestSuite** `performanceRegression memory usage should not increase significantly` — **fixed.** Assertion now uses *increase* from initial used heap (<100MB) instead of absolute peak, so it no longer fails when the test JVM has a large heap. See [FAILING_OR_IGNORED_TESTS.md](../qa/FAILING_OR_IGNORED_TESTS.md).
- Run full suite: `.\gradlew.bat :app:testDebugUnitTest` (all unit tests should pass; 6 skipped are documented @Ignore).

---

## Improvement loop (GO)

- **Trigger:** Say **GO** or "run improvement loop."
- **Start:** [IMPROVEMENT_LOOP_INDEX.md](./IMPROVEMENT_LOOP_INDEX.md) § 1 → [IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md](./IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md), common sense, reasoning, [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md).
- **Last run:** [IMPROVEMENT_LOOP_RUN_LEDGER.md](./IMPROVEMENT_LOOP_RUN_LEDGER.md) — Run 2026-03-12; Next: Mark Heavy favorites in HEAVY_IDEAS_FAVORITES; next focus UI/UX.

---

## Synthetic Data loop (START DATA LOOP)

- **Trigger:** Say **START DATA LOOP** or "Start Synthetic data loop."
- **Start:** [SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md](./SYNTHETIC_DATA_LOOP_FOR_OTHER_AGENTS.md), [SYNTHETIC_DATA_LOOP_ROUTINE.md](./SYNTHETIC_DATA_LOOP_ROUTINE.md).
- **Last run:** [SYNTHETIC_DATA_LOOP_RUN_LEDGER.md](./SYNTHETIC_DATA_LOOP_RUN_LEDGER.md) — Run 2025-03-11; Next: Read ledger + quality report; consider PLATINUM/SILVER generator or export; use "Next" to improve.

---

*Delete or archive this file after the next loop run if you prefer a single source in the ledgers.*
