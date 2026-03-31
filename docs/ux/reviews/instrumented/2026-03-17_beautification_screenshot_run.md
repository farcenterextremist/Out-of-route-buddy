# Beautification screenshot run — 2026-03-17

**Device:** SM-S911U (Galaxy S23-class), 1080×2340 @ 480dpi  
**Build:** `installDebug` on connected device (`adb devices`)  
**Workflow:** `docs/ux/SCREENSHOT_REVIEW_WORKFLOW.md`

## Evidence pack

| # | File | Description |
|---|------|-------------|
| 1 | `2026-03-17_beautification_01_trip_input.png` | Trip input (main) — dark theme, toolbar, START TRIP, Today’s Info |
| 2 | `2026-03-17_beautification_02_nav_drawer.png` | Navigation drawer open — brand header, Trip / Trip History / Settings + custom icons |
| 3 | `2026-03-17_beautification_03_settings.png` | Full Settings screen — accordion sections, row icons, dark app bar |
| 4 | `2026-03-17_beautification_04_settings_scrolled.png` | Settings after scroll (lower sections) |

## Reproduce captures (PowerShell)

```powershell
$p = "docs\ux\reviews\instrumented"
adb shell am force-stop com.example.outofroutebuddy
adb shell monkey -p com.example.outofroutebuddy -c android.intent.category.LAUNCHER 1
Start-Sleep 6
cmd /c "adb exec-out screencap -p > \"$p\01_trip.png\""
adb shell input tap 72 260   # hamburger (adjust for your resolution)
Start-Sleep 1
cmd /c "adb exec-out screencap -p > \"$p\02_drawer.png\""
adb shell input tap 200 720  # Settings row (adjust Y if needed)
Start-Sleep 2
cmd /c "adb exec-out screencap -p > \"$p\03_settings.png\""
```

Binary-safe output on Windows: use `cmd /c "adb exec-out screencap -p > path.png"` (not `Set-Content` without byte mode).

## Quick rubric notes (pleasantness / flow)

1. **Trip screen:** Toolbar wordmark reads centered over full width; road texture + outline title is on-brand. Content block is calm; consider a single accent (teal) on primary CTA if hierarchy needs punch.
2. **Drawer:** Header band matches Settings app bar (teal story); centered title + version reads organized. Three rows are distinct icons (not stock `ic_menu_*`).
3. **Settings:** Dark app bar + back + section icons improve scanability; accordion keeps density manageable.

## Next optional shots

- Light theme (toggle Theme in Settings, return to trip + drawer).
- Active trip state (notification + trip UI).
- Help dialog.
