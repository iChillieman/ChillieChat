# Critical Path 7: Further Polish

**Objective:**
Address the issues in EntriesScreen and enhance the EventsScreen to make the Events more visually
pleasing

All issues resolved in commits `d0c4019`, `11b3885`, `de3b305`, `34526e0`.

## Resolved Issues:

1. **Soft keyboard scroll** — Added `imePadding()` to EntriesScreen so the input row and entry
   list adjust when the keyboard is open.

2. **ThreadsScreen card contrast** — Changed from `surfaceContainerLow` to `ElevatedCard` with
   `secondaryContainer` color and 4dp elevation. Cards now pop in both light and dark mode.

3. **Human agent name color** — Changed from `Color(0xFFC5CAE9)` (light indigo, invisible in
   light mode) to `Color(0xFF5C6BC0)` (indigo-400, visible in both modes).

4. **WebSocket real-time updates** — Created `WebSocketManager` using OkHttp that connects to
   `wss://chillieman.com/ws/threads/{threadId}`. Incoming entries are parsed and inserted into
   Room, which automatically pushes updates to the UI via Flow. Lifecycle managed in ViewModel
   (connect on init, disconnect on onCleared).

5. **Pagination scroll position** — Fixed by keying the auto-scroll `LaunchedEffect` on the last
   entry's ID instead of entry count. When older entries load (pagination), the last entry ID
   stays the same so no scroll occurs. When a new message arrives, the last entry ID changes
   and the list scrolls to the bottom.

## Previously Resolved (first round):

- Entry list ordering and scroll-to-bottom on load
- Pagination via lowestEntryId cursor
- Public agent crash (encodeDefaults = true)
- Entry display: agent icons, timestamps, lock icons, admin pulse animation
- EventsScreen: cosmic images, status pills, active-only filter
- Removed temp EventsScreenUpdates file
