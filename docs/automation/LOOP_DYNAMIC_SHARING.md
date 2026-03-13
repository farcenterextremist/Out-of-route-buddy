# Loop dynamic sharing — convention and wiring

**Purpose:** When you run **any loop** (especially when multiple loops may run at the same time), read and write **shared state** so data is shared dynamically and other agents can react/act on the latest outputs. This ensures 100% that concurrent or sequential loop runs see each other's data.

**Research:** [LOOP_DYNAMIC_SHARING_RESEARCH.md](./LOOP_DYNAMIC_SHARING_RESEARCH.md) — problem, design, event shape.

---

## 1. Shared state locations

| What | Path | Who writes | Mode |
|------|------|------------|------|
| **Shared event log** | `docs/automation/loop_shared_events.jsonl` | Every loop | Append one JSON line per event |
| **Per-loop latest** | `docs/automation/loop_latest/<loop>.json` | Each loop writes only its own file | Overwrite (one writer per file) |
| **Loop latest folder** | `docs/automation/loop_latest/` | — | Contains improvement.json, token.json, cyber.json, synthetic_data.json |

---

## 2. At loop start (read shared state)

**Every loop must**, at the beginning of its run (e.g. Phase 0 or Step 0):

1. **Read the tail of `loop_shared_events.jsonl`**  
   - Read the last **50 lines** (or all events from the last 24 hours if timestamps are available).  
   - From this, note: which other loops have run recently, their last `summary_path`, `next_steps`, `checkpoint`, and any `key_decisions`.  
   - In your research or first step output, note: **Shared state (other loops):** [e.g. "Improvement last finished 2026-03-12; summary at …; next_steps: …; Token last run …"].

2. **Read all other loops' latest files** in `loop_latest/`  
   - If you are Improvement Loop, read `token.json`, `cyber.json`, `synthetic_data.json` (skip `improvement.json`).  
   - If you are Token Loop, read `improvement.json`, `cyber.json`, `synthetic_data.json`.  
   - Same for Cyber and Synthetic Data.  
   - Use this to **react**: e.g. if Improvement's `suggested_next_steps` include "Security PII grep" and you are Cyber Loop, you can prioritize that; if Token's `key_decisions` mention rule count, Improvement can avoid changing rules in the same run.  
   - In your research output, note: **Other loops' latest:** [one line per other loop: summary path and/or next_steps you will use].

**If a file is missing** (e.g. first run): treat as "no previous run"; continue.

---

## 3. At loop end (write shared state)

**Every loop must**, at the end of its run (e.g. after summary, before or with ledger append):

1. **Append to `loop_shared_events.jsonl`**  
   - One line (single JSON object) per event. At minimum append a **finished** event. Optionally append **started** at run start.  
   - **Required fields:** `ts` (ISO8601), `loop` (e.g. `"improvement"` | `"token"` | `"cyber"` | `"synthetic_data"`), `event` (e.g. `"started"` | `"finished"`), `run_id`.  
   - **When event is "finished":** include `summary_path`, `next_steps` (array of strings), and optionally `checkpoint`, `key_decisions`.  
   - Example line (single line, no newlines inside):  
     `{"ts":"2026-03-12T14:00:00Z","loop":"improvement","event":"finished","run_id":"2026-03-12","summary_path":"docs/automation/IMPROVEMENT_LOOP_SUMMARY_2026-03-12.md","next_steps":["Security PII grep","Next focus UI/UX"],"checkpoint":"c1a65e6"}`

2. **Update your loop's latest file** in `loop_latest/<loop>.json`  
   - **Improvement Loop** → `loop_latest/improvement.json`  
   - **Token Loop** → `loop_latest/token.json`  
   - **Cyber Security Loop** → `loop_latest/cyber.json`  
   - **Synthetic Data Loop** → `loop_latest/synthetic_data.json`  
   - Content: JSON object with at least `loop`, `last_run_ts`, `last_run_id`, `summary_path`, `suggested_next_steps`, and optionally `checkpoint`, `key_decisions`.  
   - Overwrite the file (you are the only writer for this file).

---

## 4. Event and latest-file shapes

**loop_shared_events.jsonl (one JSON object per line):**

- `ts` (string, ISO8601)  
- `loop` (string: improvement | token | cyber | synthetic_data)  
- `event` (string: started | finished | phase_done)  
- `run_id` (string)  
- If `event === "finished"`: `summary_path`, `next_steps` (array), optional `checkpoint`, `key_decisions`

**loop_latest/<loop>.json (single JSON object):**

- `loop` (string)  
- `last_run_ts` (string, ISO8601)  
- `last_run_id` (string)  
- `summary_path` (string)  
- `suggested_next_steps` (array of strings)  
- Optional: `checkpoint`, `key_decisions` (array of strings)

---

## 5. Wiring into routines

- **UNIVERSAL_LOOP_PROMPT:** Added obligation: when running any loop, at start read shared state (events tail + other loops' latest); at end write shared state (append event + update own latest). See [UNIVERSAL_LOOP_PROMPT.md](../agents/data-sets/hub/UNIVERSAL_LOOP_PROMPT.md).
- **IMPROVEMENT_LOOP_ROUTINE:** Phase 0 research includes "Read loop_shared_events.jsonl (tail) and loop_latest/*.json for other loops; note in research output." Phase 4.3 (before or with ledger) includes "Append finished event to loop_shared_events.jsonl; update loop_latest/improvement.json."
- **Token / Cyber / Synthetic Data:** Same pattern in their Step 0 and final step: read shared state at start; write shared state at end. Each loop owns its own `loop_latest/<loop>.json`.

---

## 6. Guarantee when running all loops at the same time

- **Append-only events:** Multiple loops can append to `loop_shared_events.jsonl` concurrently without overwriting each other.  
- **Per-loop latest:** Each loop writes only its own file in `loop_latest/`, so no write conflicts.  
- **Read at start:** Every loop reads the latest from others (events tail + other latest files), so agents always see the most recent data available at the moment they start and can react/act accordingly.  
- **Write at end:** Every loop publishes its own output (event + latest file), so the next run (of any loop) will see it.  

This gives **dynamic sharing** and ensures data is shared so other agents can react/act appropriately.

---

*Integrates with LOOP_DYNAMIC_SHARING_RESEARCH.md, UNIVERSAL_LOOP_PROMPT, IMPROVEMENT_LOOP_ROUTINE, and per-loop routines.*
