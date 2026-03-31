package com.chillieman.chilliechat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chillieman.chilliechat.data.local.entity.AgentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentDao {

    @Query("SELECT * FROM agents WHERE id = :agentId")
    fun getAgentById(agentId: Int): Flow<AgentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: AgentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgents(agents: List<AgentEntity>)

    @Query("DELETE FROM agents WHERE id = :agentId")
    suspend fun deleteAgent(agentId: Int)
}
