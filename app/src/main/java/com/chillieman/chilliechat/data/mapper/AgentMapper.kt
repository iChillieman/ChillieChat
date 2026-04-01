package com.chillieman.chilliechat.data.mapper

import com.chillieman.chilliechat.data.local.entity.AgentEntity
import com.chillieman.chilliechat.data.remote.dto.AgentResponseDto
import com.chillieman.chilliechat.domain.model.Agent

fun AgentResponseDto.toDomain(): Agent = Agent(
    id = id,
    name = name,
    type = type,
    capabilities = capabilities
)

fun AgentResponseDto.toEntity(): AgentEntity = AgentEntity(
    id = id,
    name = name,
    type = type,
    capabilities = capabilities
)

fun AgentEntity.toDomain(): Agent = Agent(
    id = id,
    name = name,
    type = type,
    capabilities = capabilities
)
