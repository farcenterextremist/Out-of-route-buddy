# Heavy UI Suggestions — Approval Required

**Purpose:** Structural or high-impact UI changes that require explicit approval before implementation.  
**Reference:** [BEAUTIFICATION_RESEARCH_AND_TODOS.md](./BEAUTIFICATION_RESEARCH_AND_TODOS.md)  
**Rule:** Do not implement without user approval.

---

## H1 — Collapsing Toolbar on Trip Details

| Field | Value |
|-------|-------|
| **Effort** | Medium |
| **Risk** | Medium |
| **Description** | Replace fixed toolbar with CoordinatorLayout + AppBarLayout + CollapsingToolbarLayout. Trip header collapses as user scrolls; title scales down. |
| **Rationale** | More content space; modern pattern; fits detail-screen use case. |
| **Approval gate** | User says "approve collapsing toolbar on Trip Details" |

---

## H2 — Introduce New Font Family

| Field | Value |
|-------|-------|
| **Effort** | Medium |
| **Risk** | Low–Medium |
| **Description** | Add Inter, Figtree, or similar; apply via theme. Replace system default for body/titles. |
| **Rationale** | Improved readability; modern feel; research suggests ~40% UX improvement potential. |
| **Approval gate** | User approves font choice and scope |

---

## H3 — Bottom Navigation Bar

| Field | Value |
|-------|-------|
| **Effort** | High |
| **Risk** | High |
| **Description** | Add bottom nav (Trip Input, History, Settings) if app grows to multiple main screens. |
| **Rationale** | Thumb-friendly; standard pattern; 3–5 items recommended. |
| **Approval gate** | User approves navigation restructure |

---

## H4 — Navigation Rail for Tablet

| Field | Value |
|-------|-------|
| **Effort** | High |
| **Risk** | Medium |
| **Description** | Adaptive layout: bottom nav on phone, navigation rail on tablet (sw600dp). |
| **Rationale** | Material 3 responsive pattern; better use of large screens. |
| **Approval gate** | User approves tablet support scope |

---

## H5 — Toolbar Redesign

| Field | Value |
|-------|-------|
| **Effort** | Medium |
| **Risk** | High (identity) |
| **Description** | Replace or significantly alter cracked-road toolbar (e.g. solid color, gradient, softer texture). |
| **Rationale** | Reduce noise; preserve or refine trucking identity. |
| **Approval gate** | User approves visual direction; Question Lock applies |

---

## H6 — Full Animation System

| Field | Value |
|-------|-------|
| **Effort** | High |
| **Risk** | Medium |
| **Description** | Page transitions, shared element transitions, 120–220ms micro-interactions. |
| **Rationale** | Smooth feel; perceived performance; modern UX. |
| **Approval gate** | User approves animation scope and performance budget |

---

## H7 — Bottom Sheet for Statistics

| Field | Value |
|-------|-------|
| **Effort** | Medium |
| **Risk** | Medium |
| **Description** | Replace inline expandable statistics with bottom sheet that slides up. |
| **Rationale** | Progressive disclosure; less layout shift; up to 30% engagement improvement in some studies. |
| **Approval gate** | User approves Statistics UX change |

---

## H8 — Dark-First Default Option

| Field | Value |
|-------|-------|
| **Effort** | Low–Medium |
| **Risk** | Low |
| **Description** | Add user preference: default to dark mode. |
| **Rationale** | Night driving; battery (AMOLED); user preference. |
| **Approval gate** | User approves as opt-in or default |

---

## H9 — Skeleton Loader for Trip List

| Field | Value |
|-------|-------|
| **Effort** | Medium |
| **Risk** | Low |
| **Description** | Shimmer/skeleton placeholder while trips load instead of spinner. |
| **Rationale** | Perceived performance; layout stability; modern pattern. |
| **Approval gate** | User approves loading UX change |

---

## H10 — Material 3 Migration

| Field | Value |
|-------|-------|
| **Effort** | High |
| **Risk** | High |
| **Description** | Full migration to Material 3 color system, components, HCT color space. |
| **Rationale** | Future-proof; dynamic color; consistent with platform. |
| **Approval gate** | User approves major theme refactor |

---

## Approval Workflow

For any Heavy item:

1. Document in this file
2. Do not implement until user explicitly approves
3. For H5 (Toolbar): apply Question Lock — "Would you like to see a generated image or layout or simulate a merge?"
4. Implement one at a time; verify after each

---

*Heavy UI suggestions. No implementation without approval.*
