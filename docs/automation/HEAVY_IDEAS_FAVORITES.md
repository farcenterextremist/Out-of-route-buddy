# Heavy Ideas — Favoriting & Curation

**Purpose:** You tell the agent which Heavy ideas you prefer. The loop uses this to **surface favorites first** and **keep the Heavy list lightly populated** — prioritizing your picks and suggesting pruning or deferring lower-preference items.

**References:** [FUTURE_IDEAS.md](../product/FUTURE_IDEAS.md), [LOOP_TIERING.md](./LOOP_TIERING.md), [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md)

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

**Rule:** Prefer **fewer, higher-quality** Heavy ideas per run over a long unfiltered list. When suggesting new Heavy ideas, add at most 1–2 that meet the bar above, and note: "User can add ✅ in HEAVY_IDEAS_FAVORITES to prioritize."

---

## Loop integration

- **Phase 0:** Read this file during Research. Note in research output: "Heavy favorites: [list favored ideas]; list lightly populated per user preferences."
- **Phase 4.3 (summary):** When listing Heavy tasks or "recommended new ideas":
  - **Surface favorites first** — List Heavy ideas that have ✅ in this doc before others.
  - **Keep list light** — If the full Heavy list is long, summarize as "Favorites (N): [names]. Other Heavy ideas in FUTURE_IDEAS (M) — not favored this run."
  - **New Heavy ideas** — Propose only 1–2 high-quality ideas per run that meet the quality bar above; remind user they can favorite here.

---

*Update when you add or remove favorites. Integrates with IMPROVEMENT_LOOP_ROUTINE Phase 0 and Phase 4.3.*
