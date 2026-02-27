# Jarvey Prompt Question Bank

Long word list in prompt form for future planning: questions and phrases similar to "What's next?" that trigger intent-based context or serve as test prompts.

---

## Template-Covered Questions (Instant Reply, No LLM)

These get preset/data-driven replies immediately:

| Template | Example questions |
|----------|-------------------|
| roadmap | What's next? What's on the roadmap? Next steps? |
| recent | What changed recently? Timeline? What's new? Last commit? |
| version | What's the latest version? Build number? |
| capabilities | What can you do? Help? What are you? |
| thanks | Thanks, got it, sounds good |
| priority | Prioritize X, lock in order |
| weekly_digest | Weekly board digest |
| unclear | Very short message with no match |

---

## Roadmap / Priorities (template: roadmap)

```
What's next?
What's next on the list?
What are we doing next?
What are the priorities?
What's on the roadmap?
Show me the roadmap
What's the plan?
What should we focus on?
What's coming up?
What's in the pipeline?
What's the order of work?
What are we prioritizing?
What's next for the app?
What features are next?
What's the priority order?
```

---

## Recent / Timeline (template: recent)

```
What changed recently?
What's new?
What's new in the project?
Show me recent changes
What did we do last?
Last commit?
Project history
Timeline
Recent commits
What's the project timeline?
What happened lately?
```

---

## Version / Build (template: version)

```
What's the latest app version?
What version are we on?
What's the build number?
Latest version?
Current version?
```

---

## Recovery / Crash (existing: recovery intent)

```
How does trip recovery work?
The app crashed and I lost my trip
What happens when the app crashes?
Restart recovery
Lost my trip data
```

---

## Delegation / Ownership (existing: delegation intent)

```
Who owns the emulator?
Who owns the reports screen?
Who's responsible for X?
Assign this to...
Who should I ask about...?
```

---

## Emulator (existing: emulator intent)

```
How does the emulator work?
Phone-emulator sync
Emulator sync issues
```

---

## Reports / Export (existing: reports intent)

```
When will the reports screen be done?
Reports feature status
Export trips
Share trip data
```

---

## Tests / QA (existing: tests intent)

```
What's the test status?
QA status
Failing tests
Test strategy
```

---

## Architecture / Wiring (existing: architecture intent)

```
How does it work?
Architecture overview
Wiring map
Components
```

---

## Code Location (existing: project index)

```
Where is TripInputViewModel defined?
Where is X defined?
Find file for...
```

---

## Improvements / Todo (existing: improvements intent)

```
What's on the crucial improvements list?
Crucial TODO
Improvements needed
```

---

## Security (existing: security intent)

```
Security plan
Threat model
```

---

## Workdays / Sprint (existing: workdays intent)

```
When do we work?
Sprint schedule
Workdays
```

---

## Multi-Question / Combined (for future scenarios)

```
What's next? Also, who owns the emulator?
What's the version and what changed recently?
Prioritize reports and when will it be done?
```

---

## Short / Check-in (no intent, HITL handling)

```
test
testing
hi
ok
hello
```

---

## Unclear / Clarification (no intent, HITL handling)

```
Something is broken
I have a problem
```

---

## No-Code (delegation, not implementation)

```
Write me a function to export trips to CSV
Can you implement X?
```

---

## Usage

- **Template triggers:** Add to `check_and_respond.py` choose_response for new template patterns (recent, roadmap, version, capabilities).
- **Intent expansion:** Add phrases to `context_loader.py` INTENT_CONFIG keywords.
- **Test prompts:** Use with `compose_reply.py "Re: OutOfRouteBuddy" "<body>"` for scenario runs.
- **Benchmark:** Add to `run_jarvey_benchmark.py` SCENARIOS for regression coverage.
- **Template triggers:** Add to `check_and_respond.py` choose_response for new template patterns.
