# Trip Delete Failures and Visibility Notes

## Reported Failures

- Calendar date-trip delete showed: `Failed to delete trip`.
- Statistics Month/Year delete showed: `No month trips found to delete` / `No year trips found to delete` even when data existed.
- Delete confirmation dialogs had poor dark-mode visibility (black text on dark background).

## Root Causes

1. `TripRepository.deleteTripById(...)` executed Room delete on main thread, which can fail under Room main-thread guards.
2. Month/Year bulk delete UX only reported deleted count and could mask partial/total delete failures as "none found".
3. Delete confirmation dialogs used default text colors that were not guaranteed to meet dark-mode contrast requirements.

## Fixes Implemented

- Moved `tripDao.deleteTripById(...)` to `Dispatchers.IO` in data repository.
- Added bulk delete result tracking (`attempted`, `deleted`, `failed`) in `TripInputViewModel` for Month/Year deletes.
- Added explicit failure logging for each failed trip id and for bulk summary failures.
- Updated Month/Year snackbar messaging to distinguish:
  - none found
  - full success
  - partial failures
- Applied dark-mode explicit text color handling for delete confirmation dialogs in:
  - calendar date view delete flow
  - statistics Month/Year delete flow

## Follow-up Checks

- Verify delete actions in both light/dark mode on device.
- Validate partial failure path messaging by forcing a delete failure in test/dev builds.
- Keep dark-mode contrast checks in QA matrix for all destructive confirmations.
