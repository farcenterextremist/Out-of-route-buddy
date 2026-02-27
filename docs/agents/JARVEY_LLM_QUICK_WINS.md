# Jarvey LLM Quick Wins

One-page config guide for faster, more reliable Ollama-based replies. All features already exist in the codebase; this document shows the recommended settings.

---

## Prerequisites

- **Ollama installed** — [ollama.ai](https://ollama.ai)
- **Models pulled** — Run before enabling:
  ```bash
  ollama pull qwen2.5:7b
  ollama pull phi3:mini
  ```

---

## Copy-Paste .env Block

Add these to your `scripts/coordinator-email/.env`:

```bash
# LLM Quick Wins (Ollama)
COORDINATOR_LISTENER_OLLAMA_MODEL=qwen2.5:7b
COORDINATOR_LISTENER_OLLAMA_FAST_MODEL=phi3:mini
COORDINATOR_LISTENER_OLLAMA_TIMEOUT=900
COORDINATOR_LISTENER_OLLAMA_TEMPERATURE=0.3
COORDINATOR_LISTENER_OLLAMA_MAX_TOKENS=512
JARVEY_PREWARM_OLLAMA=1
COORDINATOR_FAST_CONTEXT_FOR_RECENT=1
COORDINATOR_FAST_CONTEXT_CHARS=4000
JARVEY_OLLAMA_STREAMING=1
# JARVEY_OLLAMA_FALLBACK_TO_OPENAI=1  # requires OPENAI_API_KEY
```

---

## Verification

1. **Restart Jarvey** — Stop and restart `coordinator_listener.py`.
2. **Send a test email** — e.g. "What's next?" or "recent" to verify fast replies.
3. **Check logs** — Tokens should stream if `JARVEY_OLLAMA_STREAMING=1` is set.

---

## More Details

- [JARVEY_LLM_OPTIONS.md](JARVEY_LLM_OPTIONS.md) — Full model options and tuning
- [JARVEY_RESPONSE_TIME.md](JARVEY_RESPONSE_TIME.md) — Response time tips and troubleshooting
