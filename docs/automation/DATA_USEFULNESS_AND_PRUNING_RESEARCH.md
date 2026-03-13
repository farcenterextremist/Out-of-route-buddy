# Data usefulness and pruning — research

**Purpose:** How we evaluate which data is useful and what can be pruned. Use in Improvement Loop research (Phase 0), Synthetic Data Loop, Hub governance, and when defining retention or deprecation policy.

**Related:** [DATA_TIERS.md](../DATA_TIERS.md) (trip tiers), [SEND_TO_HUB_PROMPT.md](../agents/data-sets/hub/SEND_TO_HUB_PROMPT.md) (critique before deposit), [SYNTHETIC_DATA_LOOP_MASTER_PLAN.md](./SYNTHETIC_DATA_LOOP_MASTER_PLAN.md) (prune & mesh), [UNIVERSAL_LOOP_PROMPT.md](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md) (slop minimization).

---

## 1. What we already do

| Area | Current rule | Where |
|------|----------------|-------|
| **Trip data** | SILVER = candidate for termination; PLATINUM = demote/promote; GOLD = human-only, no auto-terminate | [DATA_TIERS.md](../DATA_TIERS.md) |
| **Hub** | Only **polished, completed** outputs; no drafts. "Critique before deposit": actionable? real artifacts? reusable without guessing? | hub/README, SEND_TO_HUB_PROMPT, UNIVERSAL_LOOP_PROMPT |
| **Synthetic Data Loop** | Prune low-quality/redundant (e.g. SILVER termination); mesh (merge/dedupe); **user approves** before tier changes | SYNTHETIC_DATA_LOOP_MASTER_PLAN, SYNTHETIC_DATA_LOOP_ROUTINE |
| **Project timeline** | Max 50 entries; **oldest pruned** | [RECENT_CHANGES_DATA.md](../agents/RECENT_CHANGES_DATA.md) |
| **Archive** | Superseded or legacy docs go to `docs/archive/`; README explains "archived; current work in CRUCIAL + ROADMAP" | docs/archive/README.md, SYSTEMS_CHECK_AND_CONTEXT_IMPROVEMENTS |

---

## 2. Research: when to retain vs prune

### Retain when

- **Archival / enduring value:** Documents core functions, supports high-risk areas, or has lasting importance for the next run or other agents.
- **Future use:** Another agent or the next loop will **reuse** it (e.g. proof of work, loop report, index, training data) without guessing.
- **Single source of truth:** Referenced as SSOT (e.g. KNOWN_TRUTHS, DATA_TIERS, CRUCIAL_IMPROVEMENTS_TODO).
- **Legislative / policy:** Required for compliance or audit (e.g. security proof, run ledger).

### Prune or archive when

- **Duplicate:** Same information exists elsewhere and is kept as canonical.
- **Temporary / housekeeping:** One-off run artifacts that no one will read again (e.g. scratch notes, superseded drafts).
- **Expired:** Retention period or "keep until" has passed (e.g. old Hub reports after N months if we adopt a policy).
- **No enduring value:** Not actionable, not traceable, not reusable; generic filler or slop.

*Sources: archival appraisal (National Archives, DCC "Appraise and select research data"), cost-benefit of selective retention (metadata and search noise).*

---

## 3. Usefulness criteria (evaluate before keep/deposit)

Use these to **evaluate** whether data is useful and worth keeping (or depositing to the Hub).

| Criterion | Meaning | Example |
|-----------|---------|--------|
| **Actionable** | Clear what to do with it; leads to concrete next steps or decisions | "Run X script"; "Apply tier change to trip IDs 1–5"; "Fix test Y per path Z" |
| **Reusable** | Another agent or human can use it **without guessing**; has context and references | Links to real files, run_id, loop #; not vague "consider improving X" |
| **Traceable** | Origin and lineage clear (who produced it, when, for which run/loop) | Date in filename, loop number, "Hub consulted" in summary |
| **References real artifacts** | Points to files, commits, test results, IDs — not abstract | Paths, trip IDs, validation_simulations list |
| **No duplicate** | Not redundant with another doc we already keep as canonical | If same content is in CRUCIAL or SSOT, prefer the canonical one |
| **Enduring** | Will still be useful next run or for another role | Proof of work, index, quality report; not one-off scratch |

*Sources: data quality dimensions (actionable, reusable, traceable); ISO 25024; our existing "critique before deposit" in SEND_TO_HUB_PROMPT and UNIVERSAL_LOOP_PROMPT.*

---

## 4. Recommendations

### For Hub deposits

- **Before "send to hub":** Run the usefulness checklist above. If the output is not actionable, not reusable, or doesn’t reference real artifacts, tighten it first (or don’t deposit).
- **Optional Hub retention:** If Hub grows large, define a simple policy (e.g. keep last 3 months of loop reports; archive older to `hub/archive/` or list as deprecated). Document in hub/README. See HUB_AND_LOOP_FUTURE_TODOS "Hub retention policy."
- **Deprecation field:** In the Hub index or in file front matter, allow optional `deprecated: true` or `superseded_by: <path>` so agents don’t follow outdated advice.

### For trip / app data

- Already defined: [DATA_TIERS.md](../DATA_TIERS.md). Pruning and termination apply to SILVER (and optionally PLATINUM) via `deleteTripsOlderThan(cutoff, maxTier)`; user approves tier changes in Synthetic Data Loop.

### For docs and automation

- **Archive superseded docs:** Move legacy or superseded prompts/plans to `docs/archive/` and note in README. Prefer one canonical doc per topic (e.g. IMPROVEMENT_LOOP_ROUTINE over 120_MINUTE_IMPROVEMENT_LOOP).
- **Cap and prune lists:** Like project_timeline (max 50, oldest pruned). Consider similar caps for Hub index rows or "suggested next steps" in summaries if they grow unbounded.

---

## 5. One-line "usefulness check" for agents

**Before keeping or depositing data, ask:** Is it **actionable** (clear next step)? Does it **reference real artifacts** (paths, IDs, results)? Would someone **reuse it without guessing**? If no to any, refine or don’t deposit. Prefer one sharp sentence over three vague ones.

---

*Use this doc in Phase 0 research (Improvement Loop, Synthetic Data Loop), when defining Hub retention/deprecation policy, or when drafting a "data usefulness checklist" for UNIVERSAL_LOOP_PROMPT or SEND_TO_HUB_PROMPT.*
