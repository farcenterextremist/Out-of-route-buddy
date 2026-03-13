# Heavy Ideas — Favoriting & Curation

**Purpose:** You tell the agent which Heavy ideas you prefer. The loop uses this to **surface favorites first** and **keep the Heavy list lightly populated** — prioritizing your picks and suggesting pruning or deferring lower-preference items. When the list hits the **cap (~50)**, the loop switches from **producing** new ideas to **judging and critiquing** existing ones until the list drops below the cap.

**References:** [FUTURE_IDEAS.md](../product/FUTURE_IDEAS.md), [LOOP_TIERING.md](./LOOP_TIERING.md), [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md)

---

## Heavy list cap: produce vs. judge/critique

| Condition | Mode | What the loop does |
|-----------|------|---------------------|
| **Heavy list count < 50** | **Produce** | Add at least 1–2 new Heavy ideas per run (quality bar); document in FUTURE_IDEAS and add rows to Favorites table. |
| **Heavy list count ≥ 50** | **Judge / critique** | Do **not** add new Heavy ideas. Instead: **judge and critique** 1–2 existing Heavy ideas — e.g. score vs quality bar, suggest merge/remove/defer, improve description or placement, note duplicates or low-value items. Report in summary. When ideas are implemented or pruned and count drops **below 50**, return to **produce** mode. |

**How to count:** Count distinct Heavy ideas in [FUTURE_IDEAS.md](../product/FUTURE_IDEAS.md) (or rows in this doc’s Favorites table). Use "around 50" as the threshold (e.g. 48–52); if in doubt, treat ≥ 50 as at cap.

**Rule:** At cap → judge/critique only. Below cap → produce at least 1–2 new Heavy ideas per run.

---

## How to use

- **Mark favorites:** In the table below, add **✅** (or "yes") in the **Favorite?** column for ideas you want to prioritize. Leave blank for "later" or "not now."
- **Keep list light:** The loop will treat **favorites** as the primary Heavy list when asking "Are you ready to implement?" and in the summary. Non-favorites stay in FUTURE_IDEAS but can be summarized as "Other Heavy ideas (not favored this run)."
- **Update anytime:** Edit this file whenever your preferences change. The next loop will read it.

---

## Favorites table

*(Add ✅ in **Favorite?** for ideas you prefer. Remove ✅ to deprioritize.)*

| Idea | FUTURE_IDEAS | Favorite? |
|------|--------------|------------|
| Trash can icon beautification | § 5.1 | |
| Scrolling top toolbar / taskbar | § 6.1 | |
| Hamburger menu left of "Out of route" title | § 6.2 | |
| Possible app name change | § 7.1 | |
| Optional email signup for updates | § 2.1 | |
| Route deviation map (instant replay) | § 3.1 | |
| Sandboxed virtual fleet | § 4.1 | |
| Multi-user data sharing | § 1.1 | |
| Driver ranking | § 1.2 | |
| Ranking chart | § 1.3 | |
| **OOR percentage goal / target** | **§ 8.1** | |
| **End-of-day trip summary notification (opt-in)** | **§ 9.1** | |
| **OOR over/under view with color semantics** | **§ 10.1** | |
| **Load cancelled mid-trip / new load (get future context)** | **§ 11.1** | |
| **Text command: "Send to hub"** | **§ 12.1** | |

---

## Quality bar for new Heavy ideas (loop guidance)

When the loop **proposes new Heavy ideas** (Phase 4.3 "File Organizer: recommended new ideas"), each idea should be **high quality**:

| Criterion | Meaning |
|-----------|--------|
| **Aligned with mission** | Supports GOAL_AND_MISSION (advanced OOR analytics, solo drivers first, no social/ads). |
| **Clear placement** | Where in the app or codebase it lives; no vague "we could add something." |
| **Sandboxed first** | Documented in FUTURE_IDEAS with description, placement, dependencies. |
| **One-by-one** | Implementable as a single feature; user can approve or reject per idea. |
| **Favoritable** | User can mark it in this doc so the Heavy list stays lightly populated. |

**Rule:** When **below cap** (< 50): Propose **at least 1–2** new Heavy ideas that meet the bar above. Add them to FUTURE_IDEAS and to the Favorites table in this doc. When **at or above cap** (≥ 50): Do not add new Heavy ideas; **judge and critique** 1–2 existing ideas instead (see § Heavy list cap). User can add ✅ to prioritize.

---

## Loop integration

- **Phase 0:** Read this file during Research. Note in research output: "Heavy favorites: [list]; list count: [N]; mode: [produce | judge/critique]."
- **Phase 4.3 (summary):** When listing Heavy tasks or "recommended new ideas":
  - **Surface favorites first** — List Heavy ideas that have ✅ in this doc before others.
  - **Keep list light** — If the full Heavy list is long, summarize as "Favorites (N): [names]. Other Heavy ideas in FUTURE_IDEAS (M) — not favored this run."
  - **Cap rule:** If Heavy list **count ≥ 50** → **judge/critique mode:** do not add new Heavy ideas; judge and critique 1–2 existing ideas (quality bar, merge/remove/defer, improve description). If count **< 50** → **produce mode:** propose at least 1–2 new Heavy ideas that meet the quality bar; add to FUTURE_IDEAS and add a row for each to the Favorites table. Remind user they can favorite here.

---

*Update when you add or remove favorites. Integrates with IMPROVEMENT_LOOP_ROUTINE Phase 0 and Phase 4.3.*
