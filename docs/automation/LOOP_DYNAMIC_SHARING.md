# Loop dynamic sharing — shared state across loops

**Purpose:** When multiple loops run (in sequence or in parallel), they share state via **`loop_shared_events.jsonl`** and **`loop_latest/<loop>.json`**. Every loop must **read** at start and **write** at end so data stays dynamic and other agents can react.

**Gates:** [LOOP_GATES.md](./LOOP_GATES.md) — at start read these files; at end append event and update latest.

---

## loop_shared_events.jsonl

**Path:** `docs/automation/loop_shared_events.jsonl`  
**Format:** One JSON object per line (JSONL). No blank lines between objects.

**At loop start:** Read the **tail** (last 50 lines, or full file if smaller) to see recent finished events from other loops.

**At loop end:** Append **one** finished event. Suggested fields:

| Field | Type | Description |
|-------|------|-------------|
| `ts` | string | ISO8601 timestamp (e.g. 2026-03-13T18:00:00Z). |
| `loop` | string | Loop name (e.g. `token`, `improvement`, `cyber`, `synthetic_data`, `file_organizer`). |
| `event` | string | `finished`. |
| `run_id` | string | Run identifier (e.g. token-20260313-1843). |
| `summary_path` | string | Path to this run’s summary or main artifact (relative to repo root or full path). |
| `next_steps` | array | Optional array of strings: suggested next steps for this or other loops. |
| `checkpoint` | string | Optional: short checkpoint or status. |

**Example line:**

```json
{"ts":"2026-03-13T18:45:00Z","loop":"token","event":"finished","run_id":"token-20260313-1843","summary_path":"docs/automation/TOKEN_LOOP_RUN_LEDGER.md","next_steps":["Convert data-separation to glob","Trim always-apply to <50 lines"]}
```

**Recommended writer (dedupe-safe):**

```powershell
.\scripts\automation\write_loop_shared_state.ps1 `
  -Loop token `
  -RunId token-20260313-1843 `
  -SummaryPath docs/automation/TOKEN_LOOP_RUN_LEDGER.md `
  -NextSteps @("Convert data-separation to glob","Trim always-apply to <50 lines>")
```

The writer blocks duplicate `finished` events for the same `loop|run_id` key while still refreshing `loop_latest/<loop>.json`.

---

## loop_latest/&lt;your_loop&gt;.json

**Path:** `docs/automation/loop_latest/<your_loop>.json`  
**Naming:** Use the loop name: `token.json`, `improvement.json`, `cyber.json`, `synthetic_data.json`, `file_organizer.json`.

**At loop start:** Read **all** `loop_latest/*.json` files (for **other** loops) to see their latest summary path and suggested next steps.

**At loop end:** **Overwrite** `loop_latest/<your_loop>.json` with your loop’s latest output. Suggested fields:

| Field | Type | Description |
|-------|------|-------------|
| `last_run_ts` | string | ISO8601 timestamp of this run. |
| `run_id` | string | Run identifier. |
| `summary_path` | string | Path to this run’s summary or main artifact. |
| `suggested_next_steps` | array | Optional array of strings. |
| `checkpoint` | string | Optional. |

**Example (token.json):**

```json
{
  "last_run_ts": "2026-03-13T18:45:00Z",
  "run_id": "token-20260313-1843",
  "summary_path": "docs/automation/TOKEN_LOOP_RUN_LEDGER.md",
  "suggested_next_steps": ["Convert data-separation.mdc to glob", "Trim always-apply to <50 lines"]
}
```

---

## Who writes what

| Loop | loop_latest file |
|------|-------------------|
| Token | `loop_latest/token.json` |
| Improvement (2-hour) | `loop_latest/improvement.json` |
| Cyber Security | `loop_latest/cyber.json` |
| Synthetic Data | `loop_latest/synthetic_data.json` |
| File-organizer | `loop_latest/file_organizer.json` |

Add a new file when a new loop is introduced; list it in [LOOPS_AND_IMPROVEMENT_FULL_AXIS.md](./LOOPS_AND_IMPROVEMENT_FULL_AXIS.md).

---

*Read at loop start (tail + latest); write at loop end (append event + overwrite your loop’s latest). See LOOP_GATES.md.*
