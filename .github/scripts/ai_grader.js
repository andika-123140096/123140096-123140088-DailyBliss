const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

async function run() {
    const apiKey = process.env.GEMINI_API_KEY;
    const modelName = process.env.GEMINI_MODEL_NAME || 'gemini-3-flash-preview';
    
    if (!apiKey) {
        console.error('Error: GEMINI_API_KEY is not set');
        process.exit(1);
    }

    // 1. Generate Codebase Context using Repomix
    console.log('Generating codebase context using repomix...');
    try {
        // Run repomix to get a XML representation of the codebase
        // We ignore binary files and resources to keep the context size manageable
        execSync('npx repomix@latest --style xml --output repomix-output.xml --ignore "**/composeResources/**,**/gradle/**,**/*.png,**/*.jpg,**/*.jpeg,**/*.gif,**/*.svg"', { stdio: 'inherit' });
    } catch (e) {
        console.error('Error: Failed to run repomix', e.message);
        process.exit(1);
    }

    const codebaseContext = fs.readFileSync('repomix-output.xml', 'utf8');

    const todayDate = new Date();
    const sprintSchedule = [
        { id: '1', title: 'Sprint 1 (Planning & Setup)', startDate: '2026-02-01', rubric: 'Repository Setup (20%), Project Structure (25%), CI/CD (20%), Documentation (25%), Collaboration (10%).' },
        { id: '2', title: 'Sprint 2 (Core Features)', startDate: '2026-02-22', rubric: 'UI Screens (25%), Navigation (20%), Data Layer (25%), CRUD (20%), Code Quality (10%).' },
        { id: '3', title: 'Sprint 3 (Advanced Features)', startDate: '2026-03-22', rubric: 'Search/Filter (25%), API/Enhanced Local (25%), Offline Support (20%), Additional Screen (15%), Bonus (15%).' },
        { id: '4', title: 'Sprint 4 (Polish & Testing)', startDate: '2026-04-19', rubric: 'Bug Fixes (25%), UI Polish (25%), Unit Tests (25%), UI Tests (15%), Coverage (10%).' },
        { id: '5-6', title: 'Sprint 5 & 6 (Final Prep & UAS)', startDate: '2026-05-24', rubric: 'App Functionality (30%), Demo & Presentation (25%), Code Quality (20%), Technical Depth (15%), Q&A (10%).' }
    ];

    const activeSprints = sprintSchedule.filter(s => new Date(s.startDate) <= todayDate);
    const activeSprintIds = activeSprints.map(s => s.id).join(', ');
    const rubricText = activeSprints.map(s => `* **${s.title}**: ${s.rubric}`).join('\n');

    const today = todayDate.toLocaleDateString('id-ID', { day: 'numeric', month: 'long', year: 'numeric' });

    // 2. Prepare Prompt
    const systemPrompt = `
# Role dan Tujuan
Anda adalah AI Penilai Proyek (Project Grader Agent) yang jeli, netral, dan objektif. Tugas Anda adalah mengevaluasi proyek Pengembangan Aplikasi Mobile (Kotlin Multiplatform & Compose) mahasiswa ITERA.

# Aturan Evaluasi
1. Analisis Kode: Periksa implementasi berdasarkan codebase yang diberikan dalam format XML. Pastikan mematuhi Clean Architecture (data, domain, presentation) dan MVVM.
2. Batasan Waktu: Hari ini adalah tanggal ${today}. Anda HANYA boleh memberikan penilaian untuk Sprint yang sudah aktif (${activeSprintIds}). JANGAN menilai sprint masa depan.
3. Output Penilaian: Berikan penilaian yang mendalam, jujur, dan membantu mahasiswa untuk memperbaiki kode mereka.

# Rubrik Penilaian Aktif (Hingga ${today})
${rubricText}

# Instruksi Format Output (WAJIB)
Gunakan format berikut secara eksak:

---START_GRADING---
# 📊 HASIL EVALUASI PROYEK AKHIR - GENAP 2025/2026

[Berikan ringkasan eksekutif tentang status proyek saat ini secara keseluruhan.]

## 🔍 Ringkasan Analisis
* **Kelebihan:** ...
* **Temuan Utama:** ...

[HANYA TAMPILKAN SPRINT YANG SUDAH AKTIF BERDASARKAN TANGGAL HARI INI]

## 📅 Sprint 1: Planning & Setup
| Kriteria | Skor (0-100) | Catatan |
| :--- | :---: | :--- |
| Repository Setup (20%) | | |
| Project Structure (25%) | | |
| CI/CD (20%) | | |
| Documentation (25%) | | |
| Collaboration (10%) | | |
| **Total Estimasi** | **Score/100** | |

### 📝 Penjelasan Sprint 1
[Berikan penjelasan mendalam mengapa skor tersebut diberikan dan apa yang bisa ditingkatkan.]

[ULANGI FORMAT DI ATAS UNTUK SPRINT 2, 3, DST. JIKA SUDAH AKTIF]

## 🏗️ Arsitektur & State Management
- [Analisis singkat layering dan penggunaan ViewModel/StateFlow]

## 🛠️ Rekomendasi Perbaikan Umum
1. ...
---END_GRADING---
    `;

    const userPrompt = `Berikut adalah seluruh codebase proyek dalam format XML:\n\n${codebaseContext}`;

    // 3. Call Gemini API
    const url = `https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=${apiKey}`;
    
    const requestBody = {
        system_instruction: { parts: [{ text: systemPrompt }] },
        contents: [{ role: "user", parts: [{ text: userPrompt }] }],
        generationConfig: {
            thinkingConfig: {
                thinkingLevel: "high"
            },
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

                if (gradingMatch) {
                    const gradingResult = gradingMatch[1].trim();
                    fs.writeFileSync('.github/penilaian-sementara.md', gradingResult);
                    console.log('Berhasil: Penilaian telah diperbarui.');
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

    // Cleanup
    if (fs.existsSync('repomix-output.xml')) {
        fs.unlinkSync('repomix-output.xml');
    }
}

run();
