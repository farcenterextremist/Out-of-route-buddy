#!/usr/bin/env python3
"""
Build the Jarvey RAG index (jarvey_embeddings.json).
Run this before using JARVEY_RAG_ENABLED=1. Chunks docs/ and embeds with the model from
JARVEY_RAG_EMBEDDING_MODEL (default: intfloat/e5-small-v2).

Usage:
  python build_rag_index.py
  python build_rag_index.py --docs-dir ../docs --output my_index.json

Requires: pip install sentence-transformers
"""

import argparse
import os
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if SCRIPT_DIR not in sys.path:
    sys.path.insert(0, SCRIPT_DIR)

from jarvey_rag import build_index, _get_index_path

REPO_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))


def main():
    parser = argparse.ArgumentParser(
        description="Build Jarvey RAG index from docs/ and phone-emulator/ (default)"
    )
    parser.add_argument(
        "--docs-dir",
        default=None,
        help="Docs directory (default: REPO_ROOT/docs + phone-emulator)",
    )
    parser.add_argument("--output", default=None, help="Output JSON path (default: jarvey_embeddings.json)")
    args = parser.parse_args()

    docs_dir = None
    if args.docs_dir:
        docs_dir = os.path.join(REPO_ROOT, args.docs_dir) if not os.path.isabs(args.docs_dir) else args.docs_dir
        if not os.path.isdir(docs_dir):
            print(f"Error: docs dir not found: {docs_dir}", file=sys.stderr)
            sys.exit(1)
    output_path = args.output or _get_index_path()

    try:
        n = build_index(docs_dir=docs_dir, output_path=output_path)
        print(f"Indexed {n} chunks to {output_path}")
    except ImportError as e:
        print(f"Error: {e}", file=sys.stderr)
        print("Run: pip install sentence-transformers", file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
