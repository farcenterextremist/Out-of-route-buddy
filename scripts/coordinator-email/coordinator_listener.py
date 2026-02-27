#!/usr/bin/env python3
"""
Coordinator listener: open line of communication while Cursor is online.
Run once (e.g. in a Cursor terminal); when a new email from the user is detected,
compose a reply as the Master Branch Coordinator via LLM and send it.

Usage:
  python coordinator_listener.py   → run loop (check inbox every N min, respond as Coordinator)
  Ctrl+C to stop.

Requires: .env with SMTP/IMAP (as for other scripts) and one of:
  - COORDINATOR_LISTENER_OPENAI_API_KEY (or OPENAI_API_KEY), or
  - COORDINATOR_LISTENER_OLLAMA_URL (or OLLAMA_URL) for local LLM (e.g. http://localhost:11434).
Optional: COORDINATOR_LISTENER_INTERVAL_MINUTES (default 3), COORDINATOR_LISTENER_OLLAMA_MODEL (default llama3.2).

State: uses same last_responded_state.txt as check_and_respond.py so we don't double-respond.
"""

import os
import sys
import time
import signal
import json
import uuid
from datetime import datetime, timezone

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if os.getcwd() != SCRIPT_DIR:
    os.chdir(SCRIPT_DIR)

REPO_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))
COORDINATOR_INSTRUCTIONS_PATH = os.path.join(REPO_ROOT, "docs", "agents", "coordinator-instructions.md")

from responded_state import (
    load_last_responded_id,
    save_responded_id,
    last_sent_within_cooldown,
    write_sent_timestamp,
    replies_in_last_hour,
)

import re


def _strip_quoted_content(body: str) -> str:
    """
    Remove quoted/forwarded content from email body so we respond only to the user's new text.
    Handles both reply-above-quote (Gmail) and reply-below-quote (some clients).
    Supports Re: replies and forwarded emails (Gmail, Outlook, Apple Mail).
    """
    if not body or not body.strip():
        return body
    lines = body.split("\n")
    result = []
    quote_start = -1
    for i, line in enumerate(lines):
        stripped = line.strip()
        stripped_lower = stripped.lower()
        if re.match(r"^On\s+.+wrote\s*:$", stripped, re.IGNORECASE):
            quote_start = i
            break
        if re.match(r"^On\s+\w{3},?\s+\w{3}\s+\d{1,2},?\s+\d{4}\s+at\s+", stripped, re.IGNORECASE):
            quote_start = i
            break
        if "-----Original Message-----" in stripped or "----- forwarded message -----" in stripped_lower:
            quote_start = i
            break
        if "---------- forwarded message ---------" in stripped_lower or "begin forwarded message" in stripped_lower:
            quote_start = i
            break
        if re.match(r"^From:\s+.+Sent:\s+", stripped, re.IGNORECASE):
            quote_start = i
            break
        if re.match(r"^-{3,}\s*Original Message\s*-{3,}", stripped, re.IGNORECASE):
            quote_start = i
            break
        result.append(line)
    above = "\n".join(result).strip()
    if above and len(above) >= 3:
        return above
    # Reply below quote: extract non-quoted lines after the quote block
    if quote_start >= 0 and quote_start + 1 < len(lines):
        after = []
        for line in lines[quote_start + 1 :]:
            s = line.strip()
            if not s:
                continue
            if s.startswith(">") or re.match(r"^On\s+.+wrote", s, re.IGNORECASE):
                continue
            # Skip forward header lines (From: with email, Date: with weekday) so we don't treat forwarded metadata as user content
            if re.match(r"^From:\s+.+@\S+", s) or re.match(r"^Date:\s+\w{3},", s):
                continue
            after.append(line.rstrip())
        return "\n".join(after).strip()
    return above


def _ensure_jarvey_signoff(reply: str) -> str:
    """Ensure reply ends with '— Jarvey' or '— Jarvey, OutOfRouteBuddy Team'. Append if missing."""
    if not reply or not reply.strip():
        return reply
    r = reply.rstrip()
    tail = r[-50:]
    # Accept em dash (—), en dash (–), hyphen (-) before Jarvey
    if "— Jarvey" in tail or "—Jarvey" in tail:
        return reply
    if "– Jarvey" in tail or "–Jarvey" in tail:
        return reply
    if "- Jarvey" in tail or "-Jarvey" in tail:
        return reply
    return r + "\n\n— Jarvey"


# Optional: try openai then anthropic
def load_env():
    env_path = os.path.join(SCRIPT_DIR, ".env")
    if not os.path.isfile(env_path):
        return {}
    env = {}
    with open(env_path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line and not line.startswith("#") and "=" in line:
                k, v = line.split("=", 1)
                env[k.strip()] = v.strip().strip('"').strip("'")
    return env


def load_coordinator_system_prompt(subject: str = "", body: str = ""):
    """Load system prompt. Uses intent-aware context from context_loader (includes project index, SSOT; ROADMAP on-demand only)."""
    if not os.path.isfile(COORDINATOR_INSTRUCTIONS_PATH):
        bot_name = os.environ.get("COORDINATOR_BOT_NAME", "Jarvey")
        return (
            f"You are {bot_name}, the Master Branch Coordinator and Human-in-the-Loop Manager for OutOfRouteBuddy. "
            "You assign work to roles (Design, UI/UX, Front-end, Back-end, DevOps, QA, Security), "
            "resolve overlap, and send emails directly to the user when they need to be consulted or updated."
        )
    with open(COORDINATOR_INSTRUCTIONS_PATH, encoding="utf-8") as f:
        instructions = f.read()
    from context_loader import load_context_for_user_message, get_capability_options_text
    project_context = load_context_for_user_message(subject or "", body or "")
    inject_capability = os.environ.get("JARVEY_INJECT_CAPABILITY_OPTIONS", "").strip().lower() in ("1", "true", "yes")
    prompt = instructions
    if project_context:
        prompt += "\n\n---\n\nProject context (use this to give accurate, project-aware replies; reference when delegating or answering \"what's next\"):\n\n" + project_context
    if inject_capability:
        cap_opts = get_capability_options_text(limit=5)
        if cap_opts:
            prompt += f"\n\n---\n\nSuggested follow-ups (use when offering options): {cap_opts}. Format: 'Reply with 1, 2, or 3 to continue, or describe what you need.'"
    prompt += (
        "\n\n---\n\n"
        "HITL persona: You are the Human-in-the-Loop Manager. You ask questions, summarize, and relay—you do not decide or invent. "
        "Respond only to what the user wrote; do not invent or hallucinate (see JARVEY_INTENT_AND_GOALS). "
        "Only write paragraphs that directly answer what the user asked. Do not add extra paragraphs about roadmap, next steps, or recent work unless the user explicitly asked for them. "
        "Do not invent dates, phases, or timelines. If the answer is not in the provided context, say 'I don't have that in my context' or 'I'll follow up with the team and email you.' Do not guess (e.g. 'two weeks', 'Phase A/B'). "
        "For 'recent changes' or 'what changed': prefer the project timeline (curated phase completions). If the timeline is empty, say 'No curated timeline entries yet; I can summarize once the team adds phase completions.' Do NOT output raw git commit hashes unless the user explicitly asked for commit history. "
        "\n\n---\n\n"
        "CRITICAL: Respond ONLY to the user's message below. Do NOT respond to quoted/forwarded content from earlier emails (e.g. \"On ... wrote:\" blocks). "
        "The body you receive has already been stripped of quoted content—treat it as the sole source of what the user is asking. "
        "TASK: The user has sent the following email. Reply as Jarvey (the Master Branch Coordinator). "
        "Before answering, explicitly acknowledge what the user asked. Example: 'You asked about recent project changes.' or 'You asked when the reports screen will be done.' Wrong: starting with a generic 'Here's an update' without reflecting their question. "
        "Your first sentence MUST restate or quote the user's question or request. Do not answer a different question than the one they asked. "
        "When the user asks multiple questions (e.g. 'prioritize X and when will it be done?'), answer each in order with bullets or numbered items. Do not skip any question. "
        "Do not use a generic \"Thanks for your email\" without addressing their point. "
        "If the subject and body are truly empty or illegible, reply with: \"I got your message but couldn't make out what you need. Can you reply with a short question or request?\" "
        "Do NOT use that phrase when the user has given a clear request (e.g. 'recent updates', 'send me a summary', 'what's new', 'tell me about X', 'tell me something', 'tell me anything', 'share something', 'give me an update', 'what can you tell me?', 'anything new?', 'catch me up', 'fill me in', 'what's happening?', 'what's going on?', 'quick summary', 'what's the latest?', 'update me', 'news?', 'status?', 'send me a new email about X', 'create an email about recent updates'). Those are clear—answer them. "
        "When the user replies with ONLY a single number 1–10, treat it as their choice from the capability menu we sent: 1=Roadmap status and next steps, 2=Recent project changes, 3=Prioritize a feature or task, 4=Where is X defined? (code/docs), 5=Trip recovery/persistence, 6=App version/build info, 7=Assign work to the team, 8=Save a note or decision, 9=Send update to coworker/family, 10=Something else. Answer accordingly (e.g. for 6 give app version and build info). Do NOT reply with 'couldn't make out' or ask for clarification for a single number 1–10. "
        "When user asks for new email about X, your reply IS that email. Do not reply with clarification. "
        "If the user's message is empty or only a single word (e.g. 'testing'), treat it as a check-in and reply that you're here and ready for their questions. "
        "When the user says 'Tell me something', 'Tell me anything', 'Share something', 'Anything new?', 'Catch me up', 'Fill me in', 'What's happening?', 'What's going on?', 'Quick summary', 'What's the latest?', 'Update me', 'News?', 'Status?', or similar check-in phrases, share a brief project update (roadmap status, recent changes, or next steps). Do NOT reply with 'couldn't make out' or ask for clarification. "
        "If the user asks for 'a new email' or 'send me an email about X', use a fitting subject (e.g. 'Recent Updates to OutOfRouteBuddy') and compose the body. Your reply IS that email. "
        "If the user's message is unclear or vague (e.g. 'something is broken' with no detail), either ask one short clarifying question or say you'll follow up and suggest they share a bit more detail; do not invent specifics they did not provide. "
        "Your reply MUST directly answer each specific question or request the user wrote. Do not send a generic acknowledgment only—address every point they raised. "
        "Do not add a second (or later) paragraph about roadmap, priorities, recent work, or next steps unless the user explicitly asked for that. "
        "If the user asked one thing, answer that one thing. Do not pad the reply with unrelated context. "
        "For out-of-scope requests (weather, jokes, politics): politely decline and redirect to the project. "
        "For frustrated users: lead with empathy, then ask for specifics. "
        "For 'when will X be done' with no timeline in context: offer to ask the team and email an estimate. "
        "For policy conflicts (request contradicts SSOT): flag for user decision; draft tradeoffs. "
        "Structure your reply: (1) Acknowledge: one short sentence that reflects back their question or request. (2) Answer: address each question or point in order; use short bullets or numbered items if they asked multiple things. (3) Next steps / handoff: who will do what, or that you will email them; then sign as \"— Jarvey\" or \"— Jarvey, OutOfRouteBuddy Team\". "
        "Response style: For 'recent', 'version', 'roadmap'—use short bullets (3-5 items) for quick scan. For multiple questions—use numbered sections. For unclear or vague—lead with empathy, then one clarifying question. For search or 'where is X'—prose plus path/file reference. "
        "Write only the email reply body (2-4 short paragraphs or equivalent with bullets). Do not implement code—only write the email reply. "
        "If the user asks for code, functions, or implementation: reply that you will assign to the Back-end Engineer (or appropriate role). Do not write any code. Use a Re: subject that fits the thread when sending. "
        "When the user asks to send to coworker or family, use the configured address. You can only send to configured recipients (COORDINATOR_EMAIL_TO, COORDINATOR_EMAIL_COWORKER, COORDINATOR_EMAIL_FAMILY). "
        "Optional structured output: When the user asks for a weekly digest or similar, you may respond with a JSON block: {\"action\": \"send_digest\", \"params\": {}}. When the message is unclear, you may respond with {\"action\": \"clarify\", \"params\": {\"options\": [\"a\", \"b\", \"c\"]}}. When the user asks to save a note or their email contains information worth saving (decisions, priorities, feedback), add {\"action\": \"save_note\", \"params\": {\"note\": \"<text to save>\", \"topic\": \"<optional topic>\"}} to your reply. When the user asks to assign work, add {\"action\": \"assign_work\", \"params\": {\"role\": \"<role>\", \"task\": \"<task>\", \"context\": \"<optional>\"}}. When adding a timeline entry, add {\"action\": \"add_to_timeline\", \"params\": {\"entry\": \"<title>\", \"phase\": \"<optional>\"}}. When offering user-initiated options, add {\"action\": \"offer_options\", \"params\": {\"options\": [\"opt1\", \"opt2\"]}}. Notes and assignments are appended to docs/agents/EMAIL_NOTES.md. If you use structured output, put the JSON block in your reply; the coordinator will execute it. "
        "When appropriate, end your reply with short multiple-choice options (e.g. '1. Roadmap 2. Recent changes 3. Assign work') so the user can reply with a number. Keep options to 3-5 unless the user asked for many. Format: 'Reply with 1, 2, or 3 to continue, or describe what you need.'"
    )
    return prompt


def _resolve_recipient_for_send(body: str, env: dict) -> str | None:
    """If user asked to send to coworker or family, return the configured address. Else None."""
    combined = (body or "").lower()
    if "coworker" in combined:
        addr = env.get("COORDINATOR_EMAIL_COWORKER", "").strip()
        if addr:
            return addr
    if "family" in combined:
        addr = env.get("COORDINATOR_EMAIL_FAMILY", "").strip()
        if addr:
            return addr.split(",")[0].strip()
    return None


def compose_reply_openai(
    subject: str,
    body: str,
    api_key: str,
    timeout: int = 60,
    base_url: str | None = None,
    model: str | None = None,
) -> str:
    try:
        from openai import OpenAI
    except ImportError:
        raise RuntimeError(
            "Coordinator listener needs the openai package. Run: pip install openai"
        )
    # When using a local OpenAI-compatible server (vLLM, LM Studio), base_url is set
    # and many servers accept any API key
    effective_key = api_key if not base_url else (api_key or "sk-not-needed")
    client_kwargs: dict = {"api_key": effective_key, "timeout": timeout}
    if base_url:
        client_kwargs["base_url"] = base_url.rstrip("/")
    client = OpenAI(**client_kwargs)
    system = load_coordinator_system_prompt(subject=subject, body=body)
    user_msg = f"Subject: {subject}\n\nBody:\n{body}"
    response = client.chat.completions.create(
        model=model or "gpt-4o-mini",
        messages=[
            {"role": "system", "content": system},
            {"role": "user", "content": user_msg},
        ],
        max_tokens=1024,
    )
    reply = (response.choices[0].message.content or "").strip()
    if not reply:
        raise RuntimeError("LLM returned empty reply")
    return reply


def compose_reply_ollama(
    base_url: str,
    model: str,
    subject: str,
    body: str,
    timeout: int = 120,
    options: dict | None = None,
    stream: bool = False,
) -> str:
    """Compose reply using local Ollama (no API key). base_url e.g. http://localhost:11434."""
    import json
    import urllib.request
    import urllib.error

    system = load_coordinator_system_prompt(subject=subject, body=body)
    user_msg = f"Subject: {subject}\n\nBody:\n{body}"
    url = base_url.rstrip("/") + "/api/chat"
    payload = {
        "model": model,
        "messages": [
            {"role": "system", "content": system},
            {"role": "user", "content": user_msg},
        ],
        "stream": stream,
    }
    if options:
        payload["options"] = options
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        url,
        data=data,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            if stream:
                reply_parts = []
                while True:
                    line = resp.readline()
                    if not line:
                        break
                    try:
                        line_str = line.decode("utf-8").strip()
                    except UnicodeDecodeError:
                        continue
                    if not line_str:
                        continue
                    try:
                        obj = json.loads(line_str)
                    except json.JSONDecodeError:
                        continue
                    if obj.get("response"):
                        reply_parts.append(obj["response"])
                    if obj.get("done"):
                        break
                reply = "".join(reply_parts).strip()
            else:
                out = json.loads(resp.read().decode("utf-8"))
                reply = (out.get("message") or {}).get("content") or ""
                reply = reply.strip()
    except urllib.error.URLError as e:
        raise RuntimeError(f"Ollama request failed (is Ollama running?): {e}") from e
    if not reply:
        raise RuntimeError("Ollama returned empty reply")
    return reply


def compose_reply_transformers(
    subject: str,
    body: str,
    model: str,
    timeout: int = 120,
    device: str | None = None,
) -> str:
    """Compose reply using local transformers (e.g. SmolLM). No API key needed."""
    try:
        from transformers import AutoTokenizer, AutoModelForCausalLM
    except ImportError:
        raise RuntimeError(
            "Transformers backend requires: pip install transformers accelerate torch. "
            "Or use COORDINATOR_LISTENER_LLM_BACKEND=openai, ollama, or anthropic."
        )
    system = load_coordinator_system_prompt(subject=subject, body=body)
    user_msg = f"Subject: {subject}\n\nBody:\n{body}"
    messages = [
        {"role": "system", "content": system},
        {"role": "user", "content": user_msg},
    ]
    tokenizer = AutoTokenizer.from_pretrained(model, trust_remote_code=True)
    causal_model = AutoModelForCausalLM.from_pretrained(model, trust_remote_code=True)
    if device:
        dev = device
    else:
        try:
            import torch
            dev = "cuda" if torch.cuda.is_available() else "cpu"
        except ImportError:
            dev = "cpu"
    causal_model = causal_model.to(dev)
    prompt = tokenizer.apply_chat_template(
        messages,
        tokenize=False,
        add_generation_prompt=True,
    )
    inputs = tokenizer(prompt, return_tensors="pt").to(dev)
    gen = causal_model.generate(
        **inputs,
        max_new_tokens=1024,
        do_sample=True,
        temperature=0.7,
        pad_token_id=tokenizer.eos_token_id,
    )
    # Decode only the generated tokens (exclude prompt)
    input_len = inputs["input_ids"].shape[1]
    output_ids = gen[0][input_len:]
    reply = tokenizer.decode(output_ids, skip_special_tokens=True).strip()
    if not reply:
        raise RuntimeError("Transformers returned empty reply")
    return reply


def compose_reply_anthropic(subject: str, body: str, api_key: str, timeout: int = 60) -> str:
    """Compose reply using Anthropic API. Stub: returns placeholder if anthropic package not installed."""
    try:
        from anthropic import Anthropic
    except ImportError:
        raise RuntimeError(
            "Anthropic backend requires: pip install anthropic. "
            "Or use COORDINATOR_LISTENER_LLM_BACKEND=openai or ollama."
        )
    client = Anthropic(api_key=api_key, timeout=timeout)
    system = load_coordinator_system_prompt(subject=subject, body=body)
    user_msg = f"Subject: {subject}\n\nBody:\n{body}"
    response = client.messages.create(
        model="claude-3-haiku-20240307",
        max_tokens=1024,
        system=system,
        messages=[{"role": "user", "content": user_msg}],
    )
    reply = ""
    for block in response.content:
        if hasattr(block, "text"):
            reply += block.text
    reply = reply.strip()
    if not reply:
        raise RuntimeError("Anthropic returned empty reply")
    return reply


_SIMPLE_INTENTS = frozenset({"recent", "version", "roadmap"})


def _prewarm_ollama(base_url: str, model: str, timeout: int = 90) -> None:
    """
    Fire a tiny request to Ollama to load the model into RAM before the first real email.
    Reduces cold-start delay when the first message arrives. Optional via JARVEY_PREWARM_OLLAMA=1.
    """
    import urllib.request
    import urllib.error

    url = base_url.rstrip("/") + "/api/chat"
    payload = json.dumps({
        "model": model,
        "messages": [{"role": "user", "content": "Hi"}],
        "stream": False,
    }).encode("utf-8")
    req = urllib.request.Request(url, data=payload, headers={"Content-Type": "application/json"}, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            _ = json.loads(resp.read().decode("utf-8"))
    except Exception:
        pass  # Non-fatal; first real request will still trigger load


def compose_reply(subject: str, body: str, env: dict, intents: list[str] | None = None) -> str:
    """
    Compose a reply using the configured LLM backend. Routes to OpenAI, Ollama, or Anthropic
    based on COORDINATOR_LISTENER_LLM_BACKEND (auto|openai|ollama|anthropic).
    When intents is provided and COORDINATOR_LISTENER_OLLAMA_FAST_MODEL is set, uses fast model
    for single simple intents (recent, version, roadmap).
    """
    backend = (env.get("COORDINATOR_LISTENER_LLM_BACKEND") or "auto").strip().lower()
    api_key = env.get("COORDINATOR_LISTENER_OPENAI_API_KEY") or env.get("OPENAI_API_KEY")
    ollama_url = env.get("COORDINATOR_LISTENER_OLLAMA_URL") or env.get("OLLAMA_URL")
    anthropic_key = env.get("ANTHROPIC_API_KEY") or env.get("COORDINATOR_LISTENER_ANTHROPIC_API_KEY")

    if backend == "auto":
        if api_key:
            backend = "openai"
        elif ollama_url:
            backend = "ollama"
        elif anthropic_key:
            backend = "anthropic"
        else:
            raise RuntimeError(
                "No LLM configured. Set COORDINATOR_LISTENER_OPENAI_API_KEY, "
                "COORDINATOR_LISTENER_OLLAMA_URL, ANTHROPIC_API_KEY, or COORDINATOR_LISTENER_LLM_BACKEND=transformers in .env."
            )

    openai_timeout = 90
    ollama_timeout = 900  # 15 min default; Ollama can be slow with large context
    try:
        t = env.get("COORDINATOR_LISTENER_OPENAI_TIMEOUT")
        if t:
            openai_timeout = int(t)
    except ValueError:
        pass
    try:
        t = env.get("COORDINATOR_LISTENER_OLLAMA_TIMEOUT")
        if t:
            ollama_timeout = int(t)
    except ValueError:
        pass

    if backend == "openai":
        openai_base = env.get("COORDINATOR_LISTENER_OPENAI_BASE_URL", "").strip() or None
        openai_model = env.get("COORDINATOR_LISTENER_OPENAI_MODEL", "").strip() or None
        # Local servers (vLLM, LM Studio) may accept any key when base_url is set
        if not api_key and not openai_base:
            raise RuntimeError("COORDINATOR_LISTENER_LLM_BACKEND=openai but no OPENAI_API_KEY set.")
        return compose_reply_openai(
            subject, body, api_key or "", timeout=openai_timeout,
            base_url=openai_base, model=openai_model,
        )
    if backend == "ollama":
        base_url = ollama_url or "http://localhost:11434"
        model = env.get("COORDINATOR_LISTENER_OLLAMA_MODEL", "llama3.2")
        fast_model = env.get("COORDINATOR_LISTENER_OLLAMA_FAST_MODEL", "").strip() or None
        if fast_model and intents and len(intents) == 1 and intents[0] in _SIMPLE_INTENTS:
            model = fast_model
        options = {}
        temp = env.get("COORDINATOR_LISTENER_OLLAMA_TEMPERATURE", "")
        if temp:
            try:
                options["temperature"] = float(temp)
            except ValueError:
                pass
        max_tok = env.get("COORDINATOR_LISTENER_OLLAMA_MAX_TOKENS", "")
        if max_tok:
            try:
                options["num_predict"] = int(max_tok)
            except ValueError:
                pass
        use_stream = env.get("JARVEY_OLLAMA_STREAMING", "").strip().lower() in ("1", "true", "yes")
        return compose_reply_ollama(
            base_url, model, subject, body,
            timeout=ollama_timeout,
            options=options if options else None,
            stream=use_stream,
        )
    if backend == "anthropic":
        if not anthropic_key:
            raise RuntimeError("COORDINATOR_LISTENER_LLM_BACKEND=anthropic but no ANTHROPIC_API_KEY set.")
        return compose_reply_anthropic(subject, body, anthropic_key, timeout=openai_timeout)
    if backend == "transformers":
        model = env.get("COORDINATOR_LISTENER_TRANSFORMERS_MODEL", "HuggingFaceTB/SmolLM2-1.7B-Instruct").strip()
        device = env.get("COORDINATOR_LISTENER_TRANSFORMERS_DEVICE", "").strip() or None
        transformers_timeout = openai_timeout
        try:
            t = env.get("COORDINATOR_LISTENER_TRANSFORMERS_TIMEOUT")
            if t:
                transformers_timeout = int(t)
        except ValueError:
            pass
        return compose_reply_transformers(subject, body, model, timeout=transformers_timeout, device=device)
    raise RuntimeError(f"Unknown COORDINATOR_LISTENER_LLM_BACKEND: {backend}. Use auto|openai|ollama|anthropic|transformers.")


def _log_structured(event: str, trace_id=None, intent=None, latency_ms=None, template_key=None, message_id=None, **extra):
    """Write one JSON line to jarvey_workflow.log when JARVEY_STRUCTURED_LOG=1 or JARVEY_LOG=structured."""
    try:
        from jarvey_log import log_structured
        log_structured(
            event, trace_id=trace_id, intent=intent, latency_ms=latency_ms,
            template_key=template_key, message_id=message_id, **extra,
        )
    except ImportError:
        pass


def run_once(env: dict, log_fn=None):
    """Run one poll cycle. log_fn(msg) is called for workflow logging when provided."""
    from read_replies import read_replies
    from send_email import send

    api_key = env.get("COORDINATOR_LISTENER_OPENAI_API_KEY") or env.get("OPENAI_API_KEY")
    ollama_url = env.get("COORDINATOR_LISTENER_OLLAMA_URL") or env.get("OLLAMA_URL")
    anthropic_key = env.get("ANTHROPIC_API_KEY") or env.get("COORDINATOR_LISTENER_ANTHROPIC_API_KEY")
    trace_id = str(uuid.uuid4())

    def _log(msg):
        if log_fn:
            log_fn(msg)

    try:
        subject, body, date, message_id = read_replies()
    except Exception as e:
        err_msg = str(e).lower()
        hint = ""
        if "authenticationfailed" in err_msg or "invalid credentials" in err_msg:
            hint = (
                " — Check COORDINATOR_IMAP_USER/IMAP_PASSWORD (or SMTP_USER/SMTP_PASSWORD) in .env; "
                "use an app password if using Gmail. Ensure IMAP is enabled for the account."
            )
        _log(f"Error reading inbox: {e}{hint}")
        print(f"Error reading inbox: {e}{hint}", file=sys.stderr)
        return

    # Strip quoted/forwarded content so we respond only to the user's actual message
    user_body = _strip_quoted_content(body or "")
    subj_trim = (subject or "").strip()
    body_trim = user_body.strip()
    if not subj_trim and not body_trim:
        return

    # When user replies with only a number 1–10, treat as capability menu choice and expand for the LLM
    body_for_compose = body_trim
    _capability_digits = ("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
    if body_trim.strip() in _capability_digits:
        try:
            from context_loader import get_capability_label_for_number
            label = get_capability_label_for_number(body_trim.strip())
            if label:
                body_for_compose = f"User chose option {body_trim.strip()}: {label}"
                _log(f"Capability number expanded: {body_trim.strip()} -> {label!r}")
        except Exception:
            pass

    _log(f"Found: subject={subj_trim[:60]!r} body={body_trim[:100]!r}...")
    _log_structured("message_found", trace_id=trace_id, subject=subj_trim[:60], body_snippet=body_trim[:100])

    # Debug: log what we're passing to the LLM (first 200 chars)
    if body_trim:
        snippet = body_trim[:200].replace("\n", " ")
        if len(body_trim) > 200:
            snippet += "..."
        print(f"User message (stripped): subject={subj_trim[:60]!r} body={snippet!r}", file=sys.stderr)

    # Dedupe: use message_id when present; otherwise fallback to content hash so we don't reply every loop
    dedupe_id = message_id
    if not dedupe_id:
        import hashlib
        blob = (subj_trim or "") + "\n" + (body_trim or "")[:500]
        dedupe_id = "hash:" + hashlib.sha256(blob.encode("utf-8", errors="replace")).hexdigest()[:32]
        print(f"Dedupe: no Message-ID, using content hash (subject: {subj_trim[:50]!r}, body: {(body_trim[:50])!r})", file=sys.stderr)

    last_id = load_last_responded_id()
    # Normalize for comparison: Message-IDs may have angle brackets; state may have been saved with or without
    def _norm_id(x):
        return (x or "").strip().strip("<>") if x else ""
    if last_id and _norm_id(dedupe_id) and _norm_id(dedupe_id) == _norm_id(last_id):
        _log("Dedupe: already responded to this message. Skipping.")
        print("Dedupe: already responded to this message. Skipping.", file=sys.stderr)
        return

    # Hard rate limit: never send more than once per COOLDOWN_SECONDS (e.g. 2 min)
    if last_sent_within_cooldown():
        _log("Cooldown: last send < 2 min ago. Skipping.")
        print("Cooldown: last send < 2 min ago. Skipping.", file=sys.stderr)
        return

    # Optional per-hour cap (e.g. COORDINATOR_LISTENER_MAX_REPLIES_PER_HOUR=6)
    max_per_hour = env.get("COORDINATOR_LISTENER_MAX_REPLIES_PER_HOUR")
    if max_per_hour:
        try:
            cap = int(max_per_hour)
            if replies_in_last_hour() >= cap:
                return
        except ValueError:
            pass

    # All replies go through LLM (no preset templates) — model gets full context to answer project questions
    # Circuit breaker: skip LLM if too many recent failures
    try:
        from llm_backoff import is_circuit_open
        if is_circuit_open():
            _log("Circuit open: skipping LLM (too many recent failures).")
            print("Circuit open: skipping LLM (too many recent failures).", file=sys.stderr)
            return
    except Exception:
        pass

    # Clarification flow: if message is unclear (short/vague), use template instead of LLM
    clarification_enabled = (env.get("JARVEY_CLARIFICATION_TEMPLATE") or "1").strip().lower() not in ("0", "false", "no")
    reply_body = None
    clarification_used = False
    t0 = time.time()
    backend_display = "LLM"
    model = ""

    if clarification_enabled:
        try:
            from template_registry import choose_response_with_confidence, _get_confidence_threshold
            _, template_body, template_key, conf = choose_response_with_confidence(subj_trim, body_trim)
            if template_key == "unclear" and conf >= _get_confidence_threshold():
                reply_body = template_body
                clarification_used = True
                backend_display = "Template"
                model = "unclear"
                _log("Clarification template: using unclear template (skipping LLM).")
                print("Clarification template: using unclear template (skipping LLM).", file=sys.stderr)
        except Exception:
            pass

    # Compose reply as Coordinator via compose_reply (routes to OpenAI/Ollama/Anthropic)
    from context_loader import detect_intents
    intents = detect_intents(subject or "", body_for_compose or "")
    fallback_used = False
    last_compose_error = None

    if not clarification_used:
        backend_raw = (env.get("COORDINATOR_LISTENER_LLM_BACKEND") or "auto").strip().lower()
        if backend_raw == "auto":
            backend_raw = "openai" if api_key else ("ollama" if ollama_url else ("anthropic" if anthropic_key else "ollama"))
        backend_display = backend_raw.capitalize()
        if backend_raw == "openai":
            model = env.get("COORDINATOR_LISTENER_OPENAI_MODEL") or "gpt-4o-mini"
        elif backend_raw == "ollama":
            model = env.get("COORDINATOR_LISTENER_OLLAMA_MODEL", "llama3.2")
        elif backend_raw == "transformers":
            model = env.get("COORDINATOR_LISTENER_TRANSFORMERS_MODEL", "HuggingFaceTB/SmolLM2-1.7B-Instruct")
        else:
            model = "claude-3-haiku-20240307"
        if backend_raw == "ollama":
            fast_model = env.get("COORDINATOR_LISTENER_OLLAMA_FAST_MODEL", "").strip() or None
            if fast_model and intents and len(intents) == 1 and intents[0] in _SIMPLE_INTENTS:
                model = fast_model
        _log(f"LLM path: composing via {backend_display}...")
        print(f"LLM path: composing via {backend_display}...", file=sys.stderr)

        _MAX_COMPOSE_RETRIES = 3
        _RETRY_DELAY_SECONDS = 30
        _TRANSIENT_PATTERNS = ("timed out", "timeout", "connection", "refused", "reset")

        for attempt in range(_MAX_COMPOSE_RETRIES):
            try:
                reply_body = compose_reply(subject or "", body_for_compose or "", env, intents=intents)
                break
            except Exception as e:
                last_compose_error = e
                err_str = str(e).lower()
                is_transient = any(p in err_str for p in _TRANSIENT_PATTERNS)
                if is_transient and attempt < _MAX_COMPOSE_RETRIES - 1:
                    _log(f"Retry {attempt + 1}/{_MAX_COMPOSE_RETRIES}: {e}. Retrying in {_RETRY_DELAY_SECONDS}s...")
                    print(f"Retry {attempt + 1}/{_MAX_COMPOSE_RETRIES}: {e}. Retrying in {_RETRY_DELAY_SECONDS}s...", file=sys.stderr)
                    time.sleep(_RETRY_DELAY_SECONDS)
                    continue
                break

        if reply_body is None and last_compose_error is not None:
            e = last_compose_error
            # Fallback: if Ollama failed and OpenAI key is set, retry with OpenAI
            fallback_enabled = env.get("JARVEY_OLLAMA_FALLBACK_TO_OPENAI", "").strip().lower() in ("1", "true", "yes")
            if backend_raw == "ollama" and fallback_enabled and api_key:
                _log("Ollama failed, retrying with OpenAI...")
                print("Ollama failed, retrying with OpenAI...", file=sys.stderr)
                try:
                    openai_timeout = 90
                    try:
                        t = env.get("COORDINATOR_LISTENER_OPENAI_TIMEOUT")
                        if t:
                            openai_timeout = int(t)
                    except ValueError:
                        pass
                    openai_base = env.get("COORDINATOR_LISTENER_OPENAI_BASE_URL", "").strip() or None
                    openai_model = env.get("COORDINATOR_LISTENER_OPENAI_MODEL", "").strip() or None
                    reply_body = compose_reply_openai(
                        subject or "", body_for_compose or "", api_key,
                        timeout=openai_timeout,
                        base_url=openai_base, model=openai_model,
                    )
                    backend_display = "OpenAI"
                    model = openai_model or "gpt-4o-mini"
                    fallback_used = True
                except Exception as fallback_err:
                    _log(f"OpenAI fallback also failed: {fallback_err}")
                    print(f"OpenAI fallback also failed: {fallback_err}", file=sys.stderr)
            if reply_body is None:
                try:
                    from llm_backoff import record_failure
                    record_failure()
                except Exception:
                    pass
                _log(f"Error composing reply: {e}")
                print(f"Error composing reply: {e}", file=sys.stderr)
                if "timed out" in str(e).lower():
                    print("Tip: Ollama can be slow. Set COORDINATOR_LISTENER_OLLAMA_TIMEOUT=900 or 1200 in .env for 15–20 min, or use OpenAI.", file=sys.stderr)
                return

        try:
            from llm_backoff import record_success
            record_success()
        except Exception:
            pass

    latency_ms = (time.time() - t0) * 1000
    token_estimate = len(reply_body or "") // 4 if reply_body else 0
    _log_structured(
        "compose_done", trace_id=trace_id, latency_ms=latency_ms, backend=backend_display, model=model,
        token_estimate=token_estimate,
    )

    # Structured output: if LLM returned JSON action, execute it
    if reply_body and not clarification_used:
        try:
            from structured_output import parse_structured_reply, execute_action, strip_structured_from_reply
            parsed = parse_structured_reply(reply_body)
            if parsed:
                # Do not replace reply with clarify when user sent a capability number (1–10)
                user_sent_number = (body_trim or "").strip() in ("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
                if parsed["action"] == "clarify" and user_sent_number:
                    reply_body = strip_structured_from_reply(reply_body) or reply_body  # keep LLM text, strip JSON
                    # If LLM returned only JSON (no prose), build a direct response for that option
                    if not (reply_body or "").strip():
                        try:
                            from context_loader import (
                                get_version_summary,
                                get_roadmap_summary,
                                get_project_timeline_curated,
                                get_capability_label_for_number,
                            )
                            n = (body_trim or "").strip()
                            if n == "6":
                                ver = get_version_summary()
                                reply_body = f"Hi,\n\nYou asked for app version / build info.\n\n{ver or 'Version info not available.'}\n\n— Jarvey"
                            elif n == "1":
                                road = get_roadmap_summary(600)
                                reply_body = f"Hi,\n\nYou asked for roadmap status and next steps.\n\n{road}\n\n— Jarvey"
                            elif n == "2":
                                timeline = get_project_timeline_curated(cap_chars=800)
                                reply_body = f"Hi,\n\nYou asked for recent project changes.\n\n{timeline or 'No curated timeline entries yet.'}\n\n— Jarvey"
                            else:
                                label = get_capability_label_for_number(n)
                                reply_body = f"Hi,\n\nYou chose option {n}: {label or 'Unknown option'}.\n\nI've noted it. Reply with more detail if you'd like, or pick another option (1–10).\n\n— Jarvey"
                        except Exception:
                            reply_body = "Hi,\n\nGot your choice. Reply with a bit more detail if you'd like.\n\n— Jarvey"
                else:
                    action_body = execute_action(parsed["action"], parsed.get("params") or {}, env)
                    if parsed["action"] in ("save_note", "assign_work", "add_to_timeline"):
                        reply_body = strip_structured_from_reply(reply_body)
                    elif action_body:
                        reply_body = action_body
        except Exception:
            pass

    reply_body = _ensure_jarvey_signoff(reply_body)

    # Re: subject - keep thread
    s = subject or ""
    reply_subj = s if s.strip().lower().startswith("re:") else f"Re: {s}"

    to_addr = _resolve_recipient_for_send(body_trim, env)
    try:
        send(reply_subj, reply_body, to_addr=to_addr)
        save_responded_id(dedupe_id)  # always save so we don't reply again to same message
        write_sent_timestamp()  # hard cooldown so we never send more than once per 2 min
        if env.get("JARVEY_CONVERSATION_MEMORY", "").strip().lower() in ("1", "true", "yes"):
            try:
                from conversation_memory import append_exchange, get_thread_id
                thread_id = get_thread_id(subject or "", message_id)
                append_exchange(thread_id, body_trim or "", reply_body)
            except Exception:
                pass
        try:
            from jarvey_choices import log_user_choice
            from conversation_memory import get_thread_id
            tid = get_thread_id(subject or "", message_id)
            log_user_choice(body_trim or "", body_trim or "", tid)
        except Exception:
            pass
        elapsed_sec = (time.time() - t0)
        _log_structured(
            "sent", trace_id=trace_id, backend=backend_display, model=model,
            template_key="llm" if not clarification_used else "unclear", message_id=dedupe_id,
            intent=",".join(intents) if intents else None, latency_ms=latency_ms,
            token_estimate=token_estimate,
        )
        _log(f"SENT (LLM: {backend_display}) in {elapsed_sec:.1f}s")
        print(f"Coordinator reply sent in {elapsed_sec:.1f}s. Responded as Coordinator ({backend_display}).")
    except Exception as e:
        _log(f"Failed to send: {e}")
        print(f"Failed to send: {e}", file=sys.stderr)


def main():
    from config_schema import validate_config
    env = validate_config(mode="listener", exit_on_error=True)
    api_key = env.get("COORDINATOR_LISTENER_OPENAI_API_KEY") or env.get("OPENAI_API_KEY")
    ollama_url = env.get("COORDINATOR_LISTENER_OLLAMA_URL") or env.get("OLLAMA_URL")
    anthropic_key = env.get("ANTHROPIC_API_KEY") or env.get("COORDINATOR_LISTENER_ANTHROPIC_API_KEY")
    transformers_backend = (env.get("COORDINATOR_LISTENER_LLM_BACKEND") or "").strip().lower() == "transformers"
    if not api_key and not ollama_url and not anthropic_key and not transformers_backend:
        print(
            "No LLM configured. Set one of:\n"
            "  - COORDINATOR_LISTENER_OPENAI_API_KEY (or OPENAI_API_KEY) in .env, or\n"
            "  - COORDINATOR_LISTENER_OLLAMA_URL (e.g. http://localhost:11434) for local Ollama, or\n"
            "  - COORDINATOR_LISTENER_LLM_BACKEND=transformers for local SmolLM.\n"
            "See docs/agents/COORDINATOR_EMAIL_LISTENER_RUNBOOK.md for a no-API-key Cursor-only option.",
            file=sys.stderr,
        )
        sys.exit(1)
    interval_min = float(env.get("COORDINATOR_LISTENER_INTERVAL_MINUTES", "3"))
    if interval_min < 1.0:
        interval_min = 1.0  # minimum 1 minute
    interval_sec = interval_min * 60

    running = True
    if float(env.get("COORDINATOR_LISTENER_INTERVAL_MINUTES", "3")) < 1.0:
        print("(Interval capped at 1 minute.)", file=sys.stderr)

    def on_sig(signum, frame):
        nonlocal running
        running = False

    try:
        signal.signal(signal.SIGINT, on_sig)
        signal.signal(signal.SIGTERM, on_sig)
    except AttributeError:
        pass

    # Optional: log workflow to file (--log or JARVEY_LOG=1)
    log_path = None
    if "--log" in sys.argv or (env.get("JARVEY_LOG", "").strip().lower() in ("1", "true", "yes")):
        log_path = os.path.join(SCRIPT_DIR, "jarvey_workflow.log")

    def _log(msg: str):
        ts = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        line = f"[{ts}] {msg}"
        print(line, file=sys.stderr)
        if log_path:
            try:
                with open(log_path, "a", encoding="utf-8") as f:
                    f.write(line + "\n")
            except OSError:
                pass

    if log_path:
        _log("Jarvey logging to " + log_path)

    # Optional: pre-warm Ollama so first email doesn't wait for model load (cold start)
    backend_at_start = "openai" if api_key else ("ollama" if ollama_url else ("anthropic" if anthropic_key else "ollama"))
    if backend_at_start == "ollama" and env.get("JARVEY_PREWARM_OLLAMA", "").strip().lower() in ("1", "true", "yes"):
        base_url = ollama_url or "http://localhost:11434"
        model = env.get("COORDINATOR_LISTENER_OLLAMA_MODEL", "llama3.2")
        prewarm_timeout = 90
        try:
            t = env.get("COORDINATOR_LISTENER_OLLAMA_TIMEOUT")
            if t:
                prewarm_timeout = min(90, int(t))
        except ValueError:
            pass
        print("Pre-warming Ollama (loading model into RAM)...", file=sys.stderr)
        try:
            _prewarm_ollama(base_url, model, timeout=prewarm_timeout)
            print("Ollama pre-warm done.", file=sys.stderr)
        except Exception as e:
            print(f"Pre-warm skipped: {e}", file=sys.stderr)

    # Send capability menu opener on startup (default: enabled; set JARVEY_SEND_OPENER_ON_STARTUP=0 to disable)
    _opener_disabled = env.get("JARVEY_SEND_OPENER_ON_STARTUP", "").strip().lower() in ("0", "false", "no")
    if not _opener_disabled:
        try:
            from send_opener import send_capability_opener
            if send_capability_opener(env):
                print("Capability opener email sent.", file=sys.stderr)
            else:
                print("Capability opener skipped (send failed).", file=sys.stderr)
        except Exception as e:
            print(f"Capability opener skipped: {e}", file=sys.stderr)

    # Jarvey avatar (robot/assistant face) in terminal
    try:
        from jarvey_face import print_face, animate_startup
        if os.environ.get("JARVEY_ANIMATE_FACE", "").lower() in ("1", "true", "yes"):
            animate_startup(stream=sys.stderr, duration=1.2)
        else:
            print_face(stream=sys.stderr)
    except ImportError:
        _JARVEY_FACE = """
  +-----------------+
  |  ( o )   ( o )  |
  |      \\___/      |
  |   Jarvey       |
  +-----------------+
"""
        print(_JARVEY_FACE.strip(), file=sys.stderr)
    print("Coordinator listener started. Checking inbox every", interval_min, "minute(s). Ctrl+C to stop.")
    if log_path:
        print("Workflow log:", log_path, file=sys.stderr)
    while running:
        run_once(env, log_fn=_log if log_path else None)
        if not running:
            break
        for _ in range(int(interval_sec)):
            if not running:
                break
            time.sleep(1)
    print("Listener stopped.")


if __name__ == "__main__":
    main()
