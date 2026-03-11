# Security Grade — OutOfRouteBuddy

**Owner:** Security Specialist (Security Coordinator)  
**Created:** 2025-02  
**Last graded:** 2025-02  
**Re-grade schedule:** Quarterly (or when significant changes land)

This document provides a quick reference for the project's security posture. For detailed findings and recommendations, see [SECURITY_NOTES.md](SECURITY_NOTES.md) and [SECURITY_PLAN.md](SECURITY_PLAN.md).

---

## 1. Grading Framework

Each variable is scored 1–5 (1 = critical gaps, 5 = exemplary). Weights sum to 100%. Authentication is N/A (no user accounts).

| Variable | Weight | Grade (1–5) | Notes |
|----------|--------|-------------|-------|
| **Secrets Management** | 15% | 4 | Local secret files are gitignored; `google-services.json` is committed only with GCP restrictions in place |
| **Data Protection** | 20% | 3 | Room DB in app-private storage; SharedPreferences unencrypted; StandaloneOfflineService stores AES key in prefs (weak) |
| **Permissions** | 10% | 5 | Minimal, justified (location, foreground service, notifications); runtime requests |
| **Input Validation** | 15% | 5 | Trip init, InputValidator, ValidationFramework, and repository validation are in place |
| **Network Security** | 10% | 5 | No remote PII transmission in the core app; HTTPS-only guidance remains for future back-end work |
| **Audit Logging** | 10% | 4 | TripInsertAudit, TripExportAudit, and TripDeleteAudit exist; PII logging policy is documented |
| **Authentication** | 5% | N/A | No user accounts; local-only app — reduces attack surface |
| **Dependency Security** | 10% | 4 | Official repos; Firebase BOM; no known vulnerable deps in scan; regular updates recommended |
| **Documentation** | 5% | 5 | SECURITY_NOTES, SECURITY_PLAN, Purple Team artifacts |

**Weighted score:** ~4.0 / 5.0  
**Overall grade:** **B+ (Good)**

---

## 2. TypeScript: What It Is and Why We Don't Use It

**TypeScript** is a typed superset of JavaScript developed by Microsoft. It adds static typing, interfaces, and compile-time checks to JavaScript. It compiles to plain JavaScript and is commonly used for:

- Large web applications (React, Angular, Vue)
- Node.js backends
- Projects requiring stronger type safety and IDE tooling

**OutOfRouteBuddy project stack:**

- **Android app:** Kotlin (primary) + Java — no TypeScript
- **Scripts:** Python utility scripts and agent-aptitude tooling

**Decision:** TypeScript is **not used** and **not needed** for this project. The Android app uses Kotlin, which already provides strong typing. Adding TypeScript would add build tooling without meaningful benefit for the current scope.

---

## 3. Re-grade Schedule

- **Quarterly:** Re-run grading after each quarter; update this file and SECURITY_PLAN Section 8.
- **On significant change:** Re-grade when adding cloud sync, authentication, new external APIs, or major dependency updates.
- **Owner:** Security Specialist (or delegated Security Coordinator).
