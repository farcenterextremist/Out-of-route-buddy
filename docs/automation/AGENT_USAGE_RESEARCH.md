# Agent Usage Research — When, Where, and How in Improvement Loops

**Purpose:** Research-backed guidance on how agents should be used in software improvement projects. Informs sub-agent design in the Improvement Loop.

**Date:** 2025-03-11  
**References:** [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md), [IMPROVEMENT_LOOP_TEAMS.md](./IMPROVEMENT_LOOP_TEAMS.md)

---

## 1. When to Use AI Agents

| Use Case | Use Agent? | Why |
|----------|------------|-----|
| **Real bugs requiring observation** | Yes | Production issues (config, queries, API payloads) are discovered by observing behavior, not just generation |
| **Iteration needed** | Yes | Tasks requiring multiple attempts, verification, refinement benefit from agent loops |
| **Complex multi-step work** | Yes | Migrations, refactors, multi-file changes need verification at each step |
| **Single-shot code generation** | Optional | Simple; agents add value when feedback loops exist |
| **High-risk changes without validation** | No | Use human gate; sandbox first |

**Core principle:** Agents become useful when they operate with **working feedback loops** — run code, inspect results, iterate until evidence of success. Not just generation.

---

## 2. Agent Types and Roles

| Agent Type | When to Use | Examples |
|------------|-------------|----------|
| **Coordinator / Orchestrator** | Always; runs the loop | Main agent that reads routine, spawns subagents, aggregates results |
| **GeneralPurpose** | Code reasoning, research, multi-file changes | Dead code removal, test fixes, BuildConfig alignment, design research |
| **Shell** | Terminal commands, tests, lint | `gradlew test`, `gradlew lint`, pulse_check.ps1 |
| **Specialist (Meta-Research)** | Improve research quality | Answer META_RESEARCH_CHECKLIST; identify gaps |
| **File Organizer** | End of loop; outputs | Recommend new ideas; propose moves/renames |

---

## 3. Sub-Agent Delegation Pattern

**Workflow (research-backed):**

1. **Planning** — Coordinator analyzes task; decides what to delegate
2. **Creation** — Spawn specialized subagents with clear roles and scope
3. **Delegation** — Distribute tasks for **parallel** execution when possible
4. **Aggregation** — Collect results; synthesize into final output

**Benefits:**

- **Context preservation** — Subagents isolate work; main agent gets final result, not intermediate steps
- **Parallelization** — Multiple subagents run simultaneously; lower latency
- **Specialization** — Each subagent can have domain-specific instructions, tools, or models
- **Resource control** — Limit concurrent subagents, timeouts, token usage per subagent

---

## 4. Where to Use Subagents (Improvement Loop Mapping)

| Phase | Subagent | Task | Parallel? |
|-------|----------|------|------------|
| **0.0b** | GeneralPurpose (Meta-Research) | META_RESEARCH_CHECKLIST; one-line meta-note | Optional; can run with 0.1 |
| **0.4** | GeneralPurpose | Design & UX research (color, typography, state flows) | Yes, with 0.1 |
| **1.1** | GeneralPurpose | Dead code removal; BuildConfig alignment | Yes |
| **1.1** | Shell | Unit tests | Yes |
| **1.2** | GeneralPurpose | Security grep; SECURITY_LOOP_CHECKLIST when Security focus | Yes |
| **2.1** | GeneralPurpose | Test health; @Ignore review | Yes |
| **2.5** | Shell | Unit tests | Sequential (after 2.1) |
| **3.2** | GeneralPurpose | Stat card review; apply design research | Yes |
| **4.3** | File Organizer | Recommend new ideas; metrics-based next focus | After summary |

---

## 5. Best Practices

| Practice | Implementation |
|----------|----------------|
| **Clear subagent descriptions** | Write detailed prompts; include file paths, constraints, output format |
| **Explicit success criteria** | "Fix or document"; "Add one cross-link"; "Do not touch X" |
| **Model selection** | Fast for Shell; more capable for code reasoning (GeneralPurpose) |
| **Timebox** | "Do not spend >10 min on any single test" |
| **Evidence bundles** | Subagent returns: what changed, why, test result, logs |
| **Small diffs** | One improvement per category; traceable changes |

---

## 6. Integration with Improvement Loop

- **Subagent Spawn Reference** in [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md) § Subagent Spawn Reference
- **Teams** in [IMPROVEMENT_LOOP_TEAMS.md](./IMPROVEMENT_LOOP_TEAMS.md) — Researchers, Meta-Researchers, File Organizer
- **Autonomy** — Subagents run with same tier rules: Light/Medium auto; Heavy requires human approval

---

*Use this research to refine subagent spawns and delegation in the Improvement Loop.*
