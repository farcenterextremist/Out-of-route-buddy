/**
 * OutOfRouteBuddy App Renderer
 * Renders the app UI inside the phone emulator based on design state
 * Improvement #1: SVG icons (gear, chevron, pause, play) instead of emoji
 */

// Inline SVGs for 1:1 app-like look (Improvement #1 – Front-end / UI/UX)
const ICONS = {
  gear: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>',
  chevronDown: '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><polyline points="6 9 12 15 18 9"/></svg>',
  chevronUp: '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><polyline points="18 15 12 9 6 15"/></svg>',
  pause: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>',
  play: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><polygon points="5 3 19 12 5 21 5 3"/></svg>',
};

const DEFAULT_DESIGN = {
  toolbar: { title: 'OOR', settingsIcon: '⚙' },
  loadedMiles: { hint: 'Loaded Miles', value: '' },
  bounceMiles: { hint: 'Bounce Miles', value: '' },
  startButton: { text: 'Start Trip' },
  pauseButton: { title: 'Pause trip' },
  todaysInfo: { title: "Today's Info" },
  totalMiles: { label: 'Total Miles', value: '0.0' },
  oorMiles: { label: 'OOR Miles', value: '0.0' },
  oorPercent: { label: 'OOR %', value: '0.0%' },
  statisticsButton: { text: 'STATISTICS' },
  statisticsPeriod: { label: 'Calendar/Current period', value: 'N/A', button: 'View' },
  weeklyStats: { title: 'Weekly Statistics', miles: '0.0', oor: '0.0', percent: '0.0%' },
  monthlyStats: { title: 'Monthly Statistics', miles: '0.0', oor: '0.0', percent: '0.0%' },
  yearlyStats: { title: 'Yearly Statistics', miles: '0.0', oor: '0.0', percent: '0.0%' },
  customElements: [],
};

let designState = { ...JSON.parse(JSON.stringify(DEFAULT_DESIGN)), _notes: {} };
let appTheme = localStorage.getItem('oorb-emulator-theme') || 'light';
let statsExpanded = false;
// Trip state: 'idle' | 'active' | 'paused'
let tripState = 'idle';

// Phase A: Undo/Redo (max 20 states)
const UNDO_MAX = 20;
let undoStack = [];
let redoStack = [];

function pushUndo() {
  undoStack.push(JSON.parse(JSON.stringify(designState)));
  if (undoStack.length > UNDO_MAX) undoStack.shift();
}

function clearRedo() {
  redoStack = [];
}

function undo() {
  if (undoStack.length === 0) return;
  redoStack.push(JSON.parse(JSON.stringify(designState)));
  designState = undoStack.pop();
  saveDesign();
  render();
  updateUndoRedoButtons();
}

function redo() {
  if (redoStack.length === 0) return;
  undoStack.push(JSON.parse(JSON.stringify(designState)));
  designState = redoStack.pop();
  saveDesign();
  render();
  updateUndoRedoButtons();
}

function canUndo() {
  return undoStack.length > 0;
}

function canRedo() {
  return redoStack.length > 0;
}

function updateUndoRedoButtons() {
  const undoBtn = document.getElementById('undo-btn');
  const redoBtn = document.getElementById('redo-btn');
  if (undoBtn) {
    undoBtn.disabled = !canUndo();
  }
  if (redoBtn) {
    redoBtn.disabled = !canRedo();
  }
}

function deepMerge(target, source) {
  const out = { ...target };
  for (const key of Object.keys(source)) {
    if (source[key] && typeof source[key] === 'object' && !Array.isArray(source[key]) &&
        target[key] && typeof target[key] === 'object') {
      out[key] = deepMerge(target[key], source[key]);
    } else if (source[key] !== undefined) {
      out[key] = source[key];
    }
  }
  return out;
}

function loadDesign() {
  try {
    const saved = localStorage.getItem('oorb-emulator-design');
    const base = JSON.parse(JSON.stringify(DEFAULT_DESIGN));
    base._notes = {};
    if (saved) {
      const parsed = JSON.parse(saved);
      designState = deepMerge(base, parsed);
      designState._notes = parsed._notes || {};
      designState.customElements = Array.isArray(parsed.customElements) ? parsed.customElements : (designState.customElements || []);
    } else {
      designState = base;
    }
  } catch (e) {
    console.warn('Failed to load design:', e);
  }
}

function saveDesign() {
  try {
    localStorage.setItem('oorb-emulator-design', JSON.stringify(designState));
  } catch (e) {
    console.warn('Failed to save design:', e);
  }
}

function getDesign() {
  return designState;
}

/** Merge design from project (Load from project). Pushes undo so user can revert. */
function mergeDesignFromProject(design) {
  if (!design || typeof design !== 'object') return;
  pushUndo();
  clearRedo();
  designState = deepMerge(designState, design);
  saveDesign();
  render();
  updateUndoRedoButtons();
}

function updateDesign(path, value) {
  pushUndo();
  clearRedo();
  const parts = path.split('.');
  let obj = designState;
  for (let i = 0; i < parts.length - 1; i++) {
    const key = parts[i];
    if (!obj[key]) obj[key] = {};
    obj = obj[key];
  }
  obj[parts[parts.length - 1]] = value;
  saveDesign();
  render();
  updateUndoRedoButtons();
}

function getNote(pathKey) {
  return designState._notes?.[pathKey] ?? '';
}

function setNote(pathKey, note) {
  if (!designState._notes) designState._notes = {};
  designState._notes[pathKey] = note;
  saveDesign();
}

function resetDesign() {
  designState = { ...JSON.parse(JSON.stringify(DEFAULT_DESIGN)), _notes: {} };
  designState.customElements = [];
  tripState = 'idle';
  undoStack = [];
  redoStack = [];
  saveDesign();
  render();
  updateUndoRedoButtons();
}

function addCustomElement(containerId, index, payload) {
  if (!designState.customElements) designState.customElements = [];
  const id = 'ce-' + Date.now() + '-' + Math.random().toString(36).slice(2, 9);
  const item = { id, type: payload.type || 'label', text: payload.text || '', containerId: containerId || 'app-body', index: index ?? designState.customElements.length };
  designState.customElements.push(item);
  pushUndo();
  clearRedo();
  saveDesign();
  render();
  updateUndoRedoButtons();
}

function createEditable(el, dataPath, dataKey, type = 'text') {
  el.dataset.editPath = dataPath;
  el.dataset.editKey = dataKey;
  el.dataset.editType = type;
  el.classList.add('editable');
  return el;
}

function setTheme(theme) {
  appTheme = theme === 'dark' ? 'dark' : 'light';
  localStorage.setItem('oorb-emulator-theme', appTheme);
  const screen = document.getElementById('phone-screen');
  const container = document.getElementById('app-content');
  if (screen) screen.setAttribute('data-theme', appTheme);
  if (container) container.setAttribute('data-theme', appTheme);
  document.getElementById('theme-toggle')?.setAttribute('title', appTheme === 'dark' ? 'Switch to Light mode' : 'Switch to Dark mode');
  document.getElementById('theme-toggle').textContent = appTheme === 'dark' ? '🌙 Dark' : '☀ Light';
}

function getTheme() {
  return appTheme;
}

function toggleStats() {
  statsExpanded = !statsExpanded;
  const section = document.getElementById('app-stats-section');
  const arrow = document.getElementById('stats-arrow');
  if (section) section.classList.toggle('expanded', statsExpanded);
  if (arrow) arrow.innerHTML = statsExpanded ? ICONS.chevronUp : ICONS.chevronDown;
}

function setTripState(state) {
  tripState = state;
  render();
}

function getTripState() {
  return tripState;
}

function showToast(msg) {
  const t = document.createElement('div');
  t.className = 'app-toast';
  t.textContent = msg;
  t.style.cssText = 'position:fixed;bottom:120px;left:50%;transform:translateX(-50%);background:rgba(0,0,0,0.85);color:#fff;padding:10px 20px;border-radius:8px;font-size:14px;z-index:9999;box-shadow:0 4px 12px rgba(0,0,0,0.3);';
  document.body.appendChild(t);
  setTimeout(() => t.remove(), 2000);
}

// Improvement #2: Settings modal (Mode, Templates, Help & Info)
function showSettingsModal() {
  const overlay = document.getElementById('settings-modal');
  if (!overlay) return;
  overlay.removeAttribute('hidden');
  overlay.classList.add('visible');
  const theme = getTheme();
  document.querySelectorAll('.app-mode-btn').forEach((btn) => {
    btn.classList.toggle('active', btn.dataset.mode === theme);
  });
}
function hideSettingsModal() {
  const overlay = document.getElementById('settings-modal');
  if (overlay) {
    overlay.classList.remove('visible');
    overlay.setAttribute('hidden', '');
  }
}

// Improvement #3: End Trip confirmation modal
function showEndTripModal() {
  const overlay = document.getElementById('end-trip-modal');
  if (!overlay) return;
  overlay.removeAttribute('hidden');
  overlay.classList.add('visible');
}
function hideEndTripModal() {
  const overlay = document.getElementById('end-trip-modal');
  if (overlay) {
    overlay.classList.remove('visible');
    overlay.setAttribute('hidden', '');
  }
}

// Improvement #6: Statistics period picker
function showPeriodPickerModal() {
  const overlay = document.getElementById('period-picker-modal');
  if (!overlay) return;
  overlay.removeAttribute('hidden');
  overlay.classList.add('visible');
}
function hidePeriodPickerModal() {
  const overlay = document.getElementById('period-picker-modal');
  if (overlay) {
    overlay.classList.remove('visible');
    overlay.setAttribute('hidden', '');
  }
}

function render() {
  const container = document.getElementById('app-content');
  const screen = document.getElementById('phone-screen');
  if (!container) return;

  // Persist current input values to design state before we replace innerHTML
  container.querySelectorAll('.app-input').forEach((input) => {
    const path = input.dataset.editPath;
    if (!path || !designState[path]) return;
    const val = input.value;
    if (designState[path].value !== val) {
      designState[path].value = val;
      saveDesign();
    }
  });

  const d = designState;
  container.setAttribute('data-theme', appTheme);
  if (screen) screen.setAttribute('data-theme', appTheme);

  container.innerHTML = `
    <div class="app-fragment">
      <!-- Toolbar (matches custom_toolbar.xml) -->
      <div class="app-toolbar">
        <div class="app-toolbar-center">
          <span class="app-toolbar-line"></span>
          <span class="app-toolbar-title editable" data-edit-path="toolbar" data-edit-key="title" data-edit-type="text">${escapeHtml(d.toolbar.title)}</span>
          <span class="app-toolbar-line"></span>
        </div>
        <button type="button" class="app-toolbar-settings editable app-toolbar-settings-svg" data-edit-path="toolbar" data-edit-key="settingsIcon" data-edit-type="text" title="Settings" aria-label="Settings">${ICONS.gear}</button>
      </div>

      <div class="app-body">
        <!-- Loaded Miles (numberDecimal) -->
        <input type="text" inputmode="decimal" pattern="[0-9.]*" class="app-input editable" placeholder="${escapeHtml(d.loadedMiles.hint)}" value="${escapeHtml(d.loadedMiles.value)}"
          data-edit-path="loadedMiles" data-edit-key="hint" data-edit-type="placeholder" />

        <!-- Bounce Miles (numberDecimal) -->
        <input type="text" inputmode="decimal" pattern="[0-9.]*" class="app-input editable" placeholder="${escapeHtml(d.bounceMiles.hint)}" value="${escapeHtml(d.bounceMiles.value)}"
          data-edit-path="bounceMiles" data-edit-key="hint" data-edit-type="placeholder" />

        <!-- Start/End Trip + Pause row -->
        <div class="app-trip-actions">
          <button type="button" class="app-button editable app-trip-main" id="start-end-btn" data-edit-path="startButton" data-edit-key="text" data-edit-type="text">
            ${escapeHtml(tripState === 'idle' ? d.startButton.text : 'End Trip')}
          </button>
          ${tripState !== 'idle' ? `<button type="button" class="app-button app-pause-btn" id="pause-btn" title="${escapeHtml(d.pauseButton?.title || 'Pause')}" aria-label="${tripState === 'paused' ? 'Resume' : 'Pause'}">${tripState === 'paused' ? ICONS.play : ICONS.pause}</button>` : ''}
        </div>
        <div class="app-progress-wrap" id="progress-wrap" style="display:none">
          <div class="app-progress-bar"></div>
        </div>

        <!-- Today's Info -->
        <h3 class="app-section-title editable" data-edit-path="todaysInfo" data-edit-key="title" data-edit-type="text">
          ${escapeHtml(d.todaysInfo.title)}
        </h3>
        <div class="app-card">
          <div class="app-card-row">
            <span class="editable" data-edit-path="totalMiles" data-edit-key="label" data-edit-type="text">${escapeHtml(d.totalMiles.label)}</span>
            <span class="value editable" data-edit-path="totalMiles" data-edit-key="value" data-edit-type="text">${escapeHtml(d.totalMiles.value)}</span>
          </div>
          <div class="app-card-row">
            <span class="editable" data-edit-path="oorMiles" data-edit-key="label" data-edit-type="text">${escapeHtml(d.oorMiles.label)}</span>
            <span class="value editable" data-edit-path="oorMiles" data-edit-key="value" data-edit-type="text">${escapeHtml(d.oorMiles.value)}</span>
          </div>
          <div class="app-card-row">
            <span class="editable" data-edit-path="oorPercent" data-edit-key="label" data-edit-type="text">${escapeHtml(d.oorPercent.label)}</span>
            <span class="value editable" data-edit-path="oorPercent" data-edit-key="value" data-edit-type="text">${escapeHtml(d.oorPercent.value)}</span>
          </div>
        </div>

        <!-- Statistics Button (icon gravity textEnd - arrow on right) -->
        <button type="button" class="app-button editable app-statistics-btn" id="statistics-btn" data-edit-path="statisticsButton" data-edit-key="text" data-edit-type="text">
          <span class="stats-btn-text">${escapeHtml(d.statisticsButton.text)}</span>
          <span id="stats-arrow" class="stats-arrow">${ICONS.chevronDown}</span>
        </button>

        <!-- Statistics Section (expandable, matches statistics_content layout) -->
        <div class="app-stats-section" id="app-stats-section">
          <p class="app-stats-period-label">${escapeHtml(d.statisticsPeriod.label)}</p>
          <div class="app-stats-period-row">
            <span class="app-stats-period-value">${escapeHtml(d.statisticsPeriod.value)}</span>
            <button type="button" class="app-button-outlined editable" data-edit-path="statisticsPeriod" data-edit-key="button" data-edit-type="text">${escapeHtml(d.statisticsPeriod.button)}</button>
          </div>
          <div class="app-stats-divider"></div>
          <div class="app-stat-block editable" data-edit-path="weeklyStats" data-edit-key="title" data-edit-type="text">
            <h4>${escapeHtml(d.weeklyStats.title)}</h4>
            <div class="app-stat-row"><span class="stat-label">${escapeHtml(d.totalMiles.label)}</span><span class="stat-value">${escapeHtml(d.weeklyStats.miles)}</span></div>
            <div class="app-stat-row"><span class="stat-label">${escapeHtml(d.oorMiles.label)}</span><span class="stat-value">${escapeHtml(d.weeklyStats.oor)}</span></div>
            <div class="app-stat-row"><span class="stat-label">${escapeHtml(d.oorPercent.label)}</span><span class="stat-value">${escapeHtml(d.weeklyStats.percent)}</span></div>
          </div>
          <div class="app-stats-divider"></div>
          <div class="app-stat-block editable" data-edit-path="monthlyStats" data-edit-key="title" data-edit-type="text">
            <h4>${escapeHtml(d.monthlyStats.title)}</h4>
            <div class="app-stat-row"><span class="stat-label">${escapeHtml(d.totalMiles.label)}</span><span class="stat-value">${escapeHtml(d.monthlyStats.miles)}</span></div>
            <div class="app-stat-row"><span class="stat-label">${escapeHtml(d.oorMiles.label)}</span><span class="stat-value">${escapeHtml(d.monthlyStats.oor)}</span></div>
            <div class="app-stat-row"><span class="stat-label">${escapeHtml(d.oorPercent.label)}</span><span class="stat-value">${escapeHtml(d.monthlyStats.percent)}</span></div>
          </div>
          <div class="app-stats-divider"></div>
          <div class="app-stat-block editable" data-edit-path="yearlyStats" data-edit-key="title" data-edit-type="text">
            <h4>${escapeHtml(d.yearlyStats.title)}</h4>
            <div class="app-stat-row"><span class="stat-label">${escapeHtml(d.totalMiles.label)}</span><span class="stat-value">${escapeHtml(d.yearlyStats.miles)}</span></div>
            <div class="app-stat-row"><span class="stat-label">${escapeHtml(d.oorMiles.label)}</span><span class="stat-value">${escapeHtml(d.yearlyStats.oor)}</span></div>
            <div class="app-stat-row"><span class="stat-label">${escapeHtml(d.oorPercent.label)}</span><span class="stat-value">${escapeHtml(d.yearlyStats.percent)}</span></div>
          </div>
        </div>
      </div>
    </div>
  `;

  // Add spacing styles (matches fragment_trip_input.xml padding/margins)
  const style = document.createElement('style');
  style.textContent = `
    .app-body { padding: 16px; }
    .app-body .app-input { margin: 12px auto 0; display: block; }
    .app-body .app-input:first-of-type { margin-top: 16px; }
    .app-body .app-button { margin: 12px auto; display: block; }
    .app-section-title { margin: 16px 0 8px; font-size: 20px; font-weight: bold; }
    .app-card { margin: 8px 0 24px; }
    .app-trip-actions { display: flex; align-items: center; justify-content: center; gap: 8px; margin: 12px auto; }
    .app-trip-main { flex: 1; max-width: 280px; }
    .app-pause-btn { width: 48px; min-width: 48px; padding: 12px; }
    .app-progress-wrap { margin: 0 auto 12px; height: 4px; background: var(--divider); border-radius: 2px; overflow: hidden; max-width: 320px; }
    .app-progress-bar { height: 100%; width: 30%; background: var(--primary); animation: progress-indeterminate 1s ease-in-out infinite; }
    @keyframes progress-indeterminate { 0% { transform: translateX(-100%); } 100% { transform: translateX(400%); } }
    .app-statistics-btn { width: 100%; display: flex; align-items: center; justify-content: center; gap: 8px; }
    .stats-arrow { margin-left: auto; }
  `;
  container.appendChild(style);

  // Inject custom elements (Phase E: Add element here)
  const appBody = container.querySelector('.app-body');
  const customList = designState.customElements || [];
  customList.forEach((item, i) => {
    if ((item.containerId || 'app-body') !== 'app-body' || !appBody) return;
    let node;
    const text = escapeHtml(item.text || '');
    switch (item.type) {
      case 'heading':
        node = document.createElement('h3');
        node.className = 'app-section-title editable';
        node.textContent = item.text || 'New Heading';
        break;
      case 'label':
        node = document.createElement('span');
        node.className = 'editable';
        node.textContent = item.text || 'New Label';
        break;
      case 'button':
        node = document.createElement('button');
        node.type = 'button';
        node.className = 'app-button editable';
        node.textContent = item.text || 'New Button';
        break;
      case 'textinput':
        node = document.createElement('input');
        node.type = 'text';
        node.className = 'app-input editable';
        node.placeholder = item.text || 'Enter text';
        break;
      case 'spacer':
        node = document.createElement('div');
        node.className = 'app-spacer';
        node.style.minHeight = '16px';
        break;
      default:
        node = document.createElement('span');
        node.className = 'editable';
        node.textContent = item.text || '';
    }
    if (item.type !== 'spacer') {
      node.dataset.editPath = 'customElements.' + i;
      node.dataset.editKey = 'text';
      node.dataset.editType = 'text';
      node.id = 'custom-el-' + (item.id || i);
    }
    appBody.appendChild(node);
  });

  // Single event delegation for app-content (no duplicate listeners on re-render)
  if (!container._delegationAttached) {
    container._delegationAttached = true;
    container.addEventListener('input', (e) => {
      if (e.target.classList?.contains('app-input')) e.target.classList.remove('app-input-error');
    });
    container.addEventListener('click', (e) => {
      const target = e.target.closest('button, .editable');
      if (!target) return;
      if (target.closest('.app-toolbar-settings')) {
        showSettingsModal();
        return;
      }
      if (target.id === 'start-end-btn') {
        if (tripState === 'idle') {
          // Improvement #5: validate loaded/bounce miles before starting
          const loadedInput = container.querySelector('[data-edit-path="loadedMiles"]');
          const bounceInput = container.querySelector('[data-edit-path="bounceMiles"]');
          const loadedVal = loadedInput?.value?.trim() ?? '';
          const bounceVal = bounceInput?.value?.trim() ?? '';
          const numeric = /^[0-9]*\.?[0-9]*$/;
          const loadedOk = numeric.test(loadedVal) && loadedVal !== '';
          const bounceOk = numeric.test(bounceVal) && bounceVal !== '';
          loadedInput?.classList.toggle('app-input-error', !loadedOk);
          bounceInput?.classList.toggle('app-input-error', !bounceOk);
          if (!loadedOk || !bounceOk) {
            showToast('Enter valid numbers for Loaded and Bounce miles');
            return;
          }
          setTripState('active');
          // Improvement #4: progress bar on Start
          setTimeout(() => {
            const wrap = document.getElementById('progress-wrap');
            if (wrap) { wrap.style.display = 'block'; setTimeout(() => { wrap.style.display = 'none'; }, 1500); }
          }, 50);
        } else {
          showEndTripModal();
        }
        return;
      }
      if (target.id === 'pause-btn') {
        tripState = tripState === 'paused' ? 'active' : 'paused';
        showToast(tripState === 'paused' ? 'Trip paused' : 'Trip resumed');
        render();
        return;
      }
      if (target.closest('.app-statistics-btn')) {
        toggleStats();
        return;
      }
      if (target.closest('.app-button-outlined')) {
        showPeriodPickerModal();
        return;
      }
    });
  }

  // Re-attach editor listeners (context menu / properties panel)
  if (window.attachEditorListeners) {
    window.attachEditorListeners();
  }

  const statsSection = document.getElementById('app-stats-section');
  const arrow = document.getElementById('stats-arrow');
  if (statsSection) statsSection.classList.toggle('expanded', statsExpanded);
  if (arrow) arrow.innerHTML = statsExpanded ? ICONS.chevronUp : ICONS.chevronDown;

  updateUndoRedoButtons();
}

function escapeHtml(text) {
  if (text == null) return '';
  const div = document.createElement('div');
  div.textContent = String(text);
  return div.innerHTML;
}

// Improvement #2 & #3: Wire modal buttons (Settings, End Trip)
function initModals() {
  const settingsOverlay = document.getElementById('settings-modal');
  document.getElementById('settings-modal-close')?.addEventListener('click', hideSettingsModal);
  settingsOverlay?.addEventListener('click', (e) => { if (e.target === settingsOverlay) hideSettingsModal(); });
  document.querySelectorAll('.app-mode-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
      const mode = btn.dataset.mode;
      if (mode) setTheme(mode);
      document.querySelectorAll('.app-mode-btn').forEach((b) => b.classList.toggle('active', b.dataset.mode === getTheme()));
    });
  });
  document.getElementById('settings-templates')?.addEventListener('click', () => { showToast('Templates'); hideSettingsModal(); });
  document.getElementById('settings-help')?.addEventListener('click', () => { showToast('Help & Info'); hideSettingsModal(); });

  const endTripOverlay = document.getElementById('end-trip-modal');
  document.getElementById('end-trip-modal-close')?.addEventListener('click', hideEndTripModal);
  endTripOverlay?.addEventListener('click', (e) => { if (e.target === endTripOverlay) hideEndTripModal(); });
  document.getElementById('end-trip-confirm')?.addEventListener('click', () => { setTripState('idle'); hideEndTripModal(); showToast('Trip ended'); });
  document.getElementById('end-trip-clear')?.addEventListener('click', () => { setTripState('idle'); hideEndTripModal(); showToast('Trip cleared'); });
  document.getElementById('end-trip-continue')?.addEventListener('click', hideEndTripModal);

  const periodOverlay = document.getElementById('period-picker-modal');
  document.getElementById('period-picker-close')?.addEventListener('click', hidePeriodPickerModal);
  periodOverlay?.addEventListener('click', (e) => { if (e.target === periodOverlay) hidePeriodPickerModal(); });
  document.getElementById('period-this-month')?.addEventListener('click', () => {
    updateDesign('statisticsPeriod.label', 'This month');
    updateDesign('statisticsPeriod.value', 'Current month');
    hidePeriodPickerModal();
    showToast('Showing this month');
  });
  document.getElementById('period-custom')?.addEventListener('click', () => {
    showToast('Custom period coming soon');
    hidePeriodPickerModal();
  });
}

// Improvement #10: Sync service reachability indicator (green dot when service responds)
function updateSyncIndicator() {
  const el = document.getElementById('sync-indicator');
  if (!el) return;
  const url = localStorage.getItem('oorb-sync-url') || 'http://127.0.0.1:8765/sync';
  fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: '{}' })
    .then(() => {
      el.classList.add('reachable');
      el.classList.remove('unreachable');
    })
    .catch(() => {
      el.classList.add('unreachable');
      el.classList.remove('reachable');
    });
}
function startSyncIndicatorPolling() {
  updateSyncIndicator();
  setInterval(updateSyncIndicator, 15000);
}

// Initialize after DOM ready so editor can attach
document.addEventListener('DOMContentLoaded', () => {
  loadDesign();
  setTheme(appTheme); // apply saved theme to screen
  initModals();
  render();
  updateUndoRedoButtons();
  startSyncIndicatorPolling();
});

// Export for editor
window.AppRenderer = {
  render,
  getDesign,
  updateDesign,
  getNote,
  setNote,
  resetDesign,
  saveDesign,
  loadDesign,
  mergeDesignFromProject,
  setTheme,
  getTheme,
  toggleStats,
  setTripState,
  getTripState,
  undo,
  redo,
  canUndo,
  canRedo,
  updateUndoRedoButtons,
  addCustomElement,
  DEFAULT_DESIGN,
};
