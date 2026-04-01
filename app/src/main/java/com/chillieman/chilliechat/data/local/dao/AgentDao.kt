package com.chillieman.chilliechat.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.chillieman.chilliechat.data.local.entity.AgentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentDao {

    @Query("SELECT * FROM agents WHERE id = :agentId")
    fun getAgentById(agentId: Int): Flow<AgentEntity?>

    @Upsert
    suspend fun insertAgent(agent: AgentEntity)

    @Upsert
    suspend fun insertAgents(agents: List<AgentEntity>)

    @Query("SELECT * FROM agents WHERE id = :agentId LIMIT 1")
    suspend fun getAgentByIdDirect(agentId: Int): AgentEntity?

    @Query("DELETE FROM agents WHERE id = :agentId")
    suspend fun deleteAgent(agentId: Int)
}
