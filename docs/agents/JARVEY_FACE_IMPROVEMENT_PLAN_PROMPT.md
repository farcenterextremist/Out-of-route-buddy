# Custom Prompt: Jarvey Face (ASCII Art) Improvement Plan

**Purpose:** Use this prompt (e.g. in Cursor or a planning session) to extend or refine Jarvey's terminal face — ASCII art, animation, and presentation.

---

## START OF PROMPT

Create a detailed, actionable plan to improve **Jarvey's face** — the ASCII art avatar shown in the terminal when the coordinator listener starts. The goal is a larger, more beautiful, and optionally dynamic (animated) face.

---

### Context: Current Implementation

**Location:** `scripts/coordinator-email/jarvey_face.py` and `scripts/coordinator-email/coordinator_listener.py`

**Current face (legacy fallback):**
```
  +-----------------+
  |  ( o )   ( o )  |
  |      \___/      |
  |   Jarvey       |
  +-----------------+
```

**New face (large static):**
- 14 lines, ~30 chars wide
- Pure ASCII (`+`, `-`, `|`, `(`, `)`, `o`, `\`, `/`) for maximum terminal compatibility
- Box-drawn eyes and "JARVEY / Email Ready" display
- Unicode variant available (`╔`, `═`, `║`, etc.) when terminal supports UTF-8

**Animation:**
- Optional blink animation via `JARVEY_ANIMATE_FACE=1`
- Uses ANSI escape codes: cursor up (`\033[nA`), clear line (`\033[2K`)
- Startup sequence: face → pause → blink → done

---

### Research: ASCII Art Best Practices

1. **Pure ASCII vs Unicode**
   - Pure ASCII (7-bit): `+ - | / \ ( ) . _ = : o O * #` — works everywhere
   - Unicode box-drawing: `┌ ┐ └ ┘ ─ │ ╭ ╮ ╯ ╰ ╔ ╗ ╚ ╝ ═ ║` — cleaner look in UTF-8 terminals
   - Detect via `sys.stderr.encoding`; fall back to ASCII if not UTF-8

2. **Animation**
   - **Asciimatics** (Python): Full terminal UI library; frame-based effects, sprites
   - **ANSI escape sequences**: Cursor positioning, clear line, colors — no extra deps
   - **Frame-by-frame**: Swap face strings in place; 2–3 frames (open, blink, smile) for idle
   - Keep animation short (< 2s) so startup isn’t delayed

3. **Design principles**
   - Monospace alignment: every line same width
   - Symmetry: eyes, borders balanced
   - Readable at 80–120 col terminals
   - Friendly, assistant-like (robot/helper), not aggressive

4. **Reference**
   - ASCII Art Archive: robots, faces
   - Box-drawing: U+2500–U+257F

---

### Requirements for the Plan

1. **Larger, more beautiful face**
   - Propose 2–3 alternative designs (e.g. robot with antenna, rounded display, minimal)
   - Specify dimensions (lines × chars) and character set (ASCII vs Unicode)
   - Ensure `jarvey_face.py` remains the single source; keep fallback in `coordinator_listener.py` if import fails

2. **Animation**
   - Document current blink behavior and any limitations (e.g. ANSI support)
   - Propose: add more frames (smile, “thinking”), or idle loop (optional, low priority)
   - Consider: `JARVEY_ANIMATE_FACE=0|1|blink|full` for different modes
   - Avoid adding heavy dependencies (e.g. asciimatics) unless needed

3. **Integration**
   - `coordinator_listener.py` imports and uses `jarvey_face`
   - `JARVEY_ANIMATE_FACE` in `.env` / `.env.example`
   - Optional: `python jarvey_face.py --animate` for testing

4. **Documentation**
   - Update `scripts/coordinator-email/README.md` with face section and env vars
   - Add a short “Jarvey face” section to `docs/agents/` if useful

---

### Deliverables

The plan should:

- List concrete tasks in implementation order
- Include at least one full ASCII face design (line-by-line)
- Cite file paths and functions
- Be reviewable before implementation (plan only; do not implement in this step)

---

### Reference Files

- `scripts/coordinator-email/jarvey_face.py` — face definitions, `print_face`, `animate_blink`, `animate_startup`
- `scripts/coordinator-email/coordinator_listener.py` — main loop; startup face display (around line 865)
- `docs/agents/JARVEY_INTELLIGENCE_PLAN.md` — improvement planning and feedback loop

---

## END OF PROMPT
