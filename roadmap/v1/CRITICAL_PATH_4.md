# Critical Path 4: Events & Threads Features

**Objective:**
Implement the hierarchical navigation flow of the chat application: viewing a list of Events, selecting an Event to view its Threads, and selecting a Thread to prepare for viewing Entries.

**Requirements & Tasks:**

1.  **Events Feature (ViewModel & State):**
    - Create `EventsViewModel` and `EventsUiState`.
    - On init, trigger a fetch to load Events from the `EventRepository`.
    - Handle Pull-to-Refresh functionality.

2.  **Events Screen (Compose):**
    - Build `EventsScreen.kt`.
    - Display a visually pleasing list of Cards representing the Events.
    - Show Title, Tags, and Start/End times.
    - Implement a `LazyColumn` for performance.
    - Handle Loading indicators and Error messages (e.g., "Network Error").
    - Clicking an Event Card passes the `event_id` to the Navigation router to navigate to the Threads screen.

3.  **Threads Feature (ViewModel & State):**
    - Create `ThreadsViewModel` taking `event_id` as a `SavedStateHandle` parameter (or NavArg).
    - Expose `ThreadsUiState`.
    - Fetch Threads belonging to the selected Event (Using the `/api/events/{event_id}` endpoint which includes nested threads).

4.  **Threads Screen (Compose):**
    - Build `ThreadsScreen.kt`.
    - Display the parent Event's details cleanly at the top.
    - Display a `LazyColumn` of Threads.
    - Show Thread Title, Tags, and Entry Count.
    - Handle loading and empty states (e.g., "No threads found for this event").
    - Clicking a Thread passes the `thread_id` to the Navigation router to navigate to the Entries screen.

**Success Criteria:**
- The app fetches Events from the remote backend and displays them in a scrollable list.
- Clicking an Event seamlessly navigates to a Threads screen that loads data specific to that Event.
- Pull-to-refresh correctly re-fetches data from the network and updates the local database.