# Critical Path 1 & 2 - Implementation Response

**Implementer:** Claude (Opus 4.6)
**Branch:** `feat/critical-path-init`
**Date:** 2026-03-30
**Addressed To:** Daedalus

---

## Overview

Both Critical Path 1 (Data Layer Setup) and Critical Path 2 (Domain Layer & Repository Integration) have been fully implemented across two commits. The application compiles successfully with the complete Hilt dependency graph resolving without errors.

---

## Critical Path 1: Data Layer Setup

### Dependencies Added

All dependencies were added to the version catalog (`gradle/libs.versions.toml`) and wired through the build files:

| Dependency | Version | Notes |
|---|---|---|
| KSP | `2.2.10-2.0.2` | See [KSP Version Note](#ksp-version-bump) below |
| Hilt | `2.59.2` | See [Hilt Version Note](#hilt-version-bump) below |
| Room | `2.7.1` | Runtime + KTX + KSP compiler |
| Retrofit | `2.11.0` | With built-in kotlinx-serialization converter |
| OkHttp | `4.12.0` | With logging interceptor |
| Kotlinx Serialization | `1.8.1` | JSON module |

### Remote DTOs Created (`data/remote/dto/`)

11 data classes created, all annotated with `@Serializable` and `@SerialName` for explicit JSON field mapping:

- **Agent:** `AgentResponseDto`, `SecurePublicAgentRequestDto`, `PrivateAgentRequestDto`
- **Event:** `EventDto`, `EventWithThreadsDto`
- **Thread:** `ThreadDto`, `ThreadRequestDto`
- **Entry:** `EntryDto`, `EntryRequestDto`, `EntryWithAgentDetailsDto`, `PagedListEntryWithAgentDetailsDto`

**Decision:** `ThreadDto.entryCount` is nullable (`Int?`) because the thread object returned from `POST /api/threads/` (creation) does not include `entry_count`, while the thread objects nested inside `EventWithThreadsDto` do. Making it nullable with a default of `null` handles both cases cleanly.

### Retrofit API Interfaces (`data/remote/api/`)

4 interfaces created covering all endpoints from the specs:

- **AgentApi** - `securePublicAgent`, `fetchPrivateAgent`, `securePrivateAgent`
- **EventApi** - `getEvents` (with optional tag filter), `getEventWithThreads`, `getEventByThreadId`
- **ThreadApi** - `createThread`, `getThreadEntries`
- **EntryApi** - `getEntries` (paginated), `getEntriesForAgent` (with `X-Agent-Secret` header), `createEntry`

### Room Entities (`data/local/entity/`)

4 entities created with proper relational integrity:

- **AgentEntity** - Simple table, PK on `id`
- **EventEntity** - All fields from the API spec, PK on `id`
- **ThreadEntity** - Foreign key to `EventEntity` with `CASCADE` delete, indexed on `event_id`
- **EntryEntity** - Foreign keys to both `ThreadEntity` and `AgentEntity` with `CASCADE` delete, indexed on `thread_id` and `agent_id`

**Decision:** I included `AgentEntity` even though it wasn't explicitly listed as a "minimum" in the CP1 doc. Caching agents locally is essential for the Entries screen (displaying agent names/types alongside messages) and avoids redundant network calls. The entries endpoint returns agent details embedded in each entry, so we extract and cache those agents during the refresh flow.

### Room DAOs (`data/local/dao/`)

4 DAOs created with `Flow` return types for observable reads and `suspend` functions for writes:

- **AgentDao** - `getAgentById` (Flow), `insertAgent`, `insertAgents`, `deleteAgent`
- **EventDao** - `getEvents` (Flow, ordered by start_time DESC), `getEventById` (Flow), `insertEvents`, `insertEvent`, `deleteAllEvents`
- **ThreadDao** - `getThreadsByEventId` (Flow, ordered by created_at DESC), `getThreadById` (Flow), `insertThreads`, `insertThread`, `deleteThreadsByEventId`
- **EntryDao** - `getEntriesByThreadId` (Flow, ordered by timestamp ASC for chat order), `getEntriesByAgentId` (Flow), `insertEntries`, `insertEntry`, `deleteEntriesByThreadId`, `getLowestEntryIdForThread` (for pagination cursor)

### AppDatabase (`data/local/AppDatabase.kt`)

Registered all 4 entities and 4 DAOs. `exportSchema = false` for now since we're in early development - can be enabled later for migration testing.

---

## Critical Path 2: Domain Layer & Repository Integration

### Domain Models (`domain/model/`)

6 clean data classes with zero annotations:

- `Agent`, `Event`, `Entry`, `EntryWithAgent`, `EventWithThreads`, `ChatThread`

**Decision - `ChatThread` naming:** I named the domain model `ChatThread` instead of `Thread` to avoid collision with `java.lang.Thread`. This is a common Android/Kotlin pitfall - having a class named `Thread` in your domain would require fully-qualified names everywhere and cause confusion in IDE auto-imports. `ChatThread` is unambiguous and self-documenting.

### Mappers (`data/mapper/`)

4 mapper files using Kotlin extension functions for ergonomic conversions:

- `AgentMapper.kt` - `AgentResponseDto.toDomain()`, `AgentResponseDto.toEntity()`, `AgentEntity.toDomain()`
- `EventMapper.kt` - `EventDto.toDomain()`, `EventDto.toEntity()`, `EventEntity.toDomain()`, `EventWithThreadsDto.toDomain()`, `EventWithThreadsDto.toEventEntity()`
- `ThreadMapper.kt` - `ThreadDto.toDomain()`, `ThreadDto.toEntity()`, `ThreadEntity.toDomain()`
- `EntryMapper.kt` - `EntryDto.toDomain()`, `EntryDto.toEntity()`, `EntryEntity.toDomain()`, `EntryWithAgentDetailsDto.toDomain()`, `EntryWithAgentDetailsDto.toEntryEntity()`

### Repository Interfaces (`domain/repository/`)

4 interfaces defined in the domain layer (no data-layer dependencies):

- **AgentRepository** - CRUD + all 3 secure/fetch agent operations
- **EventRepository** - Observable `getEvents()` Flow, `refreshEvents()`, `getEventWithThreads()`, `getEventByThreadId()`
- **ThreadRepository** - Observable `getThreadsByEventId()` Flow, `refreshThreadsForEvent()`, `createThread()`
- **EntryRepository** - Observable `getEntriesByThreadId()` Flow, `refreshEntries()` (returns `Boolean` for `hasMore` pagination signal), `createEntry()`, `getEntriesForAgent()`

### Repository Implementations (`data/repository/`)

All 4 implementations follow the **Single Source of Truth** pattern described in the README:

1. **Network fetch** -> **Cache to Room** -> **Return Flow from Room**

Example flow for `EventRepositoryImpl.getEventWithThreads()`:
- Calls `eventApi.getEventWithThreads(eventId)`
- Caches both the event (`eventDao.insertEvent`) and its threads (`threadDao.insertThreads`)
- Returns the mapped domain model

`EntryRepositoryImpl.refreshEntries()` additionally extracts and caches `AgentEntity` records from the `EntryWithAgentDetailsDto` responses, so agent data is available locally for the chat UI.

### Use Cases (`domain/usecase/`)

7 use cases created:

| Use Case | Type | Description |
|---|---|---|
| `GetEventsUseCase` | `Flow` | Returns events Flow, triggers network refresh via `onStart` |
| `GetEventWithThreadsUseCase` | `suspend` | Fetches event + threads from network, caches, returns domain model |
| `GetThreadsForEventUseCase` | `Flow` | Returns threads Flow for an event, triggers refresh via `onStart` |
| `GetEntriesUseCase` | `Flow` | Returns entries Flow for a thread, triggers refresh via `onStart` |
| `SubmitEntryUseCase` | `suspend` | Creates a new entry via API and caches locally |
| `SecureAgentUseCase` | `suspend` | Wraps all 3 agent auth operations (securePublic, securePrivate, fetchPrivate) |
| `CreateThreadUseCase` | `suspend` | Creates a new thread under an event |

**Decision - `onStart` for refresh:** The Flow-based use cases (`GetEventsUseCase`, `GetThreadsForEventUseCase`, `GetEntriesUseCase`) use `onStart { repository.refresh() }` to trigger a network fetch when the Flow is first collected. This gives the UI immediate cached data (if available) followed by fresh data once the network call completes - a natural "show cached, then refresh" UX pattern.

### Hilt Dependency Injection (`di/`)

4 modules created:

- **AppModule** (`@InstallIn(SingletonComponent)`) - Provides `CoroutineDispatcher` with `@IoDispatcher` qualifier
- **NetworkModule** (`@InstallIn(SingletonComponent)`) - Provides `Json` (with `ignoreUnknownKeys` and `coerceInputValues`), `OkHttpClient` (with logging interceptor), `Retrofit`, and all 4 API interfaces as singletons
- **DatabaseModule** (`@InstallIn(SingletonComponent)`) - Provides `AppDatabase` singleton and all 4 DAOs
- **RepositoryModule** (`@InstallIn(SingletonComponent)`) - Uses `@Binds` to map implementations to interfaces

**Decision - No `UseCaseModule`:** The CP2 spec mentioned a `UseCaseModule`, but it's unnecessary here. All use cases use `@Inject constructor`, so Hilt can construct them automatically without explicit `@Provides` or `@Binds` methods. Adding a module would just be boilerplate with no benefit.

### Application & Activity Setup

- Created `ChillieChatApplication` with `@HiltAndroidApp`
- Annotated existing `MainActivity` with `@AndroidEntryPoint`
- Registered `ChillieChatApplication` in `AndroidManifest.xml` via `android:name`
- Added `INTERNET` permission to manifest

---

## AGP 9.x Compatibility Notes

Two version/compatibility issues were encountered and resolved during implementation:

### KSP Version Bump

KSP has moved to a 2.0.x versioning scheme for Kotlin 2.2.x. The initial attempt with `2.2.10-1.0.31` failed because that version doesn't exist. The correct version is **`2.2.10-2.0.2`**.

### Hilt Version Bump

Hilt `2.56.2` is incompatible with AGP 9.x - it looks for the legacy `Android BaseExtension` API which was removed. Bumped to **`2.59.2`** which supports AGP 9.x.

### KSP Source Sets

AGP 9.x has built-in Kotlin support and by default rejects KSP's attempt to register Kotlin source sets via the `kotlin.sourceSets` DSL. Added `android.disallowKotlinSourceSets=false` to `gradle.properties` to allow KSP-generated sources. This is marked as experimental by Google but is currently required for KSP + AGP 9.x interop.

---

## What's Ready for Critical Path 3

The full data pipeline is in place. ViewModels can now:
- Inject any Use Case via `@Inject constructor`
- Collect `Flow<List<Event>>`, `Flow<List<ChatThread>>`, `Flow<List<Entry>>` and convert to `StateFlow<UiState>` using `stateIn()`
- Call suspend use cases for mutations (submit entry, create thread, secure agent)

The codebase is ready for the presentation layer (Navigation, Screens, ViewModels, Compose UI).
