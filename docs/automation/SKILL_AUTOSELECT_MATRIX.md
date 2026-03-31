# Skill Auto-Select Matrix

**Purpose:** Fast mapping from request type to the best project skill.

**Use:** At loop start or before non-trivial loop-governance work, scan this matrix and pick one primary skill.

**Role map:** See `docs/automation/LOOP_ROLE_CUBE.md` for the methodical loop-role organization model.

---

## Quick mapping

| If the request sounds like... | Use this skill | Why |
| --- | --- | --- |
| "desktop guide", "operator guide", "flywheel guide", "update the desktop doc" | `.cursor/skills/loop-desktop-guide-curator/SKILL.md` | Keeps the readable desktop guide synced from repo truth and export-tested. |
| "blind spots", "what are we missing", "loop gaps", "fortify the loop system" | `.cursor/skills/loop-blind-spot-reviewer/SKILL.md` | Surfaces hidden loop risks such as weak handoffs, proof gaps, and guide drift. |
| "loop hardening", "loop tests", "loop mocks", "loop simulations" | `.cursor/skills/loop-fortification-engineer/SKILL.md` | Adds low-risk tests, simulations, and proof-oriented checks to strengthen loop reliability. |
| "loopmaster", "super loop", "one giant loop", "duplicate loop across tabs" | `.cursor/skills/loopmaster-orchestrator/SKILL.md` | Central orchestrator for the unified Loopmaster contract, gated lanes, and bucket evidence. Optional role-based tabs when user opens them; no fixed tab count or synchronous automation. |
| "loop architect", "design the loops", "loop structure owner" | `.cursor/skills/loop-architect/SKILL.md` | Top-level architecture owner for loop structure, gates, and orchestration direction. |
| "compare loops", "consistency", "standardize all loops" | `.cursor/skills/loop-consistency-auditor/SKILL.md` | Scores runs with one contract (`X/10`). |
| "loop architecture", "gate redesign", "workflow model", "state machine loop" | `.cursor/skills/loop-architecture-blueprinter/SKILL.md` | Designs resilient loop/gate architecture upgrades safely. |
| "loop health manager", "manage loop health", "system health during loops" | `.cursor/skills/loop-health-manager/SKILL.md` | Operational owner for liveness, readiness, continuity, and diagnostic status. |
| "health check all loops", "debug the loop", "search for problems", "diagnostic sweep" | `.cursor/skills/loop-diagnostic-sweeper/SKILL.md` | Adds active problem hunting on top of health checks. |
| "fix ledger format", "normalize templates", "run log drift" | `.cursor/skills/loop-ledger-normalizer/SKILL.md` | Keeps ledger blocks consistent across loops. |
| "show readiness trend", "health metrics", "perf regression" | `.cursor/skills/loop-readiness-benchmarker/SKILL.md` | Tracks liveness/readiness trends and regressions. |
| "continuity tests", "gate regression", "shared-state reliability" | `.cursor/skills/loop-continuity-test-engineer/SKILL.md` | Turns loop assumptions into executable continuity checks. |
| "proof of quality", "quality gate", "show evidence" | `.cursor/skills/loop-quality-proof/SKILL.md` | Enforces evidence-backed quality claims. |
| "pleasant UI", "polished design", "screen flow", "professional frontend" | `.cursor/skills/frontend-pleasantness-reviewer/SKILL.md` | Scores visual pleasantness and flow with the project rubric. |
| "review this screenshot", "before and after UI", "visual review", "does this look good" | `.cursor/skills/frontend-screenshot-reviewer/SKILL.md` | Reviews UI quality using screenshot evidence instead of memory or taste alone. |
| "study Material", "learn Android UI design", "M3 curriculum", "design skills intermediate", "week 1 Android frontend" | `.cursor/skills/android-m3-design-study/SKILL.md` | Structured intermediate study path for Material 3 on Android (see `reference.md`). |
| "audit UI", "theme audit", "layout audit", "a11y pass", "touch targets", "before frontend fix" | `.cursor/skills/android-material-ui-audit/SKILL.md` | M3 + accessibility checklist for View/XML screens and prioritized fix lists. |
| "send to hub", "polished artifact", "tighten hub summary" | `.cursor/skills/hub-handoff-curator/SKILL.md` | Improves handoff quality and hub indexing. |
| "shared state stale", "loop_latest mismatch", "dynamic sharing check" | `.cursor/skills/shared-state-reconciler/SKILL.md` | Reconciles shared events and latest-state files. |
| "is this Heavy?", "approval gate", "question lock", "visual approval" | `.cursor/skills/heavy-approval-governor/SKILL.md` | Enforces Heavy-tier approval rules safely. |
| "reduce slop", "tighten wording", "critique metadata" | `.cursor/skills/critique-data-minimize-slop/SKILL.md` | Removes vague/sloppy output and metadata bloat. |
| "run master loop", "run improvement loop" | `.cursor/skills/improvement-loop-wizard/SKILL.md` | Executes routine phases with required artifacts. |
| "security review", "threat checks", "defense posture" | `.cursor/skills/security-analyst-agent/SKILL.md` | Applies security-focused review patterns. |
| "release readiness", "changelog", "ship flow" | `.cursor/skills/shipping-specialist/SKILL.md` | Handles release/versioning/deployment workflow. |

---

## Selection rules

1. Pick one **primary skill** first.
2. Add one **secondary skill** only if needed (quality, shared state, or hub handoff).
3. Avoid stacking 3+ skills unless the user asks for a broad audit.

---

## Loop default bundle

For most loop runs, use this order:

1. `improvement-loop-wizard` (or loop routine)
2. `loop-diagnostic-sweeper`
3. `loop-consistency-auditor`
4. `loop-quality-proof`
5. `hub-handoff-curator` (when depositing)

---

## Guardrails

- Skills guide workflow; they do not override user approval gates.
- Frontend/UI implementation remains Master Loop-only.
- Heavy-tier implementation remains approval-gated.
