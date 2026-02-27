# Jarvey: Detecting Briefness and “Needs Clarification”

## Concept

**Briefness detection** is the logic that decides whether a short user message (e.g. "6", "ok", "yes") should:

- Be treated as **valid** (e.g. menu choice, acknowledgment) and sent to the LLM or a specific handler, or  
- Be treated as **unclear** and trigger a “couldn’t make out what you need” clarification template.

Getting this wrong causes:

- **False unclear**: User sent a valid brief reply (e.g. "6" for “App version”) but gets the clarification template.
- **False clear**: User sent gibberish or a single unclear word and we still call the LLM or send a generic reply.

Our current approach is **rule-based**: a short body (under N characters) is “unclear” unless it’s in a **whitelist** (e.g. "1"–"10", "ok", "what's next?", "tell me something"). That works for known-good brief inputs but doesn’t scale to every possible short phrase and doesn’t use “intent” or “needs clarification” signals from libraries.

---

## Current Implementation (Rule-Based)

- **Location**: `scripts/coordinator-email/template_registry.py` (`_check_short_body`), `templates/unclear.json` (whitelist).
- **Logic**: `len(body) < max_len` and `body.lower() not in whitelist` → use unclear template.
- **Pros**: No dependencies, fast, explainable, easy to extend the whitelist.  
- **Cons**: New valid short phrases require manual whitelist updates; no notion of “intent” or “vagueness” beyond length + list.

---

## Research: Libraries That Can Help

### 1. **implicature-intent** (recommended optional add-on)

- **PyPI**: `implicature-intent` (Loom Labs, MIT, Python 3.8+).
- **Purpose**: Separates *intent* from *tone* in short conversational turns (e.g. “I’d love to but I’m swamped” → NO, not “positive”).
- **Relevant output**: `needs_clarification: bool`, `orientation` (YES/NO/NON_COMMITTAL/UNKNOWN), `confidence`, `response_type`.
- **Pros**: Zero dependencies, offline, returns `needs_clarification` and confidence; fits “should we ask for clarification?” use case.
- **Cons**: Focused on YES/NO/counter-offer style replies; docs note “Very short responses like ‘Ok’ are ambiguous without context.” So a bare "6" may still be flagged as needing clarification; we keep the **whitelist for known menu choices** (1–10) and use the library for other short messages.
- **Use in Jarvey**: For short messages **not** in the whitelist, optionally call `analyze_intent(body)`: if `not result["needs_clarification"]` and `result["orientation"] != "UNKNOWN"` (or confidence above a threshold), treat as **clear enough** and skip the unclear template; otherwise keep current behavior (unclear).

### 2. **AutoIntent** (autointent)

- **PyPI**: `autointent` (Deeppavlov, Python 3.10+).
- **Purpose**: AutoML for intent classification; fit/predict on labeled data.
- **Pros**: Good for training “clear” vs “unclear” or multi-intent on your own data.
- **Cons**: Requires labeled examples and training; heavier than a small rule-based + optional heuristic library.
- **Use in Jarvey**: Only if we later train a dedicated “brief-but-clear” vs “needs-clarification” classifier on our emails.

### 3. **Rasa / Dialogflow**

- **Purpose**: Full NLU + slot filling and forms; can handle menu choices and required slots.
- **Pros**: Mature, supports short answers and menu flows.
- **Cons**: Bigger stack, training data, and deployment; overkill just for “is this brief message unclear?”.
- **Use in Jarvey**: Consider only if we move to a full Rasa/Dialogflow dialogue model; not needed for a lightweight brevity detector.

### 4. **LangCheck / prlyn / Nyckel**

- **LangCheck**: Evaluation for LLM apps (e.g. output quality).  
- **prlyn**: Prompt linter (clarity of instructions).  
- **Nyckel**: Pretrained “needs clarification” classifier via API.  
- **Use in Jarvey**: More relevant to evaluating or pre-screening prompts/outputs than to classifying *incoming* user brevity in the listener. Can be revisited if we add a “message quality” or “clarification need” scoring step that runs before the LLM.

---

## Recommendation

1. **Keep the current rule-based brevity logic**  
   - Short body + whitelist (1–10, “ok”, “what’s next?”, etc.) stays the primary mechanism so that known-good brief replies (especially "6") never hit the unclear template.

2. **Introduce a small “brevity detector” module**  
   - Single place that implements “is this message brief?” and “should we treat it as unclear?” using:  
     - Length and whitelist (current behavior).  
     - Optional: capability menu number expansion (e.g. "6" → “User chose option 6: App version / build info”) so the rest of the pipeline sees a clear intent.

3. **Optionally integrate implicature-intent**  
   - For short messages that are **not** on the whitelist:  
     - Call `analyze_intent(body)`.  
     - If `not needs_clarification` and confidence above a threshold (and optionally `orientation != "UNKNOWN"`), treat as **not unclear** and send to LLM.  
   - This reduces false “unclear” for short but interpretable phrases we didn’t whitelist, without removing the whitelist for numbers and common replies.

4. **Document and keep dependencies optional**  
   - `implicature-intent` as an optional dependency; if not installed, the brevity detector falls back to rules + whitelist only.

---

## Implementation Plan (Optional Library Integration)

- Add **`scripts/coordinator-email/brevity_detector.py`** that:
  - Exposes `is_brief(body, max_len=15)` and `should_use_unclear_template(body, whitelist, max_len=15, use_implicature=False)`.
  - Uses the same whitelist/short-body rules as today.
  - If `use_implicature=True` and the message is short and not whitelisted, try `implicature_intent.analyze_intent(body)`; if `not result["needs_clarification"]` and `result.get("confidence", 0) >= 0.5`, return `False` (do not use unclear template).
- In **`template_registry.py`**: either call this module instead of `_check_short_body` for the unclear template, or keep `_check_short_body` and add an optional “override” that consults the brevity detector when a library is enabled.
- **Config**: e.g. `JARVEY_BREVITY_USE_IMPLICATURE=1` in `.env` to turn on the optional library path; default off so no new dependency by default.

This keeps “detecting briefness” in one place and allows a custom library (implicature-intent) to remedy over-triggering of the unclear template for short but valid messages, This keeps "detecting briefness" in one place and allows a custom library (implicature-intent) to remedy over-triggering of the unclear template for short but valid messages, while we keep the whitelist for numbers and common phrases.

---

## Implemented while we keep the whitelist for numbers and common phrases.

---

## Implemented

- **`scripts/coordinator-email/brevity_detector.py`**  
  - `is_brief(body, max_len)`, `is_whitelisted(body, whitelist)`, `should_use_unclear_template(body, max_len, whitelist, use_implicature)`, `is_capability_menu_number(body)`.
  - Optional **implicature-intent**: if `use_implicature=True` and the library is installed, non-whitelisted short messages are passed to `analyze_intent()`; if `needs_clarification` is False, we do not use the unclear template.
  - Run `python brevity_detector.py` to self-test.

- **`template_registry.py`**  
  - When **`JARVEY_BREVITY_USE_DETECTOR=1`** in `.env`, `_check_short_body` uses `brevity_detector.should_use_unclear_template()` (with the template's whitelist and `short_body_max_len`). If **`JARVEY_BREVITY_USE_IMPLICATURE=1`** as well, the optional library is used for short, non-whitelisted messages.
  - If the detector is disabled or the module is unavailable, behavior falls back to the original length + whitelist check.

- **`.env.example`**  
  - Documented: `JARVEY_BREVITY_USE_DETECTOR`, `JARVEY_BREVITY_USE_IMPLICATURE`.

To try the optional library: `pip install implicature-intent`, then set `JARVEY_BREVITY_USE_DETECTOR=1` and `JARVEY_BREVITY_USE_IMPLICATURE=1` in `scripts/coordinator-email/.env`.
