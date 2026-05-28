package com.dailybliss.app.presentation.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.domain.model.ChatSession
import com.dailybliss.app.domain.repository.AIRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatHistoryViewModel(
    private val aiRepository: AIRepository,
) : ViewModel() {

    val sessions: StateFlow<List<ChatSession>> = aiRepository.getAllChatSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun onSessionSelected(session: ChatSession, onNavigateBack: () -> Unit) {
        aiRepository.loadSession(session.id)
        onNavigateBack()
    }

    fun onStartNewSession(onNavigateBack: () -> Unit) {
        aiRepository.startNewSession()
        onNavigateBack()
    }

    fun onDeleteSession(sessionId: Long) {
        viewModelScope.launch {
            aiRepository.deleteSession(sessionId)
        }
    }
}
