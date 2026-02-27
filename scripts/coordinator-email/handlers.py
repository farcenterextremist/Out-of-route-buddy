#!/usr/bin/env python3
"""
Handler protocol and registry for Jarvey. Enables pluggable response handlers.
Templates and LLM are handlers; registry runs them in priority order.
"""

from typing import Protocol, runtime_checkable

from typing import Any


@runtime_checkable
class Handler(Protocol):
    """
    Protocol for Jarvey response handlers.
    match() returns confidence 0-1; respond() returns (subject, body).
    """

    def match(self, subject: str, body: str) -> float:
        """Return match confidence 0-1. 0 = no match."""
        ...

    def respond(self, subject: str, body: str, **kwargs: Any) -> tuple[str, str]:
        """Return (subject, body) for the reply."""
        ...

    @property
    def name(self) -> str:
        """Handler identifier for logging."""
        ...


class TemplateHandler:
    """Handler that uses template_registry."""

    def __init__(self, confidence_threshold: float = 0.7):
        self._threshold = confidence_threshold

    @property
    def name(self) -> str:
        return "template"

    def match(self, subject: str, body: str) -> float:
        from template_registry import choose_response_with_confidence, _get_confidence_threshold
        _, _, key, conf = choose_response_with_confidence(subject or "", body or "")
        threshold = _get_confidence_threshold()
        if key == "default":
            return 0.0  # Default = no template match, let LLM handle
        return conf if conf >= threshold else 0.0

    def respond(self, subject: str, body: str, **kwargs: Any) -> tuple[str, str]:
        from template_registry import choose_response_with_confidence, _get_confidence_threshold
        subj, body_text, key, conf = choose_response_with_confidence(subject or "", body or "")
        if conf < _get_confidence_threshold():
            raise ValueError("Template confidence below threshold")
        return subj, body_text


class LLMHandler:
    """Handler that uses compose_reply (OpenAI/Ollama/Anthropic)."""

    def __init__(self, env: dict[str, str]):
        self._env = env

    @property
    def name(self) -> str:
        return "llm"

    def match(self, subject: str, body: str) -> float:
        # LLM is fallback: matches when no template did (return low but non-zero so it's tried)
        return 0.5

    def respond(self, subject: str, body: str, **kwargs: Any) -> tuple[str, str]:
        import coordinator_listener as cl
        intents = kwargs.get("intents") or []
        reply = cl.compose_reply(subject or "", body or "", self._env, intents=intents)
        s = subject or ""
        subj = s if s.strip().lower().startswith("re:") else f"Re: {s}"
        return subj, reply


_CUSTOM_HANDLERS: list[type] = []


def register_handler(handler_class: type, *args: Any, **kwargs: Any) -> None:
    """Register a custom handler. Call before get_handler_registry()."""
    _CUSTOM_HANDLERS.append((handler_class, args, kwargs))


def get_handler_registry(env: dict[str, str]) -> list[Handler]:
    """Return ordered list of handlers: custom first, then LLM. No templates (all replies via LLM)."""
    handlers: list[Handler] = []
    for hcls, args, kwargs in _CUSTOM_HANDLERS:
        try:
            handlers.append(hcls(*args, **kwargs))
        except Exception:
            pass
    handlers.append(LLMHandler(env))
    return handlers


def handle_message(subject: str, body: str, env: dict[str, str], intents: list[str] | None = None) -> tuple[str, str, str]:
    """
    Run handlers in order. Returns (subject, body, handler_name).
    All replies go through LLM with full project context.
    """
    handlers = get_handler_registry(env)
    for h in handlers:
        conf = h.match(subject, body)
        if conf > 0:
            try:
                subj, body_text = h.respond(subject, body, intents=intents or [])
                return subj, body_text, h.name
            except ValueError:
                continue
            except Exception:
                raise
    # Fallback: LLM (should always have been tried)
    h = LLMHandler(env)
    subj, body_text = h.respond(subject, body, intents=intents or [])
    return subj, body_text, "llm"
