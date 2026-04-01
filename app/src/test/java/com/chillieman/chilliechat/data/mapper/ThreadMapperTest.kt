package com.chillieman.chilliechat.data.mapper

import com.chillieman.chilliechat.data.local.entity.ThreadEntity
import com.chillieman.chilliechat.data.remote.dto.ThreadDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ThreadMapperTest {

    @Test
    fun `dto toDomain maps all fields`() {
        val dto = ThreadDto(id = 10, eventId = 1, title = "Chat", tags = "general", createdAt = 500L, entryCount = 42)
        val domain = dto.toDomain()
        assertEquals(10, domain.id)
        assertEquals(1, domain.eventId)
        assertEquals("Chat", domain.title)
        assertEquals("general", domain.tags)
        assertEquals(500L, domain.createdAt)
        assertEquals(42, domain.entryCount)
    }

    @Test
    fun `dto toDomain handles null entryCount as zero`() {
        val dto = ThreadDto(id = 1, eventId = 1, title = "New", createdAt = 100L, entryCount = null)
        assertEquals(0, dto.toDomain().entryCount)
    }

    @Test
    fun `dto toDomain handles null tags`() {
        val dto = ThreadDto(id = 1, eventId = 1, title = "T", createdAt = 100L, tags = null)
        assertNull(dto.toDomain().tags)
    }

    @Test
    fun `dto toEntity maps all fields`() {
        val dto = ThreadDto(id = 10, eventId = 1, title = "Chat", tags = "general", createdAt = 500L, entryCount = 42)
        val entity = dto.toEntity()
        assertEquals(10, entity.id)
        assertEquals(1, entity.eventId)
        assertEquals("Chat", entity.title)
        assertEquals("general", entity.tags)
        assertEquals(500L, entity.createdAt)
        assertEquals(42, entity.entryCount)
    }

    @Test
    fun `entity toDomain maps all fields`() {
        val entity = ThreadEntity(id = 5, eventId = 2, title = "Thread", tags = null, createdAt = 200L, entryCount = 0)
        val domain = entity.toDomain()
        assertEquals(5, domain.id)
        assertEquals(2, domain.eventId)
        assertEquals("Thread", domain.title)
        assertNull(domain.tags)
        assertEquals(200L, domain.createdAt)
        assertEquals(0, domain.entryCount)
    }

    @Test
    fun `dto to entity to domain roundtrip preserves data`() {
        val dto = ThreadDto(id = 7, eventId = 3, title = "Round", tags = "test", createdAt = 999L, entryCount = 15)
        val domain = dto.toEntity().toDomain()
        assertEquals(dto.id, domain.id)
        assertEquals(dto.eventId, domain.eventId)
        assertEquals(dto.title, domain.title)
        assertEquals(dto.tags, domain.tags)
        assertEquals(dto.createdAt, domain.createdAt)
        assertEquals(dto.entryCount, domain.entryCount)
    }
}
