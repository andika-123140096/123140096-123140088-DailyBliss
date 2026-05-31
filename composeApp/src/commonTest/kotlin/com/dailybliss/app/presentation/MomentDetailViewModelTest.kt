package com.dailybliss.app.presentation

import com.dailybliss.app.core.util.FakeBackgroundAIProcessor
import com.dailybliss.app.data.repository.FakeMomentRepository
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.domain.usecase.DeleteMomentUseCase
import com.dailybliss.app.domain.usecase.GetMomentByIdUseCase
import com.dailybliss.app.domain.usecase.SaveMomentUseCase
import com.dailybliss.app.presentation.screens.detail.MomentDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MomentDetailViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var momentRepository: FakeMomentRepository
    private lateinit var backgroundAIProcessor: FakeBackgroundAIProcessor
    private lateinit var fileStorage: FakeFileStorage
    private lateinit var getMomentByIdUseCase: GetMomentByIdUseCase
    private lateinit var deleteMomentUseCase: DeleteMomentUseCase
    private lateinit var saveMomentUseCase: SaveMomentUseCase
    private lateinit var viewModel: MomentDetailViewModel

    private val testMoment = Moment(
        id = 1L,
        title = "Original Title",
        content = "Original Content",
    )

    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(testDispatcher)
        momentRepository = FakeMomentRepository()
        backgroundAIProcessor = FakeBackgroundAIProcessor()
        fileStorage = FakeFileStorage()

        momentRepository.insertMoment(testMoment)

        getMomentByIdUseCase = GetMomentByIdUseCase(momentRepository)
        deleteMomentUseCase = DeleteMomentUseCase(momentRepository, backgroundAIProcessor)
        saveMomentUseCase = SaveMomentUseCase(momentRepository, backgroundAIProcessor)

        viewModel = MomentDetailViewModel(
            momentId = 1L,
            getMomentByIdUseCase = getMomentByIdUseCase,
            deleteMomentUseCase = deleteMomentUseCase,
            saveMomentUseCase = saveMomentUseCase,
            backgroundAIProcessor = backgroundAIProcessor,
            fileStorage = fileStorage,
        )
        advanceUntilIdle()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadMoment should load correctly from repository`() = runTest {
        assertEquals(testMoment.title, viewModel.uiState.value.moment?.title)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `updateTitle should update state and set dirty`() = runTest {
        viewModel.updateTitle("Updated Title")
        assertEquals("Updated Title", viewModel.uiState.value.moment?.title)
        assertTrue(viewModel.uiState.value.isDirty)
    }

    @Test
    fun `updateContent should update state, extract image and set dirty`() = runTest {
        val newContent = "New content <img src=\"new_image.png\" />"
        viewModel.updateContent(newContent)
        assertEquals(newContent, viewModel.uiState.value.moment?.content)
        assertEquals("new_image.png", viewModel.uiState.value.moment?.imageUrl)
        assertTrue(viewModel.uiState.value.isDirty)
    }

    @Test
    fun `saveChanges should save updated moment and reset dirty`() = runTest {
        viewModel.updateTitle("Updated Title")
        viewModel.saveChanges()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isDirty)
        val saved = momentRepository.getAllMoments().first().find { it.id == 1L }
        assertEquals("Updated Title", saved?.title)
        // Verify AI processing was triggered via UseCase
        assertTrue(backgroundAIProcessor.processMomentCalled)
    }

    @Test
    fun `deleteMoment should remove moment from repository`() = runTest {
        var deletedCalled = false
        viewModel.deleteMoment { deletedCalled = true }
        advanceUntilIdle()

        assertTrue(deletedCalled)
        assertTrue(momentRepository.getAllMoments().first().isEmpty())
        assertTrue(backgroundAIProcessor.updateGlobalSummaryCalled)
    }
}
