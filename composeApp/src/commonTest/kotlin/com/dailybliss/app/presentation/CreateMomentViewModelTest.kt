package com.dailybliss.app.presentation

import app.cash.turbine.test
import com.dailybliss.app.core.util.FakeBackgroundAIProcessor
import com.dailybliss.app.data.repository.FakeMomentRepository
import com.dailybliss.app.domain.usecase.GetMomentByIdUseCase
import com.dailybliss.app.domain.usecase.SaveMomentUseCase
import com.dailybliss.app.presentation.screens.addnote.CreateMomentEvent
import com.dailybliss.app.presentation.screens.addnote.CreateMomentViewModel
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CreateMomentViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var momentRepository: FakeMomentRepository
    private lateinit var backgroundAIProcessor: FakeBackgroundAIProcessor
    private lateinit var fileStorage: FakeFileStorage
    private lateinit var saveMomentUseCase: SaveMomentUseCase
    private lateinit var getMomentByIdUseCase: GetMomentByIdUseCase
    private lateinit var viewModel: CreateMomentViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        momentRepository = FakeMomentRepository()
        backgroundAIProcessor = FakeBackgroundAIProcessor()
        fileStorage = FakeFileStorage()

        saveMomentUseCase = SaveMomentUseCase(momentRepository, backgroundAIProcessor)
        getMomentByIdUseCase = GetMomentByIdUseCase(momentRepository)

        viewModel = CreateMomentViewModel(
            saveMomentUseCase = saveMomentUseCase,
            getMomentByIdUseCase = getMomentByIdUseCase,
            backgroundAIProcessor = backgroundAIProcessor,
            fileStorage = fileStorage,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onTitleChange should update uiState`() = runTest {
        viewModel.onTitleChange("New Title")
        assertEquals("New Title", viewModel.uiState.value.title)
    }

    @Test
    fun `onContentChange should update uiState and extract image`() = runTest {
        val content = "Some text <img src=\"image_url.png\" /> more text"
        viewModel.onContentChange(content)
        assertEquals(content, viewModel.uiState.value.content)
        assertEquals("image_url.png", viewModel.uiState.value.imageUrl)
    }

    @Test
    fun `addImage should save images and update content with HTML`() = runTest {
        val imageBytes = "fake_image".encodeToByteArray()
        fileStorage.onSaveImageReturn = "saved_image_url.png"

        viewModel.addImage(listOf(imageBytes))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.content.contains("<img src=\"saved_image_url.png\" />"))
        assertEquals("saved_image_url.png", viewModel.uiState.value.imageUrl)
    }

    @Test
    fun `saveMoment should validate blank title and content`() = runTest {
        viewModel.saveMoment()
        assertEquals("Tuliskan sesuatu...", viewModel.uiState.value.titleError)
    }

    @Test
    fun `saveMoment should save successfully and trigger AI processing`() = runTest {
        viewModel.onTitleChange("Test Title")
        viewModel.onContentChange("Test Content")

        viewModel.events.test {
            viewModel.saveMoment()
            advanceUntilIdle()

            assertEquals(CreateMomentEvent.MomentSaved, awaitItem())

            val savedMoments = momentRepository.getAllMoments().first()
            assertEquals(1, savedMoments.size)
            assertEquals("Test Title", savedMoments[0].title)

            // Verify AI processing was triggered via UseCase
            assertTrue(backgroundAIProcessor.processMomentCalled)
        }
    }
}
