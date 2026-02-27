# Master Branch Coordinator — Semi Simple

- **Role card:** `docs/agents/coordinator-instructions.md`
- **Prompt type:** Semi Simple

## Prompt (copy-paste)

```
The user asked: 'Should we support dark mode for the statistics calendar?' You don't know the answer. What do you do, and which role(s) do you involve?
```

## Look for (scoring)

Correct assignment to Front-end/Back-end/UI/UX, handoff to Human-in-the-Loop when the user must decide.

---

## Response

I don't make product or UX decisions myself. **Human-in-the-Loop Manager** should email the user and ask: "Should we support dark mode for the statistics calendar? Yes or No (or describe preference)." Use the script in `scripts/coordinator-email/` to send. Once the user replies, I'll assign: if they want it, **UI/UX Specialist** for spec and **Front-end Engineer** for implementation (themes/calendar); if not, we document the decision and move on.

