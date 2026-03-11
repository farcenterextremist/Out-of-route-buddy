# Improvement Loop — Logic & Reasoning

**Purpose:** Inject explicit logic and reasoning into the Improvement Loop so decisions are deliberate, traceable, and improvable. Do not act on autopilot—think before each phase and each change.

**References:** [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md), [IMPROVEMENT_LOOP_COMMON_SENSE.md](./IMPROVEMENT_LOOP_COMMON_SENSE.md), [LOOP_TIERING.md](./LOOP_TIERING.md)

---

## Reasoning Checkpoints

At these points, **pause and reason** before proceeding:

| Checkpoint | When | Reasoning prompt |
|------------|------|------------------|
| **0. Before research** | Start of Phase 0 | "What do I need to know to make good decisions this run? What did last run miss?" |
| **1. Before task selection** | After research, before classification | "Given this focus and backlog, which 1–2 tasks give the highest value for lowest risk? Why?" |
| **2. Before each change** | Before editing any file | "What is my goal? What could go wrong? Is there a simpler option?" |
| **3. Before skipping** | When deferring a task | "Why am I skipping this? Document so next run can decide." |
| **4. Before summary** | End of Phase 4 | "What did I learn? What should the next run do differently?" |

---

## Reasoning Framework

**Before acting on any task:**

1. **State the goal** — What outcome do I want?
2. **Consider options** — What are 2–3 ways to achieve it? (including "do nothing")
3. **Predict consequences** — If I do X, what might break? What downstream effects?
4. **Choose and justify** — Pick one. Write one line: "Chose X because Y."

**Before classifying a task (Light/Medium/Heavy):**

1. **What's the blast radius?** — One file? One module? Whole app?
2. **What's the revert cost?** — Can we undo easily?
3. **What's the user impact?** — Visible? Invisible? Risky?
4. **Tier** — Light = additive, no logic. Medium = small, localized. Heavy = new feature, architecture.

---

## Logic Rules

| Rule | Application |
|------|-------------|
| **If uncertain → suggest, don't implement** | When reasoning yields "I'm not sure," add to summary as a suggestion. |
| **If multiple options → pick lowest risk** | When 2+ approaches exist, prefer the one with smallest blast radius. |
| **If stuck >10 min → document and defer** | Timebox. Reasoning: "Spending more time has diminishing returns." |
| **If tests fail → revert first, reason second** | Don't reason your way out of red tests. Revert, then reason about what went wrong. |
| **If design intent conflicts → ask** | Don't assume. User preferences override convenience. |
| **If user says "implement X" (Heavy) → question lock** | Do not implement. Ask: "Would you like to see a generated image or layout or simulate a merge?" See [LOOP_TIERING.md](./LOOP_TIERING.md) § Question Lock. |

---

## Reasoning Output in Summary

Add a **Reasoning** section to each summary:

```markdown
## Reasoning (this run)

| Decision | Rationale |
|----------|-----------|
| Chose [task X] over [task Y] | [X] has lower risk; [Y] would touch N files. |
| Skipped [task Z] | Would require design decision; documented for next run. |
| Applied [change] | Goal: [outcome]. Considered [A, B]; chose [A] because [reason]. |
```

**Purpose:** Makes logic traceable. Next run (or user) can see why choices were made. Improves the loop over time.

---

## Integration

- **IMPROVEMENT_LOOP_ROUTINE:** Read this doc at loop start. Apply reasoning checkpoints at Phase 0, 1, 2, 3, 4.
- **Summary template:** Include "Reasoning (this run)" in Phase 4.3.
- **Common sense:** "When uncertain → suggest" is a reasoning rule. Apply throughout.

---

*Integrates with IMPROVEMENT_LOOP_ROUTINE. Think before you act.*
