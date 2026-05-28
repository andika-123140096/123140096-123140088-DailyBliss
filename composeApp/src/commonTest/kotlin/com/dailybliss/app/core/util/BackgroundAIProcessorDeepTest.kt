package com.dailybliss.app.core.util

import app.cash.turbine.test
import com.dailybliss.app.data.local.datastore.FakeUserPreferences
import com.dailybliss.app.data.repository.FakeAIRepository
import com.dailybliss.app.data.repository.FakeMomentRepository
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.presentation.FakeFileStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BackgroundAIProcessorDeepTest {
    private val testScope = TestScope()
    private lateinit var aiRepository: FakeAIRepository
    private lateinit var momentRepository: FakeMomentRepository
    private lateinit var userPreferences: FakeUserPreferences
    private lateinit var fileStorage: FakeFileStorage
    private lateinit var processor: BackgroundAIProcessorImpl

    @BeforeTest
    fun setup() {
        aiRepository = FakeAIRepository()
        momentRepository = FakeMomentRepository()
        userPreferences = FakeUserPreferences()
        fileStorage = FakeFileStorage()
        
        processor = BackgroundAIProcessorImpl(
            aiRepository = aiRepository,
            momentRepository = momentRepository,
            userPreferences = userPreferences,
            fileStorage = fileStorage,
            applicationScope = testScope
        )
    }

    @Test
    fun `processMoment should skip if content is HTML-only`() = runTest {
        val momentId = momentRepository.insertMoment(Moment(title = "T", content = "<div></div>"))
        processor.processMoment(momentId)
        testScope.advanceUntilIdle()
        assertEquals(0, aiRepository.analyzeCallCount)
    }

    @Test
    fun `processMoment should skip if already processed and not forced`() = runTest {
        val momentId = momentRepository.insertMoment(Moment(title = "T", content = "C", mood = "M", tags = listOf("Tag")))
        processor.processMoment(momentId, force = false)
        testScope.advanceUntilIdle()
        assertEquals(0, aiRepository.analyzeCallCount)
    }

    @Test
    fun `processMoment should process if forced even if already processed`() = runTest {
        val momentId = momentRepository.insertMoment(Moment(title = "T", content = "C", mood = "M", tags = listOf("Tag")))
        processor.processMoment(momentId, force = true)
        testScope.advanceUntilIdle()
        assertEquals(1, aiRepository.analyzeCallCount)
    }

    @Test
    fun `updateGlobalSummary should handle empty repository`() = runTest {
        processor.updateGlobalSummary()
        testScope.advanceUntilIdle()
        assertEquals("", userPreferences.journalSummary.value)
    }

    @Test
    fun `updateGlobalSummary should handle blank combined text`() = runTest {
        momentRepository.insertMoment(Moment(title = "", content = "   "))
        processor.updateGlobalSummary()
        testScope.advanceUntilIdle()
        // verify no AI calls or no summary update
    }
}
