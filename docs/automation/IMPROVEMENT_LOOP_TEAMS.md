# Improvement Loop — Team Structure

**Purpose:** Define the teams at the front (research) and back (file organization) of the Improvement Loop. Autonomy rules: Light and Medium may run without human approval; Heavy always requires human-in-the-loop.

**References:** [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md), [LOOP_TIERING.md](./LOOP_TIERING.md), [team-structure.md](../agents/team-structure.md)

---

## Front: Research Teams

### Researchers

Primary research in Phase 0: CRUCIAL, REDUNDANT_DEAD_CODE, FAILING_OR_IGNORED_TESTS, security, design. See IMPROVEMENT_LOOP_ROUTINE Phase 0.

**Brainstorming & idea generation (Light):** Based on current context and research, document possible ideas, suggestions, and future improvements in [BRAINSTORM_AND_TASKS.md](./BRAINSTORM_AND_TASKS.md). Research more ways to improve; add 1–2 suggestions per loop.

### Meta-Researchers (Researchers for Researchers)

**Purpose:** Improve the quality of research produced by the primary researchers.

| Task | Action |
|------|--------|
| **Research quality review** | Assess whether research outputs are complete, well-sourced, and actionable |
| **Method improvement** | Suggest better research methods, sources, or question framing |
| **Gap identification** | Find blind spots: what did researchers miss? What should they have asked? |
| **Output refinement** | Propose clearer research notes, better one-line summaries, or more useful findings |

**Checklist:** Use [META_RESEARCH_CHECKLIST.md](./META_RESEARCH_CHECKLIST.md) — 5 questions per run.

**When:** Run at the start of Phase 0 (Phase 0.0b). Meta-researchers can spawn as a subagent to review the research plan or prior loop's research quality.

**Output:** One-line meta-note: "Research quality: X. Gaps: Y. Suggested improvement: Z."

---

## Back: File Organization Team

### File Organizer

**Purpose:** At the end of the Improvement Loop, organize outputs and recommend new ideas.

| Task | Action |
|------|--------|
| **File organization** | Structure summary, docs, and artifacts; propose moves or renames per [file-organizer.md](../agents/roles/file-organizer.md) |
| **Organizing research** | Best practices for repo layout; when "File Structure" is focus, propose one move/rename (document only; user approves before apply) |
| **Recommend new ideas** | Propose new tasks to add to Light, Medium, or Heavy (LOOP_TIERING, CRUCIAL, FUTURE_IDEAS) |
| **Idea placement** | Classify each recommendation: Light (auto), Medium (auto), or Heavy (human approval required) |
| **Research improvements & populate tasks** | Research more ways to improve; add ideas to [BRAINSTORM_AND_TASKS.md](./BRAINSTORM_AND_TASKS.md); classify and promote 1–2 per loop to CRUCIAL or FUTURE_IDEAS |
| **Heavy tier population** | **Add at least 1–2 new Heavy ideas per run** when [HEAVY_TIER_IDEAS.md](./HEAVY_TIER_IDEAS.md) is below 50. Light and Medium must produce ideas for Heavy; File Organizer adds to FUTURE_IDEAS and HEAVY_TIER_IDEAS with state (sandbox-ready / sandboxed / proposed). |
| **Sandbox improvement (every loop)** | **Improve on sandboxed ideas each run.** Add design brief, validation checklist, or advance completion % for 1–2 ideas in HEAVY_TIER_IDEAS. Use [SANDBOX_COMPLETION_PERCENTAGE.md](./SANDBOX_COMPLETION_PERCENTAGE.md) — true % only; merging should not be taken lightly. Report % in summary. |
| **Metrics-based recommendations** | Use [LOOP_METRICS_TEMPLATE.md](./LOOP_METRICS_TEMPLATE.md) to recommend next focus (e.g., "Lint warnings increased → next focus: Code Quality") |
| **Metadata-based recommendations** | When user metadata is available, use signals from [USER_METADATA_USAGE_GUIDE.md](./USER_METADATA_USAGE_GUIDE.md) (e.g., high recovery rate → suggest UI/UX focus for recovery flow) |

**When:** Phase 4 (Final Pulse & Summary). File Organizer runs after the summary is written; adds "Recommended new ideas" section.

---

## Autonomy Rules

| Tier | Autonomous? | Human approval |
|------|-------------|----------------|
| **Light** | Yes | Not required — may run without stopping |
| **Medium** | Yes | Not required — may run without stopping |
| **Heavy** | No | **Required** — visual image + user says **"approve 100% implement"** before implementation |

**Visual approval clause:** Features 100% sandboxed in Heavy must have a simple visual image (where + what it looks like) and user must say **"approve 100% implement"** before implementation. See [LOOP_TIERING.md](./LOOP_TIERING.md) § Visual Approval Clause.

**Rule:** All ideas recommended to Heavy must first be approved by a human in the loop. Light and Medium may run autonomously when the loop is configured for full autonomy.

---

## Idea Recommendation Flow

When File Organizer recommends new ideas:

1. **Classify** each idea as Light, Medium, or Heavy.
2. **Light / Medium** — Add to LOOP_TIERING examples, CRUCIAL, or suggested next steps. These may run in the next autonomous loop.
3. **Heavy** — Add to FUTURE_IDEAS (sandboxed) and [HEAVY_TIER_IDEAS.md](./HEAVY_TIER_IDEAS.md) with state (sandbox-ready / sandboxed / proposed). **Do not implement** until human explicitly approves: "Approve Heavy idea X."
4. **Cap:** Heavy tier max 50 ideas. When below cap, add at least 1–2 Heavy ideas per run. Light and Medium must produce ideas for Heavy each loop.

---

## Quick Reference

| Position | Team | Role |
|----------|------|------|
| **Front** | Researchers | Phase 0 research: CRUCIAL, dead code, tests, security, design |
| **Front** | Meta-Researchers | Improve research quality; identify gaps; refine methods |
| **Back** | File Organizer | Organize outputs; recommend new ideas to Light/Medium/Heavy |
| **Gate** | Human | Required for Heavy; optional for Light/Medium (autonomous) |
