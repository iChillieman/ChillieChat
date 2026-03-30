# ChillieChat

ChillieChat is an Android Application to use the Chat Features hosted at chillieman.com

### **The Goal:**

Make an Android Application that mimics the behavior of the SvelteKit frontend on
https://chillieman.com

Note: The Chillieman website (ChillieSite), hosted at chillieman.com has two distinct "sections"
or "feature sets", one is a Chat Application, one is The Forge. The scope of this application is
only the **chat portion** of ChillieSite.

----

# General Data terminology:

1. An `Event` is the root of every chat/discussion in the app. `Event`s contain a set of `Thread`s.
2. A `Thread` is an individual conversations, which are linked to Events, and contain ALOT of
   `Entry` elements.
3. An `Entry` is an individual message that a user contributes to a `Thread`. Each `Entry` is
   created by an `Agent`.
4. An `Agent` is a representation of the user. Each user can optionally select a name / secret, or
   contribute to the chat anonymously.

----

# Critical Path Requirements:

The following features are required in order to release v1.0.

### User Settings

- [ ] Settings Icon in the top right is always visible to the user on any screen (except the
  Settings Screen itself)
- [ ] Allow user to change their name / secret.
- [ ] Remember this name / secret between app sessions (Use SharedPreferences).

See USER_SETTINGS.md for Endpoint specifications (including response and request params)

### Events

- [ ] Fetch and display Events in visually pleasing Cards (Events Screen).
- [ ] Selecting an Event navigates user to a "Threads" screen.

See EVENTS.md for Endpoint specifications (including response and request params)

### Threads

- [ ] Fetch and display Threads in a visually pleasing List (Threads Screen). Threads are pulled via
  the `Event.id` that was selected in the Events Screen.
- [ ] Selecting a Thread navigates user to "Entries" screen - where the Threads belonging to an
  Event are displayed

See THREADS.md for Endpoint specifications (including response and request params)

### Entries

- [ ] Fetch and display Entries in a visually pleasing List (Entries Screen). This includes
  displaying: Entry Message, Agent name, Agent Type, Agent Type Icon, "Agent Has Secret" icon.
  Entries are pulled via the `Thread.id` that was selected in the Threads Screen.
- [ ] Entries that are submitted by the current user should appear on the RIGHT side of screen, with
  a Blue Background 
- [ ] Entries that are submitted by anyone else should appear on the LEFT side of
  the screen, with a gray background.
- [ ] Allow users to enter new message in a Multiline Text widget at the bottom of screen.
- [ ] IME ENTER button will submit message (only if Text Widget isn't empty). UI Button will also
  submit message (only if Text Widget isn't empty)
- [ ] Implement Websocket connection on specific Thread to automatically show new `Entry` when they
  are submitted.

See ENTRIES.md for specific Endpoints (including response and request params)

----

# Specifications / Architecture:

**Core Architecture:**

1. Hilt for all Dependency Injection.
2. Single Activity architecture. All screens and UI built with **Jetpack Compose**. Navigation
   handled by **Navigation 3 (Nav3)** for Compose (type-safe routes preferred).
3. MVVM with strong **Unidirectional Data Flow**. All business logic lives in ViewModel, UseCase, or
   Repository layers. **Never** in composables.
4. ViewModels expose a single `StateFlow<ScreenUiState>` (sealed class with Loading/Success/Error
   states). UI collects it using `collectAsStateWithLifecycle()`.
5. Data layer: Room for local persistence + Retrofit for remote APIs. All data access goes through
   Repositories (cold `Flow` or `suspend` functions).
6. Repositories **do not** expose `StateFlow` — only cold `Flow` (or suspend functions). ViewModel
   converts to `StateFlow`.
7. Optional **Domain Layer** (UseCases/Interactors) for complex business rules between Repository
   and ViewModel.
8. Kotlin Coroutines + Flow heavily used throughout.
9. Lifecycle-aware collection in Compose.

**Testing:**

- Unit tests: **JUnit5** (or JUnit4) + **Mockito** (or **Mockk**).
- ViewModel / UseCase / Repository tests: Test `StateFlow` emissions or use Turbine for Flows.
- UI tests: **Compose UI Testing** for Compose screens (primary). Espresso only if mixing legacy
  Views.
- Robolectric for instrumented tests without hardware.

----

# Other Considerations / Implementations:

1. **Unidirectional Data Flow (UDF)**
    - UI sends **events/actions** (via lambdas or sealed classes) to the ViewModel.
    - ViewModel processes them and updates the single `UiState` (exposed as `StateFlow`).

2. **Error Handling & Loading States**  
   `UiState` will be a **sealed class/interface** with variants like:
    - `Loading`
    - `Success(data)`
    - `Error(message, cause?)`
    - Sometimes `Empty` or `Offline`

3. **Coroutine Scope & Sharing Strategy**  
   In ViewModels, use `SharingStarted.WhileSubscribed(5000)`.

   **Example Consumption of a Repository Flow in a ViewModel:**

   ```kotlin
   val uiState: StateFlow<UserUiState> = repository.getUserProfile(userId)
        .map { data -> UserUiState.Success(data) }
        .catch { emit(UserUiState.Error(it)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),  // stops after 5s inactivity
            initialValue = UserUiState.Loading
        )
   ```
4. **Best Practice for Data Fetching / Processing**

   | Layer        | Recommended Observable                                | Reason                                                                      |
   |--------------|-------------------------------------------------------|-----------------------------------------------------------------------------|
   | Repository   | Cold `Flow` or `suspend fun`                          | Lazy, reusable, no UI assumptions                                           |
   | Use Case     | Cold `Flow`                                           | Business logic on streams                                                   |
   | ViewModel    | `StateFlow` (via `stateIn`)                           | Holds UI state, survives config changes, single source of truth for Compose |
   | UI (Compose) | `collectAsStateWithLifecycle()` on the VM's StateFlow | Lifecycle-aware collection                                                  |

5. **Image Rendering with Coil**
6. **Kotlinx Serialization**
7. **Baseline Profiles**