package com.dailybliss.app.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.dailybliss.app.data.local.BlissDatabase
import com.dailybliss.app.domain.model.Moment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MomentRepositoryImplTest {
    private lateinit var database: BlissDatabase
    private lateinit var repository: MomentRepositoryImpl

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        BlissDatabase.Schema.create(driver)
        database = BlissDatabase(driver)
        repository = MomentRepositoryImpl(database)
    }

    @Test
    fun `insert and get moment should work correctly`() = runTest {
        val moment = Moment(title = "Test", content = "Content")
        val id = repository.insertMoment(moment)
        
        val retrieved = repository.getMomentById(id).first()
        assertEquals("Test", retrieved?.title)
    }

    @Test
    fun `update moment should work correctly`() = runTest {
        val id = repository.insertMoment(Moment(title = "Old", content = "C"))
        val updated = Moment(id = id, title = "New", content = "C")
        
        repository.updateMoment(updated)
        
        val retrieved = repository.getMomentById(id).first()
        assertEquals("New", retrieved?.title)
    }

    @Test
    fun `delete moment should work correctly`() = runTest {
        val id = repository.insertMoment(Moment(title = "Delete", content = "C"))
        repository.deleteMoment(id)
        
        val retrieved = repository.getMomentById(id).first()
        assertEquals(null, retrieved)
    }

    @Test
    fun `getAllMoments should return all inserted moments`() = runTest {
        repository.insertMoment(Moment(title = "M1", content = "C1"))
        repository.insertMoment(Moment(title = "M2", content = "C2"))
        
        val all = repository.getAllMoments().first()
        assertEquals(2, all.size)
    }

    @Test
    fun `getMomentsByDateRange should return moments within range`() = runTest {
        val now = Clock.System.now().toEpochMilliseconds()
        val dayMillis = 24 * 60 * 60 * 1000L
        
        repository.insertMoment(Moment(title = "Today", content = "C", createdAt = Instant.fromEpochMilliseconds(now)))
        repository.insertMoment(Moment(title = "Yesterday", content = "C", createdAt = Instant.fromEpochMilliseconds(now - dayMillis)))
        
        val result = repository.getMomentsByDateRange(now - 1000, now + 1000).first()
        assertEquals(1, result.size)
        assertEquals("Today", result[0].title)
    }
}
