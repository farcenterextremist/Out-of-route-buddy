# Frontend Change Automation Gate (Master Loop Only)

**Purpose:** Decide whether a frontend/UI change is safe to automate and obviously beneficial.

**Policy:** Only the **Master Loop** may implement frontend changes. Non-master loops may research, document, and propose UI changes, but not apply them.

**Why this exists:** "Looks better" is subjective. This gate makes frontend automation evidence-based.

---

## Research basis

This gate uses established standards:

- Android accessibility and app quality guidelines (touch targets, contrast, readable UI states)
- Material Design interaction states and consistency
- UX severity/impact prioritization (frequency, impact, persistence)
- Confidence and effort discounting (prioritize safe, high-confidence changes)

Practical references:

- `docs/automation/QUALITY_STANDARDS_AND_PROOF.md`
- Android accessibility + quality docs
- Material interaction state guidance

---

## Step 1: Hard-stop eligibility checks (must all pass)

If any check fails, **do not automate**.

1. **Master-loop authority**
   - Current run is explicitly Master Loop (`start master loop` flow), or user explicitly delegated frontend implementation to master-loop policy.
2. **No structure/navigation change**
   - No new screens
   - No navigation flow rewrite
   - No layout hierarchy redesign
3. **Subtle footprint**
   - Frontend files changed <= 2
   - One UI improvement category only (copy/accessibility, spacing/alignment, state cue, or micro-motion)
4. **Accessibility-safe**
   - Touch target rule respected (48dp minimum for interactive elements)
   - Contrast rule respected (4.5:1 small text, 3:1 large text/non-text UI as applicable)
5. **Theme-safe**
   - Works in light/dark themes (no token-breaking one-off color hacks)

---

## Step 2: Score obvious benefit (OBS, 0-10)

Score each dimension 0-2, then sum.

| Dimension | 0 | 1 | 2 |
|---|---|---|---|
| **User impact** | cosmetic only | mild clarity/usability gain | clear usability/accessibility gain |
| **Frequency** | rare screen/action | occasional | frequent/daily path |
| **Persistence** | one-time annoyance | intermittent friction | repeated friction every use |
| **Evidence strength** | opinion only | heuristic + code context | measurable proof (lint/test/scanner/findings) |
| **Safety confidence** | risky/uncertain | moderate | high confidence, localized, reversible |

**OBS threshold for automation:** `OBS >= 7`

---

## Step 3: Score subtlety (SSS, 0-10)

Start at 10 and subtract penalties.

- `-4` if layout hierarchy/flow is altered
- `-3` if >2 frontend files touched
- `-2` if multiple UI categories changed in one run
- `-2` if animation is decorative (not feedback/state communication)
- `-2` if custom colors bypass theme tokens
- `-2` if any accessibility threshold is unverified

**SSS threshold for automation:** `SSS >= 8`

---

## Step 4: Automation decision rule

Frontend change is auto-allowed **only if all are true**:

1. Hard-stop eligibility checks pass
2. `OBS >= 7`
3. `SSS >= 8`
4. Quality evidence is included in summary (`Proof of Quality` block)

Otherwise:

- Mark as **proposal only**
- Add to summary + backlog with rationale
- Request user approval before implementation

---

## Required summary snippet (Master Loop)

```markdown
## Frontend Automation Gate

- Candidate change: [one-line]
- Hard-stop checks: pass/fail (list)
- Obvious Benefit Score (OBS): X/10
- Subtlety Safety Score (SSS): X/10
- Decision: [Auto-allow | Proposal only]
- Evidence: [lint/test/accessibility references]
```

---

## Allowed subtle categories (automation scope)

When gate passes, automation is limited to one category:

1. **Sharpen:** copy clarity, labels, contentDescription
2. **Align:** small spacing/alignment consistency
3. **Flow:** clear interaction states (enabled/disabled/pressed/focused)
4. **Smoothen:** subtle feedback motion (short, purposeful, non-decorative)
5. **Pop (minimal):** minor contrast emphasis using existing design tokens

No category stacking in one run.

---

## Anti-drift rule

If the team cannot show concrete evidence for benefit and subtlety, do not automate frontend changes. Default to backend/docs/test work.

