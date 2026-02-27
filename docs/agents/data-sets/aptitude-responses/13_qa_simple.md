# QA Engineer — Simple

- **Role card:** `docs/agents/roles/qa-engineer.md`
- **Prompt type:** Simple

## Prompt (copy-paste)

```
Where are unit tests for the app located (directory path)? Name one test file.
```

## Look for (scoring)

app/src/test/ or androidTest/; test strategy not implementation; handoff to Front-end/Back-end for bugs.

---

## Response

Unit tests are in **`app/src/test/java/com/example/outofroutebuddy/`**. One test file: **`TripHistoryViewModelTest.kt`** (under `presentation/`). I don’t implement the fixes; I’d hand off failures to **Front-end** or **Back-end** depending on whether the bug is in UI/ViewModel or in repository/data.

