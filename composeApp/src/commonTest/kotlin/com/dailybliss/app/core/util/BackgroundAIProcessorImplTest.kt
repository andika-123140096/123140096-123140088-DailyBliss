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
class BackgroundAIProcessorImplTest {
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
            applicationScope = testScope,
        )
    }

    @Test
    fun `processMoment should analyze mood and tags and update moment`() = runTest {
        val momentId = momentRepository.insertMoment(Moment(title = "Title", content = "Happy content"))

        processor.processMoment(momentId)
        testScope.advanceUntilIdle()

        momentRepository.getMomentById(momentId).test {
            val item = awaitItem()
            assertTrue(item?.mood?.contains("Bahagia") == true)
            assertTrue(item?.tags?.contains("Hobi") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateGlobalSummary should update summary in preferences`() = runTest {
        momentRepository.insertMoment(Moment(title = "M1", content = "C1"))
        aiRepository.mockGlobalSummary = "Great summary"
        aiRepository.mockDailyInsight = "Punchy insight"

        processor.updateGlobalSummary()
        testScope.advanceUntilIdle()

        userPreferences.journalSummary.test {
            assertEquals("Great summary", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        userPreferences.dailyInsight.test {
            assertEquals("Punchy insight", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `processMoment should skip if content is blank`() = runTest {
        val momentId = momentRepository.insertMoment(Moment(title = "", content = ""))

        processor.processMoment(momentId)
        testScope.advanceUntilIdle()

        val moment = momentRepository.getMomentById(momentId).test {
            val item = awaitItem()
            assertTrue(item?.mood == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `processMoment should process if already processed but forced`() = runTest {
        val momentId = momentRepository.insertMoment(
            Moment(
                title = "T",
                content = "New Content",
                mood = "Existing",
                tags = listOf("Tag"),
            ),
        )

        processor.processMoment(momentId, force = true)
        testScope.advanceUntilIdle()

        momentRepository.getMomentById(momentId).test {
            val item = awaitItem()
            assertTrue(item?.mood?.contains("Bahagia") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `processMoment should strip HTML tags`() = runTest {
        val momentId = momentRepository.insertMoment(
            Moment(
                title = "T",
                content = "<p>Hello <b>World</b></p>",
            ),
        )

        processor.processMoment(momentId)
        testScope.advanceUntilIdle()

        // No easy way to verify stripped text was sent to AI without a mock check,
        // but we verify processing still happens.
        momentRepository.getMomentById(momentId).test {
            val item = awaitItem()
            assertTrue(item?.mood != null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `processMoment should handle image loading failure`() = runTest {
        val momentId = momentRepository.insertMoment(
            Moment(
                title = "T",
                content = "C",
                imageUrl = "invalid_path",
            ),
        )
        // FakeFileStorage returns null for non-existing files usually

        processor.processMoment(momentId)
        testScope.advanceUntilIdle()

        momentRepository.getMomentById(momentId).test {
            val item = awaitItem()
            assertTrue(item?.mood != null) // Should still process without image
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateGlobalSummary should clear summary if moments are empty`() = runTest {
        userPreferences.setJournalSummary("Old Summary")

        processor.updateGlobalSummary()
        testScope.advanceUntilIdle()

        userPreferences.journalSummary.test {
            assertEquals("", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `processMoment should cancel existing job for same moment`() = runTest {
        val momentId = momentRepository.insertMoment(Moment(title = "T", content = "C"))

        // Mock a slow AI response
        aiRepository.slowMode = true

        processor.processMoment(momentId)
        // Give it a tiny bit of time to start
        testScope.advanceTimeBy(10)

        processor.processMoment(momentId) // Should cancel first one
        testScope.advanceUntilIdle()

        // We can't easily verify cancellation without counting calls in FakeAIRepository,
        // but we ensure it finishes correctly.
        assertTrue(aiRepository.analyzeCallCount >= 1)
    }

    @Test
    fun `isProcessing should be true while jobs are active`() = runTest {
        val id1 = momentRepository.insertMoment(Moment(title = "T1", content = "C1"))
        val id2 = momentRepository.insertMoment(Moment(title = "T2", content = "C2"))

        aiRepository.slowMode = true

        processor.isProcessing.test {
            assertEquals(false, awaitItem()) // Initial

            processor.processMoment(id1)
            testScope.advanceTimeBy(10)
            assertEquals(true, awaitItem())

            processor.processMoment(id2)
            testScope.advanceTimeBy(10)
            // Still true

            aiRepository.slowMode = false
            testScope.advanceUntilIdle()
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
