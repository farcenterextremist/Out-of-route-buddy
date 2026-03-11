# Improvement Loop — Listener

**Purpose:** Record loop events to structured data (JSONL) so we can improve the Improvement Loop over time. Events are appended to `docs/automation/loop_events.jsonl`.

**Script:** `scripts/automation/loop_listener.ps1`  
**Test:** `scripts/automation/test_loop_listener.ps1`  
**Output:** `docs/automation/loop_events.jsonl` (one JSON object per line)

---

## Events

| Event | When | Typical use |
|-------|------|-------------|
| `loop_start` | Start of Improvement Loop | run_120min_loop invokes |
| `phase_start` | Start of Phase 0, 1, 2, 3, or 4 | Agent or script invokes |
| `phase_end` | End of phase | Agent or script invokes |
| `pulse` | Each pulse (tests, lint) | pulse_check invokes |
| `metrics` | Metrics snapshot | Agent or script invokes |
| `loop_end` | End of Improvement Loop | run_120min_loop invokes |

---

## Usage

```powershell
# From repo root
.\scripts\automation\loop_listener.ps1 -Event loop_start -Note "Full 2hr run"
.\scripts\automation\loop_listener.ps1 -Event phase_start -Phase "1" -Note "Quick wins"
.\scripts\automation\loop_listener.ps1 -Event phase_end -Phase "1" -Metrics '{"tasks":3}'
.\scripts\automation\loop_listener.ps1 -Event pulse -Note "Phase 2" -Metrics '{"tests":"1021 passed"}'
.\scripts\automation\loop_listener.ps1 -Event loop_end -Note "Pulses=4"
```

**Test (simulation):**
```powershell
.\scripts\automation\test_loop_listener.ps1
.\scripts\automation\test_loop_listener.ps1 -KeepOutput  # Keep test output file
```

---

## Wiring

| Component | Invokes listener |
|-----------|------------------|
| **run_120min_loop.ps1** | loop_start at start; loop_end at end |
| **pulse_check.ps1** | pulse on each run (with tests result) |
| **Agent (Phase boundaries)** | phase_start, phase_end when following IMPROVEMENT_LOOP_ROUTINE |

---

## JSONL Format

Each line is a JSON object:

```json
{"ts":"2025-03-11T17:00:00Z","event":"pulse","run_id":"run-20250311-1700","note":"Phase 1","metrics":{"tests":"1021 passed"}}
```

---

## Verification

Run the test to ensure listener is wired and functioning:

```powershell
.\scripts\automation\test_loop_listener.ps1
```

Exit 0 = all tests passed. Main entry points (pulse_check, run_120min_loop) invoke the listener; test simulates events and validates output.

---

*Integrates with IMPROVEMENT_LOOP_ROUTINE. Use loop_events.jsonl for analysis and loop improvement.*
