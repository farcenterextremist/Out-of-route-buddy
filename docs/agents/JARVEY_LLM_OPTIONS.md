# Jarvey LLM Options and Improvements

How to improve Jarvey's LLM quality and speed. Covers open-source alternatives, model substitution, and prompt/context tweaks. For a one-page config guide, see [JARVEY_LLM_QUICK_WINS.md](JARVEY_LLM_QUICK_WINS.md).

---

## Current Setup

| Backend | Model | Config | Typical latency |
|---------|-------|--------|-----------------|
| OpenAI | gpt-4o-mini | `COORDINATOR_LISTENER_OPENAI_API_KEY` | 2–5 s |
| Ollama | llama3.2 (default) | `COORDINATOR_LISTENER_OLLAMA_MODEL` | 1–10+ min |
| Anthropic | claude-3-haiku | `ANTHROPIC_API_KEY` | 2–5 s |

Jarvey uses `compose_reply()` which routes by `COORDINATOR_LISTENER_LLM_BACKEND` (auto|openai|ollama|anthropic).

---

## Better Open-Source Models (Ollama)

Replace `llama3.2` with one of these for better quality or speed:

### Speed-focused (faster than llama3.2)

| Model | Pull command | Size | Speed | Use when |
|-------|--------------|------|-------|----------|
| **mistral** | `ollama pull mistral` | ~4 GB | ~24 tok/s | Want fastest local replies |
| **qwen2.5:7b** | `ollama pull qwen2.5:7b` | ~4.4 GB | ~20 tok/s | Good balance, multilingual |
| **phi3:mini** | `ollama pull phi3:mini` | ~2 GB | Fast | Low RAM, acceptable quality |

### Quality-focused (better than llama3.2)

| Model | Pull command | Size | Quality | Use when |
|-------|--------------|------|---------|----------|
| **qwen2.5:14b** | `ollama pull qwen2.5:14b` | ~9 GB | High | 16GB+ RAM, want best local quality |
| **llama3.3:8b** | `ollama pull llama3.3:8b` | ~4.7 GB | High | General use, instruction-following |
| **phi4:14b** | `ollama pull phi4:14b` | ~8 GB | High | Creative/email prose quality |

### Recommended for Jarvey

- **Best balance:** `qwen2.5:7b` — faster than llama3.2, good email prose, multilingual
- **Fastest:** `mistral` — 20%+ faster, similar quality
- **Best quality (local):** `qwen2.5:14b` or `llama3.3:8b`

### Substitute in .env

```bash
# After pulling: ollama pull qwen2.5:7b
COORDINATOR_LISTENER_OLLAMA_MODEL=qwen2.5:7b
```

---

## Non-Ollama Open-Source Options (OpenAI-Compatible Local)

Jarvey supports OpenAI-compatible local servers (vLLM, LM Studio, etc.) via a custom base URL.
Set `COORDINATOR_LISTENER_LLM_BACKEND=openai` and:

```bash
COORDINATOR_LISTENER_OPENAI_BASE_URL=http://localhost:8080/v1
COORDINATOR_LISTENER_OPENAI_MODEL=qwen2.5-7b   # model name as exposed by your server
```

Many local servers accept any API key when using a custom base URL; you can leave `OPENAI_API_KEY` unset or use a placeholder.

| Option | How | Pros | Cons |
|--------|-----|------|------|
| **vLLM** | `python -m vllm.entrypoints.openai.api --model qwen2.5-7b` | High throughput | More setup |
| **LM Studio** | Start local server in app, use its base URL | Easy UI | Windows/Mac only |
| **Other** | Any server exposing `/v1/chat/completions` | Drop-in for Jarvey | Model name may differ |

---

## LLM Improvements (Beyond Model Swap)

### 1. Reduce context size for simple queries

Already supported: `COORDINATOR_FAST_CONTEXT_FOR_RECENT=1` uses ~4000 chars for "recent" intent. Fewer tokens = faster inference.

### 2. System prompt distillation

Shorter, clearer instructions reduce tokens and can improve adherence. The HITL persona block and anti-hallucination rules are already in place.

### 3. Temperature and sampling

Ollama's API supports `temperature` and `top_p`. Lower temperature (e.g. 0.3) reduces hallucination. Already implemented:

```
COORDINATOR_LISTENER_OLLAMA_TEMPERATURE=0.3
```

### 4. Max tokens

Jarvey uses `max_tokens=1024` for OpenAI. For Ollama, cap output to speed inference:

```
COORDINATOR_LISTENER_OLLAMA_MAX_TOKENS=512
```

### 5. Phase 5 Enhancements

**Fallback chain:** When Ollama fails (timeout, connection error), retry with OpenAI if configured:

```
JARVEY_OLLAMA_FALLBACK_TO_OPENAI=1
```

Requires `COORDINATOR_LISTENER_OPENAI_API_KEY` (or `OPENAI_API_KEY`). On fallback success, the circuit breaker is reset.

**Model selection by intent:** Use a smaller/faster model for simple intents (recent, version, roadmap):

```
COORDINATOR_LISTENER_OLLAMA_FAST_MODEL=phi3:mini
```

When set, single-intent questions matching "recent", "version", or "roadmap" use the fast model; complex or multi-intent questions use the default model.

**Streaming:** Reduce perceived latency by streaming Ollama tokens (accumulates to full reply before sending):

```
JARVEY_OLLAMA_STREAMING=1
```

---

## Quick Substitution Steps

1. **Pull a model:**
   ```bash
   ollama pull qwen2.5:7b
   ```

2. **Pre-warm (optional):**
   ```bash
   ollama run qwen2.5:7b
   ```
   Leave running in a separate terminal.

3. **Set in .env:**
   ```
   COORDINATOR_LISTENER_OLLAMA_MODEL=qwen2.5:7b
   ```

4. **Restart Jarvey** — it will use the new model on the next LLM call.

---

## Benchmark Results

Use `run_jarvey_benchmark.py` to compare models on Jarvey scenarios:

```bash
cd scripts/coordinator-email
# Set model in .env, then:
python run_jarvey_benchmark.py
# Or LLM-only: python run_jarvey_benchmark.py --llm-only
```

Scenarios 1, 2, 4–9 use the LLM; scenario 3 uses the template path. Check `jarvey_workflow.log` for `model` and `latency_ms` in structured logs when `JARVEY_STRUCTURED_LOG=1`.

| Model | Pass rate (LLM scenarios) | Typical latency | Notes |
|-------|---------------------------|-----------------|-------|
| llama3.2 | — | — | Default |
| qwen2.5:7b | — | — | Recommended balance |
| mistral | — | — | Fastest |
| qwen2.5:14b | — | — | Best quality |

Fill in after running benchmarks with each model.

---

## References

- [Ollama library](https://ollama.com/library) — browse and pull models
- [JARVEY_RESPONSE_TIME.md](JARVEY_RESPONSE_TIME.md) — pre-warm, timeout, template path
- [coordinator_listener.py](../scripts/coordinator-email/coordinator_listener.py) — `compose_reply_ollama`, `compose_reply_openai`
