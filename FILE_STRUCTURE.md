# File Structure

Core Skeleton / Architecture of Files:

```
ChillieChat/
├── app/                                       ← Single module project
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/chillieman/chilliechat/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── MyApplication.kt           ← @HiltAndroidApp
│   │   │   │   │
│   │   │   │   ├── di/                        ← All Hilt modules
│   │   │   │   │   ├── AppModule.kt
│   │   │   │   │   ├── DatabaseModule.kt
│   │   │   │   │   ├── NetworkModule.kt
│   │   │   │   │   └── RepositoryModule.kt    (optional)
│   │   │   │   │
│   │   │   │   ├── data/                      ← Data Layer
│   │   │   │   │   ├── local/                 ← Room
│   │   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   └── entity/            ← @Entity classes
│   │   │   │   │   │
│   │   │   │   │   ├── remote/                ← Retrofit
│   │   │   │   │   │   ├── api/
│   │   │   │   │   │   └── dto/               ← API response models
│   │   │   │   │   │
│   │   │   │   │   └── repository/            ← Repository implementations
│   │   │   │   │       └── UserRepositoryImpl.kt
│   │   │   │   │
│   │   │   │   ├── domain/                    ← Domain Layer
│   │   │   │   │   ├── model/                 ← Clean domain models (no DB/JSON annotations)
│   │   │   │   │   ├── repository/            ← Interfaces only
│   │   │   │   │   └── usecase/               ← UseCase classes (e.g. GetUserProfileUseCase)
│   │   │   │   │
│   │   │   │   ├── presentation/              ← UI Layer (MVVM)
│   │   │   │   │   ├── navigation/            ← Nav3 setup
│   │   │   │   │   │   ├── AppNavigation.kt
│   │   │   │   │   │   └── Screens.kt         ← Sealed class or data objects for routes
│   │   │   │   │   │
│   │   │   │   │   ├── ui/                    ← All Compose-related code
│   │   │   │   │   │   ├── theme/
│   │   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   │   └── Color.kt / Type.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   └── screens/           ← One folder per major screen
│   │   │   │   │   │       ├── home/
│   │   │   │   │   │       │   ├── HomeScreen.kt
│   │   │   │   │   │       │   ├── HomeViewModel.kt
│   │   │   │   │   │       │   └── HomeUiState.kt
│   │   │   │   │   │       ├── detail/
│   │   │   │   │   │       └── ...
│   │   │   │   │   │
│   │   │   │   │   └── components/            ← Reusable Compose widgets
│   │   │   │   │
│   │   │   │   └── util/                      ← Helpers, extensions, constants
│   │   │   │
│   │   │   ├── res/                           ← Standard resources (drawable, values, etc.)
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   ├── test/                              ← Unit tests (ViewModel, Repository, UseCase)
│   │   │   └── java/com/chillieman/myapp/
│   │   │
│   │   └── androidTest/                       ← UI tests (Compose Testing)
│   │
│   ├── build.gradle.kts                       ← App module dependencies
│   └── proguard-rules.pro
│
├── build.gradle.kts (project level)
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml                     ← Version Catalog
│
└── README.md
```