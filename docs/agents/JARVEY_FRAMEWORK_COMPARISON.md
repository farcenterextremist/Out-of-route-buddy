# Jarvey vs. Other Bot Frameworks — Comparison & Improvement Ideas

How Jarvey (coordinator-email) compares to mainstream conversational AI frameworks and where we can improve.

---

## Framework Comparison

| Aspect | Jarvey | LangChain | Rasa | Botpress | Modern Email Agents (2025) |
|--------|--------|-----------|------|----------|----------------------------|
| **Channel** | Email (IMAP/SMTP) | Multi (API, chat, etc.) | Multi (Slack, web, etc.) | Multi (web, Slack, etc.) | Multi (Gmail API, Graph, etc.) |
| **Approach** | Poll → template/LLM → send | Code-first, chains/agents | ML + rules, intent/entity | Visual flows + NLU | RAG + orchestration + approval gates |
| **LLM** | Direct (OpenAI/Ollama/Anthropic) | Abstraction layer, many providers | Optional (LLM policies) | Best/Fast model modes | OAuth + provider abstraction |
| **State** | File-based (dedupe, cooldown) | In-memory / Redis | Tracker store | Built-in | Audit trails, compliance |
| **Testing** | Unit + scenario + mock | Pytest + mock | Rasa test stories | Built-in testing | Draft review, human approval |
| **RAG / Context** | Intent-based + optional RAG (sentence-transformers) | Document loaders, vector DB | Custom actions | Knowledge base | Semantic retrieval from KB |
| **Dialogue** | Single-turn (email reply) | Multi-turn via memory | Multi-turn via stories | Visual flows | Thread-aware, multi-turn |
| **Memory** | Optional (JARVEY_CONVERSATION_MEMORY) | Built-in | Tracker store | Built-in | Thread history |
| **Entity extraction** | Keyword + regex (extract_entities) | NER, custom | Built-in NLU | Built-in | Intent + entity pipelines |
| **Retry / backoff** | Exponential (retry_utils) | Configurable | Built-in | Built-in | Circuit breaker patterns |

---

## What Jarvey Does Well (Current State)

1. **Template-first** — Fast path for thanks, priority, weekly digest; no LLM cost.
2. **Intent-based context** — `context_loader` loads roadmap, recovery, version, etc. only when needed; keeps prompt size down.
3. **HITL persona** — Clear role as coordinator, not autonomous agent; reduces hallucination risk.
4. **Project index** — File tree + snippets so Jarvey can answer "where is X defined?"
5. **Mock LLM** — `mock_llm.py` + `--mock` for CI and simulations without Ollama/OpenAI.
6. **RAG (implemented)** — Optional `jarvey_rag.py` + `build_rag_index.py`; semantic search when `JARVEY_RAG_ENABLED=1`.
7. **Conversation memory (implemented)** — `JARVEY_CONVERSATION_MEMORY=1` loads last N exchanges per thread.
8. **Entity extraction (implemented)** — `extract_entities()` for ViewModels, Repositories, etc.; feeds entity path hints.
9. **Retry with backoff** — `retry_utils.py` exponential backoff for IMAP/SMTP.
10. **Quoted/forwarded handling** — `_strip_quoted_content` supports Re:, reply-above, reply-below, forwarded.

---

## Improvement Ideas (Prioritized)

### High priority — gaps vs. modern frameworks

#### 1. **Structured output / tool use** (LangChain agents, OpenAI agents)

- **Current:** Free-form text reply only.
- **Improvement:** For intents like "send weekly digest", return structured `{action: "send_digest", params: {...}}` and let coordinator execute; reduces hallucination and enables automation.
- **Reference:** LangChain `.with_structured_output`, OpenAI function calling.

#### 2. **Fallback / clarification flow** (Botpress)

- **Current:** "Unclear" fallback in prompt; no explicit clarification loop.
- **Improvement:** When confidence is low, send a clarification template ("Could you specify: X, Y, or Z?") instead of guessing.
- **Benefit:** Fewer wrong replies when user message is vague.

#### 3. **Circuit breaker for LLM** (Enterprise patterns)

- **Current:** Cooldown (2 min), retry on IMAP/SMTP; LLM errors retried but no circuit breaker.
- **Improvement:** After N consecutive LLM failures, stop calling LLM for a configurable window; fall back to template or "I'm temporarily unable to reply." Prevents hammering a failing provider.

#### 4. **Observability** (LangSmith / Rasa X)

- **Current:** `jarvey_workflow.log`, `trace_jarvey_workflow.py`; `JARVEY_STRUCTURED_LOG=1` for JSON.
- **Improvement:** Add trace_id, intent, latency, token count to structured logs; optional export to observability tools (e.g. OpenTelemetry).

### Medium priority — enhancements

#### 5. **LLM provider abstraction** (LangChain-style)

- **Current:** `compose_reply` routes to OpenAI/Ollama/Anthropic; already unified.
- **Improvement:** Single `compose_reply(subject, body, backend="auto")` that picks provider from env. Easier to add Groq, Gemini, etc.

#### 6. **Human approval gates** (Modern email agents)

- **Current:** Jarvey sends directly; no draft review.
- **Improvement:** Optional `JARVEY_DRAFT_MODE=1` — compose reply but write to draft folder instead of sending; user reviews before send.

#### 7. **OAuth 2.0 for Gmail** (Modern email agents)

- **Current:** App password / IMAP/SMTP.
- **Improvement:** Gmail API with OAuth 2.0 for better token refresh, security, and scope control.

### Lower priority — nice-to-have

#### 8. **Confidence-based routing** (Rasa)

- **Current:** Template confidence threshold (0.7); no LLM confidence score.
- **Improvement:** When LLM path is used, optionally score confidence; if low, send clarification instead of guessing.

#### 9. **RAG expansion** (Already implemented; enhance)

- **Current:** sentence-transformers, local JSON index, configurable model.
- **Improvement:** Expand search intent keywords; add more indexed docs; optional OpenAI embeddings for higher quality.

---

## Summary: Top 3 Improvement Areas

| Rank | Area | Why | Effort |
|------|------|-----|--------|
| 1 | **Structured output / tool use** | Reduces hallucination; enables automation (e.g. "send digest" → execute action). Modern agents use this. | Medium |
| 2 | **Clarification flow** | When user says "something is broken", send a template asking for specifics instead of LLM guessing. | Low |
| 3 | **Circuit breaker for LLM** | Avoid hammering a failing provider; graceful degradation. | Low |

---

## Mock LLM Usage

For fast tests and simulations without Ollama/OpenAI:

```bash
# Compose with mock (no API)
python compose_reply.py "Re: OutOfRouteBuddy" "What are you capable of?" --mock

# Or set env
JARVEY_USE_MOCK_LLM=1 python compose_reply.py "Re: OutOfRouteBuddy" "What's next?"
```

Mock returns deterministic replies for: capability queries, roadmap/priorities, default fallback.

---

## References

- [LangChain vs Botpress](https://creati.ai/ai-tools/langchain-for-llm-application-development/alternatives/langchain-vs-botpress-comprehensive-guide-llm-application-development/)
- [Rasa + LangChain](https://rasa.com/blog/langchain-chatbot-example)
- [Botpress LLM interfaces](https://botpress.com/blog/botpress-interfaces-llms)
- [AI Email Automation 2024–2026](https://techtablepro.com/best-ai-agents-for-email-automation-2026/) — LAMs, agentic email
- [OpenAI: A practical guide to building agents](https://openai.com/business/guides-and-resources/a-practical-guide-to-building-ai-agents/)
- [Email Automation with RAG](https://articles.chatnexus.io/knowledge-base/email-automation-with-rag-intelligent-auto-responses-and-follow-ups/) — RAG + auto-responses
