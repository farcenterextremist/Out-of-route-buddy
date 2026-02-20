# Dark theme — Trip and History

**Owner:** Front-end Engineer  
**Purpose:** Verify Trip and History screens respect dark theme (no white flashes or wrong contrast).  
**Related:** 25-point #10, `docs/agents/APP_IMPROVEMENT_25_POINT_BRAINSTORM.md`.

---

## Requirement

- All Trip and History screens (including dialogs and list items) should use theme colors so that when the user has dark theme enabled, backgrounds and text use dark-theme values.
- No **white flashes** (e.g. default window background) on transition or dialog open; use `?attr/colorSurface`, `?attr/colorOnSurface`, and theme-aware drawables.

---

## What to check

- Trip input fragment, Start/End Trip dialogs, History list and item layout, History-by-date dialog.
- Statistics section (if separate fragment/layout): ensure it uses theme attributes.
- Any full-screen or dialog that might use a hardcoded white background.

---

## Quick fixes (if needed)

- Replace hardcoded `#FFFFFF` or `android:color/white` with `?attr/colorSurface` or `?android:attr/colorBackground`.
- Ensure text colors use `?attr/colorOnSurface` or `?attr/colorOnPrimary` as appropriate.
- Test on device/emulator with dark theme enabled.

---

*Verification task; document "Done" in 25-point list when verified or fixed.*
