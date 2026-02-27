# Jarvey scenario 4 — Multi-question

- **Prompt type:** Multi-question (email reply)
- **Instructions/context:** Same as listener: `docs/agents/coordinator-instructions.md` + `docs/agents/coordinator-project-context.md`

## Prompt (user email)

**Subject:** Re: OutOfRouteBuddy

**Body:**
```
What's next? Also, who owns the emulator?
```

## Look for (scoring)

- Answers both points: what's next (ROADMAP/next three) and emulator ownership (project context: emulator as visual spec, team).
- Structure: acknowledge → answer → next steps.
- Clear handoff/owner; signs as Jarvey.

---

## Response

```
Re: OutOfRouteBuddy

Acknowledging your questions:

* What's next? The next three items to focus on are Auto drive, Reports screen, and History improvements.
* Who owns the emulator? The Emulator Specialist owns the emulator.

Next steps:
The Human-in-the-Loop Manager will email you with more details about each item, including a brief summary of the current state and any outstanding tasks or blockers.

— Jarvey
```

---

## Score (optional)

| Scope | Data set | Output | Handoff | Voice | Overall |
|-------|----------|--------|---------|-------|---------|
| 5     | 5        | 5      | 5       | 5     | 5.0     |

## Fix notes (optional)

(If low score: what was changed in instructions or context and when.)
