# UI/UX Specialist — Simple

- **Role card:** `docs/agents/roles/ui-ux-specialist.md`
- **Prompt type:** Simple

## Prompt (copy-paste)

```
Where should a 'Help & Info' entry point live in the app? Give one concrete recommendation and which file or screen you're referring to.
```

## Look for (scoring)

References to layouts/strings/docs/ux; no Kotlin; handoff to Front-end.

---

## Response

**Recommendation:** Put "Help & Info" in the **Settings** flow. The Settings dialog is in `app/src/main/res/layout/dialog_settings.xml` and already has a "Help & Info" button (`help_info_button`) that opens the help dialog. So the entry point is: user opens the app bar/settings icon → Settings dialog → "Help & Info" (no extra screen). The screen I’m referring to is the Settings modal; the help content is in `dialog_help_info.xml`. I’d hand implementation of any change to the **Front-end Engineer**.

