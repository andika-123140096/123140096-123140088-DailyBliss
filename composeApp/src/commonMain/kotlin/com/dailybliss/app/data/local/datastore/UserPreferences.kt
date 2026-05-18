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
class UserPreferences(
    private val dataStore: DataStore<Preferences>
) {
    // ==================== PREFERENCE KEYS ====================
    
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val SORT_BY = stringPreferencesKey("sort_by")
        val DEFAULT_CATEGORY = stringPreferencesKey("default_category")
        val SHOW_PREVIEW = booleanPreferencesKey("show_preview")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val NICKNAME = stringPreferencesKey("nickname")
        val AI_LANGUAGE_STYLE = stringPreferencesKey("ai_language_style")
        val COLOR_THEME = stringPreferencesKey("color_theme")
        val AI_GREETING_CACHE = stringPreferencesKey("ai_greeting_cache")
        val AI_DAILY_PROMPT_CACHE = stringPreferencesKey("ai_daily_prompt_cache")
        val AI_CACHE_TIMESTAMP = stringPreferencesKey("ai_cache_timestamp")
    }
    
    // ==================== USER PROFILE ====================

    /**
     * Observe nickname
     */
    val nickname: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.NICKNAME] ?: "User"
    }

    /**
     * Set nickname
     */
    suspend fun setNickname(name: String) {
        dataStore.edit { prefs ->
            prefs[Keys.NICKNAME] = name
            prefs.remove(Keys.AI_CACHE_TIMESTAMP) // Invalidate cache
        }
    }

    /**
     * Observe AI language style
     */
    val aiLanguageStyle: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.AI_LANGUAGE_STYLE] ?: "Santai/Kasual"
    }

    /**
     * Set AI language style
     */
    suspend fun setAiLanguageStyle(style: String) {
        dataStore.edit { prefs ->
            prefs[Keys.AI_LANGUAGE_STYLE] = style
            prefs.remove(Keys.AI_CACHE_TIMESTAMP) // Invalidate cache
        }
    }

    // ==================== AI CACHE ====================

    /**
     * Observe AI Greeting Cache
     */
    val aiGreetingCache: Flow<String?> = dataStore.data.map { it[Keys.AI_GREETING_CACHE] }

    /**
     * Set AI Greeting Cache
     */
    suspend fun setAiGreetingCache(greeting: String) {
        dataStore.edit { it[Keys.AI_GREETING_CACHE] = greeting }
    }

    /**
     * Observe AI Daily Prompt Cache
     */
    val aiDailyPromptCache: Flow<String?> = dataStore.data.map { it[Keys.AI_DAILY_PROMPT_CACHE] }

    /**
     * Set AI Daily Prompt Cache
     */
    suspend fun setAiDailyPromptCache(prompt: String) {
        dataStore.edit { it[Keys.AI_DAILY_PROMPT_CACHE] = prompt }
    }

    /**
     * Observe AI Cache Timestamp
     */
    val aiCacheTimestamp: Flow<Long> = dataStore.data.map { 
        it[Keys.AI_CACHE_TIMESTAMP]?.toLongOrNull() ?: 0L 
    }

    /**
     * Update AI Cache Timestamp
     */
    suspend fun updateAiCacheTimestamp(timestamp: Long) {
        dataStore.edit { it[Keys.AI_CACHE_TIMESTAMP] = timestamp.toString() }
    }

    // ==================== THEME ====================

    /**
     * Observe color theme
     */
    val colorTheme: Flow<String> = dataStore.data.map { prefs ->
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
    
    // ==================== DARK MODE ====================
    
    /**
     * Observe dark mode setting
     */
    val isDarkMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.DARK_MODE] ?: false
    }
    
    /**
     * Set dark mode
     */
    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = enabled
        }
    }
    
    // ==================== SORT BY ====================
    
    /**
     * Observe sort preference
     */
    val sortBy: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.SORT_BY] ?: "UPDATED_DESC"
    }
    
    /**
     * Set sort preference
     */
    suspend fun setSortBy(sortBy: String) {
        dataStore.edit { prefs ->
            prefs[Keys.SORT_BY] = sortBy
        }
    }
    
    // ==================== DEFAULT CATEGORY ====================
    
    /**
     * Observe default category
     */
    val defaultCategory: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_CATEGORY] ?: "GENERAL"
    }
    
    /**
     * Set default category
     */
    suspend fun setDefaultCategory(category: String) {
        dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_CATEGORY] = category
        }
    }
    
    // ==================== SHOW PREVIEW ====================
    
    /**
     * Observe show preview setting
     */
    val showPreview: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.SHOW_PREVIEW] ?: true
    }
    
    /**
     * Set show preview
     */
    suspend fun setShowPreview(show: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.SHOW_PREVIEW] = show
        }
    }
    
    // ==================== ONBOARDING ====================
    
    /**
     * Check if onboarding completed
     */
    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }
    
    /**
     * Set onboarding completed
     */
    suspend fun setOnboardingCompleted() {
        dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = true
        }
    }
}

