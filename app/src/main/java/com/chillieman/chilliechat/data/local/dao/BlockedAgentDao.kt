package com.chillieman.chilliechat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chillieman.chilliechat.data.local.entity.BlockedAgentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAgentDao {

    @Query("SELECT * FROM blocked_agents ORDER BY blocked_at DESC")
    fun getAllBlockedAgents(): Flow<List<BlockedAgentEntity>>

    @Query("SELECT agent_id FROM blocked_agents")
    fun getBlockedAgentIds(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun blockAgent(entity: BlockedAgentEntity)

    @Query("DELETE FROM blocked_agents WHERE agent_id = :agentId")
    suspend fun unblockAgent(agentId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_agents WHERE agent_id = :agentId)")
    suspend fun isBlocked(agentId: Int): Boolean
}
