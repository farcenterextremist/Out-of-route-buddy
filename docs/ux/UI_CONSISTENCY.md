# UI consistency rules

**Purpose:** Define the source of truth for cards, spacing, and icons so new UI stays consistent. Part of Polishing Plan Phase 3.3.

---

## Source of truth

| Element | Source of truth | Location |
|---------|-----------------|----------|
| **Stat cards (calendar history)** | Expandable card with summary row + metadata section | [item_trip_history_stat_card.xml](../../app/src/main/res/layout/item_trip_history_stat_card.xml), [TripHistoryStatCardAdapter.kt](../../app/src/main/java/com/example/outofroutebuddy/presentation/ui/history/TripHistoryStatCardAdapter.kt) |
| **Trip history list items** | Simpler list row (date, miles, OOR) | Trip history by date dialog; adapters in `presentation/ui/history/` |
| **Card elevation and radius** | MaterialCardView: 6dp elevation, 12dp corner radius | Stat card layout; reuse for other card-style surfaces |
| **Spacing** | Card margin 8dp; internal padding 16dp | Stat card layout |
| **Colors** | `text_primary_adaptive`, `text_secondary_adaptive`, `@color/error` | values/colors.xml, themes |

---

## Rules for new or updated UI

1. **Cards:** Use the same elevation (6dp) and corner radius (12dp) as the stat card unless a different style is explicitly approved.
2. **Spacing:** Use 16dp internal padding and 8dp margin between cards for list-like layouts.
3. **Icons:** Use Material icons with `app:tint="@color/text_secondary_adaptive"` for secondary actions (e.g. expand chevron).
4. **Labels:** Follow [TERMINOLOGY_AND_COPY.md](./TERMINOLOGY_AND_COPY.md) for "OOR", "Out of route", "miles", etc.

---

## Form factors and large screens (Blind Spot Plan §12)

- **Current target:** Phone. Tablet and foldable are **not** explicitly optimized (no `res/values-sw600dp` or tablet-specific layouts).
- **If supporting larger screens later:** Add `sw600dp` (or similar) resources and test on large devices; review layout and navigation for multi-pane or expanded use.

---

*Ref: [FEATURE_BRIEF_stat_cards_calendar_history.md](../product/FEATURE_BRIEF_stat_cards_calendar_history.md); [POLISHING_PLAN_PROMPT.md](../archive/prompts/POLISHING_PLAN_PROMPT.md) §3.*
