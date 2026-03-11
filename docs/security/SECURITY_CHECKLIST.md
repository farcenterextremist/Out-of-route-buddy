# Security Checklist — OutOfRouteBuddy

**Owner:** Security Specialist  
**Created:** 2025-02  
**Purpose:** Pre-release and onboarding verification

Use this checklist before releasing a new version and when onboarding new contributors.

---

## 1. Pre-Release Checklist

Before cutting a release or publishing to the Play Store:

| Item | Check | Notes |
|------|-------|-------|
| No secrets in logs | Verify no coordinates, API keys, or PII in log statements | See SECURITY_NOTES Section 2 |
| GCP restrictions | Confirm Firebase API key is restricted in Google Cloud Console (Android package, limited APIs) | See SECURITY_NOTES Section 1 |
| Dependency scan | Run `./gradlew dependencyCheckAnalyze` or equivalent; address critical/high CVEs | Use OWASP Dependency-Check or similar |
| Local secret files not committed | Ensure local secret files and `local.properties` are gitignored | See .gitignore |
| FileProvider scope | No user-controlled paths in export/share flows | See SECURITY_NOTES Section 5 |
| Permissions | All requested permissions justified and documented in AndroidManifest | Location, foreground service, notifications |

---

## 2. Onboarding Checklist

For new developers joining the project:

| Item | Action |
|------|--------|
| Read SECURITY_NOTES | Review [docs/security/SECURITY_NOTES.md](SECURITY_NOTES.md) |
| Never commit secrets | Do not commit secret env files or `local.properties` |
| No PII in logs | Avoid logging coordinates, trip IDs that could identify a user, or other PII |
| Security Specialist handoff | For features touching location, PII, credentials, or new external APIs: request Security review |

---

## 3. When to Re-run

- **Pre-release:** Run Section 1 before each release.
- **Onboarding:** Run Section 2 for each new contributor.
- **After major changes:** Re-run Section 1 if adding cloud sync, auth, or new external services.
