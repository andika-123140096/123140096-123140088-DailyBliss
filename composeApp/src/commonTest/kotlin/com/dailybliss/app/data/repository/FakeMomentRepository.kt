package com.dailybliss.app.data.repository

import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.domain.repository.MomentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Fake Repository untuk Testing
 *
 * In-memory implementation yang tidak bergantung pada database.
 * Digunakan untuk unit testing tanpa side effects.
 */
class FakeMomentRepository : MomentRepository {
    internal val moments = MutableStateFlow<List<Moment>>(emptyList())
    private var nextId = 1L

    override fun getAllMoments(): Flow<List<Moment>> = moments

    override fun getMomentById(id: Long): Flow<Moment?> = moments.map { list -> list.find { it.id == id } }

    override suspend fun insertMoment(moment: Moment): Long {
        val id = nextId++
        val newMoment = moment.copy(id = id)
        moments.update { it + newMoment }
        return id
    }

    override suspend fun updateMoment(moment: Moment) {
        moments.update { list ->
            list.map { if (it.id == moment.id) moment else it }
        }
    }

    override suspend fun deleteMoment(id: Long) {
        moments.update { list -> list.filter { it.id != id } }
    }

    override fun getMomentsByDateRange(start: Long, end: Long): Flow<List<Moment>> = moments.map { list ->
        list.filter { it.createdAt.toEpochMilliseconds() in start..end }
    }
}
