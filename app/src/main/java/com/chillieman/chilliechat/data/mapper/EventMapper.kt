package com.chillieman.chilliechat.data.mapper

import com.chillieman.chilliechat.data.local.entity.EventEntity
import com.chillieman.chilliechat.data.remote.dto.EventDto
import com.chillieman.chilliechat.data.remote.dto.EventWithThreadsDto
import com.chillieman.chilliechat.domain.model.Event
import com.chillieman.chilliechat.domain.model.EventWithThreads

fun EventDto.toDomain(): Event = Event(
    id = id,
    title = title,
    description = description,
    tags = tags,
    maxThreadAmount = maxThreadAmount,
    startTime = startTime,
    endTime = endTime
)

fun EventDto.toEntity(): EventEntity = EventEntity(
    id = id,
    title = title,
    description = description,
    tags = tags,
    maxThreadAmount = maxThreadAmount,
    startTime = startTime,
    endTime = endTime
)

fun EventEntity.toDomain(): Event = Event(
    id = id,
    title = title,
    description = description,
    tags = tags,
    maxThreadAmount = maxThreadAmount,
    startTime = startTime,
    endTime = endTime
)

fun EventWithThreadsDto.toDomain(): EventWithThreads = EventWithThreads(
    event = Event(
        id = id,
        title = title,
        description = description,
        tags = tags,
        maxThreadAmount = maxThreadAmount,
        startTime = startTime,
        endTime = endTime
    ),
    threads = threads.map { it.toDomain() }
)

fun EventWithThreadsDto.toEventEntity(): EventEntity = EventEntity(
    id = id,
    title = title,
    description = description,
    tags = tags,
    maxThreadAmount = maxThreadAmount,
    startTime = startTime,
    endTime = endTime
)
