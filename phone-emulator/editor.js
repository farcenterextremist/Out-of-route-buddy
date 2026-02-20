/**
 * Right-click to edit - Context menu + Properties panel with Notes and Save
 */

let selectedElement = null;
let pendingContextMeta = null;
let pendingAddContainer = null;
let autoSyncDebounceTimer = null;
const AUTO_SYNC_DEBOUNCE_MS = 500;

function initEditor() {
  const resetBtn = document.getElementById('reset-design');
  const copyForCursorBtn = document.getElementById('copy-for-cursor');
  const exportBtn = document.getElementById('export-design');
  const importBtn = document.getElementById('import-design');
  const importFile = document.getElementById('import-file');
  const closePanel = document.getElementById('close-panel');
  const contextMenu = document.getElementById('context-menu');
  const contextEditBtn = document.getElementById('context-edit-btn');
  const propertiesPanel = document.getElementById('properties-panel');

  const themeToggle = document.getElementById('theme-toggle');
  themeToggle?.addEventListener('click', () => {
    const next = window.AppRenderer?.getTheme() === 'dark' ? 'light' : 'dark';
    window.AppRenderer?.setTheme(next);
  });

  resetBtn?.addEventListener('click', () => {
    if (confirm('Reset all edits to default design?')) {
      window.AppRenderer?.resetDesign();
      deselect();
    }
  });

  document.getElementById('undo-btn')?.addEventListener('click', () => {
    window.AppRenderer?.undo();
  });
  document.getElementById('redo-btn')?.addEventListener('click', () => {
    window.AppRenderer?.redo();
  });

  copyForCursorBtn?.addEventListener('click', async () => {
    const output = window.CursorExporter?.copyForCursor();
    if (output) {
      try {
        await window.CursorExporter?.copyToClipboard(output);
        copyForCursorBtn.textContent = 'Copied!';
        setTimeout(() => { copyForCursorBtn.textContent = 'Copy for Cursor'; }, 2000);
      } catch (err) {
        alert('Could not copy. Output:\n\n' + output);
      }
    } else {
      alert('No edits to copy. Make changes first.');
    }
  });

  async function runSyncToProject() {
    const syncUrl = localStorage.getItem('oorb-sync-url') || 'http://127.0.0.1:8765/sync';
    const design = window.AppRenderer?.getDesign();
    if (!design) return;
    try {
      const res = await fetch(syncUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(design),
      });
      const data = await res.json().catch(() => ({}));
      if (data.ok) {
        const msg = data.count > 0 ? `Synced ${data.count} string(s) to project.` : 'No changes to apply (values match default).';
        if (window.AppRenderer?.showToast) window.AppRenderer.showToast(msg);
      } else {
        const err = data.error || res.statusText || 'Sync failed';
        if (window.AppRenderer?.showToast) window.AppRenderer.showToast('Sync failed: ' + err);
      }
    } catch (err) {
      if (window.AppRenderer?.showToast) window.AppRenderer.showToast('Sync failed. Is the sync service running?');
    }
  }
  window.runSyncToProject = runSyncToProject;

  const syncToProjectBtn = document.getElementById('sync-to-project');
  syncToProjectBtn?.addEventListener('click', async () => {
    syncToProjectBtn.disabled = true;
    syncToProjectBtn.textContent = 'Syncing…';
    await runSyncToProject();
    syncToProjectBtn.disabled = false;
    syncToProjectBtn.textContent = 'Sync to project';
  });

  const loadFromProjectBtn = document.getElementById('load-from-project');
  loadFromProjectBtn?.addEventListener('click', async () => {
    const syncUrl = localStorage.getItem('oorb-sync-url') || 'http://127.0.0.1:8765/sync';
    const designUrl = syncUrl.replace(/\/sync\/?$/, '') + '/design';
    loadFromProjectBtn.disabled = true;
    loadFromProjectBtn.textContent = 'Loading…';
    try {
      const res = await fetch(designUrl);
      const data = await res.json().catch(() => ({}));
      if (data.ok && data.design) {
        window.AppRenderer?.mergeDesignFromProject(data.design);
        if (window.AppRenderer?.showToast) window.AppRenderer.showToast('Loaded design from project.');
        deselect();
      } else {
        const err = data.error || res.statusText || 'Load failed';
        if (window.AppRenderer?.showToast) window.AppRenderer.showToast('Load failed: ' + err);
      }
    } catch (err) {
      if (window.AppRenderer?.showToast) window.AppRenderer.showToast('Load failed. Is the sync service running?');
    }
    loadFromProjectBtn.disabled = false;
    loadFromProjectBtn.textContent = 'Load from project';
  });

  exportBtn?.addEventListener('click', () => {
    const design = window.AppRenderer?.getDesign();
    if (design) {
      const blob = new Blob([JSON.stringify(design, null, 2)], { type: 'application/json' });
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = 'oorb-emulator-design.json';
      a.click();
      URL.revokeObjectURL(a.href);
    }
  });

  importBtn?.addEventListener('click', () => importFile?.click());

  importFile?.addEventListener('change', (e) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (ev) => {
        try {
          const design = JSON.parse(ev.target?.result);
          localStorage.setItem('oorb-emulator-design', JSON.stringify(design));
          window.AppRenderer?.loadDesign();
          window.AppRenderer?.render();
          deselect();
        } catch (err) {
          alert('Invalid design file: ' + err.message);
        }
      };
      reader.readAsText(file);
    }
    e.target.value = '';
  });

  contextEditBtn?.addEventListener('click', () => {
    if (pendingContextMeta) {
      selectElement(selectedElement, pendingContextMeta);
      hideContextMenu();
      pendingContextMeta = null;
    }
  });

  document.getElementById('context-add-element-btn')?.addEventListener('click', () => {
    hideContextMenu();
    if (pendingAddContainer) showAddElementDialog(pendingAddContainer);
  });

  closePanel?.addEventListener('click', deselect);

  document.addEventListener('click', (e) => {
    if (!contextMenu?.contains(e.target) && !contextEditBtn?.contains(e.target)) {
      hideContextMenu();
    }
  });

  document.addEventListener('scroll', hideContextMenu, true);

  document.addEventListener('keydown', (e) => {
    const inInput = /^(INPUT|TEXTAREA|SELECT)$/.test(document.activeElement?.tagName);
    if (inInput) return;
    if (e.ctrlKey && e.key === 'z') {
      e.preventDefault();
      if (e.shiftKey) {
        window.AppRenderer?.redo();
      } else {
        window.AppRenderer?.undo();
      }
    } else if (e.ctrlKey && e.key === 'y') {
      e.preventDefault();
      window.AppRenderer?.redo();
    }
  });
}

// Use a single contextmenu listener on app-content (event delegation) so it works
// after every re-render without re-attaching to each .editable
// Improvement #7: long-press (500–600ms) opens same context menu as right-click
const LONG_PRESS_MS = 550;
let longPressTimer = null;
let longPressMeta = null;

function attachEditorListeners() {
  const appContent = document.getElementById('app-content');
  if (!appContent || appContent._contextMenuDelegationAttached) return;
  appContent._contextMenuDelegationAttached = true;

  appContent.addEventListener('contextmenu', function(e) {
    const el = e.target.closest('.editable');
    e.preventDefault();
    e.stopPropagation();
    if (el && el.dataset.editPath && el.dataset.editKey) {
      selectedElement = el;
      pendingContextMeta = { path: el.dataset.editPath, key: el.dataset.editKey, type: el.dataset.editType || 'text' };
      pendingAddContainer = null;
      document.getElementById('context-edit-btn').style.display = '';
      document.getElementById('context-add-element-btn').style.display = '';
    } else {
      selectedElement = null;
      pendingContextMeta = null;
      pendingAddContainer = e.target.closest('.app-body') || e.target.closest('.app-card') || appContent;
      document.getElementById('context-edit-btn').style.display = 'none';
      document.getElementById('context-add-element-btn').style.display = '';
    }
    showContextMenu(e.clientX, e.clientY);
  });

  appContent.addEventListener('touchstart', function(e) {
    const el = e.target.closest('.editable');
    if (!el?.dataset?.editPath || !el?.dataset?.editKey) return;
    longPressMeta = {
      path: el.dataset.editPath,
      key: el.dataset.editKey,
      type: el.dataset.editType || 'text',
      el,
      clientX: e.touches[0].clientX,
      clientY: e.touches[0].clientY,
    };
    longPressTimer = setTimeout(() => {
      longPressTimer = null;
      if (!longPressMeta) return;
      selectedElement = longPressMeta.el;
      pendingContextMeta = { path: longPressMeta.path, key: longPressMeta.key, type: longPressMeta.type };
      showContextMenu(longPressMeta.clientX, longPressMeta.clientY);
      longPressMeta = null;
    }, LONG_PRESS_MS);
  }, { passive: true });

  appContent.addEventListener('touchend', function() {
    if (longPressTimer) clearTimeout(longPressTimer);
    longPressTimer = null;
    longPressMeta = null;
  }, { passive: true });
  appContent.addEventListener('touchcancel', function() {
    if (longPressTimer) clearTimeout(longPressTimer);
    longPressTimer = null;
    longPressMeta = null;
  }, { passive: true });
}

function handleContextMenu(e) {
  e.preventDefault();
  e.stopPropagation();
  const el = e.currentTarget;
  const path = el.dataset.editPath;
  const key = el.dataset.editKey;
  if (!path || !key) return;
  selectedElement = el;
  pendingContextMeta = { path, key, type: el.dataset.editType || 'text' };
  showContextMenu(e.clientX, e.clientY);
}

function showContextMenu(x, y) {
  const menu = document.getElementById('context-menu');
  if (!menu) return;

  menu.style.left = `${x}px`;
  menu.style.top = `${y}px`;
  menu.classList.add('visible');

  requestAnimationFrame(() => {
    const rect = menu.getBoundingClientRect();
    if (rect.right > window.innerWidth) menu.style.left = `${x - rect.width}px`;
    if (rect.bottom > window.innerHeight) menu.style.top = `${y - rect.height}px`;
  });
}

function hideContextMenu() {
  document.getElementById('context-menu')?.classList.remove('visible');
}

function selectElement(el, meta) {
  deselect();
  selectedElement = el;
  el?.classList.add('selected');
  showPropertiesPanel(meta);
}

function deselect() {
  selectedElement?.classList.remove('selected');
  selectedElement = null;
  pendingContextMeta = null;
  hidePropertiesPanel();
  hideContextMenu();
}

function showPropertiesPanel(meta) {
  const panel = document.getElementById('properties-panel');
  const content = document.getElementById('panel-content');
  if (!panel || !content) return;

  const design = window.AppRenderer?.getDesign();
  const pathParts = meta.path.split('.');
  let value = design;
  for (const p of pathParts) value = value?.[p];
  const currentValue = value?.[meta.key] ?? '';
  const pathKey = `${meta.path}.${meta.key}`;
  const currentNote = window.AppRenderer?.getNote(pathKey) ?? '';

  const livePreview = localStorage.getItem('oorb-emulator-live-preview') === 'true';
  const autoSync = localStorage.getItem('oorb-emulator-auto-sync') === 'true';
  content.innerHTML = `
    <div class="property-group">
      <label><input type="checkbox" id="prop-live-preview" ${livePreview ? 'checked' : ''} /> Live preview (update as you type)</label>
    </div>
    <div class="property-group">
      <label><input type="checkbox" id="prop-auto-sync" ${autoSync ? 'checked' : ''} /> Auto-sync to project (after each Save)</label>
    </div>
    <div class="property-group">
      <label>Value</label>
      <input type="text" id="prop-value-input" value="${escapeAttr(currentValue)}" placeholder="Enter value" aria-label="Value" />
    </div>
    <div class="property-group">
      <label>Notes</label>
      <textarea id="prop-notes-input" placeholder="Add a note about this change..." aria-label="Notes">${escapeAttr(currentNote)}</textarea>
    </div>
    <button class="save-btn" id="save-prop-btn">Save</button>
  `;

  const input = content.querySelector('#prop-value-input');
  const notesInput = content.querySelector('#prop-notes-input');
  const saveBtn = content.querySelector('#save-prop-btn');
  const livePreviewCheckbox = content.querySelector('#prop-live-preview');

  let livePreviewDebounce = null;
  const LIVE_PREVIEW_DEBOUNCE_MS = 300;

  const doSave = () => {
    const newVal = input?.value ?? '';
    const newNote = notesInput?.value ?? '';
    window.AppRenderer?.updateDesign(pathKey, newVal);
    window.AppRenderer?.setNote(pathKey, newNote);
    saveBtn.textContent = 'Saved';
    saveBtn.classList.add('saved');
    setTimeout(() => {
      saveBtn.textContent = 'Save';
      saveBtn.classList.remove('saved');
    }, 1200);
    if (localStorage.getItem('oorb-emulator-auto-sync') === 'true' && window.runSyncToProject) {
      if (autoSyncDebounceTimer) clearTimeout(autoSyncDebounceTimer);
      autoSyncDebounceTimer = setTimeout(() => {
        autoSyncDebounceTimer = null;
        window.runSyncToProject();
      }, AUTO_SYNC_DEBOUNCE_MS);
    }
  };

  saveBtn?.addEventListener('click', doSave);

  input?.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') doSave();
  });

  livePreviewCheckbox?.addEventListener('change', () => {
    localStorage.setItem('oorb-emulator-live-preview', livePreviewCheckbox.checked ? 'true' : 'false');
  });
  content.querySelector('#prop-auto-sync')?.addEventListener('change', (e) => {
    localStorage.setItem('oorb-emulator-auto-sync', e.target.checked ? 'true' : 'false');
  });

  input?.addEventListener('input', () => {
    if (!livePreviewCheckbox?.checked) return;
    if (livePreviewDebounce) clearTimeout(livePreviewDebounce);
    livePreviewDebounce = setTimeout(() => {
      livePreviewDebounce = null;
      window.AppRenderer?.updateDesign(pathKey, input?.value ?? '');
    }, LIVE_PREVIEW_DEBOUNCE_MS);
  });

  panel.style.display = 'block';
  requestAnimationFrame(() => panel.classList.add('visible'));
  input?.focus();
}

function hidePropertiesPanel() {
  const panel = document.getElementById('properties-panel');
  if (panel) {
    panel.classList.remove('visible');
    setTimeout(() => {
      panel.style.display = 'none';
      document.getElementById('panel-content').innerHTML = '<p class="panel-placeholder">Right-click a field and choose Edit</p>';
    }, 150);
  }
}

function escapeAttr(str) {
  if (str == null) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

function showAddElementDialog(containerEl) {
  const dialog = document.getElementById('add-element-dialog');
  const typeSelect = document.getElementById('add-element-type');
  const textInput = document.getElementById('add-element-text');
  const textWrap = document.getElementById('add-element-text-wrap');
  if (!dialog || !typeSelect || !textInput) return;
  const containerId = (containerEl && (containerEl.classList.contains('app-body') || containerEl.id === 'app-content')) ? 'app-body' : (containerEl?.id || 'app-body');
  dialog._addContainerId = containerId;
  typeSelect.value = 'heading';
  textInput.value = '';
  textInput.placeholder = 'New heading';
  if (textWrap) textWrap.style.display = '';
  dialog.removeAttribute('hidden');
  textInput.focus();
  typeSelect.addEventListener('change', function onTypeChange() {
    const placeholders = { heading: 'New heading', label: 'New label', button: 'New button', textinput: 'Enter text', spacer: '' };
    textInput.placeholder = placeholders[typeSelect.value] || '';
    if (textWrap) textWrap.style.display = typeSelect.value === 'spacer' ? 'none' : '';
  }, { once: true });
}

function hideAddElementDialog() {
  const dialog = document.getElementById('add-element-dialog');
  if (dialog) dialog.setAttribute('hidden', '');
  deselect();
}

function initAddElementDialog() {
  const dialog = document.getElementById('add-element-dialog');
  const closeBtn = document.getElementById('add-element-close');
  const cancelBtn = document.getElementById('add-element-cancel');
  const confirmBtn = document.getElementById('add-element-confirm');
  const typeSelect = document.getElementById('add-element-type');
  const textInput = document.getElementById('add-element-text');
  closeBtn?.addEventListener('click', hideAddElementDialog);
  cancelBtn?.addEventListener('click', hideAddElementDialog);
  dialog?.addEventListener('click', (e) => { if (e.target === dialog) hideAddElementDialog(); });
  confirmBtn?.addEventListener('click', () => {
    const containerId = dialog._addContainerId || 'app-body';
    const type = typeSelect?.value || 'label';
    const text = (type === 'spacer') ? '' : (textInput?.value ?? '');
    window.AppRenderer?.addCustomElement(containerId, undefined, { type, text });
    hideAddElementDialog();
  });
}

const originalRender = window.AppRenderer?.render;
if (window.AppRenderer) {
  window.AppRenderer.render = function() {
    if (typeof originalRender === 'function') originalRender.call(this);
    setTimeout(attachEditorListeners, 0);
  };
}

document.addEventListener('DOMContentLoaded', () => {
  initEditor();
  initAddElementDialog();
  // Delegation listener on app-content (survives re-renders); run after first paint so app-content exists
  requestAnimationFrame(() => attachEditorListeners());
});
