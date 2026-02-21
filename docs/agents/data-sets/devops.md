# DevOps Engineer — data set

## Consumes (reads / references)

- **Two-way email program:** `docs/agents/TWO_WAY_EMAIL_PROGRAM.md` — with Human-in-the-Loop, own the program so the user can email the team anytime and agents can read/respond; scripts in `scripts/coordinator-email/`.
- **Gradle:** `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `app/build.gradle.kts`.
- **CI:** `.github/` (workflows, actions).
- **Scripts:** `scripts/` (e.g. `run_tests.ps1`), `scripts/coordinator-email/` (for env/docs only; no credential access).
- **Deployment:** `docs/DEPLOYMENT.md`, `docs/ONEDRIVE_SETUP.md` (where project lives, how to run).

## Produces (writes / owns)

- Gradle/config changes, pipeline YAML, runbooks; updates to `docs/DEPLOYMENT.md` or `docs/ops/`.

## Delegation

Fix build; add CI step; document how to run tests; exclude build/.gradle from OneDrive if supported. With **Human-in-the-Loop**, design/implement the two-way email program (user can send anytime; agents read and respond)—see `docs/agents/TWO_WAY_EMAIL_PROGRAM.md`.
