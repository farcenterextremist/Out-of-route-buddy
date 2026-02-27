# Hugging Face Options for Jarvey

This document describes Hugging Face–based options for the Jarvey coordinator: embedding models for RAG, the Transformers (SmolLM) LLM backend, and the experimental email model.

## 1. RAG Embedding Models

When `JARVEY_RAG_ENABLED=1`, Jarvey uses sentence-transformers to embed docs and queries for vector search.

### Default

- **Current default:** `intfloat/e5-small-v2`
- **Previous default:** `all-MiniLM-L6-v2` (384-dim, MTEB ~56)

### Supported Models

| Model | Dimensions | Notes |
|-------|------------|-------|
| `intfloat/e5-small-v2` | 384 | Default; stronger than MiniLM |
| `BAAI/bge-small-en-v1.5` | 384 | Good quality/speed tradeoff |
| `BAAI/bge-base-en-v1.5` | 768 | Higher quality, slower |
| `all-MiniLM-L6-v2` | 384 | Lightweight fallback |
| `all-MiniLM-L12-v2` | 384 | Slightly better than L6 |

### Configuration

```bash
# .env
JARVEY_RAG_ENABLED=1
JARVEY_RAG_EMBEDDING_MODEL=intfloat/e5-small-v2
JARVEY_RAG_INDEX_PATH=jarvey_embeddings.json
JARVEY_RAG_TOP_K=3
JARVEY_RAG_CHUNK_SIZE=500
```

### Index Rebuild

Embeddings are model-specific. After changing `JARVEY_RAG_EMBEDDING_MODEL`, rebuild the index:

```bash
python scripts/coordinator-email/build_rag_index.py
```

See [JARVEY_RAG_PLAN.md](JARVEY_RAG_PLAN.md) for full RAG setup.

---

## 2. Transformers (SmolLM) Backend

Use local Hugging Face models (e.g. SmolLM) as an alternative to Ollama for composing replies.

### Configuration

```bash
# .env
COORDINATOR_LISTENER_LLM_BACKEND=transformers
COORDINATOR_LISTENER_TRANSFORMERS_MODEL=HuggingFaceTB/SmolLM2-1.7B-Instruct
# Optional: device (auto/cpu/cuda)
# COORDINATOR_LISTENER_TRANSFORMERS_DEVICE=cuda
# Optional: timeout (seconds)
# COORDINATOR_LISTENER_TRANSFORMERS_TIMEOUT=120
```

### Dependencies

```bash
pip install transformers>=4.36.0 accelerate torch
```

`sentence-transformers` may already provide `transformers` and `torch`; `accelerate` is used for device placement.

### Supported Models

- **HuggingFaceTB/SmolLM2-1.7B-Instruct** (default) — 1.7B instruct model, chat format
- Other instruct/chat models compatible with `apply_chat_template` (e.g. Mistral, Qwen instruct variants)

### Flow

When `COORDINATOR_LISTENER_LLM_BACKEND=transformers`, the coordinator uses `compose_reply_transformers()`:

1. Load model and tokenizer
2. Build chat prompt from system + user message
3. Generate reply with `max_new_tokens=1024`, `temperature=0.7`
4. Return the assistant’s reply

No API key is required; the model runs locally (CPU or CUDA).

---

## 3. Email Model (Experimental)

The `postbot/t5-small-kw2email-v2` model expands keywords into short email drafts. It is available as a standalone script for experimentation.

### Usage

```bash
python scripts/coordinator-email/compose_reply_email_model.py "Subject here" "Body or keywords"
```

### As a Module

```python
from compose_reply_email_model import compose_reply_email_model
draft = compose_reply_email_model("Meeting follow-up", "discussed timeline and next steps")
```

### Behavior

1. Extracts keywords from subject + body (stopwords removed)
2. Uses `text2text-generation` pipeline with `postbot/t5-small-kw2email-v2`
3. Returns a short draft string

### Integration Options (Future)

- **Template expansion:** When a template matches but the body is minimal, use the email model to expand keywords into a draft
- **Fallback draft:** When the main LLM times out or fails, use the email model for a minimal reply
- **Standalone script:** Current approach — no integration into the main coordinator flow yet

---

## Environment Variables Summary

| Variable | Purpose |
|----------|---------|
| `JARVEY_RAG_EMBEDDING_MODEL` | RAG embedding model (default: `intfloat/e5-small-v2`) |
| `COORDINATOR_LISTENER_LLM_BACKEND` | `auto`, `openai`, `ollama`, `anthropic`, or `transformers` |
| `COORDINATOR_LISTENER_TRANSFORMERS_MODEL` | Hugging Face model for transformers backend (default: `HuggingFaceTB/SmolLM2-1.7B-Instruct`) |
| `COORDINATOR_LISTENER_TRANSFORMERS_DEVICE` | `auto`, `cpu`, or `cuda` |
| `COORDINATOR_LISTENER_TRANSFORMERS_TIMEOUT` | Timeout in seconds |
