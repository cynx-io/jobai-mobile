# Kotlin Multiplatform with Amper - Best Practices & Guidelines

## Project Overview

This is a Kotlin Multiplatform project using **Amper** as the build system, targeting Android and iOS platforms (excluding iOS from development focus per requirements). The project implements a museum object viewer app using Compose Multiplatform with modern architecture patterns.

## Architecture & Structure

### Module Organization
- **`shared/`** - Contains shared business logic and UI components
- **`androidApp/`** - Android-specific application code
- **`iosApp/`** - iOS-specific application code (ignored in development)

### Key Technologies Used
- **Amper 0.5.0** - Modern build system for Kotlin Multiplatform
- **Compose Multiplatform** - Cross-platform UI framework
- **Ktor** - HTTP client for networking
- **Koin** - Dependency injection
- **Voyager** - Navigation and screen models
- **Kamel** - Image loading
- **kotlinx.serialization** - JSON serialization

## ✅ Best Practices - DO

### 1. Amper Configuration

**Use module.yaml for each module:**
```yaml
# shared/module.yaml
product: 
  type: lib
  platforms: [jvm, android, iosArm64, iosSimulatorArm64, iosX64]

dependencies: 
  - $compose.runtime
  - $compose.foundation
  - $compose.material3
  - $libs.androidx.lifecycle.runtime.compose

dependencies@android:
  - $libs.ktor.client.okhttp

dependencies@ios:
  - $libs.ktor.client.darwin

settings: 
  compose: enabled
  kotlin:
    serialization: json
```

**Configure Amper plugin in settings.gradle.kts:**
```kotlin
plugins {
    id("org.jetbrains.amper.settings.plugin").version("0.5.0")
}
```

### 2. Dependency Management

**Use libs.versions.toml for version catalogs:**
```toml
[versions]
ktor = "2.3.12"
koin = "3.5.6"
voyager = "1.0.0"

[libraries]
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
```

**Reference dependencies with $libs prefix:**
```yaml
dependencies:
  - $libs.ktor.client.core
  - $libs.koin.core
```

### 3. Architecture Patterns

**Follow Repository Pattern:**
```kotlin
class MuseumRepository(
    private val museumApi: MuseumApi,
    private val museumStorage: MuseumStorage,
) {
    private val scope = CoroutineScope(SupervisorJob())
    
    suspend fun refresh() {
        museumStorage.saveObjects(museumApi.getData())
    }
    
    fun getObjects(): Flow<List<MuseumObject>> = museumStorage.getObjects()
}
```

**Use Interface Abstractions:**
```kotlin
interface MuseumApi {
    suspend fun getData(): List<MuseumObject>
}

interface MuseumStorage {
    suspend fun saveObjects(newObjects: List<MuseumObject>)
    fun getObjects(): Flow<List<MuseumObject>>
}
```

**Implement Dependency Injection with Koin:**
```kotlin
val dataModule = module {
    single<MuseumApi> { KtorMuseumApi(get()) }
    single<MuseumStorage> { InMemoryMuseumStorage() }
    single { MuseumRepository(get(), get()) }
}

fun initKoin() {
    startKoin {
        modules(dataModule, screenModelsModule)
    }
}
```

### 4. Screen Models (MVVM Pattern)

**Use Voyager Screen Models:**
```kotlin
class ListScreenModel(museumRepository: MuseumRepository) : ScreenModel {
    val objects: StateFlow<List<MuseumObject>> =
        museumRepository.getObjects()
            .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
```

**Inject Screen Models with Koin:**
```kotlin
val screenModelsModule = module {
    factoryOf(::ListScreenModel)
    factoryOf(::DetailScreenModel)
}
```

### 5. Navigation with Voyager

**Define Screens as Objects:**
```kotlin
data object ListScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel: ListScreenModel = getScreenModel()
        // Screen content
    }
}

data class DetailScreen(val objectId: Int) : Screen
```

**Use Navigator for Navigation:**
```kotlin
Navigator(ListScreen) // Initial screen
navigator.push(DetailScreen(objectId)) // Navigate to detail
navigator.pop() // Go back
```

### 6. Data Modeling

**Use Kotlinx Serialization:**
```kotlin
@Serializable
data class MuseumObject(
    val objectID: Int,
    val title: String,
    val artistDisplayName: String,
    val primaryImage: String,
)
```

### 7. HTTP Client Configuration

**Configure Ktor with Content Negotiation:**
```kotlin
single {
    val json = Json { ignoreUnknownKeys = true }
    HttpClient {
        install(ContentNegotiation) {
            json(json, contentType = ContentType.Any)
        }
    }
}
```

### 8. Error Handling

**Handle Cancellation Exceptions Properly:**
```kotlin
override suspend fun getData(): List<MuseumObject> {
    return try {
        client.get(API_URL).body()
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        e.printStackTrace()
        emptyList()
    }
}
```

### 9. UI Best Practices

**Use StateFlow with Lifecycle:**
```kotlin
val objects by screenModel.objects.collectAsStateWithLifecycle()
```

**Handle Loading States:**
```kotlin
AnimatedContent(objects.isNotEmpty()) { objectsAvailable ->
    if (objectsAvailable) {
        ObjectGrid(objects = objects)
    } else {
        EmptyScreenContent(Modifier.fillMaxSize())
    }
}
```

**Use Proper Image Loading:**
```kotlin
KamelImage(
    resource = asyncPainterResource(data = obj.primaryImageSmall),
    contentDescription = obj.title,
    contentScale = ContentScale.Crop,
    modifier = Modifier.background(Color.LightGray)
)
```

### 10. Gradle Configuration

**Essential gradle.properties settings:**
```properties
kotlin.code.style=official
kotlin.mpp.androidSourceSetLayoutVersion=2
kotlin.mpp.enableCInteropCommonization=true
android.useAndroidX=true
android.nonTransitiveRClass=true
```

## ❌ Best Practices - DON'T

### 1. Build System Anti-patterns

**❌ Don't use traditional Gradle build scripts when Amper is available:**
```kotlin
// Avoid build.gradle.kts files for module configuration
// Use module.yaml instead
```

**❌ Don't hardcode versions in module.yaml:**
```yaml
# Wrong
dependencies:
  - io.ktor:ktor-client-core:2.3.12

# Correct
dependencies:
  - $libs.ktor.client.core
```

### 2. Architecture Anti-patterns

**❌ Don't put platform-specific code in shared module:**
```kotlin
// Wrong - Android-specific in shared
class AndroidSpecificImplementation : SomeInterface
```

**❌ Don't use blocking calls in repositories:**
```kotlin
// Wrong
fun getObjects(): List<MuseumObject> = runBlocking {
    api.getData()
}

// Correct
suspend fun getObjects(): List<MuseumObject> = api.getData()
```

**❌ Don't ignore CancellationException:**
```kotlin
// Wrong
catch (e: Exception) {
    emptyList()
}

// Correct
catch (e: Exception) {
    if (e is CancellationException) throw e
    emptyList()
}
```

### 3. State Management Anti-patterns

**❌ Don't use mutable state directly in UI:**
```kotlin
// Wrong
var objects by mutableStateOf(emptyList<MuseumObject>())

// Correct
val objects by screenModel.objects.collectAsStateWithLifecycle()
```

**❌ Don't create long-lived coroutines without proper scope:**
```kotlin
// Wrong
GlobalScope.launch { ... }

// Correct
private val scope = CoroutineScope(SupervisorJob())
```

### 4. Navigation Anti-patterns

**❌ Don't pass complex objects through navigation:**
```kotlin
// Wrong
data class DetailScreen(val complexObject: MuseumObject) : Screen

// Correct
data class DetailScreen(val objectId: Int) : Screen
```

### 5. Dependency Injection Anti-patterns

**❌ Don't use Service Locator pattern:**
```kotlin
// Wrong
object ServiceLocator {
    val repository = MuseumRepository()
}

// Correct - Use Koin DI
factoryOf(::MuseumRepository)
```

### 6. UI Anti-patterns

**❌ Don't forget content descriptions for accessibility:**
```kotlin
// Wrong
KamelImage(resource = asyncPainterResource(data = url))

// Correct
KamelImage(
    resource = asyncPainterResource(data = url),
    contentDescription = obj.title
)
```

**❌ Don't use hardcoded dimensions:**
```kotlin
// Wrong
Modifier.size(200.dp)

// Correct
Modifier.aspectRatio(1f)
```

## Development Workflow

### 1. Adding New Dependencies
1. Add version to `gradle/libs.versions.toml`
2. Add library definition to `[libraries]` section
3. Reference in `module.yaml` with `$libs.` prefix

### 2. Adding New Features
1. Define interfaces in shared module
2. Implement in shared module with DI
3. Add platform-specific implementations if needed
4. Update Koin modules
5. Create/update screen models
6. Implement UI components

### 3. Testing Strategy
- Unit tests for repositories and business logic
- UI tests for screen components
- Integration tests for complete flows

## Performance Considerations

1. **Use SharingStarted.WhileSubscribed** for StateFlow to avoid memory leaks
2. **Implement proper image caching** with Kamel
3. **Use LazyColumn/LazyVerticalGrid** for large lists
4. **Optimize Compose recomposition** with stable data classes

## Platform-Specific Notes

### Android
- Uses OkHttp for Ktor client
- Supports Compose UI tooling
- Uses Activity Compose for integration

### iOS (Development Ignored)
- Uses Darwin client for Ktor
- Requires platform-specific Swift integration
- Uses MainViewController for Compose integration

## File Structure Best Practices

```
├── shared/
│   ├── src/
│   │   ├── data/           # Data layer (API, Storage, Repository)
│   │   ├── di/             # Dependency injection modules
│   │   ├── screens/        # UI screens and screen models
│   │   └── App.kt          # Main app entry point
│   └── module.yaml
├── androidApp/
│   ├── src/
│   │   ├── MainActivity.kt
│   │   └── AndroidManifest.xml
│   └── module.yaml
└── gradle/
    └── libs.versions.toml  # Version catalog
```

## Common Commands

```bash
# Build project
./gradlew build

# Run Android app
./gradlew :androidApp:installDebug

# Clean build
./gradlew clean

# Generate iOS framework (if needed)
./gradlew :shared:assembleXCFramework
```

This guide provides comprehensive best practices for developing Kotlin Multiplatform applications with Amper, focusing on maintainable, scalable, and performant code architecture.