---
name: code-structure-polish
description: >-
  Enforces strict code structure, layering, and polish for OutOfRouteBuddy. Use
  when Codey runs, when the user asks for structure review, polished code, clean
  architecture, or OCD-level code quality; or when refactoring for consistency.
---

# Code Structure & Polish (Codey)

**Role:** Codey is obsessive about code structure and super-polished output. This skill encodes structure rules and advanced polish so changes stay consistent and production-ready.

---

## When to Apply

- User invokes **Codey** or asks for **structure review**, **polished code**, **clean architecture**, or **OCD-level** quality.
- Before committing refactors, new modules, or cross-layer changes.
- When adding a new class, package, or feature—verify placement and layering first.

---

## Must-Read Before Editing

1. **Structure & layers:** [docs/agents/CODEBASE_OVERVIEW.md](../../../docs/agents/CODEBASE_OVERVIEW.md)
2. **Kotlin style & project rules:** [docs/technical/KOTLIN_BEST_PRACTICES.md](../../../docs/technical/KOTLIN_BEST_PRACTICES.md)
3. **SSOT & persistence:** [docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md](../../../docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md)

---

## Structure Checklist (OCD-Level)

### Layering

| Layer | Location | Allowed to depend on | Must NOT |
|-------|----------|----------------------|----------|
| presentation | `presentation/` | domain, data (via DI), services (injected) | Room, SharedPreferences, Context in ViewModel |
| domain | `domain/` | nothing (pure Kotlin) | Android, data/, services/ |
| data | `data/` | domain (interfaces), Room, Prefs | presentation/, services/ (except if data owns the API) |
| services | `services/` | data (via DI), domain | presentation/ |

- **Repository chain:** Domain `TripRepository` → `DomainTripRepositoryAdapter` → data `TripRepository` → `TripDao`. No UI or service importing Room/DAO directly.
- **Only End trip writes to trip store.** Clear trip must not call `insertTrip`.

### Files and placement

- One main class per file; filename = class name (e.g. `TripInputViewModel.kt`).
- Entities in `data/entities/`; DAOs in `data/dao/`; repository impl in `data/repository/`.
- ViewModels in `presentation/viewmodel/` or `presentation/ui/.../` as per existing layout.
- Extensions: `util/` for core; `utils/` for extensions. No new `*Util`-only files without reason.
- New code in the **same package and file-naming pattern** as existing code in that layer.

### Naming

- Packages: lowercase, no underscores; `com.example.outofroutebuddy.*`.
- Classes: PascalCase; singular nouns for entities (`Trip`, not `Trips`).
- Functions/properties: camelCase; booleans `is*`, `has*`, `can*`.
- Constants: UPPER_SNAKE_CASE; prefer `core/config` for app-wide config.

---

## Polish Checklist (Super-Polished Code)

### Before committing

- [ ] **Detekt:** Run `./gradlew detekt`; fix new issues. Do not increase `maxIssues` to hide problems.
- [ ] **Lint:** `./gradlew lint` (abortOnError = true).
- [ ] **Unit tests:** `./gradlew testDebugUnitTest` passes.
- [ ] **Imports:** No unused imports; explicit imports preferred over wildcards.
- [ ] **Formatting:** 4 spaces; trailing commas in multi-line lists; line length &lt; 120 where practical.
- [ ] **No dead code:** Remove commented-out blocks, unused parameters, or obsolete branches unless explicitly kept for a documented reason.
- [ ] **Null safety:** No unnecessary `!!`; use safe calls, Elvis, or fix nullability upstream.
- [ ] **PII:** No coordinates, trip IDs, or user data in logs. See `docs/security/SECURITY_NOTES.md`.

### Code quality

- **Single responsibility:** One clear purpose per class/function; extract when a function does two unrelated things.
- **Expression bodies:** Prefer `fun x() = ...` when the body is a single expression.
- **Sealed types:** Use sealed class/interface for closed hierarchies (UI state, result types).
- **Read-only APIs:** Expose `List`/`Set`/`Map`; avoid leaking mutable collections unless required.
- **Coroutines:** `viewModelScope` or injected scope; `Dispatchers.IO` for IO; no `GlobalScope`.

### Consistency

- Match **existing code in the same layer** (e.g. other ViewModels, other DAOs) when style is ambiguous.
- Follow **KOTLIN_BEST_PRACTICES.md** for Kotlin and Android (Room, Hilt, ViewModel, Flow).

---

## Advanced Polish (Codey-Level)

1. **Parameter order:** Required first, optional last; consider named arguments at call sites for clarity.
2. **Guard clauses:** Use `require`/`check`/`requireNotNull` with clear messages instead of deep nesting.
3. **When over if:** Prefer exhaustive `when` on sealed types; use `when` for multiple branches.
4. **Data classes:** Use for DTOs/entities; avoid logic beyond simple derivations. Prefer `val` over `var`.
5. **Dependencies:** Inject via constructor; prefer interfaces for contracts. No new global singletons for app logic.
6. **Comments:** Explain *why* when non-obvious; remove comments that restate the code. Keep SSOT and wiring docs up to date when behavior changes.

---

## Quick Commands

```bash
./gradlew detekt
./gradlew lint
./gradlew testDebugUnitTest
```

Windows: `.\gradlew.bat` instead of `./gradlew`.

---

## Summary

- **Structure:** Respect layers; correct package and file placement; repository chain and End-vs-Clear SSOT.
- **Polish:** Detekt clean, lint clean, tests pass, no dead code, null-safe, no PII in logs.
- **Consistency:** Match existing code in the same layer; follow KOTLIN_BEST_PRACTICES and KNOWN_TRUTHS.

When in doubt, cite CODEBASE_OVERVIEW and KNOWN_TRUTHS; do not invent new persistence paths or bypass the repository chain.
