# OutOfRouteBuddy Emulator Perfection Plan

A detailed roadmap to achieve a true 1:1 emulator that matches the real Android app in appearance, behavior, and functionality.

---

## 1. Visual / Layout Gaps (1:1 Fidelity)

### 1.1 Toolbar
- [ ] **Settings icon**: Replace emoji `⚙` with actual SVG/icon matching `ic_settings.xml` (gear icon)
- [ ] **Toolbar lines**: Verify exact spacing (16dp start/end margin, 8dp gap) matches `custom_toolbar.xml`
- [ ] **Action bar height**: Confirm 56dp (`?attr/actionBarSize`) is exact

### 1.2 Statistics Button
- [ ] **Arrow icon**: Real app uses `ic_arrow_up` / `ic_arrow_down` as Material icon (chevron), not text `▲`/`▼`
- [ ] **Icon position**: Real app has `app:iconGravity="textEnd"` — arrow on the right side of text
- [ ] **Icon when collapsed**: Should show `ic_arrow_down` (expand hint); when expanded show `ic_arrow_up` (collapse hint)

### 1.3 Input Fields
- [ ] **Input type**: Real app uses `numberDecimal` and `digits="0123456789."` — emulator should restrict to numeric input
- [ ] **No stroke on light mode**: `card_white_rounded` has no border; `input_normal_background` has 1dp stroke. Trip inputs use `card_white_rounded` (no border)
- [ ] **Elevation**: Real app has `android:elevation="2dp"` on inputs

### 1.4 Start Trip Button
- [ ] **Pause button**: Real app has a Pause/Resume button (48dp, icon-only) to the right of Start when trip is active. **Currently missing in emulator**
- [ ] **Button states**: Start Trip vs End Trip — button text changes based on trip state
- [ ] **Progress bar**: Real app shows a ProgressBar below Start button when loading — **currently missing**

### 1.5 Today's Info Card
- [ ] **Exact padding**: 10dp vertical, 10dp horizontal per layout
- [ ] **Row structure**: RelativeLayout with label left, value right; verify alignment

### 1.6 Statistics Section
- [ ] **Change period button text**: Real app uses `statistics_change_period_button` = "View" (from strings.xml)
- [ ] **Divider color**: Uses `light_gray_background` (#F5F5F5 light / #2C2C2C dark), not `divider_adaptive`
- [ ] **Statistics row labels**: Secondary text color for labels (Total Miles, OOR Miles, OOR %), primary for values

### 1.7 Dark Mode
- [ ] **Card background**: `drawable-night/card_white_rounded.xml` uses #2C2C2C
- [ ] **Gradient**: `drawable-night/gradient_grey_appbar.xml` is #1E1E1E → #121212
- [ ] **Toolbar**: Light mode uses `#000000` (primary); dark mode uses `#1E1E1E` — verify toolbar matches

---

## 2. Missing UI Elements

### 2.1 Pause/Resume Button
- **Location**: To the right of Start Trip button, only visible when trip is active
- **States**: Pause icon when running, Play icon when paused
- **Behavior**: Toggle pause/resume (emulator can simulate with toast/state)
- **Layout**: 48dp width, `ic_pause` / `ic_play` drawables

### 2.2 Progress Bar
- **Location**: Below Start Trip button
- **Visibility**: Shown when trip is starting/loading
- **Emulator**: Can show briefly when "Start Trip" is clicked (simulated)

### 2.3 Trip State (Start vs End)
- **Start Trip**: Default state; validates loaded/bounce miles before starting
- **End Trip**: When trip active; shows confirmation dialog (End Trip / Clear Trip / Continue Trip)
- **Emulator**: Add simple state machine — idle → active → (optional) paused

---

## 3. Non-Functioning / Simulated Activities

### 3.1 Settings Button
- **Current**: Shows toast "Settings"
- **Real app**: Opens Settings dialog (Mode, Templates, Help & Info)
- **Plan**: Add a modal/dialog that mimics Settings layout (Mode: Light/Dark, Templates, Help & Info button). Mode can sync with emulator theme toggle.

### 3.2 Start Trip Button
- **Current**: Shows toast "Start Trip"
- **Real app**: Validates inputs, starts GPS tracking, changes to "End Trip", shows Pause button
- **Plan**: 
  - Validate loaded/bounce miles (show toast if empty)
  - Toggle to "End Trip" and show Pause button when "started"
  - Simulate progress bar briefly on start

### 3.3 End Trip Flow
- **Real app**: Confirmation dialog with End Trip / Clear Trip / Continue Trip
- **Plan**: Add modal with three buttons; "End Trip" resets to idle state

### 3.4 Pause/Resume Button
- **Real app**: Pauses/resumes GPS distance accumulation
- **Plan**: Toggle icon (pause ↔ play), show toast "Trip paused" / "Trip resumed"

### 3.5 Statistics "View" (Change Period) Button
- **Current**: Shows toast "View"
- **Real app**: Opens calendar/date picker to select period
- **Plan**: Add a simple date picker modal or period selector (e.g., "This Week", "This Month", "Custom")

### 3.6 Input Validation
- **Real app**: Loaded and bounce miles must be non-empty, numeric, positive
- **Plan**: Add `inputmode="decimal"` and pattern validation; show error state (red border) like `input_error_background.xml`

---

## 4. Cursor Exporter Gaps

### 4.1 Missing Mappings
- [ ] `statisticsPeriod.label` → `statistics_selected_period_label`
- [ ] `statisticsPeriod.button` → `statistics_change_period_button`
- [ ] `statisticsPeriod.value` → (display only, may not map to strings)

### 4.2 Input Value Sync
- [ ] When user types in Loaded/Bounce miles, persist to design state (`loadedMiles.value`, `bounceMiles.value`) so it survives re-render
- [ ] Consider whether input values should be exported (they're user data, not string resources)

---

## 5. Editor / UX Improvements

**Comprehensive plan:** See **`docs/agents/EMULATOR_EDITING_TEAM_CONSULTATION_AND_TODO.md`** for team consultation and full todo: right-click edit anything, right-click empty space to add elements, real-time enactment, long-press, and live preview.

### 5.1 Context Menu
- [ ] **Touch devices**: Right-click not available; add long-press to open context menu
- [ ] **Keyboard**: Support keyboard shortcut to open edit (e.g., Enter when focused?)
- [ ] **Edit anything**: Ensure every visible text/control is editable (data-edit-path/key + .editable)
- [ ] **Add element here**: Right-click empty/container → "Add element here" → dialog (type + text) → inject and re-render in real time

### 5.2 Properties Panel
- [ ] **Input type for numbers**: When editing numeric fields, use `type="number"` or restrict input
- [ ] **Placeholder vs value**: For inputs, allow editing both hint (placeholder) and value separately

### 5.3 Selected State
- [ ] **Outline color**: Use app accent (#03DAC6) — already done
- [ ] **Scroll into view**: When opening properties panel, ensure selected element is visible

---

## 6. Technical Debt / Robustness

### 6.1 Re-render and Event Listeners
- [ ] **Duplicate listeners**: Each `render()` adds new listeners; ensure no memory leaks or double-firing (e.g., stats button)
- [ ] **Event delegation**: Consider delegating clicks to a parent to avoid re-attaching on every render

### 6.2 Theme Toggle
- [ ] **Theme button**: Update text when theme changes (☀ Light / 🌙 Dark) — already done
- [ ] **Persistence**: Theme saved to localStorage — already done

### 6.3 Statistics Section
- [ ] **Animation**: Expand/collapse uses max-height transition — consider smoother animation
- [ ] **Arrow direction**: Match real app (down when collapsed, up when expanded)

---

## 7. Future Enhancements (Optional)

### 7.1 Additional Screens
- [ ] Trip History fragment (if added to nav graph)
- [ ] Settings/Preferences screen
- [ ] Help & Info dialog content

### 7.2 Accessibility
- [ ] Add `aria-label` / `title` to interactive elements
- [ ] Ensure focus order is logical
- [ ] Support reduced motion preference

### 7.3 Export Scope
- [ ] Support exporting layout changes (e.g., if we add new elements)
- [ ] Export to `colors.xml` if user changes theme colors in emulator

---

## 8. Implementation Priority

| Priority | Item | Effort | Impact |
|----------|------|--------|--------|
| P1 | Pause button + trip state (Start/End) | Medium | High |
| P1 | Statistics arrow icon (SVG, correct position) | Low | Medium |
| P1 | Input validation + error state | Low | Medium |
| P2 | Settings dialog (simplified) | Medium | Medium |
| P2 | End Trip confirmation dialog | Low | Medium |
| P2 | Progress bar on Start | Low | Low |
| P3 | Statistics View → period picker | Medium | Low |
| P3 | Long-press for touch devices | Low | Medium |
| P3 | Cursor exporter mappings | Low | Low |

---

## 9. Files to Modify (Summary)

| File | Changes |
|------|---------|
| `app-renderer.js` | Trip state, Pause button, Progress bar, validation, Settings/End Trip modals |
| `styles.css` | Pause button styles, progress bar, error input state, icon styles |
| `index.html` | Modal containers for Settings, End Trip, Period picker |
| `editor.js` | Long-press support, input value sync |
| `cursor-exporter.js` | Add statisticsPeriod mappings |
| **New** | `icons.js` or inline SVG for ic_settings, ic_arrow_up, ic_arrow_down, ic_pause, ic_play |

---

## 10. Reference: Real App Key Files

- **Layout**: `app/src/main/res/layout/fragment_trip_input.xml`
- **Toolbar**: `app/src/main/res/layout/custom_toolbar.xml`
- **Statistics row**: `app/src/main/res/layout/statistics_row.xml`
- **Colors**: `app/src/main/res/values/colors.xml`, `values-night/colors.xml`
- **Drawables**: `button_3d_material.xml`, `card_white_rounded.xml`, `gradient_grey_appbar.xml`
- **Strings**: `app/src/main/res/values/strings.xml`
- **Logic**: `TripInputFragment.kt` (setupClickListeners, updateUI, dialogs)

---

*Document created for future implementation. Pick items from the priority list and implement incrementally.*
