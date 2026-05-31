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
import kotlinx.coroutines.test.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MomentDetailViewModelDeepTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var momentRepository: FakeMomentRepository
    private lateinit var backgroundAIProcessor: FakeBackgroundAIProcessor
    private lateinit var fileStorage: FakeFileStorage
    private lateinit var viewModel: MomentDetailViewModel

    private val initialMoment = Moment(id = 1, title = "Original", content = "Content")

    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(testDispatcher)
        momentRepository = FakeMomentRepository()
        momentRepository.insertMoment(initialMoment)

        backgroundAIProcessor = FakeBackgroundAIProcessor()
        fileStorage = FakeFileStorage()

        viewModel = MomentDetailViewModel(
            momentId = 1,
            getMomentByIdUseCase = GetMomentByIdUseCase(momentRepository),
            deleteMomentUseCase = DeleteMomentUseCase(momentRepository, backgroundAIProcessor),
            saveMomentUseCase = SaveMomentUseCase(momentRepository, backgroundAIProcessor),
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
    fun `checkIfDirty should be true if title changed`() = runTest {
        viewModel.updateTitle("Changed")
        assertTrue(viewModel.uiState.value.isDirty)

        viewModel.updateTitle("Original")
        assertFalse(viewModel.uiState.value.isDirty)
    }

    @Test
    fun `addImage should insert at correct html index`() = runTest {
        viewModel.updateContent("Hello World") // index 5 is space
        advanceUntilIdle()

        val imageBytes = "img".encodeToByteArray()
        fileStorage.onSaveImageReturn = "url"

        viewModel.addImage(listOf(imageBytes), insertionIndex = 5)
        advanceUntilIdle()

        val newContent = viewModel.uiState.value.moment?.content
        assertTrue(newContent?.startsWith("Hello") == true)
        assertTrue(newContent?.contains("<img src=\"url\" />") == true)
        assertTrue(newContent?.endsWith("World") == true)
    }

    @Test
    fun `addImage at the end if index is -1`() = runTest {
        viewModel.updateContent("Text")
        advanceUntilIdle()

        fileStorage.onSaveImageReturn = "url"
        viewModel.addImage(listOf(byteArrayOf(1)), insertionIndex = -1)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.moment?.content?.endsWith("<img src=\"url\" /></div>") == true)
    }

    @Test
    fun `loadMoment should handle missing moment`() = runTest {
        val missingVm = MomentDetailViewModel(
            momentId = 999,
            getMomentByIdUseCase = GetMomentByIdUseCase(momentRepository),
            deleteMomentUseCase = DeleteMomentUseCase(momentRepository, backgroundAIProcessor),
            saveMomentUseCase = SaveMomentUseCase(momentRepository, backgroundAIProcessor),
            backgroundAIProcessor = backgroundAIProcessor,
            fileStorage = fileStorage,
        )
        advanceUntilIdle()
        assertEquals("Momen tidak ditemukan", missingVm.uiState.value.error)
    }
}
