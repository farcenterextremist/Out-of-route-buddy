# Jarvey scenario 6 — Unclear request

- **Prompt type:** Unclear (email reply)
- **Instructions/context:** Same as listener: `docs/agents/coordinator-instructions.md` + `docs/agents/coordinator-project-context.md`

## Prompt (user email)

**Subject:** Re: OutOfRouteBuddy

**Body:**
```
Something is broken.
```

## Look for (scoring)

- Asks for clarification or gives a safe default (e.g. "we'll follow up"; "can you share which screen or flow?").
- Does not invent details or guess.
- Signs as Jarvey.
- Output: appropriate for vague input; Handoff: may offer Human-in-the-Loop or next step.

---

## Response

```
I got your message but couldn't make out what you need. Can you reply with a short question or request?

— Jarvey
```

---

## Score (optional)

| Scope | Data set | Output | Handoff | Voice | Overall |
|-------|----------|--------|---------|-------|---------|
| 5     | 4        | 5      | 5       | 5     | 4.8     |

## Fix notes (optional)

(If low score: what was changed in instructions or context and when.)
