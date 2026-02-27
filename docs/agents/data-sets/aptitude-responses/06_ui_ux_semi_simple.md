# UI/UX Specialist — Semi Simple

- **Role card:** `docs/agents/roles/ui-ux-specialist.md`
- **Prompt type:** Semi Simple

## Prompt (copy-paste)

```
The statistics section feels cramped. Suggest two improvements (layout or copy) and say what you'd hand to the Front-end Engineer.
```

## Look for (scoring)

References to layouts/strings/docs/ux; no Kotlin; handoff to Front-end.

---

## Response

**Two improvements:**  
1. **Layout:** Add more vertical spacing between the statistics summary row and the expandable block (e.g. increase margin/padding in `statistics_row.xml` or the parent so the section doesn’t feel cramped).  
2. **Copy:** Shorten the "Monthly Statistics" label or add a one-line hint under it (e.g. in `strings.xml`) so the purpose is clear without crowding.  

Hand off to **Front-end Engineer** with these notes; they own `app/src/main/res/layout/statistics_row.xml` and `res/values/strings.xml`. I don’t write Kotlin or XML.

