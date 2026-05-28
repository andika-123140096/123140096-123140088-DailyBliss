package com.dailybliss.app.presentation

import app.cash.turbine.test
import com.dailybliss.app.data.repository.FakeNewsRepository
import com.dailybliss.app.presentation.screens.news.NewsViewModel
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
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var newsRepository: FakeNewsRepository
    private lateinit var viewModel: NewsViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        newsRepository = FakeNewsRepository()
        viewModel = NewsViewModel(newsRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNewsData should update uiState with news, weather and currency`() = runTest {
        viewModel.loadNewsData()
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertNotNull(state.weather)
        assertEquals(1, state.news.size)
        assertNotNull(state.currencyRates)
    }

    private fun assertTrue(actual: Boolean) {
        assertEquals(true, actual)
    }
}
