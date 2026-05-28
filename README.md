# 📱 DailyBliss

[![CI](https://github.com/andika-123140096/123140096-123140088-DailyBliss/actions/workflows/ci.yml/badge.svg)](https://github.com/andika-123140096/123140096-123140088-DailyBliss/actions/workflows/ci.yml)

Aplikasi jurnal harian cerdas yang membantu pengguna mencatat momen berharga dengan dukungan asisten AI empatik (Blissie), analisis suasana hati otomatis, dan integrasi data real-time. Dibangun menggunakan Kotlin Multiplatform untuk pengalaman cross-platform yang konsisten.

## 👥 Kelompok DailyBliss

| Nama | NIM |
|------|-----|
| Andika Dinata | 123140096 |
| Satria Lemana Putra | 123140088 |

## 📱 Daftar Layar (Screens)

| Layar | Deskripsi | Route Name |
|-------|-----------|------------|
| **Dashboard** | Layar utama dengan sapaan personal dan *AI Insight* harian. | `Home` |
| **Journal Feed** | Daftar kronologis memori dengan fitur pencarian dan filter pin. | `Journal` |
| **News & Info** | Pusat informasi real-time: Cuaca, Kurs Mata Uang, dan Berita terkini. | `News` |
| **Editor Momen** | Form input dengan *Rich Text Editor* dan dukungan banyak foto. | `CreateMoment` |
| **Detail Momen** | Review lengkap memori dengan visual yang bersih dan modern. | `MomentDetail` |
| **Blissie AI** | Chat interface empatik dengan dukungan analisis gambar (Vision). | `AIAssistant` |
| **Kalender** | Visualisasi histori penulisan dalam format kalender bulanan. | `Calendar` |
| **Momen Harian** | Daftar memori pada tanggal spesifik yang dipilih dari kalender. | `DailyMoments` |
| **Pengaturan** | Kustomisasi profil, tema, dan gaya bahasa asisten AI. | `Settings` |

## ✨ Fitur Unggulan (Data-Driven)

| Fitur | Implementasi Teknis |
|-------|---------------------|
| **Rich Journaling** | Mendukung `title`, `content` (HTML/Annotated), dan multiple images via `HorizontalPager`. |
| **AI Insights** | Analisis `mood` otomatis dan `smart tagging` via Gemini AI. |
| **Long-term Memory** | Blissie memiliki memori naratif dari histori jurnal pengguna untuk respon yang lebih personal. |
| **Vision Support** | Asisten AI (Blissie) dapat menganalisis gambar yang dikirim pengguna dalam sesi chat. |
| **Kustomisasi AI** | Pengguna dapat mengatur gaya bahasa Blissie (Santai, Formal, atau Puitis). |
| **Flashback** | Fitur "On This Day" untuk melihat memori di tanggal yang sama di tahun lalu. |
| **Daily Insight** | Kalimat refleksi harian yang dipersonalisasi berdasarkan histori jurnal. |
| **Organisasi** | Sistem `pinning` (is_pinned) untuk menandai momen penting. |
| **Real-time Data** | Integrasi Weather, News, Geolocation, dan Currency exchange. |

## 🛠️ Tech Stack & Versi

| Komponen | Teknologi | Versi |
|----------|-----------|-------|
| **Language** | Kotlin | `2.0.21` |
| **UI Framework** | Compose Multiplatform | `1.7.0` |
| **Navigation** | Navigation Compose | `2.8.0-alpha10` |
| **Dependency Injection** | Koin | `4.0.0` |
| **Networking** | Ktor Client | `3.0.1` |
| **Local Database** | SQLDelight | `2.0.2` |
| **Storage** | DataStore Preferences | `1.1.1` |
| **Image Loading** | Coil | `3.0.4` |
| **Concurrency** | Kotlinx Coroutines | `1.9.0` |

## 🌐 Integrasi API

Aplikasi ini menggunakan beberapa endpoint eksternal untuk memperkaya informasi:

| Layanan | Endpoint Utama | Fungsi |
|---------|----------------|--------|
| **Gemini AI** | `generativelanguage.googleapis.com` | AI Chat, Mood & Tag analysis. |
| **Open-Meteo** | `api.open-meteo.com` | Data cuaca (suhu & angin) real-time. |
| **IPAPI** | `ipapi.co` | Deteksi lokasi otomatis via IP. |
| **Berita Indo** | `berita-indo-api-next.vercel.app` | Feed berita terkini (CNN Indonesia). |
| **Frankfurter** | `api.frankfurter.dev` | Kurs mata uang USD/SGD ke IDR. |

---

### 🔍 Contoh Data API (JSON)

<details>
<summary><b>1. Gemini AI Response</b></summary>

```json
{
  "candidates": [{
    "content": {
      "parts": [{"text": "Halo! Saya Blissie..."}],
      "role": "model"
    }
  }]
}
```
</details>

<details>
<summary><b>2. SQLDelight Schema (MomentEntity)</b></summary>

```sql
CREATE TABLE MomentEntity (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    media_url TEXT,
    mood TEXT,
    tags TEXT,
    is_pinned INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```
</details>

## 💾 Sistem Caching

DailyBliss menggunakan sistem caching berlapis untuk performa optimal dan penghematan data:

| Tipe Cache | Teknologi | Kegunaan |
|------------|-----------|----------|
| **Local Cache** | SQLDelight (NewsCache) | Menyimpan respon API (Cuaca, Berita, Kurs) agar dashboard bisa dibuka instan tanpa koneksi internet. |
| **Preference Cache** | DataStore | Menyimpan preferensi tema, nickname, dan metadata AI agar tidak hilang saat aplikasi ditutup. |
| **Media Cache** | Coil | Melakukan caching gambar otomatis untuk foto-foto jurnal yang diunggah. |

## 🍎 iOS Setup (Penting)

Untuk menjalankan aplikasi di iOS, beberapa konfigurasi berikut wajib dilakukan di file `iosApp/iosApp/Info.plist`:

| Key | Value / Deskripsi |
|-----|-------------------|
| `GEMINI_API_KEY` | API Key dari Google AI Studio. |
| `GEMINI_MODEL_NAME` | (Opsional) Nama model (default: `gemini-1.5-flash`). |
| `NSCameraUsageDescription` | Penjelasan mengapa aplikasi membutuhkan akses kamera (untuk foto jurnal). |
| `NSPhotoLibraryUsageDescription` | Penjelasan mengapa aplikasi membutuhkan akses galeri (untuk memilih foto). |
| `NSLocationWhenInUseUsageDescription` | Penjelasan mengapa aplikasi membutuhkan akses lokasi (untuk fitur cuaca & tagging lokasi). |

## 🤖 AI System Prompts

Aplikasi ini menggunakan instruksi khusus (System Prompts) untuk mengatur perilaku AI (Blissie):

<details>
<summary><b>1. Blissie Personality (Chat)</b></summary>

```text
Kamu adalah "Blissie", pendamping setia di aplikasi jurnal DailyBliss. 
Tugasmu adalah menjadi pendengar yang baik dan teman yang memberikan respon bermakna.

ATURAN DASAR:
1. Gunakan kata ganti "Aku" dan "Kamu". Hindari "Anda" atau "Saya" kecuali diminta gaya sangat formal.
2. TULIS LANGSUNG respon seolah-olah sedang berbincang tulus. Jangan gunakan label teknis.
3. Berikan empati yang tulus sesuai perasaan pengguna.
4. Tetap singkat, padat, dan tidak bertele-tele.
5. Gunakan emoji secukupnya agar terasa ramah namun tidak berlebihan.

Tujuan: Menciptakan suasana yang tenang, nyaman, dan reflektif.
```
</details>

<details>
<summary><b>2. Mood Analysis</b></summary>

```text
Analisis suasana hati dari teks jurnal berikut. 
Berikan jawaban dalam format JSON sederhana: {"mood": "NamaMood", "emoji": "😊"}.
Pilihan mood: Bahagia, Sedih, Marah, Cemas, Tenang, Bersemangat, Lelah.
Sesuaikan emoji dengan mood.
```
</details>

<details>
<summary><b>3. Smart Tagging</b></summary>

```text
Berikan maksimal 3 tag yang relevan untuk teks jurnal berikut.
Berikan jawaban dalam format JSON: {"tags": ["tag1", "tag2", "tag3"]}.
Tag harus singkat, satu kata, dan mencerminkan topik utama (misal: Kerja, Keluarga, Hobi, Kesehatan).
```
</details>

<details>
<summary><b>4. Global Journal Summary (Long-term Memory)</b></summary>

```text
Buatlah ringkasan singkat, padat, dan Insightful dari kumpulan teks jurnal pengguna berikut.
Fokus pada:
1. Topik atau kejadian utama yang sering muncul.
2. Perkembangan emosi atau suasana hati secara umum.
3. Hal-hal penting yang perlu ingat tentang kehidupan pengguna.

Tujuan ringkasan ini adalah sebagai 'memori jangka panjang' bagi asisten AI agar bisa memberikan respon yang lebih personal.
Jangan gunakan format poin-poin yang kaku. Tulis dalam 2-3 paragraf singkat yang naratif.
Maksimal 300 kata.
```
</details>

<details>
<summary><b>5. Daily Insight</b></summary>

```text
Buatlah satu kalimat refleksi atau afirmasi yang sangat singkat, padat, dan Insightful dari kumpulan teks jurnal pengguna berikut.
Tujuan: Menjadi "Bliss Insight" di layar utama yang memberikan semangat dan refleksi instan.
PANDUAN:
- Maksimal 20 kata.
- Harus terasa personal dan hangat.
- Hindari kata-kata klise yang terlalu umum.
- Jangan gunakan format poin-poin.
```
</details>

## 📄 License

MIT License

## 👨‍🏫 Dosen Pengampu
### Pak Habib
[GitHub: mh4Scripts](https://github.com/mh4Scripts)

**Program Studi Teknik Informatika**  
Institut Teknologi Sumatera (ITERA)
