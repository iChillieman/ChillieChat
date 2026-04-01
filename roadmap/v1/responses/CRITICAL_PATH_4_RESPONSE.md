# Critical Path 4 Response: Events & Threads Features

**From:** Claude (Implementation)  
**To:** Daedalus (Architecture)  
**Date:** 2026-03-31

---

## Summary

CP4 is complete. The app now fetches Events from the backend, displays them in a scrollable card list, and navigates into a Threads screen when an event is tapped. Pull-to-refresh works on both screens.

---

## What Was Built

### Events Feature
- **EventsUiState** — Sealed interface: `Loading`, `Success(events, isRefreshing)`, `Error(message)`
- **EventsViewModel** — Collects from `GetEventsUseCase()` Flow (Room-backed with auto-refresh on start). Pull-to-refresh calls a dedicated `refresh()` method that hits the network and lets Room's Flow emit the update.
- **EventsScreen** — `PullToRefreshBox` wrapping a `LazyColumn` of event cards. Each card shows title, tags (as small chips), start/end times (formatted from Unix timestamps), and truncated description. Handles loading spinner, empty state ("No events found" with pull hint), and error state with retry.

### Threads Feature  
- **ThreadsUiState** — Sealed interface: `Loading`, `Success(event, threads, isRefreshing)`, `Error(message)`
- **ThreadsViewModel** — Reads `eventId` from `SavedStateHandle` (populated automatically by type-safe navigation). Calls `GetEventWithThreadsUseCase(eventId)` which hits `GET /api/events/{event_id}` and caches both the event and its threads to Room.
- **ThreadsScreen** — Event detail header (description, tags, time range) at the top, followed by a `HorizontalDivider`, then a `LazyColumn` of thread cards. Each thread card shows title, entry count badge, and tags. Empty state displayed inline when no threads exist.

### Navigation Updates
- **AppNavigation** — `EventsRoute` now routes to `EventsScreen` (replaced the old `HomeScreen` placeholder). Added `ThreadsRoute` composable. Thread cards navigate forward to `EntriesRoute` (ready for CP5).
- **MainActivity** — Title bar dynamically shows the event title when on the Threads screen, using `NavBackStackEntry.toRoute<ThreadsRoute>()` with safe fallback.

### Use Case Hardening
- **GetEventsUseCase** — Wrapped the `onStart { refreshEvents() }` call in try-catch so that network failures don't kill the Room Flow. Added a `suspend fun refresh()` method for pull-to-refresh.
- **GetThreadsForEventUseCase** — Same try-catch treatment in `onStart` for resilience.

### Cleanup
- Deleted `HomeScreen.kt` — it was a placeholder from CP3, now fully replaced by `EventsScreen`.

---

## Architecture Decisions

### ViewModel Patterns
Two different patterns used intentionally:
- **EventsViewModel** uses a **Flow-based** approach — it collects from a Room-backed Flow, meaning the UI is always reactive to database changes. The `onStart` auto-refresh seeds the data, and pull-to-refresh writes new data to Room which the Flow picks up automatically.
- **ThreadsViewModel** uses a **suspend-based** approach — it calls `GetEventWithThreadsUseCase` which is a one-shot suspend function returning `EventWithThreads`. This was chosen because (a) we need both event details AND threads together, and (b) the spec specifically calls for using the `GET /api/events/{event_id}` endpoint which returns both in one payload.

### Pull-to-Refresh
Used Material3's `PullToRefreshBox` (`@ExperimentalMaterial3Api`). For the empty state in EventsScreen, the content is wrapped in a scrollable `Box` so the pull gesture still works when there's nothing to scroll.

### Tag Display
Tags come as comma-separated strings from the backend. Displayed as small `Surface`-based chips rather than Material3 `Chip` components to avoid minimum touch target sizing issues — these are display-only, not interactive.

### Time Formatting
Used `java.time.Instant` + `DateTimeFormatter` directly (no desugaring needed since minSdk 28). Format: `"MMM d, yyyy 'at' h:mm a"`. Time ranges show start and end with an em dash separator.

### Use Case Resilience
The original `onStart { refreshEvents() }` would kill the entire Flow if the network call threw an exception — even if Room had cached data to show. Wrapping in try-catch ensures users see stale data rather than an error screen when offline. The error is still surfaced during explicit pull-to-refresh since that path doesn't suppress exceptions.

---

## What's Ready for CP5

- `EntriesRoute(threadId, threadTitle)` is already defined and wired in navigation — clicking a thread card navigates to it (currently leads nowhere since the Entries screen doesn't exist yet).
- Thread and entry data is cached in Room, so the Entries screen can observe via `GetEntriesUseCase`.
- The WebSocket connection for live chat will need the `threadId` which is available as a nav arg.

---

## Files Changed (11 files, +747/-38 lines)

**New:**
- `presentation/ui/screens/events/EventsUiState.kt`
- `presentation/ui/screens/events/EventsViewModel.kt`
- `presentation/ui/screens/events/EventsScreen.kt`
- `presentation/ui/screens/threads/ThreadsUiState.kt`
- `presentation/ui/screens/threads/ThreadsViewModel.kt`
- `presentation/ui/screens/threads/ThreadsScreen.kt`

**Modified:**
- `domain/usecase/GetEventsUseCase.kt` — try-catch + refresh method
- `domain/usecase/GetThreadsForEventUseCase.kt` — try-catch
- `presentation/navigation/AppNavigation.kt` — new routes
- `MainActivity.kt` — dynamic title for ThreadsRoute

**Deleted:**
- `presentation/ui/screens/home/HomeScreen.kt`
