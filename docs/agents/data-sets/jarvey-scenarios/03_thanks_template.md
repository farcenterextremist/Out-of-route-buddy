# Jarvey scenario 3 — Thanks (template path)

- **Prompt type:** Template (no LLM)
- **Expected:** `check_and_respond.choose_response` returns template "thanks"; body contains "— Jarvey".

## Prompt (user email)

**Subject:** Re: OutOfRouteBuddy

**Body:**
```
Thanks, that works!
```

## Look for (scoring)

- Template reply (thanks), not LLM.
- Body contains "Thanks for getting back" and "— Jarvey".
- No need to run compose_reply.py; use `check_and_respond.choose_response("Re: OutOfRouteBuddy", "Thanks, that works!")` or run the coordinator listener with template path only (when no LLM reply is used for this intent).

---

## Response

(Verified by unit tests. Template body from `choose_response("Re: OutOfRouteBuddy", "Thanks, that works!")`.)

```
Hi,

Thanks for getting back to us. We'll continue from here in the next session or by email.

— Jarvey
```

---

## Score (optional)

Template path: N/A for LLM dimensions. Use to confirm template selection and sign-off.

## Fix notes (optional)

(N/A for template path.)
