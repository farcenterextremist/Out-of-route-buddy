#!/usr/bin/env python3
"""
One-off compose: build a Jarvey reply for a given subject + body without sending email.
Uses the same prompt and LLM as coordinator_listener.py. For scenario runs and testing.

Usage:
  python compose_reply.py "Re: OutOfRouteBuddy" "What's next?"
  python compose_reply.py "Re: OutOfRouteBuddy" "What's next?" --out path/to/response.txt
  python compose_reply.py "Re: OutOfRouteBuddy" "What are you capable of?" --mock  # No LLM needed
  echo "What's next?" | python compose_reply.py "Re: OutOfRouteBuddy" -

Requires: .env with one of COORDINATOR_LISTENER_OPENAI_API_KEY (or OPENAI_API_KEY),
  or COORDINATOR_LISTENER_OLLAMA_URL (or OLLAMA_URL) for local Ollama.
  Use --mock (or JARVEY_USE_MOCK_LLM=1) for testing without LLM.
"""

import argparse
import os
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

# Use same prompt and compose logic as the listener
import coordinator_listener as cl


def main():
    parser = argparse.ArgumentParser(
        description="Compose a Jarvey reply for the given email subject and body (no send)."
    )
    parser.add_argument("subject", help="Email subject line (e.g. Re: OutOfRouteBuddy)")
    parser.add_argument(
        "body",
        nargs="?",
        default=None,
        help="Email body. Use '-' to read from stdin.",
    )
    parser.add_argument(
        "--out",
        metavar="PATH",
        help="Write reply body to this file instead of stdout.",
    )
    parser.add_argument(
        "--mock",
        action="store_true",
        help="Use mock LLM (no Ollama/OpenAI). For testing and simulations.",
    )
    args = parser.parse_args()

    body = args.body
    if body is None:
        body = ""
    elif body == "-":
        body = sys.stdin.read()

    env = cl.load_env()
    use_mock = args.mock or env.get("JARVEY_USE_MOCK_LLM", "").lower() in ("1", "true", "yes")
    api_key = env.get("COORDINATOR_LISTENER_OPENAI_API_KEY") or env.get("OPENAI_API_KEY")
    ollama_url = env.get("COORDINATOR_LISTENER_OLLAMA_URL") or env.get("OLLAMA_URL")
    anthropic_key = env.get("ANTHROPIC_API_KEY") or env.get("COORDINATOR_LISTENER_ANTHROPIC_API_KEY")

    if use_mock:
        from mock_llm import compose_reply_mock
        reply = compose_reply_mock(args.subject, body)
    elif not api_key and not ollama_url and not anthropic_key:
        print(
            "No LLM configured. Set one of:\n"
            "  - COORDINATOR_LISTENER_OPENAI_API_KEY (or OPENAI_API_KEY) in .env,\n"
            "  - COORDINATOR_LISTENER_OLLAMA_URL (e.g. http://localhost:11434) for Ollama,\n"
            "  - ANTHROPIC_API_KEY for Anthropic.\n"
            "  - Or use --mock for mock LLM (no API needed).",
            file=sys.stderr,
        )
        sys.exit(1)
    else:
        try:
            reply = cl.compose_reply(args.subject, body, env)
        except Exception as e:
            print(f"Error composing reply: {e}", file=sys.stderr)
            sys.exit(1)

    if args.out:
        with open(args.out, "w", encoding="utf-8") as f:
            f.write(reply)
    else:
        print(reply)


if __name__ == "__main__":
    main()
