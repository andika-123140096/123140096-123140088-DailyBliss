# 📱 DailyBliss

[![CI](https://github.com/andika-123140096/123140096-123140088-DailyBliss/actions/workflows/ci.yml/badge.svg)](https://github.com/andika-123140096/123140096-123140088-DailyBliss/actions/workflows/ci.yml)

Aplikasi jurnal harian yang membantu kamu mencatat dan merenungkan momen-momen berharga. Dilengkapi dengan asisten AI yang empatik, analisis suasana hati otomatis, dan dukungan input suara.

## 👥 Kelompok DailyBliss

| Nama | NIM |
|------|-----|
| Andika Dinata | 123140096 |
| Satria Lemana Putra | 123140088 |

## ✨ Fitur Utama

| Fitur | Deskripsi |
|-------|-----------|
| **Rich Journaling** | Catat momen dengan judul, konten, gambar, dan tag yang terorganisir. |
| **AI Assistant (Blissie)** | Ngobrol dengan Blissie, asisten AI empatik untuk refleksi bermakna. |
| **AI Mood Analysis** | Deteksi suasana hati otomatis dari setiap catatan jurnal. |
| **AI Smart Tagging** | Generate tag relevan secara otomatis berdasarkan konteks tulisan. |
| **Daily Reflections** | Pertanyaan reflektif harian dari AI untuk memicu kreativitas menulis. |
| **Calendar View** | Lacak dan lihat memori harian melalui tampilan kalender interaktif. |
| **Image Support** | Tambahkan foto ke momen berharga untuk kenangan visual. |
| **Cross-Platform** | Satu codebase untuk Android & iOS menggunakan Kotlin Multiplatform. |

## 🛠️ Teknologi

| Komponen | Teknologi |
|----------|-----------|
| **UI Framework** | Compose Multiplatform |
| **Design System** | Material 3 |
| **Language** | Kotlin |
| **DI** | Koin |
| **Networking** | Ktor Client |
| **Local Database** | SQLDelight |
| **Storage** | DataStore |
| **Image Loading** | Coil |
| **Navigation** | Compose Navigation |
| **State Management** | StateFlow, ViewModel |

## 🌐 Integrasi API

Aplikasi ini mengintegrasikan beberapa layanan API untuk menghadirkan fitur-fitur cerdas dan informasi real-time:

| API | Deskripsi | Kegunaan |
|-----|-----------|----------|
| **Gemini AI** | Google Generative AI | Fitur asisten chat, analisis mood, tagging otomatis, dan refleksi harian. |
| **Open-Meteo** | Weather API | Menampilkan informasi cuaca (suhu & angin) sesuai lokasi pengguna. |
| **IPAPI** | Geolocation API | Mendapatkan koordinat dan lokasi pengguna berdasarkan alamat IP. |
| **Berita Indo** | News Feed API | Menampilkan ringkasan berita terbaru dari CNN Indonesia. |
| **Frankfurter** | Currency API | Melacak nilai tukar mata uang (USD & SGD ke IDR). |

---

### 🔍 Detail API & Contoh Data

<details>
<summary><b>1. Google Gemini API (Generative AI)</b></summary>

- **URL:** `POST https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent`
- **Contoh Request:**
```json
{
  "contents": [
    {
      "parts": [{"text": "Hai Blissie, bagaimana kabarmu?"}],
      "role": "user"
    }
  ]
}
```
- **Contoh Response:**
```json
{
  "candidates": [
    {
      "content": {
        "parts": [{"text": "Halo! Saya Blissie, asisten AI-mu. Kabar saya baik dan saya siap mendengarkan ceritamu."}],
        "role": "model"
      }
    }
  ]
}
```
</details>

<details>
<summary><b>2. Open-Meteo API (Cuaca)</b></summary>

- **URL:** `GET https://api.open-meteo.com/v1/forecast?latitude=-5.4&longitude=105.2&current=temperature_2m,wind_speed_10m`
- **Contoh Response:**
```json
{
  "current": {
    "temperature_2m": 30.5,
    "wind_speed_10m": 12.3
  }
}
```
</details>

<details>
<summary><b>3. IPAPI (Geolokasi)</b></summary>

- **URL:** `GET https://ipapi.co/json/`
- **Contoh Response:**
```json
{
  "latitude": -5.3971,
  "longitude": 105.2667,
  "city": "Bandar Lampung",
  "country_name": "Indonesia"
}
```
</details>

<details>
<summary><b>4. Berita Indo API (News)</b></summary>

- **URL:** `GET https://berita-indo-api-next.vercel.app/api/cnn-news`
- **Contoh Response:**
```json
{
  "data": [
    {
      "title": "IHSG Berpeluang Menguat Hari Ini",
      "contentSnippet": "Indeks Harga Saham Gabungan (IHSG) diprediksi akan bergerak menguat pada...",
      "link": "https://www.cnnindonesia.com/ekonomi/...",
      "image": {
        "large": "https://akcdn.detik.net.id/visual/..."
      }
    }
  ]
}
```
</details>

<details>
<summary><b>5. Frankfurter API (Mata Uang)</b></summary>

- **URL:** `GET https://api.frankfurter.dev/v1/latest?base=USD&symbols=IDR`
- **Contoh Response:**
```json
{
  "amount": 1.0,
  "base": "USD",
  "date": "2024-03-21",
  "rates": {
    "IDR": 15700.0
  }
}
```
</details>

## 📄 License

MIT License

## 👨‍🏫 Dosen Pengampu
### Pak Habib
[GitHub: mh4Scripts](https://github.com/mh4Scripts)

**Program Studi Teknik Informatika**  
Institut Teknologi Sumatera (ITERA)
