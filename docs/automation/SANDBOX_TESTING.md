# Sandbox Testing — Feature Validation Before Merge

**Purpose:** Test new features in isolation before implementing them into the main project. Reduces risk and allows validation without affecting production code.

**Established:** 2025-03-11

---

## What Is Sandbox Testing?

Sandbox testing means running or validating features in a **controlled, isolated environment** before they reach the main codebase. For OutOfRouteBuddy, this applies to:

1. **Cursor improvement loop** — Commands and edits run in a sandbox; changes can be reviewed before commit.
2. **App features** — New features (auto drive, reports, address input) tested in a branch or build variant before merge.
3. **Future features** — Documented in Help & Info so users know what's coming; implementation happens only after validation.

---

## 1. Cursor Sandbox (Terminal Commands)

Cursor's **Run in Sandbox** limits what terminal commands can do:

| Setting | Effect |
|---------|--------|
| **Run in Sandbox** | Commands run in a restricted environment; cannot access files outside workspace, limited network. |
| **Command Allowlist** | Only pre-approved command prefixes run without prompt. |
| **File protection** | File-Deletion, External-File, Dotfile protection limit destructive actions. |

**Use for Improvement Loop:** Keep sandbox ON; add `cd c:\Users\brand\OutofRoutebuddy` to allowlist. Loop commands run; risky system commands are blocked.

---

## 2. Feature Sandbox (App Features)

For new app features (auto drive, reports, address input, end-of-trip notification):

| Approach | How | When to use |
|----------|-----|-------------|
| **Feature branch** | Implement in `feature/auto-drive` (or similar); test; merge when ready. | Standard Git workflow. |
| **Build variant** | Add `sandbox` or `featurePreview` build type; gate features behind a flag. | When you want a single APK with toggles. |
| **Documentation first** | Add to ROADMAP, Help & Info "Coming soon"; implement only after user approval. | Per user rule: no unwarranted changes. |

**Recommended:** Use feature branches for code; document future features in Help & Info so users see the roadmap.

---

## 3. Sandbox Phase in Improvement Loop (Optional)

Add a **sandbox phase** before Phase 1 for higher-risk items:

| Step | Action |
|------|--------|
| **Pick 1 item** | Choose one change from CRUCIAL (e.g. dead code removal, new string). |
| **Propose** | Describe the change; do not apply yet. |
| **User approval** | User says "apply" or "skip." |
| **Apply** | If approved, implement; else skip and continue loop. |

This keeps the loop autonomous for low-risk items while giving a gate for higher-risk changes.

---

## 4. Sandboxed = 100% When Documented

**Heavy-tier ideas** documented in [FUTURE_IDEAS.md](../product/FUTURE_IDEAS.md) are **100% sandboxed**. You can add ideas anytime. Run **Medium tier** Improvement Loop to apply sandbox improvements (index, cross-links, new ideas).

---

## 5. Promotion from Sandbox to Heavy (Implementation)

New feature ideas **must not** go directly to implementation. Per [LOOP_TIERING.md](./LOOP_TIERING.md):

1. **Document** in FUTURE_IDEAS.md → 100% sandboxed.
2. **Validate** — feature branch, build variant, or design brief; user confirms behavior.
3. **Confirm** — User explicitly approves: "This is ready to implement."
4. **Question lock** — When user says "implement X," ask: "Would you like to see a generated image or layout or simulate a merge?" See [LOOP_TIERING.md](./LOOP_TIERING.md) § Question Lock.
5. **Visual approval** — Generate a simple image (or layout or merge simulation) showing where the feature is implemented and what it looks like. Present to user.
6. **Explicit phrase** — Do **not** implement until user says: **"approve 100% implement"**.
7. **Then** implement.

**Goal:** 100% confidence that a feature is ready before implementation. No implementation without question lock, visual approval, and "approve 100% implement."

---

## 6. Future Features in Help & Info

Future/planned features are shown in the Help & Info dialog so users know what's coming. See `strings.xml` → `help_future_features` and `TripInputFragment.showHelpInfoDialog()`.

**Update when:** ROADMAP or CRUCIAL changes; new features are prioritized.

---

## Summary

| Layer | Sandbox mechanism |
|-------|-------------------|
| **Cursor** | Run in Sandbox + Command Allowlist |
| **App features** | Feature branch or build variant |
| **Loop** | Optional sandbox phase for user approval |
| **Heavy ideas sandboxed** | Document in FUTURE_IDEAS = 100% sandboxed; add anytime; Medium tier runs improvements |
| **Sandbox testing for merge** | Test new features in branch/build variant before merging into main; merge only when safe |
| **Promotion to implementation** | Validate → Confirm → **Question lock** ("image or layout or simulate merge?") → **Visual image/layout/merge** → User says **"approve 100% implement"** → Implement |
| **User-facing** | Help & Info "Coming soon" section |

---

*Integrates with `IMPROVEMENT_LOOP_ROUTINE.md` and `AUTONOMOUS_LOOP_SETUP.md`.*
