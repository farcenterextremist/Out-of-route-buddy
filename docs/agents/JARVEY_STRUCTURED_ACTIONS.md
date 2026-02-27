# Jarvey Structured Actions

Future pattern for LLM replies that return structured actions instead of (or in addition to) free-form text.

## Schema

When `JARVEY_STRUCTURED_ACTIONS=1` (future), the coordinator could optionally request the LLM to return a JSON block:

```json
{
  "action": "send_digest",
  "params": {}
}
```

## Supported Actions (Planned)

| Action | Description | Params |
|--------|-------------|--------|
| `send_digest` | Send weekly board digest | (none) |
| `clarify` | Ask user for clarification | `options`: list of choices |

## Execution Flow

1. LLM returns reply body; post-processor calls `parse_structured_reply(reply_text)`.
2. If result is non-null and action is known, coordinator executes (e.g. send template, send clarification email).
3. If result is null or action unknown, treat reply as normal free-form text.

## Current State

- `structured_output.parse_structured_reply()` returns `None` (stub).
- `structured_output.get_action_schema()` returns schema for documentation.
- No behavior change; enables future work without blocking.
