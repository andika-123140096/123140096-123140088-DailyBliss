package com.dailybliss.app.data.local.entity

import com.dailybliss.app.data.local.MomentEntity
import com.dailybliss.app.domain.model.Moment
import kotlinx.datetime.Instant

fun MomentEntity.toDomain(): Moment = Moment(
    id = id,
    title = title,
    content = content,
    imageUrl = media_url,
    mood = mood,
    tags = tags?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
    createdAt = Instant.fromEpochMilliseconds(created_at),
    updatedAt = Instant.fromEpochMilliseconds(updated_at),
)

data class MomentEntityValues(
    val title: String,
    val content: String,
    val mediaUrl: String?,
    val mood: String?,
    val tags: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

fun Moment.toEntityValues(): MomentEntityValues = MomentEntityValues(
    title = title,
    content = content,
    mediaUrl = imageUrl,
    mood = mood,
    tags = if (tags.isEmpty()) null else tags.joinToString(","),
    createdAt = createdAt.toEpochMilliseconds(),
    updatedAt = updatedAt.toEpochMilliseconds(),
)

fun List<MomentEntity>.toDomainList(): List<Moment> = map { it.toDomain() }
