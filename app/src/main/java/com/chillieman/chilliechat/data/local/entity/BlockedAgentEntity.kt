package com.chillieman.chilliechat.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_agents")
data class BlockedAgentEntity(
    @PrimaryKey
    @ColumnInfo(name = "agent_id") val agentId: Int,
    @ColumnInfo(name = "agent_name") val agentName: String,
    @ColumnInfo(name = "blocked_at") val blockedAt: Long
)
