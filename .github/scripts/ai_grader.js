const { execSync } = require('child_process');
const fs = require('fs');

async function run() {
    const apiKey = process.env.GEMINI_API_KEY;
    const modelName = process.env.GEMINI_MODEL_NAME || 'gemini-1.5-flash';
    
    if (!apiKey) {
        console.error('Error: GEMINI_API_KEY is not set');
        process.exit(1);
    }

    // 1. Get Git Diff
    let diff = '';
    try {
        // Try to get diff between current and previous commit
        diff = execSync('git diff HEAD~1 HEAD').toString();
    } catch (e) {
        console.warn('Warning: Could not get git diff via HEAD~1. Falling back to empty diff.');
    }

    if (!diff || diff.trim() === '') {
        console.log('No significant changes detected in git diff.');
        // We still run the AI to let it know there's no changes if necessary, 
        // but usually we can skip or provide a default message.
    }

    const today = new Date().toLocaleDateString('id-ID', { day: 'numeric', month: 'long', year: 'numeric' });

    // 2. Prepare Prompt
    const systemPrompt = `
# Role dan Tujuan
Anda adalah AI Penilai Proyek (Project Grader Agent) yang jeli, netral, dan objektif. Tugas Anda adalah mengevaluasi proyek Pengembangan Aplikasi Mobile (Kotlin Multiplatform & Compose) mahasiswa ITERA berdasarkan input git diff. 

# Aturan Evaluasi
1. Analisis Git Diff: Periksa baris yang ditambahkan (+) dan dihapus (-). Pastikan implementasi mematuhi Clean Architecture (data, domain, presentation) dan MVVM. Potong nilai jika ada code smell (misal: logika bisnis di UI, hardcode API key).
2. Fokus Per Sprint: Evaluasi HANYA berdasarkan rubrik Sprint yang sedang dinilai pada saat itu (Hari ini: ${today}). 
3. Tanpa Basa-basi: Jangan tambahkan teks pengantar atau penutup. Output Anda HANYA berupa format Markdown yang akan langsung ditulis ke .github/penilaian-sementara.md.

# Rubrik Penilaian Per Sprint

**Periode 10 - 16 Mei 2026**
* **Sprint 1 (Planning & Setup)**: 
    * Repository Setup (20%): Penamaan dan kolaborator.
    * Project Structure (25%): Struktur Clean Architecture.
    * CI/CD Pipeline (20%): GitHub Actions berjalan.
    * Documentation (25%): README lengkap & Project plan.
    * Collaboration (10%): Kontribusi commit.
* **Sprint 2 (Core Features)**: 
    * UI Screens (25%): Minimal 3 layar fungsional.
    * Navigation (20%): Navigasi beroperasi dengan arguments passing.
    * Data Layer (25%): Repository pattern, local storage.
    * CRUD (20%): Create, Read, Update, Delete beroperasi.
    * Code Quality (10%): Kode bersih, CI passing.

**Periode 24 - 30 Mei 2026**
* **Sprint 3 (Advanced Features)**: 
    * Search/Filter (25%): Fungsionalitas pencarian beroperasi.
    * API/Enhanced Local (25%): Integrasi API atau fitur lokal tingkat lanjut.
    * Offline Support (20%): Aplikasi dapat digunakan tanpa internet.
    * Additional Screen (15%): Layar Settings/Profile fungsional.
    * Bonus Features (15%): Mengimplementasikan minimal 1 fitur tambahan.
* **Sprint 4 (Polish & Testing)**: 
    * Bug Fixes (25%): Tidak ada crash.
    * UI Polish (25%): UI konsisten.
    * Unit Tests (25%): 10+ Unit tests.
    * UI Tests (15%): 3+ UI tests (alur kritis).
    * Coverage (10%): >50% code coverage.

**Periode 31 Mei - 6 Juni 2026**
* **Sprint 5 & 6 (Final Prep & UAS Demo Day)**: 
    * App Functionality (30%): Fungsionalitas penuh, UX rapi.
    * Demo & Presentation (25%): Penjelasan jelas, alur demo lancar.
    * Code Quality (20%): Clean architecture, pola pemrograman tepat.
    * Technical Depth (15%): Pemecahan masalah teknis.
    * Q&A Response (10%): Pemahaman kode.

# Format Output (Wajib)

# 📊 HASIL EVALUASI PROYEK AKHIR - GENAP 2025/2026

**Sprint yang Dinilai:** [Sebutkan Sprint Ke-Berapa]
**Status:** [LULUS / PERLU REVISI]
**Skor Total:** [XX]/100

## 🔍 Ringkasan Analisis Git Diff
* **Kelebihan:** [Poin positif berdasarkan baris kode yang ditambahkan]
* **Temuan / Pelanggaran:** [Sebutkan nama file atau baris kode yang melanggar arsitektur/best practice, jika ada]

## 📈 Rincian Penilaian
| Kriteria | Bobot | Skor | Analisis Evaluator (Berdasarkan Diff) |
| :--- | :---: | :---: | :--- |
| [Kriteria 1] | XX% | X/100 | [Penjelasan spesifik melihat perubahan kode] |
| [Kriteria 2] | XX% | X/100 | [Penjelasan spesifik melihat perubahan kode] |
| [Kriteria ...] | XX% | X/100 | [Penjelasan spesifik melihat perubahan kode] |

## 🛠️ Rekomendasi Perbaikan
1. [Saran perbaikan teknis untuk commit selanjutnya]
    `;

    const userPrompt = `Berikut adalah hasil git diff dari commit terbaru mahasiswa:\n\n${diff}`;

    // 3. Call Gemini API with Retry Logic
    const url = `https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=${apiKey}`;
    
    const requestBody = {
        system_instruction: {
            parts: [{ text: systemPrompt }]
        },
        contents: [
            {
                role: "user",
                parts: [{ text: userPrompt }]
            }
        ],
        generationConfig: {
            temperature: 0.2,
            topK: 40,
            topP: 0.95,
            maxOutputTokens: 4096,
        }
    };

    let attempt = 1;
    while (true) {
        try {
            console.log(`Attempt ${attempt}: Calling Gemini API...`);
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });

            const data = await response.json();
            
            if (response.ok && data.candidates && data.candidates[0] && data.candidates[0].content && data.candidates[0].content.parts[0]) {
                const markdownResult = data.candidates[0].content.parts[0].text;
                fs.writeFileSync('.github/penilaian-sementara.md', markdownResult);
                console.log('Penilaian berhasil disimpan ke .github/penilaian-sementara.md');
                break; // Success!
            } else {
                console.error(`Attempt ${attempt} failed. Response Status: ${response.status}`);
                console.error('Error Details:', JSON.stringify(data, null, 2));
                
                // Exponential backoff: (2^attempt * 1000ms) + random jitter
                const waitTime = Math.min(Math.pow(2, attempt) * 1000 + Math.random() * 1000, 30000); 
                console.log(`Retrying in ${Math.round(waitTime/1000)} seconds...`);
                await new Promise(resolve => setTimeout(resolve, waitTime));
                attempt++;
            }
        } catch (error) {
            console.error(`Attempt ${attempt} encountered an exception:`, error.message);
            const waitTime = Math.min(Math.pow(2, attempt) * 1000 + Math.random() * 1000, 30000);
            await new Promise(resolve => setTimeout(resolve, waitTime));
            attempt++;
        }
    }
}

run();
