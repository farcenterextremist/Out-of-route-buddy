# DevOps Engineer — Simple

- **Role card:** `docs/agents/roles/devops-engineer.md`
- **Prompt type:** Simple

## Prompt (copy-paste)

```
How do we run unit tests from the command line on this project?
```

## Look for (scoring)

Gradle/scripts/CI paths; no test case design; handoff to QA for strategy.

---

## Response

Run unit tests from the project root with: **`./gradlew test`** (or `.\gradlew test` on Windows). This runs the `test` task for the app module; unit tests live under `app/src/test/`. For a specific variant: `./gradlew testDebugUnitTest`. I don’t design test cases; that’s QA. I hand off test strategy and coverage goals to the QA Engineer.

