package com.dailybliss.app.domain.usecase

import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.domain.repository.MomentRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAllMomentsUseCaseTest {

    private val repository = mockk<MomentRepository>()
    private val useCase = GetAllMomentsUseCase(repository)

    @Test
    fun `invoke should return moments from repository`() = runTest {
        val expectedMoments = listOf(
            Moment(id = 1, title = "Test Moment", content = "Test Content"),
        )
        every { repository.getAllMoments() } returns flowOf(expectedMoments)

        useCase().collect { actualMoments ->
            assertEquals(expectedMoments, actualMoments)
        }
    }
}
