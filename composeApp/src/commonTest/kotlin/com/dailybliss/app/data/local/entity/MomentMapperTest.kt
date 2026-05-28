package com.dailybliss.app.data.local.entity

import com.dailybliss.app.data.local.MomentEntity
import com.dailybliss.app.domain.model.Moment
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class MomentMapperTest {
    @Test
    fun `MomentEntity toDomain should map correctly`() {
        val entity = MomentEntity(
            id = 1L,
            title = "T",
            content = "C",
            media_url = "url",
            mood = "M",
            tags = "T1,T2",
            is_pinned = 1L,
            created_at = 1000L,
            updated_at = 2000L,
        )

        val domain = entity.toDomain()

        assertEquals(1L, domain.id)
        assertEquals("T", domain.title)
        assertEquals(listOf("T1", "T2"), domain.tags)
        assertEquals(true, domain.isPinned)
        assertEquals(Instant.fromEpochMilliseconds(1000L), domain.createdAt)
        assertEquals("url", domain.imageUrl)
    }

    @Test
    fun `MomentEntity with empty tags toDomain should return empty list`() {
        val entity = MomentEntity(
            id = 1L,
            title = "T",
            content = "C",
            media_url = null,
            mood = null,
            tags = "",
            is_pinned = 0L,
            created_at = 1000L,
            updated_at = 2000L,
        )
        val domain = entity.toDomain()
        assertEquals(emptyList(), domain.tags)
    }

    @Test
    fun `Moment toEntityValues should map correctly`() {
        val domain = Moment(
            id = 2L,
            title = "T2",
            content = "C2",
            tags = listOf("A", "B"),
            isPinned = false,
            createdAt = Instant.fromEpochMilliseconds(3000L),
            updatedAt = Instant.fromEpochMilliseconds(4000L),
        )

        val values = domain.toEntityValues()

        assertEquals("T2", values.title)
        assertEquals("A,B", values.tags)
        assertEquals(0L, values.isPinned)
        assertEquals(3000L, values.createdAt)
    }

    @Test
    fun `List toDomainList should map all items`() {
        val entities = listOf(
            MomentEntity(1L, "T1", "C1", null, null, "", 0L, 0L, 0L),
            MomentEntity(2L, "T2", "C2", null, null, "", 0L, 0L, 0L),
        )
        val domains = entities.toDomainList()
        assertEquals(2, domains.size)
    }
}
