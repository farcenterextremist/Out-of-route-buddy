# Improvement Loop — Frontend vs Backend Breakdown

**Purpose:** Make explicit how much of an average Improvement Loop is frontend vs backend. **Intent:** Mostly backend; frontend only when ultimately obvious (accessibility, one useful string, or one subtle consistency fix).  
**References:** [IMPROVEMENT_LOOP_ROUTINE.md](./IMPROVEMENT_LOOP_ROUTINE.md), [USER_PREFERENCES_AND_DESIGN_INTENT.md](./USER_PREFERENCES_AND_DESIGN_INTENT.md)

---

## 1. Target Split (Average Run)

| Layer | Target | Rationale |
|-------|--------|-----------|
| **Backend / config / docs / tests** | **~75–85%** | Phase 1 and Phase 2 are almost entirely backend: dead code, BuildConfig, security, test health, docs, sandboxing. |
| **Frontend (UI)** | **~15–25%** | Only Phase 3 touches UI, and only in small ways: one string, one contentDescription, one useful info (e.g. version), optionally one subtle visual tweak. |
| **Process (Phase 0, 4)** | Not counted | Checkpoint, research, summary, lint — no app code. |

**Rule:** No unwarranted UI changes. Frontend work in the loop should be **obvious** (accessibility, copy, or one clearly useful piece of info). When in doubt → backend or docs, not UI.

---

## 2. By Phase (Task-Level)

### Phase 0 — Research & tiering  
**Frontend:** 0% · **Backend:** 0% (process only)

### Phase 1 — Quick wins, security, smoothness  
| Task | Frontend | Backend |
|------|----------|---------|
| 1.1 Dead code, BuildConfig, doc links | — | ✓ (services, config, docs) |
| 1.2 Security (PII grep, FileProvider, KDoc) | — | ✓ |
| 1.3 Smoothness: ErrorHandler, null safety, logging | — | ✓ |
| 1.3 Smoothness: Animation (stat card) | ✓ (one optional) | — |

**Phase 1 split:** ~90–100% backend; 0–10% frontend only if "Animation" is chosen for 1.3.

### Phase 2 — Test health & documentation  
| Task | Frontend | Backend |
|------|----------|---------|
| 2.1 Test health, @Ignore | — | ✓ (test code) |
| 2.2 Documentation, cross-links | — | ✓ (docs) |
| 2.3 Sandboxing | — | ✓ (docs, briefs) |
| 2.5 Unit tests | — | ✓ (infra) |

**Phase 2 split:** 100% backend/docs.

### Phase 3 — UI polish (minimal)  
| Task | Frontend | Backend |
|------|----------|---------|
| 3.1 Strings / accessibility | ✓ (one string or contentDescription) | — |
| 3.2 Stat card / UI consistency | ✓ (verify or one subtle tweak) | — |
| 3.3 Useful information | ✓ (e.g. version in Settings) | — |

**Phase 3 split:** 100% frontend, but **capped**: one improvement per category, "at most one subtle improvement" (Kaizen). No layout/flow changes.

### Phase 4 — Lint & summary  
**Frontend:** 0% · **Backend:** 0% (process only)

---

## 3. By File Touch (Typical Run)

Approximate distribution of **app code files** changed in an average loop:

| Area | Typical files | Examples |
|------|----------------|----------|
| **Backend / config** | 2–4 | `BuildConfig.kt`, `TripTrackingService.kt`, repository/DAO, `ErrorHandler`, test classes |
| **Docs** | 1–2 | README, FAILING_OR_IGNORED_TESTS, summary |
| **Frontend** | 1–3 | `strings.xml`, one Fragment or Adapter (e.g. Settings version, stat card contentDescription), optionally one layout/color |

So by **file count**: roughly **2–3 backend/config files** per **1–2 frontend files** in a typical run — about **60–75% backend**, **25–40% frontend** by file touch. Frontend should stay on the lower end unless the run’s focus is UI/UX.

---

## 4. Last Run (2025-03-11) — Actual

| File | Layer |
|------|--------|
| BuildConfig.kt, core/config/README.md, TripTrackingService.kt | Backend / config |
| SettingsFragment.kt (version in About) | Frontend |
| TripHistoryStatCardAdapter.kt (contentDescription), strings.xml | Frontend |

**Count:** 3 backend/config, 3 frontend → 50/50 that run. The routine still intends **mostly backend**; that run had no dead-code removals and no security task, so Phase 1 was lighter on backend than usual. A run that does 1.1 + 1.2 + 1.3 (backend smoothness) + Phase 2 will skew clearly to backend.

---

## 5. Guidelines for the Agent

1. **Prefer backend:** When choosing among quick wins or smoothness items, prefer backend (dead code, constants, security, error handling, logging, null safety) over frontend (animation, new UI text).
2. **Phase 3 is the only frontend phase:** Limit UI changes to what’s in Phase 3: one string/contentDescription, one useful info, one subtle consistency check. No new screens, no layout/flow changes.
3. **Obvious only:** Frontend changes must be obviously beneficial (accessibility, version visible, typo). When uncertain → suggest in summary, don’t implement.
4. **Report in summary:** In the summary, note "Frontend vs backend: X backend files/tasks, Y frontend files/tasks" so the split stays visible and can be corrected next run if it drifts.

---

## 6. Summary Table

| Metric | Target (average loop) |
|--------|------------------------|
| **Task count** | ~75–85% backend/config/docs/tests, ~15–25% frontend |
| **File touch** | ~60–75% backend/config, ~25–40% frontend (lower end when focus ≠ UI/UX) |
| **Frontend scope** | Only Phase 3; one improvement per category; no layout/flow changes |
| **When in doubt** | Backend or docs, not UI |

---

*Use this breakdown when classifying tasks and when writing the loop summary, so the loop stays mostly backend with minimal, obvious frontend.*
