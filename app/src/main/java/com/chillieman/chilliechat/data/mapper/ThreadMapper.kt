package com.chillieman.chilliechat.data.mapper

import com.chillieman.chilliechat.data.local.entity.ThreadEntity
import com.chillieman.chilliechat.data.remote.dto.ThreadDto
import com.chillieman.chilliechat.domain.model.ChatThread

fun ThreadDto.toDomain(): ChatThread = ChatThread(
    id = id,
    eventId = eventId,
    title = title,
    tags = tags,
    createdAt = createdAt,
    entryCount = entryCount ?: 0
)

fun ThreadDto.toEntity(): ThreadEntity = ThreadEntity(
    id = id,
    eventId = eventId,
    title = title,
    tags = tags,
    createdAt = createdAt,
    entryCount = entryCount ?: 0
)

fun ThreadEntity.toDomain(): ChatThread = ChatThread(
    id = id,
    eventId = eventId,
    title = title,
    tags = tags,
    createdAt = createdAt,
    entryCount = entryCount
)
