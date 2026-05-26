package com.dailybliss.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * User Preferences menggunakan DataStore
 *
 * DataStore adalah pengganti SharedPreferences yang lebih modern:
 * - Asynchronous dengan Coroutines dan Flow
 * - Type-safe dengan Preferences Keys
 * - Tidak blocking main thread
 *
 * @param dataStore Instance DataStore dari platform
 */
class UserPreferences(private val dataStore: DataStore<Preferences>) {
    // ==================== PREFERENCE KEYS ====================

    private object Keys {
        val NICKNAME = stringPreferencesKey("nickname")
        val AI_LANGUAGE_STYLE = stringPreferencesKey("ai_language_style")
        val COLOR_THEME = stringPreferencesKey("color_theme")
        val JOURNAL_SUMMARY = stringPreferencesKey("journal_summary")
    }

    // ==================== USER PROFILE ====================

    /**
     * Observe nickname
     */
    val nickname: Flow<String> =
        dataStore.data.map { prefs ->
            prefs[Keys.NICKNAME] ?: "User"
        }

    /**
     * Set nickname
     */
    suspend fun setNickname(name: String) {
        dataStore.edit { prefs ->
            prefs[Keys.NICKNAME] = name
        }
    }

    /**
     * Observe AI language style
     */
    val aiLanguageStyle: Flow<String> =
        dataStore.data.map { prefs ->
            prefs[Keys.AI_LANGUAGE_STYLE] ?: "Santai/Kasual"
        }

    /**
     * Set AI language style
     */
    suspend fun setAiLanguageStyle(style: String) {
        dataStore.edit { prefs ->
            prefs[Keys.AI_LANGUAGE_STYLE] = style
        }
    }

    // ==================== JOURNAL SUMMARY ====================

    /**
     * Observe Global Journal Summary
     */
    val journalSummary: Flow<String> =
        dataStore.data.map {
            it[Keys.JOURNAL_SUMMARY] ?: ""
        }

    /**
     * Update Global Journal Summary
     */
    suspend fun setJournalSummary(summary: String) {
        dataStore.edit { it[Keys.JOURNAL_SUMMARY] = summary }
    }

    // ==================== THEME ====================

    /**
     * Observe color theme
     */
    val colorTheme: Flow<String> =
        dataStore.data.map { prefs ->
            prefs[Keys.COLOR_THEME] ?: "Sage Green"
        }

    /**
     * Set color theme
     */
    suspend fun setColorTheme(themeName: String) {
        dataStore.edit { prefs ->
            prefs[Keys.COLOR_THEME] = themeName
        }
    }
}
