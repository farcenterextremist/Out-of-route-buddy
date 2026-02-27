# Front-end Engineer — Semi Simple

- **Role card:** `docs/agents/roles/frontend-engineer.md`
- **Prompt type:** Semi Simple

## Prompt (copy-paste)

```
We want to add a 'Export to PDF' button on the trip history screen. What file(s) would you change and what would you need from the Back-end or ViewModel?
```

## Look for (scoring)

Concrete paths (res/layout, presentation/); no repository or service logic; handoff to Back-end for data.

---

## Response

**Files to change:** Trip history screen is in `app/.../presentation/ui/history/` — I’d add the button to the history fragment’s layout (e.g. `fragment_trip_history.xml` or the toolbar/row that holds actions) and in `TripHistoryFragment.kt` wire the click to the ViewModel. **ViewModel:** `TripHistoryViewModel` already has `exportToPDF()` (or export flow); I need a way to trigger it (e.g. `viewModel.exportToPDF()`). If PDF generation or data shape isn’t there yet, I’d ask **Back-end Engineer** for the export API or a method that returns a file/URI for the current trip list. I don’t implement repository or service logic.

