package com.chillieman.chilliechat.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "entries",
    foreignKeys = [
        ForeignKey(
            entity = ThreadEntity::class,
            parentColumns = ["id"],
            childColumns = ["thread_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AgentEntity::class,
            parentColumns = ["id"],
            childColumns = ["agent_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("thread_id"), Index("agent_id")]
)
data class EntryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "agent_id") val agentId: Int,
    @ColumnInfo(name = "thread_id") val threadId: Int,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "tags") val tags: String? = null,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "is_deleted", defaultValue = "0") val isDeleted: Boolean = false,
    @ColumnInfo(name = "reported_at") val reportedAt: Long? = null,
    @ColumnInfo(name = "reported_count", defaultValue = "0") val reportedCount: Int = 0
)
