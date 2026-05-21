package com.dailybliss.app.core.network

import com.dailybliss.app.BuildConfig

/**
 * Android implementation of ApiConfig
 *
 * Mengambil API key dari BuildConfig yang di-generate
 * dari local.properties saat build time.
 *
 * Setup:
 * 1. Buat file local.properties di root project
 * 2. Tambahkan: GEMINI_API_KEY=your_api_key_here
 * 3. Build project (API key akan di-inject ke BuildConfig)
 */
class AndroidApiConfig : ApiConfig {
    override val geminiApiKey: String = BuildConfig.GEMINI_API_KEY
    override val geminiModelName: String = BuildConfig.GEMINI_MODEL_NAME
}
