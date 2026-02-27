# Master Branch Coordinator — Simple

- **Role card:** `docs/agents/coordinator-instructions.md`
- **Prompt type:** Simple

## Prompt (copy-paste)

```
We need to add a new 'Export trip to PDF' button on the trip history screen. Who should do what? Assign work.
```

## Look for (scoring)

Correct assignment to Front-end/Back-end/UI/UX, handoff to Human-in-the-Loop when the user must decide.

---

## Response

**Assignment:**  
- **Front-end Engineer** (see `docs/agents/roles/frontend-engineer.md`): Add the "Export trip to PDF" button to the trip history screen layout and wire it to the ViewModel.  
- **Back-end Engineer** (see `docs/agents/roles/backend-engineer.md`): Add or expose any repository/export logic needed to generate PDF from trip data (e.g. in `TripExporter` or a new PDF path).  
- **UI/UX Specialist** (see `docs/agents/roles/ui-ux-specialist.md`): Confirm placement and label for the button in the trip history flow.  

Order: UI/UX can confirm first; Front-end and Back-end can work in parallel once the contract (e.g. "export PDF for current list") is clear. QA for test coverage after implementation.

