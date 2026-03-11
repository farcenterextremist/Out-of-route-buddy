# Store listing and release checklist — OutOfRouteBuddy

**Purpose:** Checklist for publishing to Google Play (or another store). Ref: Blind Spot Plan §6.

Use [docs/release/GOOGLE_PLAY_FIRST_RELEASE_PACKET.md](release/GOOGLE_PLAY_FIRST_RELEASE_PACKET.md) as the working packet for privacy policy text, Data safety draft answers, store listing copy, and asset prep.
Review [docs/release/FIRST_RELEASE_GO_NO_GO.md](release/FIRST_RELEASE_GO_NO_GO.md) before any internal-testing or production upload so open blockers stay explicit.
For a 10-minute go/no-go check, use [docs/automation/SHIPABILITY_CHECKLIST.md](automation/SHIPABILITY_CHECKLIST.md).

---

## 1. Privacy policy and in-app disclosure

- [ ] **Privacy policy:** Publish a privacy policy that states what data the app collects (e.g. Firebase Analytics/Crashlytics events; no PII). See [docs/security/SECURITY_NOTES.md](security/SECURITY_NOTES.md) §10 (Analytics and Crashlytics) for the list of events.
- [ ] **Host at a stable URL** and add that URL to the Play Console listing.
- [ ] **In-app:** If the store or jurisdiction requires it, add a link to the privacy policy in the app (e.g. Settings or About). Do not add this until the final public URL is chosen.

---

## 2. Data safety (Play Console)

In Google Play Console, complete the **Data safety** section:

- State that **data is stored on-device** (trip history, preferences).
- If you send crash/analytics to Firebase, declare **crash data** and **diagnostics** (or as categorized by Play). No PII is sent; see SECURITY_NOTES §10.

---

## 3. Release keystore (do first)

- **Create once** (if not already done):
  ```bash
  keytool -genkey -v -keystore outofroutebuddy.keystore -alias outofroutebuddy -keyalg RSA -keysize 2048 -validity 10000
  ```
- **Store** the keystore file and passwords in a **secure place** (e.g. encrypted backup or secret manager). **Do not commit** the keystore or put it in the repo.
- **Document** in [docs/DEPLOYMENT.md](DEPLOYMENT.md): where the release keystore is stored and who has access. Used for signing release builds only.

---

## 3a. Legal review (before first production release)

Before first production upload, run a **legal review** to verify:

- [ ] **Privacy policy** — Accurate, complete, matches app behavior.
- [ ] **Data safety form** — Aligned with privacy policy.
- [ ] **Terms of service** — If required for your jurisdiction or business model.
- [ ] **In-app disclosure** — Privacy policy link in app if required.
- [ ] **Store policies** — Compliance with Google Play Developer Program Policies.

**Tip:** Use legal counsel or a team of agent lawyers to verify the above. See `docs/SHIP_TO_GOOGLE_PLAY_PLAN_PROMPT.md` §1 (Store and legal prerequisites) and `C:\Users\brand\Desktop\OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt` Phase C.

---

## 4. Release checklist (per release)

- [ ] **Version bump:** Update `versionCode` and `versionName` in `app/build.gradle.kts` (source of truth; see DEPLOYMENT).
- [ ] **CHANGELOG:** Update [CHANGELOG.md](../CHANGELOG.md) with the release date and changes.
- [ ] **Build:** Run `./gradlew assembleRelease` and smoke-test the release build.
- [ ] **Sign:** For the first Play release, prefer Android Studio **Generate Signed Bundle / APK** and produce a signed `.aab` using the release keystore (see DEPLOYMENT § APK Signing / Keystore).
- [ ] **Upload:** Upload the signed `.aab` to Play Console Internal testing first, then promote when validated.
- [ ] **Release notes:** Add store release notes for users.

---

*Blind Spot Plan §6. See [DEPLOYMENT.md](DEPLOYMENT.md) for build commands and keystore details.*
