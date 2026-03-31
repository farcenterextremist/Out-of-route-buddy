# Android M3 Design Study — Reference (Intermediate)

## Official anchors (bookmark)

- Material Design 3: https://m3.material.io/
- Android Views & themes: https://developer.android.com/develop/ui/views
- Accessibility: https://developer.android.com/guide/topics/ui/accessibility
- Dynamic color (Material You): https://developer.android.com/develop/ui/views/theming/dynamic-colors

## Week 1 — System & structure

| Topic | Action |
|--------|--------|
| Color roles | Map `primary`, `onPrimary`, `surface`, `onSurface`, `error`, `outline` to existing app colors; flag one-off hex without a role. |
| Dynamic color | On device: compare app with wallpaper-driven palette on/off. |
| Type scale | List `TextAppearance` / text styles; align to M3 scale names mentally (display, headline, title, body, label). |
| Shape | Pick one corner family (e.g. 12dp) for cards/dialogs in a flow. |

**Deliverable:** One-page “role map” for the screen you care about most.

## Week 2 — States, touch, motion, a11y

| Topic | Action |
|--------|--------|
| States | For primary CTA: visible disabled? pressed/focused? |
| Touch targets | Minimum ~48dp; list two undersized tappable views. |
| Motion | One transition worth adding vs one that would add noise. |
| A11y | `contentDescription` on icon-only controls; one contrast check (pair hex in a contrast checker). |

**Deliverable:** Reusable pre-ship checklist (5–10 bullets).

## 15-minute daily habits

1. Screenshot same screen **light vs dark** — note one breakage.
2. Steal **one spacing rule** from a system app (section gaps).
3. One-sentence critique: hierarchy + single fix.

## Terminology

- **Affordance** — reads as interactive.
- **Progressive disclosure** — advanced options later.
- **Visual weight** — size/weight/color draws attention.
- **Whitespace** — groups content; not “empty.”
- **Semantic color** — error vs primary vs surface, not “favorite blue.”

## Optional week 3 (stretch)

- Large screens / window size classes for tablets/foldables.
- Edge-to-edge and gesture/back expectations per current Android docs.
- Compose: `MaterialTheme` and token flow—only if project direction includes Compose.

## OutOfRouteBuddy tie-in

- Pleasantness rubric: `docs/ux/PLEASANTNESS_AND_FLOW_STANDARD.md` (and related `docs/ux/*`).
- Use **frontend-pleasantness-reviewer** skill when evaluating shipped-quality screens.
