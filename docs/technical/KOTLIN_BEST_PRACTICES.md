# Kotlin Best Practices — OutOfRouteBuddy

**Purpose:** Single reference for Kotlin style and best practices in the app. Follow this when writing or editing Kotlin in `app/`. Official references: [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html), [Android Kotlin style guide](https://developer.android.com/kotlin/style-guide).

---

## 1. Naming and files

- **Files:** One main class per file; file name = class name (e.g. `TripInputViewModel.kt`). For top-level-only files use PascalCase (e.g. `DateExtensions.kt`). Avoid `Util`-only names.
- **Packages:** All lowercase, no underscores; follow existing `com.example.outofroutebuddy.*` structure.
- **Classes/objects:** PascalCase. Prefer singular nouns for entities (e.g. `Trip`, not `Trips`).
- **Functions/properties:** camelCase. Boolean properties: `is*`, `has*`, `can*` (e.g. `isActive`, `hasTrips`).
- **Constants:** UPPER_SNAKE_CASE for compile-time constants (`const val` or top-level in `object`). Prefer `core/config` for app-wide config (e.g. `ValidationConfig`).

---

## 2. Null safety and types

- Prefer non-null types when a value is always present; use nullable (`T?`) only when absence is valid.
- Use safe calls (`?.`), Elvis (`?:`), and `?.let { }` instead of explicit `if (x != null)` where idiomatic.
- Avoid unnecessary `!!`; fix the nullability upstream or use safe alternatives.
- Prefer `requireNotNull`/`checkNotNull` for guard clauses with clear messages.
- Use `List<T>`, `Set<T>`, `Map<K,V>` (read-only) in APIs; expose mutable collections only when callers must modify.

---

## 3. Functions and expressions

- Prefer expression bodies when the body is a single expression: `fun fullName() = "$firstName $lastName"`.
- Use default parameters instead of overloads when behavior is the same.
- Keep parameters ordered: required first, optional last; consider named arguments for clarity at call sites.
- Prefer `when` over long `if`/`else` for multiple branches; use exhaustive `when` on sealed types/enums.

---

## 4. Classes and data

- Use `data class` for models that hold data (entities, DTOs); avoid adding logic beyond simple derivations.
- Prefer `val` over `var`; use `var` only when state must change.
- Use `sealed class` or `sealed interface` for closed hierarchies (e.g. UI state, result types).
- Prefer composition over inheritance; use interfaces for contracts (e.g. `TripRepository`).

---

## 5. Coroutines and Flow

- Scope: launch in a `CoroutineScope` (e.g. `viewModelScope`, `applicationScope`); avoid global `GlobalScope`.
- Prefer `StateFlow`/`SharedFlow` for hot streams exposed from ViewModels or services; use `Flow` for cold streams.
- Collect in the UI layer with `repeatOnLifecycle` or `flowWithLifecycle`; cancel when lifecycle is destroyed.
- Use `suspend` for operations that wait (IO, delay); use `Dispatchers.IO` for disk/network, `Dispatchers.Main` or `Main.immediate` for UI updates.
- Prefer `flow { }` or `channelFlow` for callback-to-Flow conversion; avoid blocking in `flow { }` unless on the right dispatcher.

---

## 6. Android and Room

- **Room:** Use `@Entity`, `@Dao`, `@Database`; prefer `suspend` DAO functions for one-shot reads/writes. Use `Flow` for reactive queries. Keep entities in `data/`; DAOs in `data/dao/`.
- **ViewModels:** Expose UI state via `StateFlow` or `LiveData`; use `viewModelScope` for coroutines. No Android framework types (Context, Activity) in ViewModel constructor; inject use-case or repository.
- **Dependency injection:** Use Hilt; inject constructors, not fields. Prefer `@Inject constructor` and modules in `di/`.
- **Resources:** Use `getString(R.string.xxx)` and `@string/xxx` in XML; no hardcoded user-facing strings in Kotlin.

---

## 7. Formatting and style

- **Indentation:** 4 spaces; no tabs. Trailing commas in multi-line argument/literal lists.
- **Line length:** Prefer &lt; 120 characters; break after operators or commas when wrapping.
- **Braces:** Use `if (cond) { }` with braces for multi-line; single-line `if` without braces is acceptable when body is one short statement.
- **Imports:** Use explicit imports for clarity; wildcards only when many same-package symbols. Remove unused imports.

---

## 8. Error handling and logging

- Prefer `Result<T>` or sealed result types for expected failures (e.g. validation); use exceptions for programming errors or unexpected failures.
- Do not log PII (coordinates, trip IDs that could identify a user). Use a `debugLog(msg)`-style helper gated by `BuildConfig.DEBUG` for verbose logs. See `docs/technical/LOGGING_POLICY.md` and `docs/security/SECURITY_NOTES.md`.

---

## 9. Testing

- Use descriptive test names: `should_doSomething_whenCondition` or `givenX_whenY_thenZ`.
- Prefer test doubles (fakes, stubs) over mocks when possible; keep tests readable.
- For ViewModels: use `runTest` and `TestScope`; advance time with `advanceTimeBy` when testing delays.

---

## 10. Project-specific

- **Repository chain:** Domain interface in `domain/repository/`; implementation in `data/repository/`; adapter in `data/repository/DomainTripRepositoryAdapter`. No UI or services importing Room/DAO directly.
- **Only End trip writes to the trip store.** Clear trip must not call `insertTrip`. See `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md`.
- **util vs utils:** `util/` for core utilities; `utils/` for extensions. Prefer existing packages when adding helpers.
- **Detekt:** Run `./gradlew detekt`; config in `app/config/detekt/detekt.yml`. Address new issues before increasing `maxIssues`.

---

*When in doubt, match existing code in the same layer (e.g. other ViewModels, other DAOs) and refer to the official Kotlin and Android style guides above.*
