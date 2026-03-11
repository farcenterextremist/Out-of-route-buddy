# Security Plan — OutOfRouteBuddy

**Created:** 2025-02-20  
**Source:** Multi-Agent Purple Team Exercise  
**Owner:** Security Specialist (coordinated by Master Branch Coordinator)

This document summarizes the security plan produced by the comprehensive Purple Team exercise involving Coordinator, Red Team, Blue Team, Security Specialist, Back-end, DevOps, and QA roles.

---

## 1. Overview

The security plan covers the main attack surfaces in the shipped app:

1. **Android Service Boundaries** — Internal service communication (TripRepository, TripStateManager, UnifiedLocationService, etc.)
2. **Export/Delete Flows** — Trip export and delete (covered in 2025-02-22 exercise)

---

## 2. Agent Roles and Responsibilities

| Role | Responsibility |
|------|----------------|
| **Coordinator** | Orchestrate exercise; assign Security Specialist, Red Team, Blue Team, Back-end, DevOps, QA |
| **Security Specialist** | Attack surface summary; threat model; SECURITY_NOTES updates |
| **Red Team** | Simulate attacks against app boundaries and data flows |
| **Blue Team** | Check alarms; propose and document remediations |
| **Back-end** | Implement validation and audit improvements |
| **DevOps** | Build and release pipeline support where needed |
| **QA** | Security regression tests (optional) |

---

## 3. Implemented Remediations

### 3.1 Android Service Boundaries

| Control | Implementation |
|---------|----------------|
| Trip insert audit | `Log.w("TripInsertAudit", "trip_inserted trip_id=$tripId result=true")` in TripRepository |
| Validation layers | Trip init, InputValidator, ValidationFramework, entity validation (existing) |

### 3.2 Export/Delete (2025-02-22)

| Control | Implementation |
|---------|----------------|
| Export audit | `TripExportAudit` in TripHistoryViewModel |
| Delete audit | `TripDeleteAudit` in TripHistoryViewModel |
| FileProvider scope | Documentation in SECURITY_NOTES; no user-controlled paths |

---

## 4. API Endpoint Security

| Endpoint | Method | Auth | Hardening |
|----------|--------|------|-----------|
No custom local sync endpoints are currently part of the active toolchain.

---

## 5. Worker-to-Worker Attack Simulations

### 5.1 Android Services (ViewModel → Repository)

- **Attack:** Malicious trip data (NaN, negative values) flowing to TripRepository
- **Mitigation:** Trip init, InputValidator, ValidationFramework reject; TripInsertAudit for detection

### 5.2 Agent vs Agent (Red Team → Blue Team)

- **Purple protocol:** Red attacks; Blue checks "Did the alarm go off?"; Blue remediates if not; proof of work logged

---

## 6. Deliverables

| Deliverable | Location |
|-------------|----------|
| Attack surface summary | `docs/agents/data-sets/security-exercises/artifacts/2025-02-20-attack-surface-summary.md` |
| Purple exercise log | `docs/agents/data-sets/security-exercises/2025-02-20-security-plan.md` |
| Trip insert audit | `app/.../data/repository/TripRepository.kt` |
| SECURITY_NOTES updates | `docs/security/SECURITY_NOTES.md` (Sections 6, 7) |
| Proof of work | `docs/agents/security-team-proof-of-work.md` |

---

## 7. References

- Purple Team protocol: `docs/agents/purple-team-protocol.md`
- Red Team agent: `docs/agents/roles/red-team-agent.md`
- Blue Team agent: `docs/agents/roles/blue-team-agent.md`
- Security notes: `docs/security/SECURITY_NOTES.md`

---

## 8. Security Grading Summary

**Current grade:** B+ (Good) — see [SECURITY_GRADE.md](SECURITY_GRADE.md) for variables and scores.

| Variable | Grade | Notes |
|----------|-------|-------|
| Secrets Management | 4/5 | .env gitignored; google-services.json committed (GCP-restricted) |
| Data Protection | 3/5 | Room in app-private storage; StandaloneOfflineService key in prefs (S-1) |
| Permissions | 5/5 | Minimal, justified; runtime requests |
| Input Validation | 5/5 | Multi-layer validation across domain, repository, and framework layers |
| Network Security | 5/5 | No PII transmission in the shipped app |
| Audit Logging | 4/5 | Trip/export/delete audits |
| Dependency Security | 4/5 | Official repos; regular updates recommended |
| Documentation | 5/5 | SECURITY_NOTES, SECURITY_PLAN, Purple artifacts |

---

## 9. Gap Register (S-1 through S-5)

| ID | Gap | Severity | Remediation |
|----|-----|----------|-------------|
| S-1 | StandaloneOfflineService stores AES key in SharedPreferences | Medium | Migrate to Android Keystore (KeyStore + EncryptedSharedPreferences) for key storage |
| S-2 | SharedPreferences for trip state/prefs not encrypted | Low | Consider EncryptedSharedPreferences for trip state and sensitive prefs |
| S-3 | Firebase API key in google-services.json | Low | Verify GCP restrictions (Android package, API limits); document in SECURITY_NOTES |
| S-4 | PII in logs risk | Low | **L1 (implemented):** AppLogger used in MainActivity, TripPersistenceManager, OfflineDataManager, PreferencesManager; debug/verbose no-op in release; warn/error messages must not contain PII. See docs/technical/LOGGING_POLICY.md. |

---

## 10. TypeScript Note

TypeScript is **not used** and **not needed** for this project. The Android app uses Kotlin; the phone emulator uses vanilla JavaScript; scripts use Python. See [SECURITY_GRADE.md](SECURITY_GRADE.md) Section 2 for full explanation.
