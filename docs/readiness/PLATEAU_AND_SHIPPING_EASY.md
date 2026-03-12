# When We've Plateaued on the Improvement Loop and Shipping Is Easy

**Purpose:** Describe the state where the Improvement Loop has done most of its work and shipping a release is low-friction. Use this to decide when to run the loop less often or when to shift focus to product/features instead of polish.

**References:** [GOAL_AND_MISSION.md](../GOAL_AND_MISSION.md), [CRUCIAL_IMPROVEMENTS_TODO.md](../CRUCIAL_IMPROVEMENTS_TODO.md), [docs/automation/IMPROVEMENT_LOOP_ROUTINE.md](../automation/IMPROVEMENT_LOOP_ROUTINE.md), [docs/release/FIRST_RELEASE_GO_NO_GO.md](../release/FIRST_RELEASE_GO_NO_GO.md)

---

## What "plateau" means

The loop **plateaus** when:

- Most runs produce **few or no high-value tasks** — suggested next steps are minor (e.g. "rotate design research topic") or repeat the same deferred items (e.g. Gradle 9, one ignored test).
- **CRUCIAL_IMPROVEMENTS_TODO** is mostly ✅ or explicitly deferred; no critical open work that blocks ship or quality.
- **Build, tests, and lint** are stable — no recurring red state; fixes are one-offs, not systemic.
- **REDUNDANT_DEAD_CODE_REPORT** and **FAILING_OR_IGNORED_TESTS** are either cleaned up or documented with clear "defer" reasons; the loop isn't finding new debt faster than we fix it.
- **Security and store prerequisites** are met or in a known state (privacy policy, keystore, Data safety, etc.) so a release decision is about "when" not "if."

Plateau does **not** mean "nothing left to do." It means the **marginal return** of another loop run is low and shipping is **easy** in the sense below.

---

## What "shipping is easy" means

Shipping is **easy** when:

1. **Repeatable path** — You can go from "tag release" to "signed build uploaded" in a small number of steps (version bump, changelog, build, sign, upload) without fighting the build or environment. See [STORE_CHECKLIST.md](../STORE_CHECKLIST.md) and [DEPLOYMENT.md](../DEPLOYMENT.md).
2. **No surprise blockers** — Store checklist (privacy, keystore, Data safety, release notes) is done or explicitly scheduled; [FIRST_RELEASE_GO_NO_GO.md](../release/FIRST_RELEASE_GO_NO_GO.md) blockers are resolved or accepted as deferred with notes.
3. **Confidence in quality** — Unit tests pass; lint is clean or known; manual smoke / release matrix (or equivalent) has been run and signed off so you're not shipping blind.
4. **Loop optional** — You can ship **without** running a full Improvement Loop first; the loop becomes maintenance (e.g. monthly or pre-major-feature) rather than a gate.

---

## Signals that we're there

| Signal | Meaning |
|--------|--------|
| **Loop summary says "Light + Medium only; no Heavy; suggested next steps are refinements"** | Little high-impact work left in the loop's scope. |
| **CRUCIAL summary table is mostly Done or Deferred** | Backlog is under control. |
| **Build and tests green consistently** | No recurring instability. |
| **Store checklist and go/no-go have no open blockers** | Release is unblocked. |
| **Ship instructions (e.g. OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt) are repeatable** | Same steps work each time. |
| **Grand progress bar is mostly green** | Most parameters at ceiling; see [GRAND_PROGRESS_BAR.md](./GRAND_PROGRESS_BAR.md). |

---

## What to do when we've plateaued

- **Run the loop less often** — e.g. monthly or only before a major feature / refactor, instead of every 1–2 weeks.
- **Use the loop for maintenance** — Keep it for: dependency bumps, security notes, occasional dead-code cleanup, and rotating design research. Don't expect big "suggested next steps" every time.
- **Shift focus** — Prioritize product (features, UX, adoption) and "what finished looks like" ([WHAT_FINISHED_LOOKS_LIKE.md](./WHAT_FINISHED_LOOKS_LIKE.md)) over incremental polish.
- **Keep the progress bar updated** — So we notice if something slips from green to amber and can run a targeted loop or fix.

---

*Plateau = loop has done its job for this phase. Shipping easy = release path is repeatable and unblocked. See [WHAT_FINISHED_LOOKS_LIKE.md](./WHAT_FINISHED_LOOKS_LIKE.md) for the end-state bar and [GRAND_PROGRESS_BAR.md](./GRAND_PROGRESS_BAR.md) for the aggregate view.*
