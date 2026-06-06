# 📱 DailyBliss

[![CI](https://github.com/andika-123140096/123140096-123140088-DailyBliss/actions/workflows/ci.yml/badge.svg)](https://github.com/andika-123140096/123140096-123140088-DailyBliss/actions/workflows/ci.yml)

**DailyBliss** adalah aplikasi jurnal cerdas lintas platform yang dirancang untuk membantu pengguna mengabadikan setiap momen berharga. Dengan dukungan asisten AI empatik (Blissie), analisis suasana hati otomatis, dan integrasi data real-time, DailyBliss mengubah aktivitas mencatat menjadi pengalaman yang reflektif dan bermakna.

Dibangun dengan **Kotlin Multiplatform (KMP)**, aplikasi ini menawarkan performa native yang konsisten baik di Android maupun iOS dengan satu basis kode (Single Codebase).

---

## 👥 Tim Pengembang

| Nama | NIM | Peran |
|------|-----|-------|
| **Andika Dinata** | 123140096 | Lead Developer / Architect |
| **Satria Lemana Putra** | 123140088 | UI/UX Designer / Developer |

---

## ✨ Fitur Utama

### ✍️ Rich Journaling Experience
*   **Multi-Media Support:** Tambahkan foto ke dalam jurnal dengan antarmuka yang modern.
*   **Rich Text Editor:** Simpan memori dengan detail narasi yang mendalam.

### 🤖 Intelligent AI Assistant (Blissie)
*   **Empathetic Chat:** Berinteraksi dengan Blissie yang memberikan respon tulus dan menenangkan.
*   **Mood Analysis:** Deteksi otomatis suasana hati (Bahagia, Sedih, Tenang, dll.) dari teks jurnal.
*   **Smart Tagging:** Klasifikasi otomatis kategori jurnal (Kerja, Hobi, Keluarga) berbasis AI.
*   **Long-term Memory:** Blissie mengingat narasi hidup Anda untuk memberikan respon yang lebih personal.

### 📊 Insights & Analytics
*   **Visual Statistics:** Pantau tren suasana hati Anda melalui grafik distribusi mood yang intuitif.
*   **Daily Bliss Insight:** Dapatkan kalimat afirmasi harian yang dipersonalisasi khusus untuk Anda.
*   **Flashback (On This Day):** Temukan kembali memori di tanggal yang sama pada tahun-tahun sebelumnya.

### 🌐 Smart Integrations
*   **Real-time Weather:** Pantau cuaca saat ini berdasarkan lokasi Anda.
*   **Global News:** Feed berita terkini untuk tetap terinformasi.
*   **Currency Tracker:** Pantau nilai tukar mata uang (USD/SGD ke IDR) secara instan.
*   **Offline First:** Akses data jurnal dan cache informasi kapan saja tanpa ketergantungan internet.

---

## 📱 Arsitektur Layar (Screens)

| Layar | Deskripsi | Route |
|-------|-----------|-------|
| **Dashboard** | Pusat kendali dengan *Quick Insight*, cuaca, dan ringkasan harian. | `Home` |
| **Journal Feed** | Daftar kronologis memori dengan fitur pencarian dan filter. | `Journal` |
| **Editor Momen** | Antarmuka input jurnal dengan dukungan unggah foto. | `CreateMoment` |
| **Detail Momen** | Layar review memori dengan visual bersih dan navigasi media. | `MomentDetail` |
| **Statistics** | Visualisasi data dan statistik distribusi suasana hati pengguna. | `Statistics` |
| **Blissie AI** | Chat interface interaktif dengan asisten AI empatik. | `AIAssistant` |
| **Kalender** | Histori penulisan dalam format kalender interaktif. | `Calendar` |
| **News & Info** | Agregator berita, cuaca, dan kurs mata uang real-time. | `News` |
| **Settings** | Kustomisasi profil, tema (Dark/Light), dan gaya bicara AI. | `Settings` |

---

## 🛠️ Arsitektur & Teknologi

DailyBliss mengimplementasikan **Clean Architecture** dengan pemisahan *layer* yang tegas (`domain`, `data`, `presentation`) untuk memastikan kode yang *scalable* dan mudah diuji.

### Tech Stack
| Komponen | Teknologi | Versi |
|----------|-----------|-------|
| **Framework** | Compose Multiplatform | `1.7.0` |
| **Navigation** | Navigation Compose | `2.8.0-alpha10` |
| **DI** | Koin | `4.0.0` |
| **Networking** | Ktor Client | `3.0.1` |
| **Database** | SQLDelight | `2.0.2` |
| **Storage** | DataStore Preferences | `1.1.1` |
| **Image Loading** | Coil | `3.0.4` |
| **Testing** | MockK, Turbine, Kover | - |

---

## 🌐 Integrasi API & Caching

### API Endpoints
*   **Gemini AI:** `generativelanguage.googleapis.com` (AI Core).
*   **Open-Meteo:** `api.open-meteo.com` (Weather data).
*   **IPAPI:** `ipapi.co` (Auto-location detection).
*   **Berita Indo:** `berita-indo-api-next.vercel.app` (News aggregator).
*   **Frankfurter:** `api.frankfurter.dev` (Currency rates).

### Caching Strategy
Aplikasi menggunakan sistem **Offline-First**:
1.  **SQLDelight (`NewsCache`):** Menyimpan data berita dan cuaca untuk akses luring.
2.  **SQLDelight (`MomentEntity`):** Basis data utama untuk seluruh momen jurnal.
3.  **DataStore:** Menyimpan preferensi pengguna dan metadata asisten AI.
4.  **Coil Cache:** Mengoptimalkan pemuatan gambar dengan *disk caching*.

---

## 🚀 Cara Menjalankan Proyek

### 1. Prasyarat
*   Android Studio Ladybug atau versi terbaru.
*   JDK 17.
*   Xcode (untuk menjalankan target iOS).

### 2. Konfigurasi API Key
Buat file `local.properties` di root project dan tambahkan API Key dari [Google AI Studio](https://aistudio.google.com/):
```properties
GEMINI_API_KEY=YOUR_API_KEY_HERE
GEMINI_MODEL_NAME=gemini-1.5-flash
```

### 3. iOS Setup
Di `iosApp/iosApp/Info.plist`, pastikan permission berikut sudah dikonfigurasi:
*   `NSCameraUsageDescription` (Akses Kamera)
*   `NSPhotoLibraryUsageDescription` (Akses Galeri)
*   `NSLocationWhenInUseUsageDescription` (Akses Lokasi)

---

## 🧠 AI System Documentation

DailyBliss menggunakan instruksi sistem (System Prompts) yang dirancang khusus untuk menciptakan kepribadian Blissie yang unik.

<details>
<summary><b>Lihat Detail System Prompts</b></summary>

#### Blissie Personality
> Kamu adalah "Blissie", pendamping setia di aplikasi jurnal DailyBliss. Tugasmu adalah menjadi pendengar yang baik dan teman yang memberikan respon bermakna. Gunakan bahasa yang hangat, empatik, dan personal.

#### Mood & Tag Analysis
> Menganalisis teks jurnal untuk mengekstrak suasana hati (mood) dalam format JSON dan memberikan tag kategori yang relevan (maksimal 3).

#### Narrative Memory
> Membuat ringkasan naratif dari histori jurnal pengguna untuk digunakan sebagai memori jangka panjang Blissie, memastikan respon yang diberikan selalu kontekstual.
</details>

---

## 👨‍🏫 Dosen Pengampu
### **Pak Habib (mh4Scripts)**
[GitHub Profile](https://github.com/mh4Scripts)

**Program Studi Teknik Informatika**  
Institut Teknologi Sumatera (ITERA)

---

## 📄 Lisensi
Proyek ini dilisensikan di bawah **MIT License**.
