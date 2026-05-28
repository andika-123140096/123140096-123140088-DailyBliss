package com.dailybliss.app.domain.usecase

import com.dailybliss.app.core.util.FakeBackgroundAIProcessor
import com.dailybliss.app.data.repository.FakeMomentRepository
import com.dailybliss.app.domain.model.Moment
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MomentOperationsTest {
    private lateinit var repository: FakeMomentRepository
    private lateinit var aiProcessor: FakeBackgroundAIProcessor
    private lateinit var saveUseCase: SaveMomentUseCase
    private lateinit var deleteUseCase: DeleteMomentUseCase

    @BeforeTest
    fun setup() {
        repository = FakeMomentRepository()
        aiProcessor = FakeBackgroundAIProcessor()
        saveUseCase = SaveMomentUseCase(repository, aiProcessor)
        deleteUseCase = DeleteMomentUseCase(repository, aiProcessor)
    }

    @Test
    fun `SaveMomentUseCase should trigger AI processing after save`() = runTest {
        val moment = Moment(title = "Test", content = "Content")
        val id = saveUseCase(moment)
        
        assertTrue(id > 0)
        assertEquals(id, aiProcessor.lastProcessedMomentId)
        assertTrue(aiProcessor.processMomentCalled)
    }

    @Test
    fun `DeleteMomentUseCase should trigger global summary update after delete`() = runTest {
        val id = repository.insertMoment(Moment(title = "T", content = "C"))
        
        deleteUseCase(id)
        
        assertTrue(repository.moments.value.isEmpty())
        assertTrue(aiProcessor.updateGlobalSummaryCalled)
    }
}
