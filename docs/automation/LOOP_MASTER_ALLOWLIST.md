# Master Command Allowlist — 2-Hour Improvement Loop

**Purpose:** Add commands to Cursor's Command Allowlist for autonomous execution.  
**Location:** Cursor Settings → Agents → Auto-Run → Command Allowlist  
**Full guide:** See [AUTONOMOUS_LOOP_SETUP.md](./AUTONOMOUS_LOOP_SETUP.md) for no-human-intervention setup.

---

## Quick setup (prefix matching)

Cursor uses **prefix matching**. One entry covers all loop commands:

| Add this | Covers |
|----------|--------|
| `cd c:\Users\brand\OutofRoutebuddy` | All commands starting with this (gradlew, powershell, pulse, etc.) |

---

## Commands to Allow (if not using prefix)

Add each of these if you prefer explicit entries:

```
cd c:\Users\brand\OutofRoutebuddy
```

```
cd c:\Users\brand\OutofRoutebuddy; .\gradlew.bat :app:testDebugUnitTest --no-daemon
```

```
cd c:\Users\brand\OutofRoutebuddy; .\gradlew.bat :app:lintDebug --no-daemon
```

```
cd c:\Users\brand\OutofRoutebuddy; .\gradlew.bat clean --no-daemon
```

```
cd c:\Users\brand\OutofRoutebuddy; .\gradlew.bat assembleDebug --no-daemon
```

```
cd c:\Users\brand\OutofRoutebuddy; powershell -File .\scripts\automation\pulse_check.ps1
```

```
cd c:\Users\brand\OutofRoutebuddy; powershell -File .\scripts\automation\pulse_check.ps1 -Note "Phase N: description"
```

```
cd c:\Users\brand\OutofRoutebuddy; .\scripts\automation\run_120min_loop.ps1
```

---

## Optional (if loop uses these)

```
cd c:\Users\brand\OutofRoutebuddy; .\gradlew.bat jacocoSuiteTestsOnly --no-daemon
```

```
cd c:\Users\brand\OutofRoutebuddy; .\gradlew.bat assembleDebug --warning-mode all --no-daemon 2>&1
```

---

## How to Add

1. Open **Cursor Settings** (Ctrl+, or Cmd+,)
2. Go to **Agents** → **Auto-Run** (or **Cursor Settings** → **Agents**)
3. Find **Command Allowlist** (under Protection settings)
4. Click **Add** and paste each command above
5. Or: When the agent runs a command and it's blocked, choose **Add to allowlist**

---

## Scope

- **Workspace:** `c:\Users\brand\OutofRoutebuddy`
- **Shell:** PowerShell (Windows)
- **Gradle:** `gradlew.bat` (no WSL required for these commands)

---

*Created for 2-hour improvement loop autonomy. Update paths if repo moves.*
