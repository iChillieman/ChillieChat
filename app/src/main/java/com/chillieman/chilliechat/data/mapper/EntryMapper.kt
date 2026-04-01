package com.chillieman.chilliechat.data.mapper

import com.chillieman.chilliechat.data.local.entity.EntryEntity
import com.chillieman.chilliechat.data.remote.dto.EntryDto
import com.chillieman.chilliechat.data.remote.dto.EntryWithAgentDetailsDto
import com.chillieman.chilliechat.domain.model.Entry
import com.chillieman.chilliechat.domain.model.EntryWithAgent

fun EntryDto.toDomain(): Entry = Entry(
    id = id,
    agentId = agentId,
    threadId = threadId,
    content = content,
    tags = tags,
    timestamp = timestamp
)

fun EntryDto.toEntity(): EntryEntity = EntryEntity(
    id = id,
    agentId = agentId,
    threadId = threadId,
    content = content,
    tags = tags,
    timestamp = timestamp
)

fun EntryEntity.toDomain(): Entry = Entry(
    id = id,
    agentId = agentId,
    threadId = threadId,
    content = content,
    tags = tags,
    timestamp = timestamp
)

fun EntryWithAgentDetailsDto.toDomain(): EntryWithAgent = EntryWithAgent(
    id = id,
    agentId = agentId,
    threadId = threadId,
    content = content,
    tags = tags,
    timestamp = timestamp,
    agent = agent.toDomain()
)

fun EntryWithAgentDetailsDto.toEntryEntity(): EntryEntity = EntryEntity(
    id = id,
    agentId = agentId,
    threadId = threadId,
    content = content,
    tags = tags,
    timestamp = timestamp
)
