# OutOfRouteBuddy – Attack Surface Summary (Security Plan 2025-02-20)

Used by Red Team to scope targets for comprehensive security exercise. Extends 2025-02-22 summary with sync API and Android service boundaries.

---

## In scope (this run)

### 1. Sync Service API (scripts/emulator-sync-service/sync_service.py)

| Endpoint | Method | Auth | Risk |
|----------|--------|------|------|
| `/design` | GET | None | Read strings.xml; info disclosure if exposed |
| `/sync` | POST | None | Write to strings.xml; any local process can overwrite |

**Attack vectors:**
- Malicious JSON with unexpected keys (not in EMULATOR_TO_PROJECT) — currently accepted and ignored; no key allowlist
- Oversized payload — no size limit; DoS or memory exhaustion
- Malformed JSON — returns 400; acceptable
- XML injection in values — `escape_xml_value()` mitigates; verified

**Key files:** `scripts/emulator-sync-service/sync_service.py`

---

### 2. Android Service Boundaries (worker-to-worker)

**Attack surface:** Internal service communication via Flow/StateFlow and DI.

| Source | Sink | Validation layer |
|--------|------|------------------|
| TripInputViewModel | TripRepository.insertTrip() | InputValidator, ValidationFramework, Trip init |
| UnifiedTripService | TripStateManager | TripState structure |
| UnifiedLocationService | realTimeGpsData StateFlow | LocationValidationService |
| TripRepository | TripDao (Room) | Trip domain model, Room |

**Attack vectors:**
- **Scenario A:** Trip with `actualMiles = NaN` or negative `loadedMiles` — Trip init throws; InputValidator.sanitizeMiles() rejects
- **Scenario B:** TripStateManager state corrupted — services consume without re-validation; lower risk (internal only)
- **Scenario C:** GPS data bypass — LocationValidationService validates; services should not accept unvalidated data

**Key files:**
- `app/.../domain/models/Trip.kt` — domain validation
- `app/.../util/InputValidator.kt` — sanitizeMiles, sanitizeFilePath
- `app/.../validation/ValidationFramework.kt` — trip data validation
- `app/.../data/repository/` — TripRepository, DomainTripRepositoryAdapter

---

### 3. Trip Export / Delete (existing, 2025-02-22)

- **Trip export:** TripHistoryViewModel.exportTrips() / exportToPDF() → TripExporter → cache + FileProvider. Audit: TripExportAudit.
- **Trip delete:** TripHistoryViewModel.deleteTrip() → TripRepository.deleteTrip(). Audit: TripDeleteAudit.
- **FileProvider:** res/xml/file_paths.xml; TripExporter uses fixed filenames only.

---

## Not in scope this run

- Phishing/social engineering (Specialist role)
- Location data exfiltration
- Backup/restore; sync workers
- DAO/repository injection
- Deep link or intent handling
- Coordinator email (SMTP/IMAP) — credentials in .env, gitignored

---

## Threat model additions

- **Sync API local abuse:** Any process on 127.0.0.1 can POST to /sync and overwrite strings.xml. Mitigation: key allowlist, size limit, audit log.
- **Malicious internal service input:** Defense in depth — Trip, InputValidator, ValidationFramework all reject invalid values. Add trip_insert audit for detection.
