# Role data sets

Each file in this folder defines what a role **consumes** (reads, references) and **produces** (writes, owns). Used by the coordinator to delegate work and point roles at the right paths.

## Shared / agent training (all roles)

- **`docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md`** — Canonical known truths and single sources of truth for persistence, recovery, calendar, GPS, theme, and settings. When working on any of those areas, consume this doc so behavior and SSOT stay consistent.

| Role | File |
|------|------|
| Project Design / Creative Manager | [design-creative.md](./design-creative.md) |
| UI/UX Specialist | [ui-ux.md](./ui-ux.md) |
| Front-end Engineer | [frontend.md](./frontend.md) |
| Back-end Engineer | [backend.md](./backend.md) |
| DevOps Engineer | [devops.md](./devops.md) |
| QA Engineer | [qa.md](./qa.md) |
| Security Specialist | [security.md](./security.md) |
| Email Editor / Market Guru | [email-editor.md](./email-editor.md) |
| File Organizer | [file-organizer.md](./file-organizer.md) |
| Human-in-the-Loop Manager | [human-in-the-loop.md](./human-in-the-loop.md) |

See **`docs/agents/DATA_SETS_AND_DELEGATION_PLAN.md`** for the full plan and delegation matrix.
