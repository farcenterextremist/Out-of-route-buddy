# loop_latest — latest output per loop

Each loop writes **one** JSON file here at **loop end** (see [LOOP_GATES.md](../LOOP_GATES.md), [LOOP_DYNAMIC_SHARING.md](../LOOP_DYNAMIC_SHARING.md)).

**Convention:** `docs/automation/loop_latest/<loop_name>.json`  
Examples: `token.json`, `improvement.json`, `cyber.json`, `synthetic_data.json`, `file_organizer.json`.

**At loop start:** Read all `*.json` in this directory (other loops’ latest).  
**At loop end:** Overwrite your loop’s file with `last_run_ts`, `summary_path`, `suggested_next_steps`, etc.
