# Critical Path 6: Testing & Polish

**Objective:**
Solidify the application for a v1.0 release. This path ensures the core functionality is robust, the UI is smooth, and the codebase demonstrates senior-level testing practices for the portfolio.

**Requirements & Tasks:**

1.  **Unit Testing (Domain & Data Layers):**
    - **UseCases:** Write JUnit tests for the UseCases. Mock the Repositories using Mockito or MockK. Verify that success and error states from the repositories are correctly transformed or propagated.
    - **Mappers:** Test the data mappers to ensure edge cases (like null fields or missing capabilities) are handled correctly when transforming DTOs to Entities to Domain Models.
    - **ViewModels:** Write tests for the ViewModels using `Turbine` to verify the `StateFlow` emissions. Ensure that calling an intent on the ViewModel properly transitions the `UiState` from `Loading` to `Success` (or `Error`).

2.  **UI Testing (Compose):**
    - Write basic Compose UI tests for the core screens (`SettingsScreen`, `EventsScreen`, `ThreadsScreen`, `EntriesScreen`).
    - Verify that loading indicators appear when the state is loading.
    - Verify that error messages or snackbars appear on error states.
    - Verify that user inputs (typing in the chat box, clicking the send button) trigger the correct callbacks.

3.  **Error Handling & Edge Cases:**
    - Ensure global error handling is in place (e.g., handling network timeouts, 500 errors gracefully without crashing).
    - Add user-friendly UI for empty states (e.g., "No events available right now", "Be the first to say something in this thread!").

4.  **Polish & Performance:**
    - Verify smooth scrolling in all `LazyColumn`s.
    - Ensure proper handling of configuration changes (screen rotations shouldn't lose text in the input box).
    - Check the WebSocket lifecycle: ensure it disconnects cleanly when navigating away from the `EntriesScreen` and reconnects when navigating back.

**Success Criteria:**
- High test coverage on UseCases and ViewModels.
- Core UI flows are verified by automated tests.
- Application handles offline states and network errors gracefully.
- App is fully ready to be published or demonstrated as a high-quality portfolio piece.