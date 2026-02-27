# Jarvey Response Time Tips

How to improve how quickly Jarvey responds to email while preserving reply quality.

---

## Jarvey Not Responding?

If Jarvey hasn't replied to a message:

1. **Timeout** — Ollama may be timing out before finishing. Default is 15 min. Set in `.env`:
   ```
   COORDINATOR_LISTENER_OLLAMA_TIMEOUT=1200
   ```
   (1200 = 20 min; use 900 for 15 min.)

2. **Circuit breaker** — After repeated LLM failures, Jarvey skips compose to avoid hammering. Reset with:
   ```
   python scripts/coordinator-email/diagnose_jarvey.py --reset-circuit
   ```

3. **Run diagnostics** — Check inbox, dedupe, cooldown, and subject/FROM filters:
   ```
   python scripts/coordinator-email/diagnose_jarvey.py
   ```
   Use `--fix-dedupe` to clear state and force a reply (may cause duplicate if we already sent).

4. **Listener running?** — Ensure `python scripts/coordinator-email/coordinator_listener.py` is running in a terminal.

5. **Benchmark / training simulation** — For fast validation without LLM, run `python run_jarvey_benchmark.py --simulate` (10/10 pass with mock). For full training report: `python send_jarvey_training_report.py --simulate --dry-run`. Add `--live` to stream output to a timestamped log file (`benchmark_logs/benchmark_run_*.log`).

---

## Quick Wins

For the fastest path to better response time, see [JARVEY_LLM_QUICK_WINS.md](JARVEY_LLM_QUICK_WINS.md) — a one-page config guide with copy-paste `.env` settings for qwen2.5:7b, phi3:mini, streaming, pre-warm, and fast context.

---

## Root Causes of Slow Responses

- **Ollama:** Local models can take 1–10+ minutes with large context (8000 chars). First request has cold-start delay (model load into RAM).
- **Large context:** Base context (core + project index + SSOT) + intent snippets = many tokens; more tokens = slower inference.
- **Template path:** Many common intents now use templates (instant); only unmatched or complex questions go to the LLM.

---

## Tips to Improve Response Time

### 1. Pre-warm Ollama

**Option A (automatic):** Set `JARVEY_PREWARM_OLLAMA=1` in `.env`. The listener fires a tiny request at startup to load the model into RAM before the first real email.

**Option B (manual):** Run the model in a separate terminal before starting the listener:

```bash
ollama run llama3.2
```

Leave it running. The first email will be faster because the model is already loaded.

### 2. Use OpenAI for Faster Responses

Set `COORDINATOR_LISTENER_OPENAI_API_KEY` in `.env`. GPT-4o-mini typically responds in 2–5 seconds vs. 1–10+ minutes for local Ollama.

### 3. Consider a Smaller Ollama Model

For speed (with possible quality trade-off):

- `llama3.2:1b` — faster, less capable
- `phi3:mini` — good balance of speed and quality

Set in `.env`:

```
COORDINATOR_LISTENER_OLLAMA_MODEL=phi3:mini
```

### 4. Increase Poll Interval (Operational)

If Jarvey is slow and you don't need instant replies, increase the poll interval to reduce load:

```
COORDINATOR_LISTENER_INTERVAL_MINUTES=5
```

### 5. Template Path = Instant (Strategic Ollama)

Messages that match templates skip the LLM and respond immediately. No configuration needed.

**Template-covered (instant):** recent, roadmap, version, capabilities, thanks, priority, weekly digest, unclear.

**Ollama path:** Complex questions, multi-part questions, "where is X", delegation, recovery, architecture, etc. — only when no template matches. Prefer expanding templates over adding LLM calls.

### 6. Lighter Context for "Recent" Intent (Optional)

When the user asks only about recent updates (e.g. "what changed?" or "recent updates") and the message is short, use a reduced context cap to speed up Ollama:

```
COORDINATOR_FAST_CONTEXT_FOR_RECENT=1
COORDINATOR_FAST_CONTEXT_CHARS=4000
```

This applies when intents are only `["recent"]` and the combined subject+body length is under 100 characters. Test that reply quality remains acceptable before relying on this.

---

## Quality vs. Speed Trade-offs

| Option | Speed | Quality |
|--------|-------|---------|
| OpenAI (gpt-4o-mini) | Fast (2–5 s) | High |
| Ollama (llama3.2) | Slow (1–10 min) | Good |
| Ollama (phi3:mini) | Medium | Good |
| Ollama (llama3.2:1b) | Faster | Lower |
| Ollama (qwen2.5:7b) | Medium | High |
| Ollama (mistral) | Fast | Good |
| Template path | Instant | Consistent |

For more model options and substitution steps, see [JARVEY_LLM_OPTIONS.md](JARVEY_LLM_OPTIONS.md).

---

## Timeout Configuration

If Ollama times out, increase the timeout in `.env`:

```
COORDINATOR_LISTENER_OLLAMA_TIMEOUT=600
```

Default is 600 seconds (10 min). For very slow systems, you may need this.

---

## Recommended Ollama Setup

For speed and reliability when using Ollama, add these to `.env`:

| Env var | Value | Why |
|---------|-------|-----|
| `JARVEY_PREWARM_OLLAMA` | 1 | Load model at startup; first reply 2–5 min faster |
| `COORDINATOR_LISTENER_OLLAMA_FAST_MODEL` | phi3:mini | 2–3x faster for simple questions (recent, version, roadmap) |
| `COORDINATOR_FAST_CONTEXT_FOR_RECENT` | 1 | Lighter context for "what changed?" — fewer tokens |
| `COORDINATOR_FAST_CONTEXT_CHARS` | 4000 | Cap context when fast path applies |
| `JARVEY_OLLAMA_FALLBACK_TO_OPENAI` | 1 | When Ollama times out, retry with OpenAI (requires API key) |
| `COORDINATOR_LISTENER_OLLAMA_TIMEOUT` | 600 | 10 min timeout for slow systems |
