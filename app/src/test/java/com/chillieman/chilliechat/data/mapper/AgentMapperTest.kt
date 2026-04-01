package com.chillieman.chilliechat.data.mapper

import com.chillieman.chilliechat.data.local.entity.AgentEntity
import com.chillieman.chilliechat.data.remote.dto.AgentResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AgentMapperTest {

    @Test
    fun `dto toDomain maps all fields correctly`() {
        val dto = AgentResponseDto(id = 1, name = "TestAgent", type = "PRIVATE", capabilities = "read,write")
        val domain = dto.toDomain()
        assertEquals(1, domain.id)
        assertEquals("TestAgent", domain.name)
        assertEquals("PRIVATE", domain.type)
        assertEquals("read,write", domain.capabilities)
    }

    @Test
    fun `dto toDomain handles null capabilities`() {
        val dto = AgentResponseDto(id = 2, name = "Public", type = "PUBLIC", capabilities = null)
        val domain = dto.toDomain()
        assertNull(domain.capabilities)
    }

    @Test
    fun `dto toEntity maps all fields correctly`() {
        val dto = AgentResponseDto(id = 1, name = "TestAgent", type = "PRIVATE", capabilities = "chat")
        val entity = dto.toEntity()
        assertEquals(1, entity.id)
        assertEquals("TestAgent", entity.name)
        assertEquals("PRIVATE", entity.type)
        assertEquals("chat", entity.capabilities)
    }

    @Test
    fun `entity toDomain maps all fields correctly`() {
        val entity = AgentEntity(id = 3, name = "Bot", type = "PUBLIC", capabilities = null)
        val domain = entity.toDomain()
        assertEquals(3, domain.id)
        assertEquals("Bot", domain.name)
        assertEquals("PUBLIC", domain.type)
        assertNull(domain.capabilities)
    }

    @Test
    fun `dto to entity to domain roundtrip preserves data`() {
        val dto = AgentResponseDto(id = 5, name = "RoundTrip", type = "PRIVATE", capabilities = "all")
        val domain = dto.toEntity().toDomain()
        assertEquals(dto.id, domain.id)
        assertEquals(dto.name, domain.name)
        assertEquals(dto.type, domain.type)
        assertEquals(dto.capabilities, domain.capabilities)
    }
}
