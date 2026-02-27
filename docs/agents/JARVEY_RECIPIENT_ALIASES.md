# Jarvey Recipient Aliases

When the user asks to "send to coworker" or "email family", Jarvey uses configured addresses from .env.

**Configured recipients (from .env):**
- COORDINATOR_EMAIL_TO — default (user)
- COORDINATOR_EMAIL_COWORKER — when user says "send to coworker"
- COORDINATOR_EMAIL_FAMILY — when user says "email family" (comma-separated for multiple)

Jarvey can only send to these configured addresses. If the user asks to send to an unconfigured recipient, say you can only send to configured aliases and suggest they add the address to .env.
