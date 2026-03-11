# Backup and restore

**Purpose:** Document what Android Auto Backup includes for this app and the decision on trip data. Ref: Blind Spot Plan §1.

---

## Current configuration

- **AndroidManifest.xml:** `android:allowBackup="true"`, `android:fullBackupContent="@xml/backup_rules"`.
- **backup_rules.xml:** Contains explicit include/exclude rules (see below). When `full-backup-content` is empty, Android defaults to backing up `sharedpref`, `database`, and `files` in the app's internal storage (see [Auto Backup](https://developer.android.com/guide/topics/data/autobackup)).
- **data_extraction_rules.xml:** Cloud-backup and device-transfer sections are not configured (commented out).

---

## What gets backed up (with current rules)

With the exclude rules in `res/xml/backup_rules.xml`:

- **Excluded:** Room database (`databases/`), SharedPreferences used for trip state and crash recovery (`shared_prefs/`). This prevents trip and location-related data from being included in Google Auto Backup or device transfer.
- **Included:** Nothing explicitly included; any other app data that might exist in `files/` or other domains would follow platform default unless excluded. For this app, the intent is that **trip and location data are not backed up** so they do not leave the device or restore to a new device in a way that could duplicate or leak data.

---

## Decision

- **Trip/location data:** Must not be included in cloud backup or device transfer. Room database and trip-related SharedPreferences are excluded via backup_rules.xml.
- **Restore behavior:** On a new device or after reinstall, the user will not get restored trip history from backup; they start fresh. This avoids conflicts and keeps trip data device-local.

---

## If you change backup rules

- To include specific preferences (e.g. theme only), use `<include domain="sharedpref" path="..."/>` for that file only and keep `database` and trip-related prefs excluded.
- Document any change here and in SECURITY_NOTES §Backup and restore.

---

*Blind Spot Plan implementation. See [SECURITY_NOTES.md](../security/SECURITY_NOTES.md) for the short checklist.*
