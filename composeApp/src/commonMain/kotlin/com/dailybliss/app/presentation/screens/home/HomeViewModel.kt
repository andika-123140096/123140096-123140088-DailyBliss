package com.dailybliss.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.data.local.datastore.UserPreferences
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.domain.repository.AIRepository
import com.dailybliss.app.domain.usecase.GetAllMomentsUseCase
import com.dailybliss.app.domain.usecase.GetMomentsFromSameDayUseCase
import com.dailybliss.app.presentation.screens.ai.ChatMessage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class HomeUiState(
    val greeting: String = "",
    val pinnedMoments: List<Moment> = emptyList(),
    val memoryLaneMoments: List<Moment> = emptyList(),
    val dailyPrompt: String? = null,
    val isLoading: Boolean = true,
)

class HomeViewModel(
    private val aiRepository: AIRepository,
    private val userPreferences: UserPreferences,
    private val getAllMomentsUseCase: GetAllMomentsUseCase,
    private val getMomentsFromSameDayUseCase: GetMomentsFromSameDayUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeSettings()
        observePinnedMoments()
        loadMemoryLane()
        loadDailyPrompt()
    }

    private fun observePinnedMoments() {
        viewModelScope.launch {
            getAllMomentsUseCase().map { moments ->
                moments.filter { it.isPinned }
            }.collect { pinned ->
                _uiState.update { it.copy(pinnedMoments = pinned) }
            }
        }
    }

    private fun loadMemoryLane() {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dayMonth = "${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"

        viewModelScope.launch {
            getMomentsFromSameDayUseCase(dayMonth).collect { moments ->
                _uiState.update { it.copy(memoryLaneMoments = moments) }
            }
        }
    }

    private fun loadDailyPrompt() {
        viewModelScope.launch {
            val lastTimestamp = userPreferences.aiCacheTimestamp.first()
            val now = Clock.System.now().toEpochMilliseconds()
            val cachedPrompt = userPreferences.aiDailyPromptCache.first()

            if (cachedPrompt != null && (now - lastTimestamp) < CACHE_DURATION) {
                _uiState.update { it.copy(dailyPrompt = cachedPrompt) }
            } else {
                val prompt = aiRepository.generateDailyPrompt()
                if (prompt != null) {
                    _uiState.update { it.copy(dailyPrompt = prompt) }
                    userPreferences.setAiDailyPromptCache(prompt)
                    userPreferences.updateAiCacheTimestamp(now)
                }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                userPreferences.nickname,
                userPreferences.aiLanguageStyle,
                userPreferences.aiGreetingCache,
                userPreferences.aiCacheTimestamp,
            ) { nickname, style, cachedGreeting, lastTimestamp ->
                val now = Clock.System.now().toEpochMilliseconds()
                val isCacheValid = cachedGreeting != null && (now - lastTimestamp) < CACHE_DURATION

                GreetingParams(nickname, style, cachedGreeting, isCacheValid)
            }.collectLatest { params ->
                if (params.isCacheValid && params.cachedGreeting != null) {
                    _uiState.update { it.copy(greeting = params.cachedGreeting, isLoading = false) }
                } else {
                    fetchGreeting(params.nickname, params.style)
                }
            }
        }
    }

    private fun fetchGreeting(nickname: String, style: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val greeting = aiRepository.chat(
                    listOf(
                        ChatMessage(
                            role = "user",
                            text = """
                                Berikan sapaan singkat, hangat, dan puitis untuk pengguna bernama '$nickname' di aplikasi jurnal 'DailyBliss'. 
                                Gunakan gaya bahasa: '$style'.
                                Maksimal 2 kalimat. 
                                Berikan kesan tenang dan blissful. 
                                Sapa pengguna dengan namanya. 
                                Jangan gunakan markdown.
                            """.trimIndent(),
                        ),
                    ),
                )
                _uiState.update { it.copy(greeting = greeting, isLoading = false) }
                userPreferences.setAiGreetingCache(greeting)
                userPreferences.updateAiCacheTimestamp(Clock.System.now().toEpochMilliseconds())
            } catch (e: Exception) {
                _uiState.update { it.copy(greeting = "Selamat datang kembali, $nickname.", isLoading = false) }
            }
        }
    }

    private data class GreetingParams(
        val nickname: String,
        val style: String,
        val cachedGreeting: String?,
        val isCacheValid: Boolean,
    )

    companion object {
        private const val CACHE_DURATION = 24 * 60 * 60 * 1000L // 24 jam dalam ms
    }
}
