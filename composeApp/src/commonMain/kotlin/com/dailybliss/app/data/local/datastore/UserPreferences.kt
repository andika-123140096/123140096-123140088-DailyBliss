package com.dailybliss.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
interface UserPreferences {
    val nickname: Flow<String>
    suspend fun setNickname(name: String)
    val aiLanguageStyle: Flow<String>
    suspend fun setAiLanguageStyle(style: String)
    val journalSummary: Flow<String>
    suspend fun setJournalSummary(summary: String)
    val dailyInsight: Flow<String>
    suspend fun setDailyInsight(insight: String)
    val isDarkMode: Flow<Boolean>
    suspend fun setDarkMode(isDark: Boolean)
}

/**
 * User Preferences implementation using DataStore
 */
class DataStoreUserPreferences(private val dataStore: DataStore<Preferences>) : UserPreferences {
    // ==================== PREFERENCE KEYS ====================

    private object Keys {
        val NICKNAME = stringPreferencesKey("nickname")
        val AI_LANGUAGE_STYLE = stringPreferencesKey("ai_language_style")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val JOURNAL_SUMMARY = stringPreferencesKey("journal_summary")
        val DAILY_INSIGHT = stringPreferencesKey("daily_insight")
    }

    // ==================== USER PROFILE ====================

    override val nickname: Flow<String> =
        dataStore.data.map { prefs ->
            prefs[Keys.NICKNAME] ?: "User"
        }

    override suspend fun setNickname(name: String) {
        dataStore.edit { prefs ->
            prefs[Keys.NICKNAME] = name
        }
    }

    override val aiLanguageStyle: Flow<String> =
        dataStore.data.map { prefs ->
            prefs[Keys.AI_LANGUAGE_STYLE] ?: "Santai/Kasual"
        }

    override suspend fun setAiLanguageStyle(style: String) {
        dataStore.edit { prefs ->
            prefs[Keys.AI_LANGUAGE_STYLE] = style
        }
    }

    // ==================== JOURNAL SUMMARY ====================

    override val journalSummary: Flow<String> =
        dataStore.data.map {
            it[Keys.JOURNAL_SUMMARY] ?: ""
        }

    override suspend fun setJournalSummary(summary: String) {
        dataStore.edit { it[Keys.JOURNAL_SUMMARY] = summary }
    }

    override val dailyInsight: Flow<String> =
        dataStore.data.map {
            it[Keys.DAILY_INSIGHT] ?: ""
        }

    override suspend fun setDailyInsight(insight: String) {
        dataStore.edit { it[Keys.DAILY_INSIGHT] = insight }
    }

    // ==================== THEME ====================

    override val isDarkMode: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[Keys.IS_DARK_MODE] ?: false
        }

    override suspend fun setDarkMode(isDark: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_DARK_MODE] = isDark
        }
    }
}
