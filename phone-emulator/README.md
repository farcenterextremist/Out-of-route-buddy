# OutOfRouteBuddy Virtual Phone Emulator

A Figma-like web-based phone emulator for the OutOfRouteBuddy app. Deploy it anywhere and let users **touch/click elements to edit** labels, text, and content in real time.

## Features

- **Virtual phone frame** – Displays the app in a realistic device mockup
- **Touch-to-edit** – Toggle Edit Mode, then tap any button or field to change its text
- **Properties panel** – Edit selected elements (like Figma’s inspector)
- **Persistent edits** – Changes are saved to `localStorage`
- **Copy for Cursor** – Generates Cursor-ready instructions and copies to clipboard; paste into Cursor chat to apply edits to the real project
- **Export/Import** – Save designs as JSON and load them later
- **Reset** – Restore the default design

## Quick Start

1. Open `index.html` in a browser (or serve the folder with any static server).
2. **Right-click** any field (toolbar, inputs, buttons, labels).
3. Click **Edit** in the context menu.
4. Change the value, add notes, and click **Save**.
5. Click **Copy for Cursor** to copy the edits as instructions, then paste into Cursor chat to apply them to the Android project.

## Deployment

The emulator is static HTML/CSS/JS. Deploy it to:

- **GitHub Pages** – Push the `phone-emulator` folder to a repo and enable Pages
- **Netlify** – Drag and drop the folder or connect a repo
- **Vercel** – Import the project and deploy
- **Any static host** – Upload the folder as-is

No build step or server required.

## File Structure

```
phone-emulator/
├── index.html         # Main page
├── styles.css         # Emulator and app styling
├── app-renderer.js    # Renders app UI from design state
├── cursor-exporter.js # Maps edits to project files, generates Cursor instructions
├── editor.js          # Touch-to-edit logic
└── README.md
```

## Design State

Edits are stored as JSON. Example:

```json
{
  "toolbar": { "title": "OOR" },
  "loadedMiles": { "hint": "Loaded Miles" },
  "startButton": { "text": "Start Trip" },
  ...
}
```

Use **Export Design** to download and **Import Design** to load a saved design.

## Customization

To add more editable elements, in `app-renderer.js`:

1. Add the field to `DEFAULT_DESIGN`.
2. Add an HTML element with `data-edit-path` and `data-edit-key`.
3. Add the `editable` class.

The editor will automatically support the new element.
