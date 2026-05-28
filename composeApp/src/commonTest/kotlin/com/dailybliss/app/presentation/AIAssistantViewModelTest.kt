package com.dailybliss.app.presentation

import app.cash.turbine.test
import com.dailybliss.app.data.repository.FakeAIRepository
import com.dailybliss.app.presentation.screens.ai.AIAssistantViewModel
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

@OptIn(ExperimentalCoroutinesApi::class)
class AIAssistantViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var aiRepository: FakeAIRepository
    private lateinit var viewModel: AIAssistantViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        aiRepository = FakeAIRepository()
        viewModel = AIAssistantViewModel(aiRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sendMessage should update uiState and clear input`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals("", initialState.input)

            viewModel.onInputChange("Hello AI")
            assertEquals("Hello AI", awaitItem().input)

            viewModel.sendMessage()
            advanceUntilIdle()
            
            val stateAfterSend = expectMostRecentItem()
            assertEquals("", stateAfterSend.input)
            assertEquals(2, stateAfterSend.messages.size)
            assertEquals("Hello AI", stateAfterSend.messages[0].text)
            assertEquals("Mock AI Response", stateAfterSend.messages[1].text)
        }
    }
}
