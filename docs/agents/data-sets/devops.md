# DevOps Engineer — data set

## Consumes (reads / references)

- **Gradle:** `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `app/build.gradle.kts`.
- **CI:** `.github/` (workflows, actions).
- **Scripts:** `scripts/` (e.g. `run_tests.ps1`).
- **Deployment:** `docs/DEPLOYMENT.md`, `docs/ONEDRIVE_AND_GIT_SETUP.md` (where project lives, how to run).

## Produces (writes / owns)

- Gradle/config changes, pipeline YAML, runbooks; updates to `docs/DEPLOYMENT.md` or `docs/ops/`.

## Delegation

Fix build; add CI step; document how to run tests; exclude build/.gradle from OneDrive if supported.
