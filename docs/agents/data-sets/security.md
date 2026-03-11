# Security Specialist — data set

## Consumes (reads / references)

- **Known truths & SSOT:** `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md` — where trip/location data lives, no PII in logs; use with `docs/security/SECURITY_NOTES.md`.
- **Manifest:** `app/src/main/AndroidManifest.xml` — permissions, components.
- **Services/data:** `app/.../services/`, `app/.../data/` — for data handling and exposure.
- **Credentials:** Review only committed config templates; never real secrets or local env files.
- **Docs:** Any doc describing PII, location, or sensitive flows.

## Produces (writes / owns)

- `docs/security/REVIEW_<feature>.md` or `THREAT_NOTES.md`; hardening recommendations (docs or comments). No implementation.

## Delegation

Review Auto drive for privacy; confirm local secret files are not committed; write a short threat note for lost-device scenario.
