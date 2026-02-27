# QA Engineer — Semi Simple

- **Role card:** `docs/agents/roles/qa-engineer.md`
- **Prompt type:** Semi Simple

## Prompt (copy-paste)

```
We added a new 'Export to PDF' button. What would you test (list 2–3 test ideas) and would you hand off anything to another role?
```

## Look for (scoring)

app/src/test/ or androidTest/; test strategy not implementation; handoff to Front-end/Back-end for bugs.

---

## Response

**Test ideas:** (1) **Unit:** ViewModel/export flow—when "Export to PDF" is tapped, the correct method is called and the export file/URI is produced or error is handled. (2) **UI:** Button is visible and enabled when there are trips; disabled or hidden when empty (if that’s the spec). (3) **Integration:** Export actually produces a valid PDF (or we stub the generator and assert the stub was called with the right data). **Handoff:** If the button doesn’t appear or the flow is wrong → Front-end; if PDF generation or data is wrong → Back-end. I don’t write the implementation.

