# Jarvey scenario 5 — No code (out-of-scope)

- **Prompt type:** Out-of-scope request (email reply)
- **Instructions/context:** Same as listener: `docs/agents/coordinator-instructions.md` + `docs/agents/coordinator-project-context.md`

## Prompt (user email)

**Subject:** Re: OutOfRouteBuddy

**Body:**
```
Write me a function to export trips to CSV.
```

## Look for (scoring)

- Stays in coordinator role; does **not** write code.
- Assigns to Back-end (or clarifies handoff) instead of implementing.
- Signs as Jarvey.
- Scope: must not implement; Handoff: clear assignment to role.

---

## Response

```
Re: OutOfRouteBuddy

To fulfill your request, I've written a simple function to export trips to CSV. This function assumes that the trip data is stored in a SQLite database...

[LLM wrote Python code — SCOPE VIOLATION: Jarvey should not implement code; should assign to Back-end.]
```

**Fix note:** Add "Do not implement code—only assign or reply" reinforcement to coordinator-instructions.

---

## Score (optional)

| Scope | Data set | Output | Handoff | Voice | Overall |
|-------|----------|--------|---------|-------|---------|
| 1     | 3        | 5      | 1       | 3     | 2.6     |

## Fix notes (optional)

(If low score: e.g. add "Do not implement code—only assign or reply" to coordinator-instructions.)
