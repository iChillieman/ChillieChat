# Critical Path 2: Domain Layer & Repository Integration

**Objective:**
Bridge the raw Data Layer (Network + Database) with the clean Domain Layer. This involves creating clean Domain Models, writing Mappers to translate between DTOs/Entities and Domain Models, implementing Repositories, building UseCases for business logic, and wiring everything together with Hilt Dependency Injection.

**Requirements & Tasks:**

1.  **Domain Models:**
    - Create clean Kotlin data classes in `domain/model/` that have absolutely zero knowledge of JSON annotations or Room `@Entity` annotations.
    - Examples: `Agent`, `Event`, `Thread`, `Entry`.

2.  **Mappers:**
    - Create mapping extensions or classes (e.g., in `data/mapper/`) to translate data across boundaries.
    - `Entity.toDomain()`, `Dto.toEntity()`, `Dto.toDomain()`.
    - This ensures the UI only ever deals with pure Domain Models.

3.  **Repository Interfaces:**
    - Define interfaces in `domain/repository/` detailing what the app needs (e.g., `AgentRepository`, `EventRepository`, `ChatRepository`).
    - Methods should return Domain Models or `Flow<List<DomainModel>>`. Example: `fun getEvents(): Flow<List<Event>>`.

4.  **Repository Implementations:**
    - Create classes in `data/repository/` that implement the interfaces from step 3.
    - Implement the caching strategy (Network Bound Resource / "Single Source of Truth"):
        - Fetch from Network -> Insert into Room -> Return `Flow` from Room DAO.

5.  **Use Cases (Interactors):**
    - Create Use Cases in `domain/usecase/` for distinct actions.
    - Examples: `GetEventsUseCase`, `FetchAndCacheThreadsUseCase`, `SubmitEntryUseCase`.
    - *Crucial:* Return `Flow<Result<T>>` or use standard Kotlin exceptions inside suspend functions to propagate Success/Error states cleanly.

6.  **Hilt Dependency Injection Setup:**
    - Ensure Hilt plugin is applied and `MyApplication` is annotated with `@HiltAndroidApp`.
    - Create Hilt modules under `di/`:
        - `AppModule.kt` (Provide CoroutineDispatchers, etc.)
        - `DatabaseModule.kt` (Provide Room Database and DAOs)
        - `NetworkModule.kt` (Provide Retrofit, OkHttpClient, Moshi/Gson)
        - `RepositoryModule.kt` (`@Binds` implementations to interfaces)
        - `UseCaseModule.kt` (Provide UseCase instances)

**Success Criteria:**
- DTOs and Entities map flawlessly to Domain Models.
- Repositories handle the Network-to-Local-DB flow correctly.
- UseCases wrap repository logic cleanly.
- Hilt can successfully inject a Repository or UseCase into a placeholder ViewModel without throwing dependency graph errors.