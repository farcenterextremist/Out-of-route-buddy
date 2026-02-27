# Coordinator Email – Human-in-the-Loop

This folder contains the script used by the **Human-in-the-Loop Manager** to send you (the user) emails with suggestions, questions, and updates from the team.

**Mailing list / consultation:** The recipient you set in `.env` (`COORDINATOR_EMAIL_TO`) is the project owner on the consultation mailing list. Jarvey (the email coordinator) and the Human-in-the-Loop Manager use this to recommend changes, consult with you on decisions, and send updates. Add your email below to join.

## Setup

1. **Copy the example env file**
   ```bash
   cp .env.example .env
   ```

2. **Edit `.env`** with your settings:
   - `COORDINATOR_EMAIL_TO` – Your email address (recipient).
   - `COORDINATOR_EMAIL_FROM` – Sender address (often same as SMTP user).
   - `COORDINATOR_SMTP_HOST` – e.g. `smtp.gmail.com`, `smtp.office365.com`.
   - `COORDINATOR_SMTP_PORT` – Usually `587` (TLS) or `465` (SSL).
   - `COORDINATOR_SMTP_USER` – Your SMTP username / email.
   - `COORDINATOR_SMTP_PASSWORD` – App password or account password (prefer app-specific password).

3. **Do not commit `.env`** – It is listed in `.gitignore` for `scripts/coordinator-email/.env`.

If `python` is not found when you run the script, see **`docs/Make_Python_Available.md`** (add Python to PATH or run `scripts\ensure_python_on_path.ps1` once).

### Jarvey's own email (current setup)

**Jarvey has a dedicated email address.** This is the recommended and current setup.

| Env var | Value | Purpose |
|---------|-------|---------|
| **COORDINATOR_EMAIL_FROM** | Jarvey's address (e.g. `mybrandonhelperbot@gmail.com`) | Jarvey sends as this; this is the inbox we read |
| **COORDINATOR_EMAIL_TO** | Your personal email | Where you receive Jarvey's emails; we only process replies FROM this address |
| **COORDINATOR_SMTP_USER** / **COORDINATOR_IMAP_USER** | Jarvey's address | Same as FROM; SMTP/IMAP credentials for the bot account |
| **COORDINATOR_SMTP_PASSWORD** / **COORDINATOR_IMAP_PASSWORD** | App password for the bot account | Gmail/Outlook app-specific password |

**Flow:**
1. Jarvey sends emails **from** his address **to** you.
2. You reply from your email client; your reply goes **to** Jarvey's inbox.
3. Jarvey reads **his** inbox and only accepts messages **from** your address (COORDINATOR_EMAIL_TO).
4. Clear sender/reply flow: no same-inbox edge cases.

### Same-inbox (legacy / alternative)

If `COORDINATOR_EMAIL_FROM` and `COORDINATOR_EMAIL_TO` are the same address (one Gmail for both), the agent adds `X-OutOfRouteBuddy-Sent: true` to every email it sends. When reading, it skips messages with that header so it does not reply to its own sends. **We now use Jarvey's own email; same-inbox is supported but not the primary setup.**

## Usage

The Human-in-the-Loop Manager (or you, when testing) runs:

```bash
# From repo root
python scripts/coordinator-email/send_email.py "Subject line" "Body text in plain text."

# Or from this folder
python send_email.py "Subject line" "Body text."
```

- **First argument:** Subject line.
- **Second argument:** Body (plain text). Use quotes so the whole message is one argument.

For multi-line bodies, use a body file: `python send_email.py "Subject" @path/to/body.txt`

**After Phase A/B/C execution (automated):** Run as the last step so the user gets the summary without manual action: `python scripts/coordinator-email/send_phase_completion_email.py phase_abc` or `scripts\coordinator-email\send_phase_completion_email.bat phase_abc`. Presets are in `send_phase_completion_email.py`; add more for other milestones.

---

## Agent entrypoint (read + reply)

For the agent (Jarvey / Coordinator, Human-in-the-Loop Manager), a single script provides read and send:

```bash
# Read latest reply → JSON on stdout: {"found": true, "subject": "...", "body": "...", "date": "..."}
python scripts/coordinator-email/agent_email.py read

# Send email (body can be @path/to/body.txt)
python scripts/coordinator-email/agent_email.py send "Subject" "Body"
```

The agent can use the JSON from `agent_email.py read` in context without opening a file. See **`docs/agents/OPEN_LINE_OF_COMMUNICATION.md`**.

**Alternative:** `read_replies.py --json` also prints JSON; `read_replies.py` without flags writes `last_reply.txt` as before.

---

## Two-way email (user-facing)

See **TWO_WAY_EMAIL_UX.md** for quick instructions: how the user replies, how agents read, one-line summary.

---

## Automatic responses (check and respond)

To **respond to the user's emails automatically** (without starting a Cursor session), use `check_and_respond.py`:

```bash
# From repo root or this folder
python scripts/coordinator-email/check_and_respond.py
```

- **What it does:** Reads the inbox for the latest reply from the user. If there is a **new** reply (not already responded to), it composes a response via the LLM with full project context and sends it. State is stored in `last_responded_state.txt` so we don't reply twice to the same message.
- **LLM:** All replies are composed by the LLM (no preset templates) so the model can answer project questions with full context.
- **Dry run:** `python check_and_respond.py --dry-run` writes the draft to `auto_reply_draft.txt` and does not send.

**Schedule it (optional):** Run every 15–30 minutes so replies are answered without you opening Cursor.

- **Windows (Task Scheduler):** Create a task that runs `python C:\path\to\repo\scripts\coordinator-email\check_and_respond.py` (use full path to Python and script). Trigger: every 15 or 30 minutes.
- **macOS/Linux (cron):** `*/15 * * * * cd /path/to/repo && python scripts/coordinator-email/check_and_respond.py` (every 15 minutes).

State and draft files are in `.gitignore` (`last_responded_state.txt`, `auto_reply_draft.txt`).

---

## Coordinator listener (open line while Cursor is online)

To have **Jarvey** (Master Branch Coordinator and Human-in-the-Loop Manager) read your emails and respond (assign work, delegate, send updates) for as long as Cursor is open, run the Coordinator listener once in a Cursor terminal:

```bash
# From repo root or this folder
python scripts/coordinator-email/coordinator_listener.py
```

- **What it does:** Every N minutes (default 3), checks the inbox for your latest reply. If there is a **new** reply (not already responded to), the script composes a reply as **Jarvey** (using the Coordinator instructions and an LLM) and sends it. Uses the same state file as `check_and_respond.py` so we don't double-respond.
- **Startup opener:** On startup, Jarvey sends an initial email with 5-10 options (roadmap, recent changes, prioritize, where is X, recovery, version, assign work, save note, send to coworker, etc.). Set `JARVEY_SEND_OPENER_ON_STARTUP=0` in `.env` to disable.

**How Jarvey replies work (reply flow):**
1. The listener runs every N minutes and reads the inbox via `read_replies.py`.
2. Only messages **from the user** are considered: when `COORDINATOR_EMAIL_TO` is set, only messages from that address are considered. No subject filter (any message from the user is processed).
3. **Dedupe:** The message's Message-ID (or a content hash if missing) is compared to `last_responded_state.txt`; if it matches, we skip. **Cooldown:** We never send more than once per 2 minutes (shared with check_and_respond).
4. **LLM path:** All replies are composed by the LLM with full project context (coordinator-instructions, project index, intent-based docs, SSOT, etc.).
5. After every send, state and cooldown are updated so we don't double-reply or spam.

- **Requires:** `.env` with SMTP/IMAP (as for other scripts) and **one of**:
  - **OpenAI:** `COORDINATOR_LISTENER_OPENAI_API_KEY` (or `OPENAI_API_KEY`) and `pip install openai`
  - **Local LLM (no API key):** `COORDINATOR_LISTENER_OLLAMA_URL=http://localhost:11434` (or `OLLAMA_URL`). Install [Ollama](https://ollama.com), run e.g. `ollama run llama3.2`, then run the listener—no API key needed.
  - **Transformers (SmolLM):** `COORDINATOR_LISTENER_LLM_BACKEND=transformers` and `pip install transformers accelerate torch`. Uses local Hugging Face models (e.g. SmolLM2-1.7B-Instruct). See [HUGGINGFACE_OPTIONS.md](../docs/agents/HUGGINGFACE_OPTIONS.md).
- **Optional:** `COORDINATOR_LISTENER_INTERVAL_MINUTES=3` (minimum 3), `COORDINATOR_LISTENER_OLLAMA_MODEL=llama3.2`, `COORDINATOR_LISTENER_OLLAMA_TIMEOUT=900` (default 15 min; use 1200 for 20 min if Ollama still times out), `COORDINATOR_LISTENER_OLLAMA_FAST_MODEL=phi3:mini` (2–3x faster replies on simple questions like "recent", "version", "roadmap"), `JARVEY_OLLAMA_FALLBACK_TO_OPENAI=1` (for reliability: timeouts retry with OpenAI; requires `COORDINATOR_LISTENER_OPENAI_API_KEY`). For Transformers backend: `COORDINATOR_LISTENER_TRANSFORMERS_MODEL=HuggingFaceTB/SmolLM2-1.7B-Instruct`, `COORDINATOR_LISTENER_TRANSFORMERS_DEVICE=cuda` (or `cpu`). For response-time tips and "Jarvey not responding" troubleshooting, see **[docs/agents/JARVEY_RESPONSE_TIME.md](../docs/agents/JARVEY_RESPONSE_TIME.md)**.
- **Project context:** The listener injects an expanded context into the Coordinator's prompt: core summary ([coordinator-project-context.md](../docs/agents/coordinator-project-context.md)), a **project index** (file tree of docs, app source, scripts), condensed SSOT and ROADMAP, plus on-demand snippets by intent (roadmap, recovery, emulator, tests, app structure, GPS, todos, UX, etc.). Jarvey can answer "where is X defined?" using the project index. Set `COORDINATOR_MAX_CONTEXT_CHARS` in `.env` (default 12000; set lower e.g. 8000 for Ollama if needed) to cap total context size.
- **Stop:** Ctrl+C in the terminal. When you close Cursor (or the terminal), the listener stops. So "open line so long as Cursor is online" = run this script in a Cursor terminal when you open the project and leave it running.

**No OpenAI key and no Ollama?** Use the [Coordinator email listener runbook](../docs/agents/COORDINATOR_EMAIL_LISTENER_RUNBOOK.md): a Cursor chat loop where you (or the agent) read inbox, compose as Coordinator, and send—no script LLM required.

**See or record Jarvey's workflow (live documentation):**
- **Log while running:** `python coordinator_listener.py --log` or set `JARVEY_LOG=1` in `.env` — appends each step (found message, dedupe, LLM path, sent) to `jarvey_workflow.log` in real time. Same for `check_and_respond.py --log` when run via cron/Task Scheduler.
- **Trace one cycle:** `python trace_jarvey_workflow.py` — runs read → decide → compose → send once, logs to `jarvey_workflow.log`. Use `--dry-run` to compose without sending.
- **Watch in terminal:** Run `python coordinator_listener.py` in the foreground (not background) to see output in real time.
- **Full docs:** [JARVEY_WORKFLOW_LOGGING.md](../docs/agents/JARVEY_WORKFLOW_LOGGING.md)

**Jarvey not responding?** Run `python diagnose_jarvey.py` to check inbox read, dedupe state, cooldown, and subject/FROM filters. Use `--fix-dedupe` to clear state and force a reply (may cause duplicate if we already sent). Use `--reset-circuit` to reset the LLM circuit breaker when Ollama is fixed. See the Jarvey Diagnosis Plan for full steps.

**Jarvey's face:** On startup, the listener shows an ASCII art avatar.

- **Animate**: set `JARVEY_ANIMATE_FACE=1` in `.env` for a short startup animation (blink + status scan).
- **Color**: set `JARVEY_FACE_COLOR=1` in `.env` to force ANSI color (TTY-only by default).
- **Theme**: set `JARVEY_FACE_THEME=ascii|unicode|auto` (default `auto`).
- **Preview**:
  - `python jarvey_face.py`
  - `python jarvey_face.py --animate-startup`
  - `python jarvey_face.py --theme unicode --color`

See [JARVEY_FACE_IMPROVEMENT_PLAN_PROMPT.md](../docs/agents/JARVEY_FACE_IMPROVEMENT_PLAN_PROMPT.md) for extending the design.

### Jarvey benchmark

Run all 9 Jarvey scenarios (template + LLM) and get automated pass/fail:

```bash
python scripts/coordinator-email/run_jarvey_benchmark.py
```

- **What it does:** Runs scenarios 1–9 (all use the LLM). Writes replies to `docs/agents/data-sets/jarvey-scenarios/benchmark_output/`. Prints a summary table with pass/fail per scenario.
- **Requires:** `.env` with `COORDINATOR_LISTENER_OPENAI_API_KEY` or `COORDINATOR_LISTENER_OLLAMA_URL` for LLM scenarios.
- **Interpret results:** Each scenario has heuristic checks (e.g. Scenario 5 must not contain code; Scenario 6 must not invent meeting/report). Exit code 0 if all pass, 1 if any fail. See [SCENARIO_RUN_RESULTS.md](../docs/agents/data-sets/jarvey-scenarios/SCENARIO_RUN_RESULTS.md) for benchmark run history.

### Jarvey scorecard

To score Jarvey's email replies and see which dimensions need improvement, use the **Email coordinator (Jarvey)** section in [docs/agents/AGENT_APTITUDE_AND_SCORING.md](../docs/agents/AGENT_APTITUDE_AND_SCORING.md) §3.1.1 and the scorecard table in [docs/agents/data-sets/agent-aptitude-scorecard.md](../docs/agents/data-sets/agent-aptitude-scorecard.md). To record scores: (1) Run the two test prompts (Simple: "What's next?", Semi-simple: "Can we prioritize the reports screen and when will it be done?") using `compose_reply.py` or the listener, (2) paste each reply into the corresponding file in `docs/agents/data-sets/jarvey-scenarios/` (01_simple_whats_next.md, 02_semi_simple_prioritize_reports.md), (3) score each reply on Scope, Data set, Output, Handoff, Voice (1–5), (4) fill the "Email coordinator (Jarvey)" rows in the scorecard with the five dimension scores and Overall. Re-run after changes to compare.

---

## Reading replies (IMAP)

When the user replies to a coordinator email, you can fetch the latest reply with:

```bash
python read_replies.py
```

- **Requires:** IMAP enabled on the account (Gmail: Settings → Forwarding and POP/IMAP → Enable IMAP).
- **Credentials:** Uses `COORDINATOR_IMAP_USER` / `COORDINATOR_IMAP_PASSWORD` if set in `.env`; otherwise falls back to `COORDINATOR_SMTP_USER` / `COORDINATOR_SMTP_PASSWORD` (same Gmail app password usually works).
- **Output:** Writes the latest reply from the user to `last_reply.txt` in this folder (subject + body). When `COORDINATOR_EMAIL_TO` is set, only messages **from** that address are considered. No subject filter (any message from the user is processed).
- **Run on demand:** When the user says "I replied," run this script, then read `last_reply.txt` to respond or update team parameters.

## Tests

Run the full Jarvey test suite (115+ unit tests; no IMAP/SMTP/LLM required):

```bash
python scripts/coordinator-email/run_all_jarvey_tests.py -v
```

Or run individual modules: `test_check_and_respond.py`, `test_coordinator_listener.py`, `test_read_replies.py`, `test_send_email.py`, `test_mock_llm.py`. See [docs/agents/TESTING_SUITE.md](../docs/agents/TESTING_SUITE.md) for full documentation. To grade the workflow (pipeline, reply quality, benchmark), see [WORKFLOW_SCORING_CHART.md](../docs/agents/WORKFLOW_SCORING_CHART.md).

Optional: set `COORDINATOR_DRY_RUN=1` in `.env` (or pass `dry_run=True` to `send_email.send()`) to log sends without sending (useful for integration tests).

## Requirements

- Python 3.6+
- Standard library only for send/read/check_and_respond. **Coordinator listener** requires one of: `pip install openai` + `COORDINATOR_LISTENER_OPENAI_API_KEY`, Ollama + `COORDINATOR_LISTENER_OLLAMA_URL`, or `pip install transformers accelerate torch` + `COORDINATOR_LISTENER_LLM_BACKEND=transformers`.

## Hugging Face options

- **RAG embeddings:** Default model is `intfloat/e5-small-v2`. Set `JARVEY_RAG_EMBEDDING_MODEL` and rebuild the index after changing. See [JARVEY_RAG_PLAN.md](../docs/agents/JARVEY_RAG_PLAN.md).
- **Transformers backend:** Use `COORDINATOR_LISTENER_LLM_BACKEND=transformers` for local SmolLM (no API key). See [HUGGINGFACE_OPTIONS.md](../docs/agents/HUGGINGFACE_OPTIONS.md).
- **Email model (experimental):** `python compose_reply_email_model.py "Subject" "Body"` — keyword-to-email draft via `postbot/t5-small-kw2email-v2`. Standalone script for experimentation.

## Extending Jarvey

- **Templates:** Add JSON files to `templates/`. See [JARVEY_ARCHITECTURE.md](../docs/agents/JARVEY_ARCHITECTURE.md) § Adding a template.
- **Intents:** Edit `intents/intents.json` to add keywords and context sources for the LLM.
- **Handlers:** Implement the `Handler` protocol in `handlers.py` and call `register_handler()` for custom logic (e.g. Slack integration).
- **Health check:** Run `python health_check.py` to validate config and test IMAP/SMTP/LLM. Use `--listener` to require an LLM backend. See [JARVEY_HEALTH_CHECK_DOCUMENT.md](../docs/agents/JARVEY_HEALTH_CHECK_DOCUMENT.md) for all checks and diagnostics.

## Reusing this framework

To build a similar email bot (another project or another coordinator name), see:

- **[docs/agents/EMAIL_BOT_FRAMEWORK.md](../docs/agents/EMAIL_BOT_FRAMEWORK.md)** — overview of the setup (instructions, context, templates, listener).
- **[docs/agents/EMAIL_BOT_FRAMEWORK_CHECKLIST.txt](../docs/agents/EMAIL_BOT_FRAMEWORK_CHECKLIST.txt)** — checklist for naming, instructions, context, reply logic, and docs.

## Security

- Keep `.env` out of version control and off shared machines.
- Use an app-specific password for Gmail/Outlook rather than your main account password.
- Restrict who can run the script if it runs in a shared or automated environment.
