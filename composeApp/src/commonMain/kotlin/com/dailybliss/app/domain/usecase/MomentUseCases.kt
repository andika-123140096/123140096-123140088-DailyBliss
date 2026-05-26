package com.dailybliss.app.domain.usecase

import com.dailybliss.app.core.util.BackgroundAIProcessor
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.domain.repository.MomentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.toInstant

enum class MomentSortBy(val displayName: String) {
    TITLE_ASC("Judul (A-Z)"),
    TITLE_DESC("Judul (Z-A)"),
    CREATED_ASC("Dibuat (Lama)"),
    CREATED_DESC("Dibuat (Baru)"),
    UPDATED_ASC("Diupdate (Lama)"),
    UPDATED_DESC("Diupdate (Baru)"),
}

class GetAllMomentsUseCase(private val repository: MomentRepository) {
    operator fun invoke(): Flow<List<Moment>> = repository.getAllMoments()
}

class SaveMomentUseCase(
    private val repository: MomentRepository,
    private val aiProcessor: BackgroundAIProcessor,
) {
    suspend operator fun invoke(moment: Moment): Long {
        val id = if (moment.id == 0L) {
            repository.insertMoment(moment)
        } else {
            repository.updateMoment(moment)
            moment.id
        }
        aiProcessor.updateGlobalSummary()
        return id
    }
}

class DeleteMomentUseCase(
    private val repository: MomentRepository,
    private val aiProcessor: BackgroundAIProcessor,
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteMoment(id)
        aiProcessor.updateGlobalSummary()
    }
}

class GetMomentByIdUseCase(private val repository: MomentRepository) {
    operator fun invoke(id: Long): Flow<Moment?> = repository.getMomentById(id)
}

class GetMomentsForDateUseCase(private val repository: MomentRepository) {
    operator fun invoke(date: kotlinx.datetime.LocalDate): Flow<List<Moment>> {
        val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
        val startOfDay = kotlinx.datetime.LocalDateTime(date.year, date.month, date.dayOfMonth, 0, 0, 0, 0)
            .toInstant(tz).toEpochMilliseconds()
        val endOfDay = kotlinx.datetime.LocalDateTime(date.year, date.month, date.dayOfMonth, 23, 59, 59, 999_999_999)
            .toInstant(tz).toEpochMilliseconds()
        return repository.getMomentsByDateRange(startOfDay, endOfDay)
    }
}
