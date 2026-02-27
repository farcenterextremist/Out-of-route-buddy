/**
 * Exports emulator edits as Cursor-ready instructions
 * Maps design state to project files (strings.xml, etc.)
 */

const EMULATOR_TO_PROJECT = {
  'toolbar.title': { file: 'app/src/main/res/values/strings.xml', stringName: 'oor' },
  'loadedMiles.hint': { file: 'app/src/main/res/values/strings.xml', stringName: 'loaded_miles' },
  'bounceMiles.hint': { file: 'app/src/main/res/values/strings.xml', stringName: 'bounce_miles' },
  'startButton.text': { file: 'app/src/main/res/values/strings.xml', stringName: 'start_trip' },
  'todaysInfo.title': { file: 'app/src/main/res/values/strings.xml', stringName: 'todays_info' },
  'totalMiles.label': { file: 'app/src/main/res/values/strings.xml', stringName: 'total_miles' },
  'oorMiles.label': { file: 'app/src/main/res/values/strings.xml', stringName: 'oor_miles' },
  'oorPercent.label': { file: 'app/src/main/res/values/strings.xml', stringName: 'oor_percent' },
  'statisticsButton.text': { file: 'app/src/main/res/values/strings.xml', stringName: 'statistics' },
  'statisticsPeriod.label': { file: 'app/src/main/res/values/strings.xml', stringName: 'statistics_period_label' },
  'statisticsPeriod.button': { file: 'app/src/main/res/values/strings.xml', stringName: 'statistics_change_period_button' },
  'statisticsPeriod.value': { file: 'app/src/main/res/values/strings.xml', stringName: 'statistics_period_value' },
  'monthlyStats.title': { file: 'app/src/main/res/values/strings.xml', stringName: 'monthly_statistics' },
};

function getValueAtPath(obj, path) {
  return path.split('.').reduce((o, k) => o?.[k], obj);
}

function generateCursorInstructions(design, defaultDesign) {
  const changes = [];
  for (const [path, mapping] of Object.entries(EMULATOR_TO_PROJECT)) {
    const current = getValueAtPath(design, path);
    const original = getValueAtPath(defaultDesign, path);
    if (current !== undefined && current !== original) {
      changes.push({
        path,
        file: mapping.file,
        stringName: mapping.stringName,
        oldValue: original,
        newValue: current,
      });
    }
  }

  if (changes.length === 0) {
    return `No edits to apply. All values match the default design.`;
  }

  const lines = [
    'Apply these string changes to the OutOfRouteBuddy project. Update app/src/main/res/values/strings.xml:',
    '',
    ...changes.map(c => {
      const escaped = String(c.newValue).replace(/'/g, "\\'");
      return `- Change <string name="${c.stringName}"> to: ${escaped}`;
    }),
    '',
    'For each item, replace the existing string value with the new value. Escape apostrophes as \\'.',
  ];

  return lines.join('\n');
}

function generateXmlPatch(design, defaultDesign) {
  const changes = [];
  for (const [path, mapping] of Object.entries(EMULATOR_TO_PROJECT)) {
    const current = getValueAtPath(design, path);
    const original = getValueAtPath(defaultDesign, path);
    if (current !== undefined && current !== original) {
      changes.push({ stringName: mapping.stringName, value: current });
    }
  }

  if (changes.length === 0) return null;

  const xmlLines = changes.map(c => {
    const escaped = String(c.value).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    return `    <string name="${c.stringName}">${escaped}</string>`;
  });

  return `<!-- Paste these into app/src/main/res/values/strings.xml, replacing the matching name attributes -->\n\n${xmlLines.join('\n')}`;
}

function copyForCursor() {
  const design = window.AppRenderer?.getDesign();
  const defaultDesign = window.AppRenderer?.DEFAULT_DESIGN;
  if (!design || !defaultDesign) return null;

  const instructions = generateCursorInstructions(design, defaultDesign);
  const xmlPatch = generateXmlPatch(design, defaultDesign);

  const fullOutput = [
    '--- BEGIN: Paste this into Cursor chat to apply emulator edits ---',
    '',
    instructions,
    '',
    '--- Alternative: XML snippets to replace in strings.xml ---',
    '',
    xmlPatch || '(no changes)',
    '',
    '--- END ---',
  ].join('\n');

  return fullOutput;
}

function copyToClipboard(text) {
  if (navigator.clipboard?.writeText) {
    return navigator.clipboard.writeText(text);
  }
  const ta = document.createElement('textarea');
  ta.value = text;
  ta.style.position = 'fixed';
  ta.style.opacity = '0';
  document.body.appendChild(ta);
  ta.select();
  try {
    document.execCommand('copy');
    return Promise.resolve();
  } finally {
    document.body.removeChild(ta);
  }
}

window.CursorExporter = {
  generateCursorInstructions,
  generateXmlPatch,
  copyForCursor,
  copyToClipboard,
};
