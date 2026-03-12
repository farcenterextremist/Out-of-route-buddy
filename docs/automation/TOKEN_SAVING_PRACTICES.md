# Token Saving — Standard Practices

**Purpose:** Single source of standard token-saving practices. **Updated each token loop:** at **start** (Step 0) we read and optionally refresh from latest research; at **end** (Step 7) we record what worked and what didn't for this run and add any new practices we learned.

**References:** [TOKEN_REDUCTION_LOOP.md](./TOKEN_REDUCTION_LOOP.md), [TOKEN_LOOP_IMPROVEMENT_PLAN.md](./TOKEN_LOOP_IMPROVEMENT_PLAN.md), [TOKEN_LOOP_NEXT_TASKS.md](./TOKEN_LOOP_NEXT_TASKS.md)

**Last updated:** 2026-03-11 (research merge; Launch 3 prep)

---

## 1. Standard practices (canonical list)

### Rules

- **Always-apply:** Only one rule should be always-apply; keep it under ~50 lines (~500 tokens). Every always-apply rule is sent on every prompt. ([Developer Toolkit](https://developertoolkit.ai/en/cursor-ide/advanced-techniques/token-management/))
- **Convert others:** Use glob-scoped (e.g. `app/**/*.kt`) or agent-decided (`description` set, `alwaysApply: false`) for feature/style rules.
- **Reference, don't inline:** In rules, point to files (e.g. `@docs/agents/CODEBASE_OVERVIEW.md`) instead of pasting long examples.

### Conversation

- **Start fresh when:** Context gauge >60%; shifting area of codebase; agent repeats or uses outdated info; after finishing one logical task. ([Developer Toolkit](https://developertoolkit.ai/en/cursor-ide/advanced-techniques/token-management/))
- **Front-load context:** Put relevant files and instructions in the first message instead of building context over many messages.
- **Focused prompts:** Ask specific questions rather than dumping whole files; plan the request before asking. ([Dre Dyson](https://dredyson.com/token-management-101-a-beginners-guide-to-optimizing-ai-usage-in-cursor-ide/), [Medium](https://medium.com/@2026jwutubechnl/good-habits-that-actually-save-tokens-in-cursor-ai-3b962916691f))

### File context

- **Minimum viable context:** Attach only files the agent needs; let it open more if required. ([Developer Toolkit](https://developertoolkit.ai/en/cursor-ide/advanced-techniques/token-management/))
- **Use @folder:** For directory overviews instead of attaching every file in the folder.

### Model choice

- **Faster/cheaper model for:** Simple renames, formatting, single-line fixes, comments. **Stronger model for:** Multi-file features, complex debugging, architecture decisions. ([Developer Toolkit](https://developertoolkit.ai/en/cursor-ide/advanced-techniques/token-management/))
- **Cursor Auto Mode** can reduce cost by selecting cheaper models when appropriate. ([Medium](https://medium.com/@2026jwutubechnl/good-habits-that-actually-save-tokens-in-cursor-ai-3b962916691f))

### Cursor-specific

- **Dynamic context discovery:** Cursor loads less upfront; agent pulls what it needs; long tool outputs go to files. Keeps context under ~50k tokens per chunk where possible. ([Cursor blog](https://cursor.com/blog/dynamic-context-discovery), [Developer Toolkit](https://developertoolkit.ai/en/cursor-ide/advanced-techniques/token-management/))
- **Memory:** Disable memories for quick projects to save tokens (e.g. "Hi" with memory can cost thousands of tokens). ([Dre Dyson](https://dredyson.com/token-management-101-a-beginners-guide-to-optimizing-ai-usage-in-cursor-ide/))
- **Workspace settings:** `files.watcherExclude` and `search.exclude` for `.gradle`, `build`, `.cursor` reduce noise and indexing.

### General LLM / prompt optimization (for reference)

- **Prompt diet:** Remove filler and ceremonial phrasing; condense instructions. Can yield large token reductions. ([Dev.to](https://dev.to/lakshmisravyavedantham/i-put-my-prompts-on-a-diet-and-cut-my-llm-bill-by-72-2425), [Burnwise](https://www.burnwise.io/blog/token-optimization-guide))
- **Output control:** `max_tokens` and format constraints can save 20–40% on output (output tokens often 3–8× cost of input). ([Burnwise](https://www.burnwise.io/blog/token-optimization-guide))
- **Context limit caveat:** Many models degrade before advertised limit (e.g. 200K window unreliable around 130K). ([Zylos](https://zylos.ai/research/2026-01-19-llm-context-management))

---

## 2. When we update this doc

| When | Action |
|------|--------|
| **Loop start (Step 0)** | Read this doc; note if practices need refresh from latest research; align run with current list. |
| **Loop end (Step 7)** | Append one line to §3 "What worked / What didn't" for this run (run_id, 1–2 bullets). Add any new practice learned this run to §1 and cite source. |

---

## 3. What worked / What didn't (by run)

*(At Step 7 of each token loop, append one short block below.)*

**token-20260311-2115:** What worked — Snapshot fix (always_apply_count from rules array); improvement plan read at Step 0; single-quoted metrics for listener. What didn't — (none this run).

**token-20260311-2031:** What worked — Snapshot + listener wired; one always-apply rule; rules reference docs. What didn't — Snapshot reported always_apply_count 3 (bug; correct 1); duplicate .cursor/rules paths.

---

*Update at loop start (read/refresh) and loop end (record what worked/didn't + new practices).*
