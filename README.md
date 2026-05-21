# 📱 DailyBliss

[![CI](https://github.com/andika-123140096/123140096-123140088-DailyBliss/actions/workflows/ci.yml/badge.svg)](https://github.com/andika-123140096/123140096-123140088-DailyBliss/actions/workflows/ci.yml)

Aplikasi jurnal harian yang membantu kamu mencatat dan merenungkan momen-momen berharga. Dilengkapi dengan asisten AI yang empatik, analisis suasana hati otomatis, dan dukungan input suara.

**Kelompok DailyBliss:**
- Andika Dinata (123140096)
- Satria Lemana Putra (123140088)

> **📚 Dokumentasi Lengkap**
> 
> | Dokumen | Deskripsi |
> |---------|-----------|
> | [🚀 Cara Menjalankan](./docs/CARA_MENJALANKAN.md) | **BACA INI DULU!** Panduan setup dan running aplikasi |
> | [📋 Panduan Project](./docs/PANDUAN_PROJECT.md) | Informasi lengkap tentang project, timeline, dan penilaian |
> | [🌿 Git Workflow](./docs/GIT_WORKFLOW.md) | Cara menggunakan Git dan branching strategy |
> | [📜 Aturan Modifikasi](./docs/ATURAN_MODIFIKASI.md) | Apa yang boleh dan tidak boleh dimodifikasi |
> | [🏗️ Struktur Kode](./docs/STRUKTUR_KODE.md) | Penjelasan arsitektur dan struktur folder |
> | [🔧 Troubleshooting](./docs/TROUBLESHOOTING.md) | Solusi untuk masalah umum |

## ✨ Fitur Utama

- 📝 **Rich Journaling** - Catat momen dengan judul, konten, gambar, dan tag yang terorganisir.
- 🤖 **AI Assistant (Blissie)** - Ngobrol dengan Blissie, asisten AI yang dirancang untuk menjadi pendengar yang baik dan memberikan refleksi bermakna.
- 🎭 **AI Mood Analysis** - Secara otomatis mendeteksi suasana hati dari setiap catatan jurnalmu.
- 🏷️ **AI Smart Tagging** - Menghasilkan tag yang relevan secara otomatis berdasarkan konteks tulisanmu.
- 💡 **Daily Reflections** - Dapatkan pertanyaan reflektif harian dari AI untuk membantumu mulai menulis.
- 📅 **Calendar View** - Lacak dan lihat memori harianmu berdasarkan tampilan kalender yang interaktif.
- ⚙️ **Customizable Settings** - Atur preferensi aplikasi dan tampilan tema sesuai keinginan.
- 🖼️ **Image Support** - Tambahkan foto ke momen berhargamu untuk kenangan yang lebih visual.
- 📱 **Cross-Platform** - Pengalaman yang konsisten di Android & iOS dari satu codebase Kotlin Multiplatform.
- 🌙 **Modern UI** - Desain bersih dan minimalis menggunakan Material 3.

## 🏗️ Arsitektur & Teknologi

### Clean Architecture + MVVM

Aplikasi ini mengikuti prinsip **Clean Architecture** untuk memastikan kode tetap terorganisir, mudah diuji, dan skalabel.

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER (Compose)              │
│  ┌───────────────┐        ┌───────────────┐                 │
│  │    Screen     │◄──────►│   ViewModel   │                 │
│  │  (Composable) │ State  │  (StateFlow)  │                 │
│  └───────────────┘        └───────┬───────┘                 │
└───────────────────────────────────┼─────────────────────────┘
                                    │
┌───────────────────────────────────┼─────────────────────────┐
│                      DOMAIN LAYER (Pure Kotlin)             │
│                    ┌──────────────▼──────────────┐          │
│                    │         Use Cases           │          │
│                    │    (Business Logic)         │          │
│                    └──────────────┬──────────────┘          │
│                    ┌──────────────▼──────────────┐          │
│                    │    Repository Interface     │          │
│                    └──────────────┬──────────────┘          │
└───────────────────────────────────┼─────────────────────────┘
                                    │
┌───────────────────────────────────┼─────────────────────────┐
│                       DATA LAYER (Frameworks)                │
│                    ┌──────────────▼──────────────┐          │
│                    │   Repository Implementation │          │
│                    └──────────────┬──────────────┘          │
│              ┌────────────────────┼────────────────────┐    │
│              │                    │                    │    │
│        ┌─────▼─────┐        ┌─────▼─────┐       ┌─────▼────┐│
│        │ SQLDelight│        │    Ktor   │       │ DataStore││
│        │  (Local)  │        │  (Remote) │       │  (Prefs) ││
│        └───────────┘        └───────────┘       └──────────┘│
└─────────────────────────────────────────────────────────────┘
```

### Tech Stack

| Komponen | Teknologi |
|----------|-----------|
| **UI Framework** | Compose Multiplatform (1.7.0) |
| **Design System** | Material 3 |
| **Language** | Kotlin (2.0.21) |
| **DI** | Koin (4.0.0) |
| **Networking** | Ktor Client (3.0.1) |
| **Local Database** | SQLDelight (2.0.2) |
| **Image Loading** | Coil (3.0.4) |
| **Navigation** | Compose Navigation (Type-safe) |
| **State Management** | StateFlow, ViewModel (KMP Lifecycle) |
| **AI Integration** | Google Gemini API (1.5 Flash) |

## 📁 Struktur Project

```
composeApp/src/
├── commonMain/kotlin/com/dailybliss/app/
│   ├── core/                      # Core utilities (DI, Network, Util)
│   ├── data/                      # Data layer (Local, Remote, Repo Impl)
│   │   ├── local/                 # SQLDelight DAOs & DataStore
│   │   ├── remote/                # Gemini Service & API DTOs
│   │   └── repository/            # Repository Implementations
│   ├── domain/                    # Domain layer (Models, Interfaces, Use Cases)
│   │   ├── model/                 # Moment & Content models
│   │   ├── repository/            # Repository Interfaces
│   │   └── usecase/               # Business logic / Use Cases
│   └── presentation/              # Presentation layer (UI & ViewModel)
│       ├── navigation/            # Type-safe Navigation
│       ├── screens/               # Screen composables + ViewModels
│       ├── components/            # Reusable UI components
│       └── theme/                 # Material 3 Theme
│
├── commonMain/sqldelight/         # SQLDelight schema (.sq files)
├── androidMain/kotlin/            # Android-specific implementations
└── iosMain/kotlin/                # iOS-specific implementations
```

## 🚀 Getting Started

### Prasyarat

- **Android Studio Ladybug (2024.2.1)** atau lebih baru
- **JDK 17** atau 21
- **Xcode 15+** (khusus untuk build iOS di macOS)

### Setup

1. **Clone repository**
   ```bash
   git clone git@github.com:andika-123140096/123140096-123140088-DailyBliss.git
   cd 123140096-123140088-DailyBliss
   ```

2. **Konfigurasi API Key**

   Aplikasi membutuhkan API key Google Gemini. Salin template file dan isi API key kamu:
   ```bash
   cp local.properties.example local.properties
   # Edit local.properties dan isi:
   # GEMINI_API_KEY=AIza...
   ```
   Dapatkan API key gratis di [Google AI Studio](https://aistudio.google.com/).

3. **Build & Run**
   - **Android**: Pilih konfigurasi `composeApp` dan klik **Run** (▶️).
   - **iOS**: Folder `iosApp/` saat ini belum disertakan, namun framework Kotlin tetap bisa di-build untuk target iOS.

## 🧪 Testing

Kami menggunakan `kotlin.test`, `coroutines-test`, dan `turbine` untuk memastikan stabilitas aplikasi.

```bash
# Jalankan semua unit test
./gradlew allTests

# Jalankan test khusus Android
./gradlew :composeApp:testDebugUnitTest
```

## 📄 License

MIT License

## 👨‍🏫 Dosen Pengampu
### Pak Habib
[GitHub: mh4Scripts](https://github.com/mh4Scripts)

**Program Studi Teknik Informatika**  
Institut Teknologi Sumatera (ITERA)
