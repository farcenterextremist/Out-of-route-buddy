#!/usr/bin/env python3
"""
Jarvey RAG: optional embedding + vector search for docs.
When JARVEY_RAG_ENABLED=1 and "search" intent is detected, run vector search
and append top chunks to context. Uses sentence-transformers (configurable model)
and a local JSON index (jarvey_embeddings.json).
"""

import json
import math
import os

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))

# Default index path; override with JARVEY_RAG_INDEX_PATH
DEFAULT_INDEX_PATH = os.path.join(SCRIPT_DIR, "jarvey_embeddings.json")
DEFAULT_CHUNK_SIZE = 500
DEFAULT_TOP_K = 3
DEFAULT_EMBEDDING_MODEL = "intfloat/e5-small-v2"

# Model cache: keyed by model name to avoid reloading on every query
_model_cache: dict = {}


def _load_env() -> dict:
    """Load .env from coordinator-email dir."""
    env_path = os.path.join(SCRIPT_DIR, ".env")
    if not os.path.isfile(env_path):
        return {}
    env = {}
    try:
        with open(env_path, encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith("#") and "=" in line:
                    k, v = line.split("=", 1)
                    env[k.strip()] = v.strip().strip('"').strip("'")
    except OSError:
        pass
    return env


def _get_index_path() -> str:
    env = _load_env()
    path = env.get("JARVEY_RAG_INDEX_PATH") or os.environ.get("JARVEY_RAG_INDEX_PATH", "")
    return (path or DEFAULT_INDEX_PATH).strip()


def _get_embedding_model_name() -> str:
    """Return embedding model name from env; default all-MiniLM-L6-v2."""
    env = _load_env()
    name = env.get("JARVEY_RAG_EMBEDDING_MODEL") or os.environ.get("JARVEY_RAG_EMBEDDING_MODEL", "")
    return (name or DEFAULT_EMBEDDING_MODEL).strip()


def _get_top_k() -> int:
    """Return top_k from env; default 3."""
    env = _load_env()
    val = env.get("JARVEY_RAG_TOP_K") or os.environ.get("JARVEY_RAG_TOP_K", "")
    if not val:
        return DEFAULT_TOP_K
    try:
        return max(1, min(int(val), 20))
    except ValueError:
        return DEFAULT_TOP_K


def _get_chunk_size() -> int:
    """Return chunk size from env; default 500."""
    env = _load_env()
    val = env.get("JARVEY_RAG_CHUNK_SIZE") or os.environ.get("JARVEY_RAG_CHUNK_SIZE", "")
    if not val:
        return DEFAULT_CHUNK_SIZE
    try:
        return max(100, min(int(val), 2000))
    except ValueError:
        return DEFAULT_CHUNK_SIZE


def _get_model(model_name: str | None = None):
    """Return cached SentenceTransformer; load and cache if not present."""
    from sentence_transformers import SentenceTransformer

    name = model_name or _get_embedding_model_name()
    if name not in _model_cache:
        _model_cache[name] = SentenceTransformer(name)
    return _model_cache[name]


def _chunk_text(text: str, chunk_size: int | None = None) -> list[str]:
    """Split text into overlapping chunks of ~chunk_size chars."""
    size = chunk_size if chunk_size is not None else _get_chunk_size()
    chunks = []
    start = 0
    while start < len(text):
        end = min(start + size, len(text))
        chunk = text[start:end].strip()
        if chunk:
            chunks.append(chunk)
        start = end
        if start < len(text) and len(text) - start > size // 2:
            start -= size // 3  # Overlap
    return chunks


def _cosine_similarity(a: list[float], b: list[float]) -> float:
    """Cosine similarity between two vectors."""
    dot = sum(x * y for x, y in zip(a, b))
    norm_a = math.sqrt(sum(x * x for x in a))
    norm_b = math.sqrt(sum(x * x for x in b))
    if norm_a == 0 or norm_b == 0:
        return 0.0
    return dot / (norm_a * norm_b)


def search(query: str, index_path: str | None = None, top_k: int | None = None) -> list[str]:
    """
    Embed query, search index, return top_k chunk texts.
    Returns [] if index missing, sentence-transformers not installed, or on error.
    """
    path = index_path or _get_index_path()
    if not os.path.isfile(path):
        return []

    try:
        model = _get_model()
    except ImportError:
        return []

    try:
        with open(path, encoding="utf-8") as f:
            data = json.load(f)
    except (json.JSONDecodeError, OSError):
        return []

    chunks = data.get("chunks", [])
    embeddings = data.get("embeddings", [])
    if not chunks or len(embeddings) != len(chunks):
        return []

    index_model = data.get("model_name", "")
    current_model = _get_embedding_model_name()
    if index_model and index_model != current_model:
        import sys
        print(
            f"Warning: RAG index was built with model {index_model!r}, "
            f"current model is {current_model!r}. Rebuild index: python build_rag_index.py",
            file=sys.stderr,
        )

    k = top_k if top_k is not None else _get_top_k()
    query_embedding = model.encode(query, convert_to_numpy=True).tolist()

    scored = []
    for i, emb in enumerate(embeddings):
        sim = _cosine_similarity(query_embedding, emb)
        scored.append((sim, chunks[i].get("text", "")))

    scored.sort(key=lambda x: -x[0])
    return [text for _, text in scored[:k] if text]


def build_index(
    docs_dir: str | list[str] | None = None,
    output_path: str | None = None,
    chunk_size: int | None = None,
) -> int:
    """
    Walk docs_dir (default REPO_ROOT/docs + phone-emulator), chunk .md files, embed, save to output_path.
    docs_dir can be a single path or list of paths. Returns number of chunks indexed.
    """
    default_dirs = [
        os.path.join(REPO_ROOT, "docs"),
        os.path.join(REPO_ROOT, "phone-emulator"),
    ]
    roots = docs_dir if docs_dir is not None else default_dirs
    if isinstance(roots, str):
        roots = [roots]
    out = output_path or _get_index_path()
    size = chunk_size if chunk_size is not None else _get_chunk_size()

    try:
        model = _get_model()
    except ImportError:
        raise ImportError("sentence-transformers required. pip install sentence-transformers")

    chunks_with_meta: list[dict] = []
    for root in roots:
        if not os.path.isdir(root):
            continue
        for dirpath, _dirs, files in os.walk(root):
            for name in files:
                if not name.lower().endswith(".md"):
                    continue
                fp = os.path.join(dirpath, name)
                try:
                    with open(fp, encoding="utf-8") as f:
                        text = f.read()
                except OSError:
                    continue
                rel = os.path.relpath(fp, REPO_ROOT).replace("\\", "/")
                for chunk_text in _chunk_text(text, size):
                    chunks_with_meta.append({"text": chunk_text, "source": rel})

    if not chunks_with_meta:
        return 0

    model_name = _get_embedding_model_name()
    texts = [c["text"] for c in chunks_with_meta]
    embeddings = model.encode(texts, convert_to_numpy=True).tolist()

    data = {
        "chunks": chunks_with_meta,
        "embeddings": embeddings,
        "chunk_size": size,
        "model_name": model_name,
    }
    os.makedirs(os.path.dirname(out) or ".", exist_ok=True)
    with open(out, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=0, ensure_ascii=False)

    return len(chunks_with_meta)
