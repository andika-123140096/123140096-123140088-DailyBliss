# 🏗️ Struktur Kode & Arsitektur

Dokumen ini menjelaskan struktur kode dan arsitektur yang digunakan dalam project **DailyBliss**.

---

## 📐 Arsitektur: Clean Architecture + MVVM

Aplikasi ini menggunakan pola **Clean Architecture** yang dipadukan dengan **MVVM (Model-View-ViewModel)**. Pemisahan layer ini memastikan kode yang bersih, mudah diuji, dan independen dari framework UI.

```
┌──────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                         │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │                         UI (Screen)                         │  │
│  │            Composable functions, UI state rendering         │  │
│  └────────────────────────────────────────────────────────────┘  │
│                              ▲ │                                  │
│                    State     │ │ Events                           │
│                              │ ▼                                  │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │                        ViewModel                            │  │
│  │         StateFlow, event handling, UI state management      │  │
│  └────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
                               ▲ │
                               │ │ Calls
                               │ ▼
┌──────────────────────────────────────────────────────────────────┐
│                          DOMAIN LAYER                             │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │                        Use Cases                            │  │
│  │               Business logic, orchestration                 │  │
│  └────────────────────────────────────────────────────────────┘  │
│                              ▲ │                                  │
│                              │ │                                  │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │                  Repository Interface                       │  │
│  │                    Contract/abstraction                     │  │
│  └────────────────────────────────────────────────────────────┘  │
│                                                                   │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │                       Domain Models                         │  │
│  │               Pure Kotlin data classes                      │  │
│  └────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
                               ▲ │
                               │ │ Implements
                               │ ▼
┌──────────────────────────────────────────────────────────────────┐
│                           DATA LAYER                              │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │                Repository Implementation                    │  │
│  │            Coordinates data sources, caching                │  │
│  └────────────────────────────────────────────────────────────┘  │
│              ┌───────────────┼───────────────┐                    │
│              ▼               ▼               ▼                    │
│  ┌──────────────────┐ ┌──────────────┐ ┌─────────────────┐       │
│  │   Local Source   │ │ Remote Source│ │   DataStore     │       │
│  │    (SQLDelight)  │ │    (Ktor)    │ │  (Preferences)  │       │
│  └──────────────────┘ └──────────────┘ └─────────────────┘       │
└──────────────────────────────────────────────────────────────────┘
```

---

## 📁 Struktur Folder Detail

```
composeApp/src/
│
├── commonMain/kotlin/com/dailybliss/app/     # Shared code
│   │
│   ├── core/                                 # Core utilities
│   │   ├── di/                               # Dependency Injection (Koin)
│   │   │   └── AppModule.kt                  # Shared Koin modules
│   │   │
│   │   ├── network/                          # Network configuration
│   │   │   ├── ApiConfig.kt                  # expect: API configuration
│   │   │   └── HttpClientFactory.kt          # Ktor client setup
│   │   │
│   │   └── util/                             # Utilities
│   │       ├── DatabaseDriverFactory.kt      # expect: DB driver factory
│   │       └── VoiceToTextParser.kt          # Voice input interface
│   │
│   ├── data/                                 # Data Layer
│   │   ├── local/
│   │   │   ├── entity/
│   │   │   │   └── MomentMapper.kt           # Entity ↔ Domain mappers
│   │   │   └── datastore/
│   │   │       └── UserPreferences.kt        # DataStore preferences
│   │   │
│   │   ├── remote/
│   │   │   ├── api/
│   │   │   │   ├── GeminiService.kt          # Gemini API service
│   │   │   │   └── SystemPrompts.kt          # AI system instructions
│   │   │   └── dto/
│   │   │       └── GeminiDto.kt              # API Request/Response DTOs
│   │   │
│   │   └── repository/
│   │       ├── MomentRepositoryImpl.kt       # Moment repo implementation
│   │       └── AIRepositoryImpl.kt           # AI repo implementation
│   │
│   ├── domain/                               # Domain Layer (Pure Kotlin)
│   │   ├── model/
│   │   │   ├── Moment.kt                     # Main domain model
│   │   │   └── MomentContent.kt              # Content structure
│   │   │
│   │   ├── repository/
│   │   │   ├── MomentRepository.kt           # Moment repo interface
│   │   │   └── AIRepository.kt               # AI repo interface
│   │   │
│   │   └── usecase/
│   │       └── MomentUseCases.kt             # Business logic use cases
│   │
│   ├── presentation/                         # Presentation Layer
│   │   ├── navigation/
│   │   │   ├── Routes.kt                     # Type-safe navigation routes
│   │   │   └── AppNavHost.kt                 # Navigation host & graph
│   │   │
│   │   ├── screens/
│   │   │   ├── home/                         # Home & Journal screens
│   │   │   ├── addnote/                      # Create/Edit Moment screen
│   │   │   ├── detail/                       # Moment Detail screen
│   │   │   ├── ai/                           # AI Assistant (Blissie) screen
│   │   │   └── settings/                     # App settings screen
│   │   │
│   │   ├── components/
│   │   │   └── MomentComponents.kt           # Reusable UI components
│   │   │
│   │   └── theme/
│   │       ├── Theme.kt                      # Material 3 theme setup
│   │       └── Type.kt                       # Typography
│   │
│   └── App.kt                                # Root composable
│
├── commonMain/sqldelight/                    # SQLDelight schema
│   └── com/dailybliss/app/data/local/
│       └── Moment.sq                         # Database schema & queries
│
├── androidMain/kotlin/com/dailybliss/app/    # Android-specific code
│   ├── MainActivity.kt                       # Android entry point
│   ├── DailyBlissApplication.kt              # Application class (DI init)
│   └── core/util/                            # Android-specific utils
│
└── iosMain/kotlin/com/dailybliss/app/        # iOS-specific code
    ├── MainViewController.kt                 # iOS entry point
    └── core/util/                            # iOS-specific utils
```

---

## 🔑 Penjelasan Setiap Layer

### 1. Domain Layer (Jantung Aplikasi)

**Karakteristik:**
- Pure Kotlin (bebas dari framework UI/DB/Network).
- Berisi aturan bisnis inti.
- Bersifat stabil dan jarang berubah.

**Models (`domain/model/`)**
Mempresentasikan data dalam bentuk yang paling bersih untuk aplikasi.

**Repository Interface (`domain/repository/`)**
Mendefinisikan kontrak data tanpa peduli bagaimana data tersebut diambil (DB/API).

**Use Cases (`domain/usecase/`)**
Membungkus satu tugas bisnis spesifik, memudahkan pembacaan alur kerja aplikasi.

### 2. Data Layer (Infrastruktur)

**Karakteristik:**
- Implementasi konkret dari repository.
- Menangani caching dan sinkronisasi data.
- Tempat berinteraksi dengan SQLDelight, Ktor, dan DataStore.

**Mappers (`data/local/entity/`)**
Mengubah data dari database (Entity) menjadi model domain (Moment) dan sebaliknya.

**Gemini Service (`data/remote/api/`)**
Implementasi komunikasi dengan Google Gemini API menggunakan Ktor Client.

### 3. Presentation Layer (Antarmuka Pengguna)

**Karakteristik:**
- Dibangun menggunakan Compose Multiplatform.
- Menggunakan ViewModel untuk menjaga state UI tetap bertahan saat rotasi atau perubahan konfigurasi.

**UI State Flow**
ViewModel memaparkan `StateFlow` yang di-collect oleh Composable screens. Perubahan pada state secara otomatis memicu recomposition UI.

---

## 🎯 Prinsip Pengembangan

1. **Dependency Injection**: Menggunakan Koin untuk mengelola instansi objek dan memudahkan testing.
2. **Offline-First**: Data disimpan di SQLDelight terlebih dahulu sebelum disinkronkan atau diproses lebih lanjut.
3. **Type-Safe Navigation**: Menggunakan `navigation-compose` dengan dukungan serialization untuk route yang aman.
4. **Platform Independence**: Memaksimalkan `commonMain` agar logika aplikasi bisa digunakan di Android dan iOS tanpa perubahan.

---

## 🔑 Penjelasan Setiap Layer

### 1. Domain Layer (Paling Dalam)

**Karakteristik:**
- Pure Kotlin (tidak ada dependency ke framework)
- Berisi business logic
- Tidak tahu tentang database atau API

**Models (`domain/model/`)**
```kotlin
// Domain model - representasi data dalam aplikasi
data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: NoteCategory,
    // ... pure data, no framework dependencies
)
```

**Repository Interface (`domain/repository/`)**
```kotlin
// Contract - mendefinisikan operasi yang tersedia
interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun insertNote(note: Note): Long
    // ... tanpa implementation details
}
```

**Use Cases (`domain/usecase/`)**
```kotlin
// Business logic yang spesifik
class GetAllNotesUseCase(
    private val repository: NoteRepository
) {
    operator fun invoke(sortBy: NoteSortBy): Flow<List<Note>> {
        return repository.getAllNotes().map { notes ->
            // Business logic: sorting, filtering, etc.
            sortNotes(notes, sortBy)
        }
    }
}
```

### 2. Data Layer (Tengah)

**Karakteristik:**
- Implementasi repository
- Berinteraksi dengan database dan API
- Mapping antara entity dan domain model

**Entity & Mapper (`data/local/entity/`)**
```kotlin
// Mapper: Entity (database) ↔ Domain Model
fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        title = title,
        // ... mapping
    )
}
```

**Repository Implementation (`data/repository/`)**
```kotlin
class NoteRepositoryImpl(
    private val database: NoteDatabase
) : NoteRepository {
    
    override fun getAllNotes(): Flow<List<Note>> {
        // Implementation: query database, map to domain
        return database.noteQueries.getAllNotes()
            .asFlow()
            .mapToList()
            .map { entities -> entities.toDomainList() }
    }
}
```

**Remote API (`data/remote/`)**
```kotlin
// DTO: Data Transfer Object untuk API
@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    // ... untuk serialization
)

// Service: Komunikasi dengan API
class GeminiService(private val client: HttpClient) {
    suspend fun generateContent(prompt: String): Result<String> {
        // API call implementation
    }
}
```

### 3. Presentation Layer (Paling Luar)

**Karakteristik:**
- UI dengan Compose
- ViewModel dengan StateFlow
- Event handling

**ViewModel (`presentation/screens/*/`)**
```kotlin
class HomeViewModel(
    private val getAllNotesUseCase: GetAllNotesUseCase
) : ViewModel() {
    
    // UI State menggunakan StateFlow
    val uiState: StateFlow<HomeUiState> = getAllNotesUseCase()
        .map { notes -> HomeUiState.Success(notes) }
        .stateIn(viewModelScope, ...)
    
    // Handle user actions
    fun onSearchQueryChange(query: String) { ... }
}

// Sealed interface untuk UI State
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val notes: List<Note>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
```

**Screen (`presentation/screens/*/`)**
```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel()
) {
    // Collect state
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Render based on state
    when (val state = uiState) {
        is HomeUiState.Loading -> LoadingIndicator()
        is HomeUiState.Success -> NotesList(state.notes)
        is HomeUiState.Error -> ErrorMessage(state.message)
    }
}
```

---

## 🔄 Dependency Flow

```
┌──────────────────────────────────────────────────────────────┐
│                    DEPENDENCY DIRECTION                      │
│                                                              │
│   Presentation ──────────► Domain ◄────────── Data           │
│       │                      │                    │          │
│       │                      │                    │          │
│   Knows about:           Knows about:        Knows about:    │
│   - Domain models        - Nothing else      - Domain        │
│   - Use cases            - Pure Kotlin       - Frameworks    │
│   - Compose                                  - Database      │
│   - Navigation                               - Network       │
│                                                              │
│   TIDAK knows:           TIDAK knows:        TIDAK knows:    │
│   - Data layer           - Data layer        - Presentation  │
│   - Database             - Presentation      - UI            │
│   - Network              - Frameworks                        │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 🧩 expect/actual Pattern

Pattern untuk kode platform-specific:

```kotlin
// ═══════════════════════════════════════════════════════════
// commonMain - EXPECT (Declaration only, no implementation)
// ═══════════════════════════════════════════════════════════

// File: commonMain/.../ApiConfig.kt
expect object ApiConfig {
    val geminiApiKey: String
}

// ═══════════════════════════════════════════════════════════
// androidMain - ACTUAL (Android implementation)
// ═══════════════════════════════════════════════════════════

// File: androidMain/.../ApiConfig.android.kt
actual object ApiConfig {
    actual val geminiApiKey: String = BuildConfig.GEMINI_API_KEY
}

// ═══════════════════════════════════════════════════════════
// iosMain - ACTUAL (iOS implementation)
// ═══════════════════════════════════════════════════════════

// File: iosMain/.../ApiConfig.ios.kt
actual object ApiConfig {
    actual val geminiApiKey: String
        get() = NSBundle.mainBundle.objectForInfoDictionaryKey("GEMINI_API_KEY") as? String ?: ""
}
```

---

## 💉 Dependency Injection dengan Koin

```kotlin
// ═══════════════════════════════════════════════════════════
// Module Definitions
// ═══════════════════════════════════════════════════════════

// Network Module
val networkModule = module {
    single { HttpClientFactory.create() }    // Singleton
    singleOf(::GeminiService)                // Auto-inject dependencies
}

// Repository Module
val repositoryModule = module {
    singleOf(::NoteRepositoryImpl) bind NoteRepository::class
    //       ↑ Implementation        ↑ Interface (for injection)
}

// ViewModel Module
val viewModelModule = module {
    viewModelOf(::HomeViewModel)             // Scoped to lifecycle
    viewModelOf(::AddNoteViewModel)
}

// ═══════════════════════════════════════════════════════════
// Initialization
// ═══════════════════════════════════════════════════════════

// Android
class NoteAIApplication : Application() {
    override fun onCreate() {
        initKoin(platformModules = listOf(androidModule)) {
            androidContext(this@NoteAIApplication)
        }
    }
}

// iOS
fun initKoinIOS() {
    initKoin(platformModules = listOf(iosModule))
}

// ═══════════════════════════════════════════════════════════
// Usage in ViewModel/Screen
// ═══════════════════════════════════════════════════════════

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel()  // Auto-injected!
) {
    // ...
}
```

---

## 🧪 Testing Structure

```kotlin
// ═══════════════════════════════════════════════════════════
// Fake Repository untuk Testing
// ═══════════════════════════════════════════════════════════

class FakeNoteRepository : NoteRepository {
    private val notes = MutableStateFlow<List<Note>>(emptyList())
    
    override fun getAllNotes(): Flow<List<Note>> = notes
    
    override suspend fun insertNote(note: Note): Long {
        notes.update { it + note.copy(id = nextId++) }
        return nextId
    }
}

// ═══════════════════════════════════════════════════════════
// Unit Test dengan Turbine (Flow testing)
// ═══════════════════════════════════════════════════════════

class NoteRepositoryTest {
    
    @Test
    fun `insertNote should add note to list`() = runTest {
        // Arrange
        val repository = FakeNoteRepository()
        
        // Act
        repository.insertNote(Note(title = "Test"))
        
        // Assert dengan Turbine
        repository.getAllNotes().test {
            val notes = awaitItem()
            assertEquals(1, notes.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

---

## 📊 Data Flow Example

Contoh: User menambah note baru

```
┌────────────────────────────────────────────────────────────────┐
│ 1. USER ACTION                                                 │
│    User tap "Save" button di AddNoteScreen                     │
└────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────┐
│ 2. SCREEN                                                      │
│    onClick = { viewModel.saveNote() }                          │
└────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────┐
│ 3. VIEWMODEL                                                   │
│    fun saveNote() {                                            │
│        viewModelScope.launch {                                 │
│            saveNoteUseCase(note)                               │
│        }                                                       │
│    }                                                           │
└────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────┐
│ 4. USE CASE                                                    │
│    suspend operator fun invoke(note: Note): Result<Long> {     │
│        // Validation                                           │
│        if (note.isEmpty) return Result.failure(...)            │
│        // Delegate to repository                               │
│        return repository.insertNote(note)                      │
│    }                                                           │
└────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────┐
│ 5. REPOSITORY IMPLEMENTATION                                   │
│    override suspend fun insertNote(note: Note): Long {         │
│        val values = note.toEntityValues()                      │
│        queries.insertNote(...)                                 │
│        return queries.lastInsertId().executeAsOne()            │
│    }                                                           │
└────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────┐
│ 6. DATABASE (SQLDelight)                                       │
│    INSERT INTO NoteEntity (title, content, ...) VALUES (...)   │
└────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────┐
│ 7. FLOW UPDATE                                                 │
│    getAllNotes query otomatis emit data baru                   │
│    UI ter-update karena collect StateFlow                      │
└────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Best Practices

### 1. Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Package | lowercase | `com.example.noteai.domain` |
| Class | PascalCase | `NoteRepository`, `HomeViewModel` |
| Function | camelCase | `getAllNotes()`, `onSaveClick()` |
| Variable | camelCase | `noteList`, `isLoading` |
| Constant | SCREAMING_SNAKE | `MAX_TITLE_LENGTH` |
| File | PascalCase.kt | `NoteRepository.kt` |

### 2. File Organization

```kotlin
// Urutan dalam file:
class HomeViewModel(
    // 1. Constructor parameters
    private val repository: NoteRepository
) : ViewModel() {
    
    // 2. Constants
    companion object {
        private const val DEBOUNCE_MS = 300L
    }
    
    // 3. Private state
    private val _searchQuery = MutableStateFlow("")
    
    // 4. Public state
    val uiState: StateFlow<HomeUiState> = ...
    
    // 5. Public functions
    fun onSearchQueryChange(query: String) { ... }
    
    // 6. Private functions
    private fun sortNotes(notes: List<Note>): List<Note> { ... }
}
```

### 3. UI State Pattern

```kotlin
// Sealed interface untuk semua kemungkinan state
sealed interface UiState {
    data object Loading : UiState
    data class Success(val data: Data) : UiState
    data class Error(val message: String) : UiState
}

// Gunakan when expression untuk handle semua state
when (val state = uiState) {
    is UiState.Loading -> LoadingIndicator()
    is UiState.Success -> Content(state.data)
    is UiState.Error -> ErrorMessage(state.message)
}
```

---

*Dokumen ini adalah bagian dari template project Pengembangan Aplikasi Mobile - ITERA*
