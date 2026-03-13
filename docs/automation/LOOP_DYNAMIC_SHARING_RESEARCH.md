# Loop dynamic sharing — research and design

**Purpose:** When multiple loops run at the same time (or in parallel), ensure data is shared dynamically so every agent can read the latest from other loops and react/act appropriately. This doc summarizes current behavior, the problem, and the chosen design.

**Status:** Research complete; design and wiring in [LOOP_DYNAMIC_SHARING.md](./LOOP_DYNAMIC_SHARING.md).

---

## 1. Current shared files (who writes what)

| File(s) | Written by | Mode | When |
|---------|------------|------|------|
| **IMPROVEMENT_LOOP_RUN_LEDGER.md** | Improvement Loop | Append block | Phase 4.3 |
| **IMPROVEMENT_LOOP_SUMMARY_&lt;date&gt;.md** | Improvement Loop | Overwrite per date | Phase 4.3 |
| **TOKEN_LOOP_RUN_LEDGER.md** | Token Loop | Append block | Step 7 |
| **TOKEN_LOOP_NEXT_TASKS.md** | Token Loop | Append section or overwrite | Step 7 |
| **token_loop_events.jsonl** | Token Loop (listener) | Append line per event | Steps 0–7 |
| **token_loop_snapshots/&lt;run_id&gt;.json** | Token Loop | One file per run | Start |
| **CYBER_SECURITY_LOOP_RUN_LEDGER.md** | Cyber Security Loop | Append block | Phase 3 |
| **Hub (hub/*.md, hub/README index)** | Any loop / role | New file + append row to README | When "send to hub" or deposit |
| **LOOP_LESSONS_LEARNED.md** | Improvement Loop (optional) | Append bullets | Phase 4.3 |
| **CRUCIAL_IMPROVEMENTS_TODO.md**, **TASKS_INDEX**, etc. | Any loop | Read + possibly edit | Phase 0, during run |

---

## 2. Problem when running all loops at the same time

- **Stale reads:** Loop B starts and reads Hub/ledgers; Loop A is still running and writes later. Loop B never sees A’s output.
- **Overwrites:** If two loops write the same file (e.g. same-date summary, or NEXT_TASKS), one can overwrite the other.
- **Hub index:** Two agents read hub/README, each appends a row; both read the same “current” table and write back — possible duplicates or lost rows if not careful.
- **No “what just happened”:** There is no single place that says “Improvement Loop just finished; summary at X; suggested next steps Y” so Token or Cyber can react (e.g. pull Y into their context).
- **Append-only ledgers** are safe for concurrent appends (each loop appends its own block), but readers may see an incomplete picture if they read mid-run.

---

## 3. Design: shared state so agents can react

Two mechanisms:

### 3.1 Append-only shared event log

**File:** `docs/automation/loop_shared_events.jsonl`

- **One line per event** (JSON object). Each loop **appends** when it starts, when it finishes a phase (optional), and when it finishes.
- **No overwrites** — safe when multiple loops run at once.
- **At loop start:** Read the **tail** of this file (e.g. last 50 lines, or all events since last 24h) to see what other loops did (e.g. “Improvement Loop finished at 14:00; summary at …; next_steps: […]”).

**Event shape (example):**

```json
{"ts":"2026-03-12T14:00:00Z","loop":"improvement","event":"finished","run_id":"2026-03-12","summary_path":"docs/automation/IMPROVEMENT_LOOP_SUMMARY_2026-03-12.md","next_steps":["Security PII grep","Next focus UI/UX"],"checkpoint":"c1a65e6"}
```

```json
{"ts":"2026-03-12T14:05:00Z","loop":"token","event":"started","run_id":"token-20260312-1405"}
```

### 3.2 Per-loop “latest” output (no conflicts)

**Folder:** `docs/automation/loop_latest/`

- **One file per loop:** `improvement.json`, `token.json`, `cyber.json`, `synthetic_data.json`.
- Each loop **writes only its own file** when it finishes (or at phase boundaries if desired). No two loops write the same file → no overwrite conflicts.
- **At loop start:** Read **all other** `loop_latest/*.json` files to get “latest summary path, next steps, key decisions” from each other loop and react (e.g. use Improvement’s suggested next steps as input to Token or Cyber).

**Shape (example) improvement.json:**

```json
{
  "loop": "improvement",
  "last_run_ts": "2026-03-12T14:00:00Z",
  "last_run_id": "2026-03-12",
  "summary_path": "docs/automation/IMPROVEMENT_LOOP_SUMMARY_2026-03-12.md",
  "suggested_next_steps": ["Security PII grep", "Next focus UI/UX"],
  "checkpoint": "c1a65e6",
  "key_decisions": ["Light+Medium only; Heavy deferred"]
}
```

---

## 4. How this makes sharing “dynamic”

- **Before:** Loops only shared via static docs (ledgers, Hub index). When running concurrently, agents could read stale data or overwrite each other.
- **After:**  
  - **At start:** Every loop reads `loop_shared_events.jsonl` (tail) and `loop_latest/<other>.json` for every other loop. So each agent sees “what the others last produced” and can react (e.g. “Improvement suggested X; I’ll include X in my run”).  
  - **At end (and optionally at phase boundaries):** Every loop appends to `loop_shared_events.jsonl` and updates `loop_latest/<own_loop>.json`. So the next loop (or same loop in a future run) sees up-to-date data.

This gives **dynamic sharing** without requiring a central coordinator: append-only events + per-loop latest files avoid races and keep data current for reaction/action.

---

## 5. References

- [LOOP_DYNAMIC_SHARING.md](./LOOP_DYNAMIC_SHARING.md) — Convention and wiring (read at start, write at end).
- [LOOPS_AND_IMPROVEMENT_FULL_AXIS.md](./LOOPS_AND_IMPROVEMENT_FULL_AXIS.md) — All loops and outputs.
- [UNIVERSAL_LOOP_PROMPT.md](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md) — Updated to require read/write shared state when running with other loops.
