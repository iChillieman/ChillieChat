package com.chillieman.chilliechat.data.mapper

import com.chillieman.chilliechat.data.local.entity.EntryEntity
import com.chillieman.chilliechat.data.remote.dto.AgentResponseDto
import com.chillieman.chilliechat.data.remote.dto.EntryDto
import com.chillieman.chilliechat.data.remote.dto.EntryWithAgentDetailsDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EntryMapperTest {

    private val entryDto = EntryDto(
        id = 100, agentId = 1, threadId = 10,
        content = "Hello world", tags = "greeting", timestamp = 1234567890L
    )

    @Test
    fun `entryDto toDomain maps all fields`() {
        val domain = entryDto.toDomain()
        assertEquals(100, domain.id)
        assertEquals(1, domain.agentId)
        assertEquals(10, domain.threadId)
        assertEquals("Hello world", domain.content)
        assertEquals("greeting", domain.tags)
        assertEquals(1234567890L, domain.timestamp)
    }

    @Test
    fun `entryDto toDomain handles null tags`() {
        val dto = entryDto.copy(tags = null)
        assertNull(dto.toDomain().tags)
    }

    @Test
    fun `entryDto toEntity maps all fields`() {
        val entity = entryDto.toEntity()
        assertEquals(100, entity.id)
        assertEquals(1, entity.agentId)
        assertEquals(10, entity.threadId)
        assertEquals("Hello world", entity.content)
        assertEquals("greeting", entity.tags)
        assertEquals(1234567890L, entity.timestamp)
    }

    @Test
    fun `entryEntity toDomain maps all fields`() {
        val entity = EntryEntity(
            id = 50, agentId = 2, threadId = 5,
            content = "test", tags = null, timestamp = 999L
        )
        val domain = entity.toDomain()
        assertEquals(50, domain.id)
        assertEquals(2, domain.agentId)
        assertEquals("test", domain.content)
        assertNull(domain.tags)
    }

    @Test
    fun `entryWithAgentDetailsDto toDomain maps entry and agent`() {
        val dto = EntryWithAgentDetailsDto(
            id = 200, agentId = 3, threadId = 20,
            content = "Agent message", tags = null, timestamp = 5000L,
            agent = AgentResponseDto(id = 3, name = "Bot", type = "PUBLIC", capabilities = null)
        )
        val domain = dto.toDomain()
        assertEquals(200, domain.id)
        assertEquals(3, domain.agentId)
        assertEquals("Agent message", domain.content)
        assertEquals("Bot", domain.agent.name)
        assertEquals("PUBLIC", domain.agent.type)
    }

    @Test
    fun `entryWithAgentDetailsDto toEntryEntity drops agent data`() {
        val dto = EntryWithAgentDetailsDto(
            id = 200, agentId = 3, threadId = 20,
            content = "msg", tags = "tag", timestamp = 5000L,
            agent = AgentResponseDto(id = 3, name = "Bot", type = "PUBLIC", capabilities = null)
        )
        val entity = dto.toEntryEntity()
        assertEquals(200, entity.id)
        assertEquals(3, entity.agentId)
        assertEquals(20, entity.threadId)
        assertEquals("msg", entity.content)
        assertEquals("tag", entity.tags)
        assertEquals(5000L, entity.timestamp)
    }

    @Test
    fun `entryDto to entity to domain roundtrip preserves data`() {
        val domain = entryDto.toEntity().toDomain()
        assertEquals(entryDto.id, domain.id)
        assertEquals(entryDto.agentId, domain.agentId)
        assertEquals(entryDto.threadId, domain.threadId)
        assertEquals(entryDto.content, domain.content)
        assertEquals(entryDto.tags, domain.tags)
        assertEquals(entryDto.timestamp, domain.timestamp)
    }
}
