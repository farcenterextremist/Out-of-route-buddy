# Token Initiative — Policy Reference (Golden Storage Rules & Token Boss)

**Purpose:** Single source of truth for token policy. **All agents** get the essentials via the **always-apply rule** (`.cursor/rules/self-improvement.mdc` — Token awareness). This doc is the **full policy reference**; there is no round table convening — other agents do not receive this doc in context unless they load it. The always-apply rule already makes Golden Storage Rules and Token Boss authority available to every agent.

**When:** Any agent can reference this doc when clarifying token policy or Token Boss authority. At token loop start we do **not** convene a round table; we record state and start the listener.

## Token saving recommendations (all agents — easy to see)

**Every agent should follow these to save tokens:**

| # | Recommendation | Action |
|---|----------------|--------|
| 1 | **Minimal context** | @-mention only files you need; let the agent open more if required. |
| 2 | **Reference, don't inline** | Point to files (e.g. `@docs/agents/CODEBASE_OVERVIEW.md`) instead of pasting long blocks in rules or prompts. |
| 3 | **One always-apply rule** | Only one rule has `alwaysApply: true`; keep it short (~50 lines). Use globs for the rest. |
| 4 | **New chat when gauge >60%** | Start a new chat when the context gauge is high or when switching task/codebase. |
| 5 | **Front-load context** | Put relevant docs and paths in the first message instead of building over many messages. |
| 6 | **Use faster model for simple work** | Simple renames, formatting, single-line fixes use a faster/cheaper model (Cmd/Ctrl+/). |

**Latest recommended tasks from the token loop:** [TOKEN_LOOP_NEXT_TASKS.md](../automation/TOKEN_LOOP_NEXT_TASKS.md) — read the most recent run section. **Standard practices (updated each loop start/end):** [TOKEN_SAVING_PRACTICES.md](../automation/TOKEN_SAVING_PRACTICES.md). **Master plan (completion %):** [TOKEN_LOOP_MASTER_PLAN.md](../automation/TOKEN_LOOP_MASTER_PLAN.md).

---

# 1. The Token Initiative

The **Token Initiative** is project-wide policy to:

- **Save token spend** — Reduce Cursor IDE token usage without sacrificing output quality.
- **Manage context squish** — Avoid context window bloat (rules, conversation history, file context) so we stay effective and avoid summarization/dropping.
- **Boost production** — Smart and safe token optimization makes the whole team more productive: less waste, clearer context, better decisions.

**Every agent** participates. Token optimization is not optional; it is part of how we work.

---

# 2. Golden Storage Rules (all agents must abide)

These rules apply to **every agent** in every session:

| Rule | Meaning |
|------|---------|
| **Minimal context** | @-mention or attach only the files you need. Let the agent open more if required; don’t front-load everything. |
| **Reference, don’t inline** | In rules and docs, point to files (e.g. `@docs/agents/CODEBASE_OVERVIEW.md`) instead of pasting long blocks. |
| **One always-apply rule** | Only one project rule should have `alwaysApply: true`; keep it short (~50 lines). All other rules use globs or description-only. |
| **New chat when gauge >60%** | Start a new chat when the context gauge is high or when switching task/codebase area. Don’t pile history. |
| **Front-load when starting a task** | Put relevant docs and paths in the first message instead of building context over many messages. |
| **Obey Token Boss** | Token Boss may dictate model choice and token policy. Comply with model delegation and storage rules. |
| **State at loop start** | At the start of every token loop we record current state (snapshot) for rollback and progress tracking. Don’t skip it. |

Violations waste tokens and hurt the whole team. When in doubt, ask or check [TOKEN_REDUCTION_LOOP.md](../automation/TOKEN_REDUCTION_LOOP.md) and [TOKEN_LOOP_LISTENER.md](../automation/TOKEN_LOOP_LISTENER.md).

---

# 3. Token Boss — Authority and Role

**Token Boss** is the single authority for token policy and resource allocation. Think of Token Boss as:

- **Federal Reserve** — Sets token “monetary” policy: how much context we use, when to reset, what counts as waste.
- **IRS** — Enforces the rules: audits always-apply rules, checks Golden Storage compliance, tracks listener data.
- **Secretary of the Treasury** — Allocates resources: **may dictate which LLM models are delegated to which agents**. If an agent is underperforming (e.g. burning too many tokens for the value delivered), Token Boss can direct that agent to use a faster or cheaper model until performance improves.

**In practice:**

- Token Boss is represented by the token loop process and docs: [TOKEN_REDUCTION_LOOP.md](../automation/TOKEN_REDUCTION_LOOP.md), [TOKEN_LOOP_LISTENER.md](../automation/TOKEN_LOOP_LISTENER.md), and this briefing.
- When the user says “start token loop,” the agent running the loop acts as Token Boss for that run: records state, runs the audit steps, and uses listener data to improve the loop.
- **Model delegation:** Token Boss (via docs or run instructions) can specify that certain task types use a faster model (e.g. simple renames, formatting) and reserve stronger models for complex multi-file or architecture work. Underperforming agents may be directed to use the delegated model until metrics improve.

All agents must abide by Token Boss directives. No agent is exempt from the Golden Storage Rules or from model delegation when Token Boss applies it.

---

# 4. What We Do at Token Loop Start

1. **Record current state** — Run `token_loop_state_snapshot.ps1` with the RunId. Snapshot goes to `docs/automation/token_loop_snapshots/<run_id>.json`. Use it for rollback and progress tracking (what works / what doesn’t).
2. **Start listener** — Fire `token_loop_start` with the **same RunId**; then run steps 1–6 with step_start/step_end and token_loop_end. Listener data is used to improve the loop.

(No round table — all agents already get Golden Storage Rules and Token Boss via the always-apply rule.)

---

# 5. Handoff

After recording state and starting the listener, the agent running the token loop proceeds with the 6 steps in [TOKEN_REDUCTION_LOOP.md](../automation/TOKEN_REDUCTION_LOOP.md). All other agents, when invoked, follow the Golden Storage Rules and Token Boss policy via the **always-apply rule** (self-improvement.mdc).

*End of Token Initiative — Policy Reference.*
