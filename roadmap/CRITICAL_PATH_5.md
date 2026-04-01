# Critical Path 5: Entries Feature & WebSockets (Real-time Chat)

**Objective:**
Deliver the core user experience: a real-time chat interface. Users must be able to view past messages (Entries), submit new messages, and see incoming messages instantly via WebSockets.

**Requirements & Tasks:**

1.  **Entries Architecture (ViewModel & State):**
    - Create `EntriesViewModel` taking `thread_id` from Navigation.
    - Expose `EntriesUiState` (Loading, Success, Error).
    - On init, fetch the historical Entries for the thread using the Paginated API endpoint.
    - Handle Pagination (fetching older messages when scrolling up).

2.  **WebSocket Integration:**
    - Create a `WebSocketManager` or `ChatClient` class responsible for opening a connection to `wss://{host}/ws/threads/{thread_id}`.
    - Manage connection lifecycle (connect on screen enter, disconnect on screen exit/backgrounding).
    - Parse incoming JSON payloads into Domain `Entry` models.
    - Expose a `SharedFlow` or callback mechanism so the ViewModel can react to new incoming entries and append them to the UI state.

3.  **Entries Screen (Compose UI Layout):**
    - Build `EntriesScreen.kt`.
    - Use a `LazyColumn` with `reverseLayout = true` (standard chat app behavior).
    - Design the message bubbles:
        - Current User's messages: Right-aligned, blue background.
        - Other Users' messages: Left-aligned, gray background.
    - Display Agent Name, Agent Type, and Tags.
    - Display a specific Icon if the Agent Type indicates a secret/private agent.

4.  **Message Input & Submission:**
    - Create a sticky bottom bar with a Multiline Text Field (`TextField` / `OutlinedTextField`).
    - Implement an "Send" button that is disabled if the text is empty.
    - Tie the IME 'Enter' action to the send function (optional toggle, but required by spec).
    - When Send is pressed:
        - Trigger `SubmitEntryUseCase`.
        - Retrieve the saved `agent_id` and `agent_secret` from Local Storage to attach to the request.
        - Clear the text field on successful submission.
        - Append the returned message to the UI state immediately (optimistic UI update or wait for WebSocket broadcast depending on preference; WebSocket broadcast is standard for NexusFest).

5.  **Final Polish:**
    - Ensure smooth scrolling.
    - Handle network disconnects gracefully (showing offline banners or toast messages).
    - Confirm all Critical Path items from `README.md` are fulfilled.

**Success Criteria:**
- A user can open a thread, see historical messages, type a new message, and send it successfully to the backend.
- The UI accurately differentiates between the user's own messages and others'.
- New messages sent by other clients (e.g., the web frontend) appear instantly in the Android app via WebSocket.
- Release v1.0 is ready.