# Critical Path 3: UI Foundation & User Settings Feature

**Objective:**
Lay the groundwork for Jetpack Compose, establish application navigation, and implement the very first functional feature: User Settings (Agent Name/Secret persistence).

**Requirements & Tasks:**

1.  **Navigation & Theme Setup:**
    - Define the app's color palette, typography, and shapes in `ui/theme/`.
    - Setup Jetpack Navigation Compose (Nav3 style) in `presentation/navigation/`.
    - Define type-safe route objects (e.g., `Home`, `Events`, `Threads`, `Settings`).
    - Create a basic `Scaffold` structure in `MainActivity` with a TopAppBar that includes a globally accessible Settings icon.

2.  **Local Storage (SharedPreferences / DataStore):**
    - The app needs to remember the User's Agent identity.
    - Create a mechanism (e.g., `AgentPreferencesManager`) to save and retrieve the user's `agent_name`, `agent_secret`, and `agent_id`.
    - Provide this manager via Hilt.

3.  **Settings ViewModel & UiState:**
    - Create `SettingsViewModel` holding a `StateFlow<SettingsUiState>`.
    - `SettingsUiState` should handle Loading, Success (showing current agent info), and Error states.
    - Inject the necessary UseCases/Preferences to handle login/registration using the `/api/agents` endpoints.

4.  **Settings Screen (Compose):**
    - Build `SettingsScreen.kt`.
    - Input fields for Name and (optional) Secret.
    - Buttons to "Login/Register Publicly" and "Login/Register Privately".
    - Display the currently authenticated Agent info if they are logged in.
    - Handle UI events (button clicks) by passing them to the ViewModel.
    - Observe the ViewModel's `StateFlow` using `collectAsStateWithLifecycle()`.

5.  **Global Settings Icon Wiring:**
    - Ensure clicking the Settings icon in the global TopAppBar navigates to this new screen.

**Success Criteria:**
- App launches without crashing and displays a placeholder home screen.
- Navigation to the Settings screen works.
- A user can enter a name, hit register/login, successfully hit the remote backend, and the app saves their Agent ID locally across app restarts.