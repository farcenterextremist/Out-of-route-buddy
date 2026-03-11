# Sandbox — True Completion Percentage

**Purpose:** Define a **true** percentage of completion for sandboxed ideas. Merging features should not be taken lightly; the percentage reflects real readiness, not just "documented."

**References:** [HEAVY_TIER_IDEAS.md](./HEAVY_TIER_IDEAS.md), [FUTURE_IDEAS.md](../product/FUTURE_IDEAS.md), [SANDBOX_TESTING.md](./SANDBOX_TESTING.md), [LOOP_TIERING.md](./LOOP_TIERING.md)

---

## Completion Stages (True %)

| % | Stage | Criteria |
|---|-------|----------|
| **0** | Proposed | Idea exists in brainstorm/worker todos; not yet in FUTURE_IDEAS |
| **20** | Documented | In FUTURE_IDEAS with description, placement, dependencies |
| **40** | Design brief or validation plan | FEATURE_BRIEF exists, or validation checklist (what to test, how to verify) |
| **60** | Validated in sandbox | Feature branch or build variant; code exists and runs |
| **80** | User confirmed behavior | User has seen and confirmed the feature works as expected |
| **100** | Ready for merge | Visual approval done; user said "approve 100% implement"; safe to merge |

---

## Rules

- **Merging is not taken lightly.** 100% = ready for merge, not "documented."
- **Documented ≠ 100%.** A feature in FUTURE_IDEAS is at most 20% until it has a design brief (40%) or validation (60%+).
- **Medium tier improves sandboxed ideas each loop.** Add design brief, validation checklist, or advance one idea's % (e.g. 20→40 by adding FEATURE_BRIEF).
- **Update HEAVY_TIER_IDEAS** with % when Medium tier advances an idea.

---

## Example

| Idea | Before | Medium tier action | After |
|------|--------|-------------------|-------|
| Route deviation map | 20% (documented) | Add validation checklist | 40% |
| Auto drive detected | 60% (branch exists) | User confirms behavior | 80% |
| Reports screen | 40% (brief exists) | Validate in branch | 60% |

---

*Integrates with IMPROVEMENT_LOOP_TEAMS and HEAVY_TIER_IDEAS. Medium tier advances % each loop.*
