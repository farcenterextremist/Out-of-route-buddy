# Jarvey RAG Plan (Deferred)

Optional embedding + vector search for docs when file index and intent-based snippets are insufficient.

## Options

### Option A: sentence-transformers + local JSON index

- **Deps:** `pip install sentence-transformers` (pulls in transformers and torch)
- **Flow:** Chunk docs (configurable size), embed with configurable model, store in `jarvey_embeddings.json`. Model is cached after first load.
- **Query:** Embed user query, cosine-similarity search, return top-k chunks.
- **Env:** `JARVEY_RAG_ENABLED=1`, `JARVEY_RAG_INDEX_PATH=jarvey_embeddings.json`, `JARVEY_RAG_EMBEDDING_MODEL=intfloat/e5-small-v2`, `JARVEY_RAG_TOP_K=3`, `JARVEY_RAG_CHUNK_SIZE=500`

### Option B: OpenAI embeddings + Chroma/FAISS

- **Deps:** `pip install chromadb` or `faiss-cpu`, OpenAI API
- **Flow:** Same chunking; use OpenAI `text-embedding-3-small` for embeddings; store in vector DB.
- **Query:** Embed query, search vector DB, return top 3 chunks.
- **Env:** `JARVEY_RAG_ENABLED=1`, `JARVEY_RAG_PROVIDER=openai`

## When to Use

- Intent: "find anything about X" or "search for X in the docs"
- File index substring match returns nothing useful
- User asks broad questions that span multiple files

## Integration Point

In `context_loader.load_context_for_user_message`, when `JARVEY_RAG_ENABLED=1` and a "search" or "find" intent is detected, run vector search and append top 3 chunks to context before base + intent snippets.

## Model Upgrade

To switch embedding models (e.g. for better quality):

1. Set `JARVEY_RAG_EMBEDDING_MODEL` in `.env` (e.g. `intfloat/e5-small-v2`, `BAAI/bge-small-en-v1.5`, or `all-MiniLM-L12-v2`).
2. **Rebuild the index:** `python scripts/coordinator-email/build_rag_index.py` — embeddings are model-specific, so the index must be regenerated after changing the model.
3. The index stores `model_name`; if it differs from the current env model, search logs a warning.

Supported models: `intfloat/e5-small-v2` (default), `BAAI/bge-small-en-v1.5`, `BAAI/bge-base-en-v1.5`, `all-MiniLM-L6-v2`, `all-MiniLM-L12-v2`.

## Status

**Implemented (Option A).** `jarvey_rag.py` + `build_rag_index.py`. Set `JARVEY_RAG_ENABLED=1`, run `python build_rag_index.py`, then use "search" intent (find anything about, search the docs, etc.).
