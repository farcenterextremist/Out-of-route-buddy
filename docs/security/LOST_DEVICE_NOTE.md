# Lost or stolen device — user guidance

**Owner:** Security Specialist  
**Purpose:** Short threat note for the user: what data is on the device and what we recommend if the device is lost or stolen. No implementation in the app; guidance only.

---

## What's on the device

- **Trip data:** Completed trips (dates, miles, loaded/bounce) and in-progress trip state if the app was open. Stored in the app's private storage (Room database and preferences).
- **Location data:** Last known location may be in trip state or recovery data; no ongoing broadcast when the app is closed.
- **No account or password:** OutOfRouteBuddy does not store a user account or login; there is no remote server that holds your trips. Everything is local to the device.

---

## What we recommend

1. **Remote wipe (if available):** Use your phone vendor's or MDM's "find my device" / remote wipe so the device can be erased if it's lost or stolen. That will remove the app and its data along with everything else.
2. **Change any shared credentials:** If you use the same email account for coordinator email (Human-in-the-Loop), consider changing the password or revoking app passwords for that account so the lost device cannot send or read email as you.
3. **Assume trip data could be read:** Until the device is wiped or recovered, someone with physical access could in theory extract app data (e.g. via backup or root). Trip history is not encrypted with a separate key; it's protected only by the OS app-private storage. For most users this is acceptable; for higher sensitivity, we could add optional app-level encryption in a future version.
4. **No server to notify:** Because we don't sync to a cloud, there is no "revoke device" or "logout everywhere" — wiping or changing email credentials is the main lever.

---

*This is guidance only. We do not implement remote wipe or device management in the app.*
