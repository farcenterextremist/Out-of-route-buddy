# Synthetic Data Loop — Research & Self-Improvement

**Purpose:** Store findings from **research at the start of each loop run** (current/popular data-loop and synthetic-data best practices) and **suggested improvements** so the loop is self-improving. Agents update this doc in Phase 0 and can propose changes to the routine or master plan.

**When:** At **loop start** (Phase 0), do a short web/research step; append or update § Findings and § Suggested improvements. Use findings to inform the run and add 1–2 concrete suggestions for the next run or for the routine.

**Reference:** [SYNTHETIC_DATA_LOOP_ROUTINE.md](./SYNTHETIC_DATA_LOOP_ROUTINE.md) Phase 0, [SYNTHETIC_DATA_LOOP_MASTER_PLAN.md](./SYNTHETIC_DATA_LOOP_MASTER_PLAN.md).

---

## Findings (append per run; date each entry)

### 2025–2026 (current)

- **Define use case first.** Clearly define the purpose of synthetic data (training, testing, privacy-safe sharing, augmentation); this drives fidelity, scale, and structure. *Source: IBM, YData Fabric; applies to our loop: we use synthetic for PLATINUM/SILVER tiers, testing, and suggesting improvements to human data gathering.*
- **Quality depends on source.** Synthetic data quality depends on the source data and domain understanding; validate against business rules and preserve critical relationships. *Applies: our tier separation (GOLD vs PLATINUM/SILVER) and quality report phase align with this.*
- **Schema and constraints.** Document types, identifiers, constraints, and relationships; exclude arbitrary IDs where appropriate; validate schema in generated data. *Applies: DATA_TIERS and TripEntity.dataTier give us a clear schema; we can add schema checks to the quality report.*
- **Avoid overfitting synthetic to source.** Use holdout techniques and sufficient variability; cover edge cases and rare events, not only common patterns. *Applies: when creating synthetic trips, vary parameters (miles, OOR %, scenarios) per SIMULATIONS_AND_MOCKS.*
- **Quality validation.** Implement statistical checks, correlation preservation, business-rule compliance, and privacy metrics. *Applies: our quality report (Phase 3) and tier policies are a light form of this; we can add a short "validation checklist" to the report.*
- **Human-in-the-loop.** HITL pipelines use human review at key decision points; feedback loops: annotate → model predicts → human reviews → retrain/improve. *Applies: we already require user approval before tier changes (Phase 4); we can make "suggest improvements to human data gathering" more explicit in the quality report.*
- **Continuous improvement.** Data loops improve when runs are logged, findings are reused, and the process is iterated. *Applies: run ledger, "Next" items, and this research doc make the loop self-improving.*

**Loop #2 (2026-03-12):**

- **Iterative generation and validation.** Best practice: define strict schemas first, use parameterized variation, generate in batches, validate each batch (schema checks + human spot-checks), then refine. *Applies: our Phase 1 (create/gather) and Phase 3 (quality report) mirror this; we can add "batch" and "validation checklist" to Phase 3.*
- **Report Loop # every run.** User request: always report **Loop #** to the user plus proof of work and how we benefit; ledger template updated to include Loop #. *Applies: ledger template and every run block now include Loop #; agent reports to user at end.*
- **40–60% time / 70% cost.** Industry context: synthetic data in production can cut model/data dev time and cost significantly; 80% synthetic / 20% real can outperform either alone. *Applies: our loop keeps synthetic separate (GOLD untouched) while allowing synthetic to suggest improvements to human data gathering—aligns with hybrid value.*

---

## Suggested improvements (for routine or next run)

*Agents add 1–2 items per run when research or run experience suggests a change. Implement when agreed or leave for next run.*

| Date       | Suggestion | Status |
|------------|------------|--------|
| 2026-03-12 | Add Phase 0 research step: at loop start, search current data-loop/synthetic-data best practices; update this doc; add 1–2 suggested improvements. | Done (this doc + routine updated.) |
| 2026-03-12 | In quality report (Phase 3), add optional "Validation checklist": schema consistency, tier separation verified, business rules (e.g. GOLD human-only) noted. | Proposed for next run. |
| 2026-03-12 | **Ledger and report: include Loop # every run;** report to user proof of work and how we benefit. | Done (template + Loop #2 block). |
| — | *Future: add schema validation step for generated synthetic data (e.g. required fields, tier enum).* | Backlog. |

---

## How to use this doc (agents)

1. **At loop start (Phase 0):** Search for "synthetic data best practices", "data quality loop", "human-in-the-loop data" (or similar). Prefer recent (2024–2025) and authoritative sources (e.g. IBM, Kubeflow, ML pipeline vendors).
2. **Append to § Findings** 2–4 bullets with a one-line "Applies: …" for our loop. No need to paste full articles; cite and summarize.
3. **Add to § Suggested improvements** 1–2 concrete items (e.g. "Add X to Phase 2" or "In quality report, include Y"). Status: Proposed | Done | Backlog.
4. **Use in the run:** Let findings inform Create/Gather (Phase 1) and Quality report (Phase 3). Reference this doc in the run ledger "Next" when you add a suggested improvement.

---

*This doc makes the loop self-improving by tying research to the routine and to the ledger.*
