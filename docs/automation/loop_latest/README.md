# Loop latest outputs

**Purpose:** One JSON file per loop with that loop’s **latest run output** (summary path, suggested next steps, checkpoint). Each loop writes only its own file so there are no write conflicts when multiple loops run at the same time.

**Usage:** At **loop start**, every loop reads the **other** loops’ files here (e.g. Improvement reads token.json, cyber.json, synthetic_data.json) to react/act on the latest from others. At **loop end**, every loop overwrites its own file (e.g. Improvement writes improvement.json).

**Convention:** See [LOOP_DYNAMIC_SHARING.md](../LOOP_DYNAMIC_SHARING.md). Files: `improvement.json`, `token.json`, `cyber.json`, `synthetic_data.json`. Shape: `loop`, `last_run_ts`, `last_run_id`, `summary_path`, `suggested_next_steps`, optional `checkpoint`, `key_decisions`.
