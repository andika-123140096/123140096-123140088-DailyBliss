package com.dailybliss.app.presentation

import app.cash.turbine.test
import com.dailybliss.app.data.repository.FakeAIRepository
import com.dailybliss.app.domain.model.ChatSession
import com.dailybliss.app.presentation.screens.ai.ChatHistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChatHistoryViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var aiRepository: FakeAIRepository
    private lateinit var viewModel: ChatHistoryViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        aiRepository = FakeAIRepository()
        viewModel = ChatHistoryViewModel(aiRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sessions should reflect repository data`() = runTest {
        viewModel.sessions.test {
            assertEquals(emptyList<ChatSession>(), awaitItem())

            val session = ChatSession(1, "Test Chat", 1000, 1000)
            aiRepository.addFakeSession(session)

            assertEquals(listOf(session), awaitItem())
        }
    }

    @Test
    fun `onSessionSelected should load session and navigate back`() = runTest {
        val session = ChatSession(1, "Test Chat", 1000, 1000)
        aiRepository.addFakeSession(session)
        var navigatedBack = false

        viewModel.onSessionSelected(session) { navigatedBack = true }
        advanceUntilIdle()

        assertTrue(navigatedBack)
        aiRepository.currentSessionId.test {
            assertEquals(1L, awaitItem())
        }
    }

    @Test
    fun `onStartNewSession should clear session and navigate back`() = runTest {
        val session = ChatSession(1, "Test Chat", 1000, 1000)
        aiRepository.addFakeSession(session)
        aiRepository.loadSession(1L)
        var navigatedBack = false

        viewModel.onStartNewSession { navigatedBack = true }
        advanceUntilIdle()

        assertTrue(navigatedBack)
        aiRepository.currentSessionId.test {
            assertEquals(null, awaitItem())
        }
    }

    @Test
    fun `onDeleteSession should remove session from repository`() = runTest {
        val session = ChatSession(1, "Test Chat", 1000, 1000)
        aiRepository.addFakeSession(session)

        viewModel.onDeleteSession(1L)
        advanceUntilIdle()

        viewModel.sessions.test {
            assertEquals(emptyList<ChatSession>(), awaitItem())
        }
    }
}
