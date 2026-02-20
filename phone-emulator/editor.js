/**
 * Right-click to edit - Context menu + Properties panel with Notes and Save
 */

let selectedElement = null;
let pendingContextMeta = null;

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

  closePanel?.addEventListener('click', deselect);

  document.addEventListener('click', (e) => {
    if (!contextMenu?.contains(e.target) && !contextEditBtn?.contains(e.target)) {
      hideContextMenu();
    }
  });

  document.addEventListener('scroll', hideContextMenu, true);
}

// Use a single contextmenu listener on app-content (event delegation) so it works
// after every re-render without re-attaching to each .editable
function attachEditorListeners() {
  const appContent = document.getElementById('app-content');
  if (!appContent || appContent._contextMenuDelegationAttached) return;
  appContent._contextMenuDelegationAttached = true;

  appContent.addEventListener('contextmenu', function(e) {
    const el = e.target.closest('.editable');
    if (!el) return;
    e.preventDefault();
    e.stopPropagation();

    const path = el.dataset.editPath;
    const key = el.dataset.editKey;
    if (!path || !key) return;

    selectedElement = el;
    pendingContextMeta = { path, key, type: el.dataset.editType || 'text' };

    showContextMenu(e.clientX, e.clientY);
  });
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

  content.innerHTML = `
    <div class="property-group">
      <label>Value</label>
      <input type="text" id="prop-value-input" value="${escapeAttr(currentValue)}" placeholder="Enter value" />
    </div>
    <div class="property-group">
      <label>Notes</label>
      <textarea id="prop-notes-input" placeholder="Add a note about this change...">${escapeAttr(currentNote)}</textarea>
    </div>
    <button class="save-btn" id="save-prop-btn">Save</button>
  `;

  const input = content.querySelector('#prop-value-input');
  const notesInput = content.querySelector('#prop-notes-input');
  const saveBtn = content.querySelector('#save-prop-btn');

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
  };

  saveBtn?.addEventListener('click', doSave);

  input?.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') doSave();
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

const originalRender = window.AppRenderer?.render;
if (window.AppRenderer) {
  window.AppRenderer.render = function() {
    if (typeof originalRender === 'function') originalRender.call(this);
    setTimeout(attachEditorListeners, 0);
  };
}

document.addEventListener('DOMContentLoaded', () => {
  initEditor();
  // Delegation listener on app-content (survives re-renders); run after first paint so app-content exists
  requestAnimationFrame(() => attachEditorListeners());
});
