# Scenario 9: Where is TripInputViewModel defined?

**Type:** LLM  
**Purpose:** Verify Jarvey can answer "where is X defined?" using the project index.

## Input

- **Subject:** Re: OutOfRouteBuddy
- **Body:** Where is TripInputViewModel defined?

## Expected

Jarvey references the file path from the project index (e.g. `app/src/main/.../TripInputViewModel.kt`).

## Pass criteria

- Jarvey sign-off present
- Mentions TripInputViewModel
- References file path (`.kt`, `app/`, or `src/`)
