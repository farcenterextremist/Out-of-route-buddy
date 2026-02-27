# DevOps Engineer — Semi Simple

- **Role card:** `docs/agents/roles/devops-engineer.md`
- **Prompt type:** Semi Simple

## Prompt (copy-paste)

```
We want to run unit tests in CI on every push. What would you use (e.g. Gradle task, script) and where is the config?
```

## Look for (scoring)

Gradle/scripts/CI paths; no test case design; handoff to QA for strategy.

---

## Response

**Use:** Gradle task `test` (or `testDebugUnitTest`) on every push. **Config:** Add a CI job (e.g. GitHub Actions, Jenkins) that runs `./gradlew test` from the repo root; config lives in `.github/workflows/` (if GitHub Actions) or the CI server’s pipeline config. The project’s Gradle files are `build.gradle.kts` (root and app). No test case design; I’d hand off test strategy and coverage expectations to the **QA Engineer**.

