---
name: frontend-design-consistency
description: Audits Android UI for visual consistency issues: duplicate icons, font size mismatches, spacing irregularities, color inconsistencies, and widget alignment problems. Use when polishing settings screens, preference lists, dialogs, toolbars, or any repeated UI pattern where elements should look uniform.
---

# Frontend Design Consistency Auditor

## Purpose

Catch visual inconsistencies that make screens look unpolished — duplicate icons, mismatched font sizes, uneven spacing, wrong colors in dark/light mode, and widgets that don't align. This is the "OCD pass" skill.

## Trigger

Use this skill when requests mention:

- "duplicate icons"
- "font size mismatch"
- "inconsistent spacing"
- "looks off"
- "polish the UI"
- "design audit"
- "visual consistency"
- "icon reuse"
- "settings look weird"
- Any request to tidy up repeated UI elements

## Audit Checklist

Run through every item. Report findings as a table.

### 1. Icon Uniqueness

- [ ] Every accordion/section header icon is different from its children's icons
- [ ] No two sibling preferences share the same icon unless they are genuinely related
- [ ] Header icons represent the section's purpose, not a specific child's function
- [ ] Light/dark mode: icons tint correctly via `@color/settings_pref_icon` or adaptive tint

### 2. Typography Hierarchy

- [ ] Section headers use `TextAppearance.OORB.SectionTitle` (18sp, bold, sans-serif-medium)
- [ ] Preference titles use default preference title style (~16sp, regular weight)
- [ ] Preference summaries use a lighter/smaller style than titles
- [ ] No child item has text larger than or equal to its parent header
- [ ] Dialog titles use `TextAppearance.OORB.SectionTitle`
- [ ] Body text uses `TextAppearance.OORB.Body` (16sp) or `BodySecondary` (14sp)

### 3. Spacing & Alignment

- [ ] All preferences have consistent vertical padding
- [ ] Icons are uniformly sized (24dp) with consistent `iconSpaceReserved="true"`
- [ ] Widget area (switches, arrows, dropdowns) is right-aligned and uniform width
- [ ] Toolbar elements (back button, title, action button) are symmetrically padded
- [ ] Accordion expand/collapse arrows are vertically centered

### 4. Color Consistency

- [ ] No hardcoded colors — use `@color/` adaptive resources
- [ ] Dark mode: text is visible (no black-on-dark, no white-on-light)
- [ ] Highlight/ripple colors use `@color/accent` (not teal, not yellow)
- [ ] Switch thumb/track tints use the custom `switch_thumb_tint` / `switch_track_tint`
- [ ] Dialog backgrounds use `@drawable/dialog_background` or `dialog_settings_background`

### 5. Widget State

- [ ] Switches show distinct checked vs unchecked states in both themes
- [ ] Disabled preferences are visually muted (alpha or gray)
- [ ] Selected/focused states don't use unexpected colors (teal, yellow)
- [ ] ListPreference dialogs use `OORBAlertDialog` style

## Files to Audit

Settings screens:
- `app/src/main/res/xml/preferences.xml` — icon and title assignments
- `app/src/main/res/layout/preference_accordion_widget.xml` — arrow widget
- `app/src/main/java/.../AccordionHeaderPreference.kt` — header styling
- `app/src/main/java/.../SettingsFragment.kt` — accordion behavior
- `app/src/main/res/layout/fragment_settings_root.xml` — settings toolbar

Theme/style files:
- `app/src/main/res/values/styles.xml` — text appearances, switch styles
- `app/src/main/res/values/themes.xml` — light theme
- `app/src/main/res/values-night/themes.xml` — dark theme
- `app/src/main/res/values/colors.xml` — color definitions

Toolbar/navigation:
- `app/src/main/res/layout/custom_toolbar.xml` — main toolbar
- `app/src/main/res/menu/drawer_menu.xml` — drawer items
- `app/src/main/res/layout/nav_drawer_header.xml` — drawer header

## Output Format

```
## Design Consistency Audit

| Category          | Issue                                    | File                  | Fix                          |
|-------------------|------------------------------------------|-----------------------|------------------------------|
| Icon Uniqueness   | Header X shares icon with child Y        | preferences.xml:10    | Change header to ic_sec_foo  |
| Typography        | Child title is same size as header       | AccordionHeader.kt    | Apply SectionTitle to header |
| Spacing           | Uneven padding between sections          | preferences.xml       | Add consistent margins       |
| Color             | Hardcoded #333 in dark mode              | styles.xml:45         | Use @color/adaptive          |

**Pass/Fail:** X/5 categories clean
```

## Rules

- Never change functionality — this skill is cosmetic only
- Prefer existing drawables over creating new ones
- Always check both light and dark mode
- Use adaptive colors (`@color/text_primary_adaptive`, etc.) — never hardcode
- Headers must be visually distinct from children (bigger, bolder, or different weight)
- When creating new icons, match the 24dp/24dp viewport convention and use `@color/settings_pref_icon` fill
