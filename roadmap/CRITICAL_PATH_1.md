# Critical Path 1: The Data Layer Setup

**Objective:**
Establish the core data foundation for ChillieChat. This means mapping the NexusFest backend specifications (from `specs/`) into Retrofit API interfaces, Remote Data Transfer Objects (DTOs), Room Entities, and local Database Data Access Objects (DAOs).

**Requirements & Tasks:**

1.  **Dependencies Check:**
    - Ensure Retrofit, Moshi/Gson (or Kotlinx Serialization), OkHttp (with logging interceptor), and Room are correctly added to `build.gradle.kts` (app level) and synced.

2.  **Remote DTOs:**
    - Create Kotlin data classes reflecting the JSON structures in `specs/`.
    - Place them under `data/remote/dto/`.
    - Key Models:
        - `AgentResponseDto`, `SecurePublicAgentRequestDto`, `PrivateAgentRequestDto`
        - `EventDto`, `EventWithThreadsDto`
        - `ThreadDto`
        - `EntryDto`, `EntryRequestDto`, `PagedListEntryWithAgentDetailsDto`, `EntryWithAgentDetailsDto`

3.  **Retrofit API Interfaces:**
    - Create interfaces under `data/remote/api/` defining the HTTP endpoints.
    - Examples: `AgentApi`, `EventApi`, `ThreadApi`, `EntryApi`.
    - Use suspend functions with appropriate annotations (e.g., `@GET("/api/events/")`, `@POST("/api/entries/")`).

4.  **Room Entities:**
    - Create Room `@Entity` classes under `data/local/entity/` to mirror the data we need to persist locally (for offline viewing or caching).
    - *Note:* Consider what needs to be cached. At a minimum, we likely want to cache `EventEntity`, `ThreadEntity`, and `EntryEntity`.
    - Handle TypeConverters for any nested lists or complex objects if necessary (e.g., storing tags as strings).

5.  **Room DAOs:**
    - Create DAOs under `data/local/dao/` to handle local read/writes.
    - Implement `Flow` return types for observable reads (e.g., `fun getEvents(): Flow<List<EventEntity>>`).
    - Implement `suspend` functions for inserts/updates (e.g., `suspend fun insertEvents(events: List<EventEntity>)`).

6.  **Database Assembly:**
    - Define the `AppDatabase` class extending `RoomDatabase` in `data/local/` and register all Entities and DAOs.

**Success Criteria:**
- The application compiles.
- DTOs exactly match the backend JSON schemas.
- Retrofit interfaces perfectly map to the backend endpoints.
- Room database is successfully configured with `@Database`, Entities, and DAOs.