# LLM Loop

**Purpose:** Provide the permanent local-first loop for LLM-assisted work in Cursor while preserving the existing token-loop history and tooling. The Android `app` remains the codebase being edited; the LLM runtime lives on the desktop side in Cursor/automation.

**Default local provider:** `Ollama`

**Compatibility rule:** The token loop remains a permanent lane inside the LLM loop. Historical `token_loop_start`, `token_loop_end`, `token_loop_events.jsonl`, and `token-...` run IDs stay valid.

---

## What this means now

- `start llm loop` is a permanent trigger for local LLM workflow work.
- `start token loop` remains valid and still runs the current token-audit lane.
- The Android app does **not** need to host the model.
- Local-first is the default; cloud/API providers are optional and secondary.

---

## Current lanes

| Lane | Purpose | Current entrypoint |
|------|---------|--------------------|
| `token_audit` | Reduce token spend, context squish, and loop overhead | `scripts/automation/run_token_loop.ps1` |

`run_llm_loop.ps1` is now the permanent top-level entrypoint for the local-first LLM loop. The `token_audit` lane remains the first stable lane under that contract so we can expand the loop without breaking existing automation.

---

## Provider strategy

### Default now: Ollama

Use `Ollama` for local desktop automation inside Cursor/PowerShell:

- easiest local scripting surface
- simple local API/CLI workflow
- good fit for small open-source models used to help edit the Android app

Official command examples:

```powershell
ollama serve
ollama pull gemma3
ollama run gemma3
ollama ls
ollama ps
```

### Optional later

- local low-level runtime: `llama.cpp`
- cloud/API providers: optional, not default
- in-app or mobile inference: separate future track, not part of this first pass

---

## Monitoring stance

- Local Ollama-first runs should stay lightweight.
- Provider-specific monitoring is optional.
- The current OpenAI usage monitor remains available for OpenAI-backed scripts, but it is **not** the identity of the LLM loop.

---

## Next evaluation phase

When we move beyond contract cleanup, evaluate lightweight open-source models for desktop editing help first:

- ultra-light: `SmolLM`-class models
- small general-purpose: `Qwen` small variants
- stronger small models: `Gemma` small variants
- coding/helpful compact options: `Phi` small variants

Evaluation questions:

- Which model feels useful for code editing without slowing the loop?
- Which model works well enough through Ollama for scriptable local automation?
- Which prompts keep output short, reliable, and cheap?

---

## Start commands

Preferred broader trigger:

```powershell
.\scripts\automation\run_llm_loop.ps1
```

Compatibility trigger:

```powershell
.\scripts\automation\run_token_loop.ps1
```

Both are valid. `run_llm_loop.ps1` is the permanent top-level entrypoint, and `run_token_loop.ps1` remains the durable token-audit lane entrypoint underneath it.
