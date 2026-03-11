# DB migration and schema export (DB1/DB2)

**Purpose:** Document Room schema export and migration path for production. Ref: Advanced Improvement Plan Pillar 1 (DB1/DB2); PROJECT_AUDIT.

---

## Schema export

- **exportSchema:** `true` in `AppDatabase` (see `app/src/main/java/.../data/AppDatabase.kt`).
- **Schema location:** `app/schemas/com.example.outofroutebuddy.data.AppDatabase/` — versioned JSON files (e.g. `2.json`) are generated on build.
- **Gradle:** `kapt { arguments { arg("room.schemaLocation", "$projectDir/schemas") } }` in `app/build.gradle.kts`.

Schema files should be committed so migrations can be validated and new migrations (e.g. v2 → v3) can be written against the last exported schema.

---

## Migration path

- **Current version:** 2.
- **MIGRATION_1_2:** Adds GPS metadata and related columns to `trips` (see `AppDatabase.MIGRATION_1_2`).
- **fallbackToDestructiveMigration:** Enabled so the app does not crash on migration failure; may cause data loss. Prefer adding and testing explicit migrations for production (see T4 migration tests in the improvement plan).

When introducing schema version 3+, add a new `Migration(2, 3)` and reference the exported `2.json` schema when writing the migration SQL.

---

*See [QUALITY_AND_ROBUSTNESS_PLAN.md](../QUALITY_AND_ROBUSTNESS_PLAN.md) and Advanced Improvement Plan Pillar 1.*
