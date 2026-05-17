const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

async function run() {
    const apiKey = process.env.GEMINI_API_KEY;
    const modelName = process.env.GEMINI_MODEL_NAME || 'gemini-1.5-flash';
    
    if (!apiKey) {
        console.error('Error: GEMINI_API_KEY is not set');
        process.exit(1);
    }

    const contextPath = '.github/context-window.md';
    const isFirstRun = !fs.existsSync(contextPath);
    let contextContent = '';

    // 1. Get Git Diff & Context
    let diff = '';
    try {
        if (isFirstRun) {
            console.log('Context window not found. Performing full evaluation from initial commit...');
            // Compare from the specified initial commit to HEAD
            diff = execSync('git diff 5192bfad97b533f6b1aa368162a5b09e2729e329 HEAD').toString();
        } else {
            console.log('Context window found. Performing incremental evaluation...');
            contextContent = fs.readFileSync(contextPath, 'utf8');
            diff = execSync('git diff HEAD~1 HEAD').toString();
        }
    } catch (e) {
        console.warn('Warning: Could not get git diff. Falling back to empty diff.', e.message);
    }

    if (!diff || diff.trim() === '') {
        console.log('No significant changes detected in git diff.');
    }

    const today = new Date().toLocaleDateString('id-ID', { day: 'numeric', month: 'long', year: 'numeric' });

    // 2. Prepare Prompt
    const systemPrompt = `
# Role dan Tujuan
Anda adalah AI Penilai Proyek (Project Grader Agent) yang jeli, netral, dan objektif. Tugas Anda adalah mengevaluasi proyek Pengembangan Aplikasi Mobile (Kotlin Multiplatform & Compose) mahasiswa ITERA.

# Aturan Evaluasi
1. Analisis Kode: Periksa implementasi berdasarkan input git diff. Pastikan mematuhi Clean Architecture (data, domain, presentation) dan MVVM.
2. Evaluasi Multi-Sprint: Berikan penilaian untuk SEMUA Sprint (1-6) yang kriterianya terpenuhi dalam kode. Jangan batasi hanya pada satu sprint.
3. Manajemen Memori: Anda diberikan "Context Window" (jika ada) yang berisi status proyek sebelumnya. Gunakan ini untuk mempertahankan pemahaman arsitektur meskipun diff yang diberikan kecil.
4. Output Terpisah: Anda WAJIB memisahkan output penilaian dan output update context menggunakan penanda (markers) yang ditentukan.

# Rubrik Penilaian Per Sprint (Sprint 1 - 6)

* **Sprint 1 (Planning & Setup)**: Repository Setup (20%), Project Structure (25%), CI/CD (20%), Documentation (25%), Collaboration (10%).
* **Sprint 2 (Core Features)**: UI Screens (25%), Navigation (20%), Data Layer (25%), CRUD (20%), Code Quality (10%).
* **Sprint 3 (Advanced Features)**: Search/Filter (25%), API/Enhanced Local (25%), Offline Support (20%), Additional Screen (15%), Bonus (15%).
* **Sprint 4 (Polish & Testing)**: Bug Fixes (25%), UI Polish (25%), Unit Tests (25%), UI Tests (15%), Coverage (10%).
* **Sprint 5 & 6 (Final Prep & UAS)**: App Functionality (30%), Demo & Presentation (25%), Code Quality (20%), Technical Depth (15%), Q&A (10%).

# Instruksi Format Output (WAJIB)
Gunakan format berikut secara eksak:

---START_GRADING---
# 📊 HASIL EVALUASI PROYEK AKHIR - GENAP 2025/2026

[Tampilkan rincian penilaian untuk setiap Sprint 1-6. Jika belum ada progres pada sprint tertentu, berikan skor 0 dengan catatan yang sesuai.]

## 🔍 Ringkasan Analisis
* **Kelebihan:** ...
* **Temuan:** ...

## 📈 Rincian Penilaian (Sprint 1 - 6)
[Gunakan tabel atau list yang jelas untuk setiap sprint]

## 🛠️ Rekomendasi Perbaikan
1. ...
---END_GRADING---

---START_CONTEXT---
# 🧠 PROJECT CONTEXT WINDOW (DO NOT EDIT MANUALLY)
Simpan ringkasan teknis proyek di sini:
- Struktur Folder: [Deteksi apakah KMP/Compose Multiplatform sudah benar]
- Fitur Terimplementasi: ...
- Arsitektur: [Data/Domain/Presentation/MVVM]
- Tech Stack: ...
---END_CONTEXT---
    `;

    const userPrompt = isFirstRun 
        ? `Berikut adalah git diff lengkap dari awal proyek:\n\n${diff}`
        : `Berikut adalah Context Window saat ini:\n\n${contextContent}\n\nDan berikut adalah git diff terbaru:\n\n${diff}`;

    // 3. Call Gemini API
    const url = `https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=${apiKey}`;
    
    const requestBody = {
        system_instruction: { parts: [{ text: systemPrompt }] },
        contents: [{ role: "user", parts: [{ text: userPrompt }] }],
        generationConfig: {
            temperature: 0.1,
            maxOutputTokens: 8192,
        }
    };

    let attempt = 1;
    while (attempt <= 3) {
        try {
            console.log(`Attempt ${attempt}: Calling Gemini API...`);
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestBody)
            });

            const data = await response.json();
            
            if (response.ok && data.candidates?.[0]?.content?.parts?.[0]?.text) {
                const fullText = data.candidates[0].content.parts[0].text;
                
                // 4. Parse Output
                const gradingMatch = fullText.match(/---START_GRADING---([\s\S]*?)---END_GRADING---/);
                const contextMatch = fullText.match(/---START_CONTEXT---([\s\S]*?)---END_CONTEXT---/);

                if (gradingMatch && contextMatch) {
                    const gradingResult = gradingMatch[1].trim();
                    const contextResult = contextMatch[1].trim();

                    fs.writeFileSync('.github/penilaian-sementara.md', gradingResult);
                    fs.writeFileSync(contextPath, contextResult);
                    
                    console.log('Berhasil: Penilaian dan Context Window telah diperbarui.');
                    break;
                } else {
                    console.error('Error: AI tidak memberikan format markers yang benar.');
                    console.log('Full AI Response for debugging:', fullText);
                }
            } else {
                console.error(`Attempt ${attempt} failed. Status: ${response.status}`, JSON.stringify(data, null, 2));
            }
        } catch (error) {
            console.error(`Attempt ${attempt} error:`, error.message);
        }
        
        const waitTime = Math.pow(2, attempt) * 1000;
        await new Promise(r => setTimeout(r, waitTime));
        attempt++;
    }
}

run();
