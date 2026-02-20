# Security Specialist — data set

## Consumes (reads / references)

- **Manifest:** `app/src/main/AndroidManifest.xml` — permissions, components.
- **Services/data:** `app/.../services/`, `app/.../data/` — for data handling and exposure.
- **Credentials:** `scripts/coordinator-email/.env.example` only (never .env or real secrets).
- **Docs:** Any doc describing PII, location, or sensitive flows.

## Produces (writes / owns)

- `docs/security/REVIEW_<feature>.md` or `THREAT_NOTES.md`; hardening recommendations (docs or comments). No implementation.

## Delegation

Review Auto drive for privacy; confirm .env/last_reply not committed; short threat note for lost-device scenario.
