# Critical Path 3 - Implementation Response

**Implementer:** Claude (Opus 4.6)
**Branch:** `feat/critical-3-impl`
**Date:** 2026-03-30
**Addressed To:** Daedalus

---

## Overview

Critical Path 3 (UI Foundation & User Settings Feature) is fully implemented in a single commit. The app now launches with a proper Scaffold + TopAppBar structure, navigates to a fully functional Settings screen via the gear icon, and persists the user's Agent identity across app restarts using DataStore.

---

## 1. Navigation & Theme Setup

### Theme Reorganization

The wizard-generated theme files lived at `ui/theme/` which didn't match FILE_STRUCTURE.md. Moved everything to `presentation/ui/theme/` and updated package declarations accordingly.

**Color palette:** Replaced the default purple Material theme with a ChillieChat blue brand palette:
- Primary: `#1E88E5` (blue) with dark/light variants
- Accent: `#26A69A` (teal)
- Custom surface colors for both light and dark themes
- Dynamic color still supported on Android 12+ devices (falls back to ChillieChat palette on older devices)

**Typography:** Expanded beyond the wizard's single `bodyLarge` definition to include `headlineLarge`, `headlineMedium`, `titleLarge`, `bodyLarge`, `bodyMedium`, and `labelSmall` — enough for all the screens we'll build in CP4/CP5.

### Navigation

**Decision - `navigation-compose` 2.9.7 over Nav3:** The README mentions "Nav3" but the `navigation3` library is still in early alpha and not production-ready. I used the established `navigation-compose` 2.9.7 which fully supports type-safe routes via `@Serializable` — achieving the same developer experience the spec intended. This is a drop-in upgrade path to Nav3 when it stabilizes.

**Route definitions (`presentation/navigation/Screens.kt`):**

```kotlin
@Serializable object EventsRoute
@Serializable data class ThreadsRoute(val eventId: Int, val eventTitle: String)
@Serializable data class EntriesRoute(val threadId: Int, val threadTitle: String)
@Serializable object SettingsRoute
```

`ThreadsRoute` and `EntriesRoute` carry both the ID and the display title as parameters — this lets the TopAppBar show the correct title immediately without waiting for a network fetch.

**NavHost (`presentation/navigation/AppNavigation.kt`):** Currently wires `EventsRoute` → `HomeScreen` (placeholder) and `SettingsRoute` → `SettingsScreen`. The `ThreadsRoute` and `EntriesRoute` composable destinations are defined but not yet wired — ready for CP4/CP5 to plug in.

### Scaffold & TopAppBar

**`ChillieChatTopBar` (`presentation/components/ChillieChatTopBar.kt`):**
- Reusable component accepting `title`, `showSettingsIcon`, `showBackButton`, and click callbacks
- Settings gear icon (`Icons.Default.Settings`) visible on all screens except Settings itself
- Back arrow (`Icons.AutoMirrored.Filled.ArrowBack`) shown on non-root screens
- Uses `TopAppBarDefaults` colors tied to the MaterialTheme

**`MainActivity` rewrite:**
- Removed the wizard's `NavigationSuiteScaffold`, `AppDestinations` enum, and `Greeting` composable entirely
- Replaced with `Scaffold` + `ChillieChatTopBar` + `AppNavigation(NavHost)`
- Route-aware title: shows "ChillieChat" on the events screen, "Settings" on the settings screen
- Settings icon navigates with `launchSingleTop = true` to prevent duplicate stack entries

---

## 2. Local Storage (DataStore)

**Decision - DataStore over SharedPreferences:** The CP3 spec listed both as options. I chose DataStore Preferences because:
1. It's Google's recommended replacement for SharedPreferences
2. It returns `Flow<Preferences>` natively — fits the reactive/UDF architecture perfectly
3. It handles threading automatically (no risk of ANR from main-thread reads)
4. Migration path to Proto DataStore is trivial if we later need structured data

**`AgentPreferencesManager` (`data/local/AgentPreferencesManager.kt`):**
- Stores: `agent_id` (Int), `agent_name` (String), `agent_secret` (String), `agent_type` (String)
- Exposes `agentPreferences: Flow<AgentPreferences>` for reactive observation
- `saveAgent()` and `clearAgent()` suspend functions for writes
- `@Singleton` + `@Inject constructor` — Hilt provides it automatically, no module needed
- Uses `@param:ApplicationContext` annotation target to avoid Kotlin 2.2.x compiler warning about future annotation behavior (KT-73255)

**Data class `AgentPreferences`:** Simple holder with all-nullable fields. When all fields are null, no agent is logged in. This avoids a separate "is logged in" flag.

---

## 3. Settings ViewModel & UiState

**`SettingsUiState` sealed interface:**
- `Loading` — shown while reading DataStore on init
- `Success(currentAgent, nameInput, secretInput, isSubmitting)` — main interactive state
- `Error(message)` — shown on API failures with a "Try Again" button

**`SettingsViewModel`:**
- On `init`, reads `AgentPreferencesManager.agentPreferences.first()` to hydrate state. If a saved agent exists, reconstructs an `Agent` domain model and pre-fills the input fields.
- `onNameChanged` / `onSecretChanged` — simple input state updates
- `loginPublic()` — calls `SecureAgentUseCase.securePublic()`, saves to DataStore on success
- `loginPrivate()` — calls `SecureAgentUseCase.securePrivate()`, saves to DataStore on success (including the secret)
- `logout()` — clears DataStore, resets to empty `Success` state
- `dismissError()` — re-runs `loadSavedAgent()` to recover from error state

**Decision - MutableStateFlow over stateIn:** Since the SettingsViewModel needs to handle imperative mutations (form input, submit results, error recovery), a `MutableStateFlow` with `update {}` is more natural than piping everything through `stateIn()`. The README's `stateIn` pattern is better suited for the data-observation ViewModels in CP4/CP5.

---

## 4. Settings Screen (Compose)

**`SettingsScreen` (`presentation/ui/screens/settings/SettingsScreen.kt`):**

Three visual states matching the sealed interface:

1. **Loading** — centered `CircularProgressIndicator`
2. **Success** — scrollable column containing:
   - **Current Agent Card** (if logged in): Shows agent name, type, ID, "Secured with secret" indicator if applicable, and a Logout button. Uses `primaryContainer` colors for visual prominence.
   - **Input Section**: "Set Up Your Agent" (or "Change Agent" if already logged in) header, Name text field, Secret text field (with `PasswordVisualTransformation`), and two action buttons
   - **"Login / Register Publicly"** button (enabled when name is non-blank)
   - **"Login / Register Privately"** outlined button (enabled when both name and secret are non-blank)
   - Helper text explaining the difference between public and private agents
   - All inputs disabled + spinner shown during `isSubmitting`
3. **Error** — centered error card with message and "Try Again" button

**Decision - `hiltViewModel()` import:** The `androidx.hilt.navigation.compose.hiltViewModel` import is deprecated in favor of `androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel`. Used the non-deprecated import to avoid build warnings.

---

## 5. Dependencies Added

| Dependency | Version | Purpose |
|---|---|---|
| `navigation-compose` | 2.9.7 | NavHost + type-safe routes |
| `hilt-navigation-compose` | 1.3.0 | `hiltViewModel()` in composable destinations |
| `lifecycle-runtime-compose` | 2.10.0 | `collectAsStateWithLifecycle()` |
| `datastore-preferences` | 1.2.1 | Agent identity persistence |
| `material-icons-extended` | (BOM) | Settings, Person, Lock, ArrowBack icons |

**Note on material-icons-extended:** This dependency adds APK size (~2-3 MB). If that becomes a concern later, we can switch to `material-icons-core` (smaller set) or use custom drawable resources for the few icons we need. For development speed, the extended set is more convenient.

---

## What's Ready for Critical Path 4

The UI foundation is in place. The next critical path can:
- Add `EventsViewModel` + `EventsScreen` wired to `GetEventsUseCase` → the placeholder `HomeScreen` is ready to be replaced
- Add `ThreadsViewModel` + `ThreadsScreen` with navigation from events
- Wire `ThreadsRoute` and `EntriesRoute` destinations into `AppNavigation.kt`
- Use `AgentPreferencesManager` to include agent identity when submitting entries

The navigation routes already carry `eventId`/`eventTitle` and `threadId`/`threadTitle` parameters, so screens can display titles immediately and fetch data in the background.
