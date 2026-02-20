/**
 * OutOfRouteBuddy App Renderer
 * Renders the app UI inside the phone emulator based on design state
 */

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
};

let designState = { ...JSON.parse(JSON.stringify(DEFAULT_DESIGN)), _notes: {} };
let appTheme = localStorage.getItem('oorb-emulator-theme') || 'light';
let statsExpanded = false;
// Trip state: 'idle' | 'active' | 'paused'
let tripState = 'idle';

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

function updateDesign(path, value) {
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
  tripState = 'idle';
  saveDesign();
  render();
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
  if (arrow) arrow.textContent = statsExpanded ? '▲' : '▼';
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
        <button type="button" class="app-toolbar-settings editable" data-edit-path="toolbar" data-edit-key="settingsIcon" data-edit-type="text" title="Settings">⚙</button>
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
          ${tripState !== 'idle' ? `<button type="button" class="app-button app-pause-btn" id="pause-btn" title="${escapeHtml(d.pauseButton?.title || 'Pause')}" aria-label="${tripState === 'paused' ? 'Resume' : 'Pause'}">${tripState === 'paused' ? '▶' : '⏸'}</button>` : ''}
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
          <span id="stats-arrow" class="stats-arrow">▼</span>
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

  // Single event delegation for app-content (no duplicate listeners on re-render)
  if (!container._delegationAttached) {
    container._delegationAttached = true;
    container.addEventListener('click', (e) => {
      const target = e.target.closest('button, .editable');
      if (!target) return;
      if (target.closest('.app-toolbar-settings')) {
        showToast('Settings');
        return;
      }
      if (target.id === 'start-end-btn') {
        if (tripState === 'idle') {
          setTripState('active');
          setTimeout(() => {
            const wrap = document.getElementById('progress-wrap');
            if (wrap) { wrap.style.display = 'block'; setTimeout(() => { wrap.style.display = 'none'; }, 1500); }
          }, 50);
        } else {
          showToast('End Trip');
          setTripState('idle');
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
        showToast('View');
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
  if (arrow) arrow.textContent = statsExpanded ? '▲' : '▼';
}

function escapeHtml(text) {
  if (text == null) return '';
  const div = document.createElement('div');
  div.textContent = String(text);
  return div.innerHTML;
}

// Initialize after DOM ready so editor can attach
document.addEventListener('DOMContentLoaded', () => {
  loadDesign();
  setTheme(appTheme); // apply saved theme to screen
  render();
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
  setTheme,
  getTheme,
  toggleStats,
  setTripState,
  getTripState,
  DEFAULT_DESIGN,
};
