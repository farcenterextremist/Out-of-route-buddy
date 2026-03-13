# All loops for each agent — workflow guide

**Purpose:** One place that explains **every loop** in the project, **which agent runs it**, and the **workflow** (trigger → phases/steps → outputs). Use this when onboarding an agent or when you need the full picture.

**References:** [LOOPS_AND_IMPROVEMENT_FULL_AXIS.md](./LOOPS_AND_IMPROVEMENT_FULL_AXIS.md), [LOOP_MASTER_ROLE.md](./LOOP_MASTER_ROLE.md), [UNIVERSAL_LOOP_PROMPT.md](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md).

---

## What every agent must do before any loop

**Universal rules (apply to every loop, every agent):**

1. **Read Loop Master** — [LOOP_MASTER_ROLE.md](./LOOP_MASTER_ROLE.md). If the user said **"start master loop"**, you are the Loop Master: do **Step 0.M** first, then run the Improvement Loop.
2. **Read the Hub** — Open [hub/README.md](../agents/data-sets/hub/README.md); scan the index; read or skim files relevant to your loop. Note **Hub consulted** and **Advice/rules applied** at run start.
3. **Research self-improvement and loop-improvement** — Include [LOOP_LESSONS_LEARNED.md](./LOOP_LESSONS_LEARNED.md), [SELF_IMPROVING_LOOP_RESEARCH.md](./SELF_IMPROVING_LOOP_RESEARCH.md), [CURSOR_SELF_IMPROVEMENT.md](./CURSOR_SELF_IMPROVEMENT.md) in your research. Note what you applied.
4. **Tiers** — Auto-implement **Light** and **Medium**. **Drastic loop improvements** (routine changes, new phases, new loops) = **Heavy**: document only; do not implement without human approval.
5. **Shared state** — At start read [loop_shared_events.jsonl](./loop_shared_events.jsonl) (tail) and [loop_latest/](./loop_latest/) (other loops’ latest). At end append a **finished** event to `loop_shared_events.jsonl` and update **loop_latest/&lt;your_loop&gt;.json**. See [LOOP_DYNAMIC_SHARING.md](./LOOP_DYNAMIC_SHARING.md).
6. **Minimize slop** — Output specific, traceable, actionable. Critique before depositing. When you have polished output, consider **send to hub** (deposit to `docs/agents/data-sets/hub/` and add a line to hub/README.md).

---

## Loop 1: Master Loop (orchestrator)

| What | Detail |
|------|--------|
| **Who runs it** | Any agent when the user says **"start master loop"**. That agent becomes the **Loop Master**. |
| **Trigger** | User says **"start master loop"**. |
| **Goal** | Research all loops, align universal files, read the Hub, then run the **full Improvement Loop**. |

### Workflow

1. **Step 0.M (Loop Master only)**  
   - Research all loops from [LOOPS_AND_IMPROVEMENT_FULL_AXIS.md](./LOOPS_AND_IMPROVEMENT_FULL_AXIS.md).  
   - Compare and scrutinize: naming, ledger format, Hub usage, proof of work.  
   - Update universal files if needed (IMPROVEMENT_LOOP_FOR_OTHER_AGENTS, LOOP_TIERING, etc.).  
   - **Read the Hub** — hub/README.md and relevant files from the index. Note Hub consulted and advice applied.  
2. **Then** run the **full Improvement Loop** (checkpoint → Phase 0–4 → summary → ledger → shared state).  

**Outputs:** Step 0.M notes; then same outputs as the Improvement Loop (summary, ledger, pulse, loop_latest/improvement.json, optional Hub deposit).

---

## Loop 2: Improvement Loop (2-hour / GO)

| What | Detail |
|------|--------|
| **Who runs it** | Any agent (often general-purpose or “Improvement Loop” agent). Triggered by **GO** or **"start improvement loop"** or **"run improvement loop"**. If user said **"start master loop"**, the Loop Master runs this after Step 0.M. |
| **Trigger** | **GO** / **start improvement loop** / **run improvement loop** (or after Step 0.M when user said **start master loop**). |
| **Goal** | Obvious improvements, security hardening, smoothness. Mostly backend (~75–85%). Light and Medium auto; Heavy requires human approval. |

### Workflow (phases)

| Phase | Name | What happens |
|-------|------|----------------|
| **0** | Research & allowlist | 0.0a User preferences; 0.0 Checkpoint; 0.0c Read Hub; 0.0d Read shared state (other loops). 0.1 Research (CRUCIAL, last summary, security, design, HEAVY_IDEAS_FAVORITES, self-improvement docs). 0.1b Classify tasks Light/Medium/Heavy; if Heavy exist, prompt user. Optional: 0.3 Cursor self-improvement, 0.6 Token audit (short), 0.4 Design/UX research. |
| **1** | Quick wins + security + smoothness | Dead code (1–2 items), security (PII grep, FileProvider, etc.; when Security focus run securitySimulations), one smoothness fix. Pulse check. |
| **2** | Test health & documentation | Test health (FAILING_OR_IGNORED_TESTS), doc cross-links, **sandboxing** (improve 1–2 ideas in HEAVY_IDEAS_FAVORITES/FUTURE_IDEAS). Run unit tests. Pulse check. |
| **3** | UI polish + smoothness | One string/accessibility fix; stat card/UI consistency per design research; one useful info. One subtle improvement only. Pulse check. |
| **4** | Final pulse & summary | Lint; final pulse; **write summary** (IMPROVEMENT_LOOP_SUMMARY_&lt;date&gt;.md); **append ledger block** (IMPROVEMENT_LOOP_RUN_LEDGER); **write shared state** (append to loop_shared_events.jsonl, update loop_latest/improvement.json). Optional: send to hub; optional LOOP_LESSONS_LEARNED bullets. |

**Outputs:** IMPROVEMENT_LOOP_SUMMARY_&lt;date&gt;.md, one block in IMPROVEMENT_LOOP_RUN_LEDGER.md, pulse_log, loop_latest/improvement.json, event in loop_shared_events.jsonl. Optional: Hub deposit.

**Docs:** [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md), [LOOP_TIERING.md](./LOOP_TIERING.md), [IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md](./IMPROVEMENT_LOOP_FOR_OTHER_AGENTS.md).

---

## Loop 3: Token Loop

| What | Detail |
|------|--------|
| **Who runs it** | Any agent when the user says **"start token loop"** or **"run token reduction loop"** or **"token audit"**. No human in the loop — runs autonomously. |
| **Trigger** | **start token loop** / **run token reduction loop** / **token audit**. |
| **Goal** | Reduce Cursor token usage; manage context; audit rules, doc references, conversation hygiene; recommend next TODOs. |

### Workflow (steps)

| Step | Action |
|------|--------|
| **Start** | Snapshot state: `token_loop_state_snapshot.ps1 -RunId <run_id>`. Listener: `token_loop_start`. |
| **0** | Deep research: token-saving practices, TOKEN_LOOP_IMPROVEMENT_PLAN, TOKEN_SAVING_PRACTICES; analyze snapshots and token_loop_events.jsonl; list .cursor/rules (alwaysApply, line counts). |
| **1** | Audit rules — count lines, alwaysApply, estimate token cost. |
| **2** | Check for new always-apply; consider converting to glob or description-only. |
| **3** | Doc references — no rule inlines large examples; use pointers to docs/. |
| **4** | Conversation reminder — fresh chat when gauge >60%; front-load context. |
| **5** | Update token docs; refresh TOKEN_SAVING_PRACTICES if new research. |
| **6** | One-line summary for Improvement Loop if run as part of it. |
| **7** | Organize results; write/update TOKEN_LOOP_NEXT_TASKS; append TOKEN_LOOP_RUN_LEDGER; update TOKEN_SAVING_PRACTICES §3. Progress report: **Loop #**, **proof of work**, **benefits**. |
| **End** | Listener: `token_loop_end`. Append to loop_shared_events.jsonl; update loop_latest/token.json. |

**Outputs:** Snapshot in token_loop_snapshots/&lt;run_id&gt;.json, token_loop_events.jsonl, TOKEN_LOOP_RUN_LEDGER, TOKEN_LOOP_NEXT_TASKS, progress report (Loop #, proof of work, benefits). Optional: Hub deposit.

**Docs:** [TOKEN_REDUCTION_LOOP.md](./TOKEN_REDUCTION_LOOP.md), [TOKEN_LOOP_LISTENER.md](./TOKEN_LOOP_LISTENER.md). Scripts: run_token_loop.ps1, token_loop_listener.ps1, token_loop_state_snapshot.ps1.

---

## Loop 4: Cyber Security Loop (Red / Blue / Purple)

| What | Detail |
|------|--------|
| **Who runs it** | **Red Team** agent (attack), **Blue Team** agent (detect/fix), or both in one **Purple** exercise. User or Lead sets scope. |
| **Trigger** | User says run Cyber Security Loop, or Purple Team exercise, or invokes script (e.g. run_cyber_security_loop.ps1). Security simulations: `./gradlew :app:securitySimulations`. |
| **Goal** | Red attacks a target; Blue checks if alarm went off; if not, Blue fixes; proof of work and training data for future runs. |

### Workflow (Purple protocol)

| Step | Who | Action |
|------|-----|--------|
| 1. Scope | User / Lead | Target (e.g. trip export), environment, off-limits. Red confirms scope. |
| 2. Red attacks | Red Team | One or more attack actions; document (target, action, result, Blue visibility, artifacts). |
| 3. Blue checks | Blue Team | For each Red action: Did our alarm go off? If yes, what detected it? If no, gap — document what should have detected it. |
| 4. Fix if missed | Blue Team | Propose or implement fix; record in proof-of-work log. Optionally Red re-tests. |
| 5. Save and reuse | Both | Append to proof-of-work log; save artifacts under docs/agents/data-sets/security-exercises/. At end: append to loop_shared_events.jsonl; update loop_latest/cyber.json. |

**Modes:** **Simulated** — `./gradlew :app:securitySimulations` (SecuritySimulationTest + Purple training JSON). **Agent-driven** — Red/Blue in chat; document in proof of work.

**Outputs:** Proof of work (security-team-proof-of-work.md or run log), validation_simulations, training data (e.g. purple-training.json), data summary. Deposit to Hub when polished. loop_latest/cyber.json.

**Docs:** [purple-team-protocol.md](../agents/purple-team-protocol.md), [security-team-proof-of-work.md](../agents/security-team-proof-of-work.md), Red/Blue agent cards in docs/agents/roles/.

---

## Loop 5: Synthetic Data Loop

| What | Detail |
|------|--------|
| **Who runs it** | Any agent when generating or curating synthetic/training data (e.g. for security, analytics, or testing). |
| **Trigger** | User asks for synthetic data generation or curation; or when aligning with UNIVERSAL_LOOP_PROMPT and Hub. |
| **Goal** | Create or curate training datasets and summaries; keep quality high; deposit to Hub when polished. |

### Workflow

- Research what data is needed (e.g. security scenarios, trip patterns).  
- Create or gather datasets; prune and mesh; quality report.  
- User approval if required; ledger append.  
- At end: append to loop_shared_events.jsonl; update loop_latest/synthetic_data.json. Consider **send to hub** with clear naming (YYYY-MM-DD_&lt;role&gt;_&lt;description&gt;.ext).  

**Outputs:** Training datasets, summaries; Hub deposit when polished; loop_latest/synthetic_data.json.

**Docs:** SYNTHETIC_DATA_LOOP_ROUTINE (if present); Hub index; [UNIVERSAL_LOOP_PROMPT.md](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md).

---

## Loop 6: File-organizer / data-sets (index)

| What | Detail |
|------|--------|
| **Who runs it** | **File Organizer** agent (or any agent) when user says **"organize data"** or when improving data-sets index. |
| **Trigger** | **organize data** / data-sets index task. |
| **Goal** | Index of where data lives (hub, role data sets, aptitude, security, board-meeting); organization; deposit to Hub when polished. |

### Workflow

- Review docs/agents/data-sets/, hub/, security-exercises/, etc.  
- Update or create index (e.g. data-sets README, file-organizer index).  
- Deposit to Hub with YYYY-MM-DD_file-organizer_&lt;description&gt;.md; add line to hub/README.md.  
- At end: append to loop_shared_events.jsonl if this loop has a loop_latest file; otherwise optional.  

**Outputs:** Index docs, Hub deposit (file-organizer index, data-organized index).

**Docs:** [data-sets/README.md](../agents/data-sets/README.md), [FILE_STRUCTURE.md](../agents/FILE_STRUCTURE.md).

---

## Send to hub (not a loop — command)

| What | Detail |
|------|--------|
| **Who** | Any agent when the user says **"send to hub"** or when the agent finishes polished output. |
| **Trigger** | User says **"send to hub"** or agent decides output is completed and polished. |
| **Action** | Save output to **docs/agents/data-sets/hub/** with naming **YYYY-MM-DD_&lt;role-or-topic&gt;_&lt;short-description&gt;.&lt;ext&gt;**; optionally add one line to **hub/README.md** index. Hub = this folder; not GitHub. |

**Doc:** [SEND_TO_HUB_PROMPT.md](../agents/data-sets/hub/SEND_TO_HUB_PROMPT.md).

---

## Summary table: which agent runs which loop

| Loop | Trigger | Typical agent | Human in the loop? |
|------|---------|----------------|--------------------|
| **Master** | start master loop | Any (becomes Loop Master) | Yes (user triggers; then Improvement may prompt for Heavy) |
| **Improvement** | GO / start improvement loop | Any (general, Improvement Loop) | Yes for Heavy tasks; otherwise autonomous (Light/Medium) |
| **Token** | start token loop / token audit | Any | No — autonomous |
| **Cyber Security** | Purple / Cyber Security Loop | Red + Blue (separate or together) | Yes (scope, review) |
| **Synthetic Data** | Generate/curate synthetic data | Any | Depends on routine |
| **File-organizer** | organize data | File Organizer | Optional |
| **Send to hub** | "send to hub" or polished output | Any | No — agent deposits |

---

## How workflows connect

- **Loop Master** runs first when user says "start master loop"; it does Step 0.M (research all loops, Hub, universal files) then runs the **Improvement Loop**.  
- **Improvement Loop** can optionally run a short **Token** audit in Phase 0.6; when Security focus it runs **securitySimulations** (Cyber).  
- **All loops** read **Hub** at start and **shared state** (loop_shared_events.jsonl, loop_latest/) so they can react to other loops.  
- **All loops** write **shared state** at end (event + loop_latest/&lt;loop&gt;.json) so the next run of any loop sees their output.  
- **Polished output** from any loop can be sent to **Hub** (send to hub) for other agents to use.

---

*Keep this doc in sync with LOOPS_AND_IMPROVEMENT_FULL_AXIS.md and LOOP_MASTER_ROLE.md when adding or changing loops.*
