package com.dailybliss.app.data.remote.api

import com.dailybliss.app.data.remote.dto.*

object AITools {
    val ALL_TOOLS = listOf(
        GeminiTool(
            functionDeclarations = listOf(
                GeminiFunctionDeclaration(
                    name = "get_moments",
                    description = "Mencari catatan jurnal atau momen harian pengguna berdasarkan tanggal atau kata kunci.",
                    parameters = GeminiFunctionParameters(
                        properties = mapOf(
                            "date" to GeminiFunctionProperty(
                                type = "string",
                                description = "Tanggal momen dalam format YYYY-MM-DD (contoh: 2024-05-20).",
                            ),
                            "keyword" to GeminiFunctionProperty(
                                type = "string",
                                description = "Kata kunci untuk mencari di isi momen.",
                            ),
                        ),
                    ),
                ),
                GeminiFunctionDeclaration(
                    name = "get_news",
                    description = "Mengambil ringkasan berita terbaru.",
                    parameters = GeminiFunctionParameters(
                        properties = mapOf(
                            "category" to GeminiFunctionProperty(
                                type = "string",
                                description = "Kategori berita (contoh: technology, business, sports).",
                                enum = listOf("general", "technology", "business", "sports", "entertainment", "health", "science"),
                            ),
                        ),
                    ),
                ),
                GeminiFunctionDeclaration(
                    name = "get_weather",
                    description = "Mengambil data cuaca saat ini di lokasi tertentu.",
                    parameters = GeminiFunctionParameters(
                        properties = mapOf(
                            "location" to GeminiFunctionProperty(
                                type = "string",
                                description = "Nama kota atau lokasi (contoh: Jakarta, Tokyo).",
                            ),
                        ),
                        required = listOf("location"),
                    ),
                ),
                GeminiFunctionDeclaration(
                    name = "get_currency_rate",
                    description = "Mengambil kurs konversi mata uang terbaru.",
                    parameters = GeminiFunctionParameters(
                        properties = mapOf(
                            "base" to GeminiFunctionProperty(
                                type = "string",
                                description = "Mata uang asal (contoh: USD, EUR, IDR).",
                            ),
                            "target" to GeminiFunctionProperty(
                                type = "string",
                                description = "Mata uang tujuan (contoh: IDR, JPY, SGD).",
                            ),
                        ),
                        required = listOf("base", "target"),
                    ),
                ),
            ),
        ),
    )
}
