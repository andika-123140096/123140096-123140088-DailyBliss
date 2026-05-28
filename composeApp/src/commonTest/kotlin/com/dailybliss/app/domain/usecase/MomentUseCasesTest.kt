package com.dailybliss.app.domain.usecase

import app.cash.turbine.test
import com.dailybliss.app.core.util.FakeBackgroundAIProcessor
import com.dailybliss.app.data.repository.FakeMomentRepository
import com.dailybliss.app.domain.model.Moment
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toInstant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MomentUseCasesTest {
    private lateinit var repository: FakeMomentRepository
    private lateinit var aiProcessor: FakeBackgroundAIProcessor
    private lateinit var getAllMomentsUseCase: GetAllMomentsUseCase
    private lateinit var getMomentByIdUseCase: GetMomentByIdUseCase
    private lateinit var saveMomentUseCase: SaveMomentUseCase
    private lateinit var deleteMomentUseCase: DeleteMomentUseCase
    private lateinit var getMomentsForDateUseCase: GetMomentsForDateUseCase

    @BeforeTest
    fun setup() {
        repository = FakeMomentRepository()
        aiProcessor = FakeBackgroundAIProcessor()
        getAllMomentsUseCase = GetAllMomentsUseCase(repository)
        getMomentByIdUseCase = GetMomentByIdUseCase(repository)
        saveMomentUseCase = SaveMomentUseCase(repository, aiProcessor)
        deleteMomentUseCase = DeleteMomentUseCase(repository, aiProcessor)
        getMomentsForDateUseCase = GetMomentsForDateUseCase(repository)
    }

    @Test
    fun `GetAllMomentsUseCase should return moments from repository`() = runTest {
        val moment = Moment(id = 1, title = "Test", content = "Content")
        repository.insertMoment(moment)

        getAllMomentsUseCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Test", result[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GetMomentByIdUseCase should return correct moment`() = runTest {
        val id = repository.insertMoment(Moment(title = "Target", content = "Content"))
        
        getMomentByIdUseCase(id).test {
            val result = awaitItem()
            assertEquals("Target", result?.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SaveMomentUseCase should insert new moment and update AI summary`() = runTest {
        val moment = Moment(title = "New", content = "Content")
        
        val id = saveMomentUseCase(moment)
        
        assertTrue(id > 0)
        assertTrue(aiProcessor.updateGlobalSummaryCalled)
        repository.getMomentById(id).test {
            assertEquals("New", awaitItem()?.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SaveMomentUseCase should update existing moment and update AI summary`() = runTest {
        val id = repository.insertMoment(Moment(title = "Old", content = "Content"))
        val moment = Moment(id = id, title = "Updated", content = "Content")
        
        saveMomentUseCase(moment)
        
        assertTrue(aiProcessor.updateGlobalSummaryCalled)
        repository.getMomentById(id).test {
            assertEquals("Updated", awaitItem()?.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DeleteMomentUseCase should remove moment and update AI summary`() = runTest {
        val id = repository.insertMoment(Moment(title = "Delete", content = "Content"))
        
        deleteMomentUseCase(id)
        
        assertTrue(aiProcessor.updateGlobalSummaryCalled)
        repository.getMomentById(id).test {
            assertEquals(null, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GetMomentsForDateUseCase should return moments within date range`() = runTest {
        val date = LocalDate(2023, 10, 27)
        val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
        val createdAt = kotlinx.datetime.LocalDateTime(2023, 10, 27, 12, 0)
            .toInstant(tz)
        
        repository.insertMoment(Moment(title = "On Date", content = "Content", createdAt = createdAt))
        repository.insertMoment(Moment(title = "Other Date", content = "Content", createdAt = Clock.System.now()))

        getMomentsForDateUseCase(date).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("On Date", result[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
