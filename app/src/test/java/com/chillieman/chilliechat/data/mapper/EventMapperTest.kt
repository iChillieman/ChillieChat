package com.chillieman.chilliechat.data.mapper

import com.chillieman.chilliechat.data.local.entity.EventEntity
import com.chillieman.chilliechat.data.remote.dto.EventDto
import com.chillieman.chilliechat.data.remote.dto.EventWithThreadsDto
import com.chillieman.chilliechat.data.remote.dto.ThreadDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EventMapperTest {

    private val fullDto = EventDto(
        id = 1, title = "Hackathon", description = "Build stuff",
        tags = "dev,fun", maxThreadAmount = 10,
        startTime = 1711234567L, endTime = 1711239999L
    )

    @Test
    fun `dto toDomain maps all fields`() {
        val domain = fullDto.toDomain()
        assertEquals(1, domain.id)
        assertEquals("Hackathon", domain.title)
        assertEquals("Build stuff", domain.description)
        assertEquals("dev,fun", domain.tags)
        assertEquals(10, domain.maxThreadAmount)
        assertEquals(1711234567L, domain.startTime)
        assertEquals(1711239999L, domain.endTime)
    }

    @Test
    fun `dto toDomain handles all nullable fields as null`() {
        val dto = EventDto(id = 2, title = "Minimal", startTime = 100L)
        val domain = dto.toDomain()
        assertNull(domain.description)
        assertNull(domain.tags)
        assertNull(domain.maxThreadAmount)
        assertNull(domain.endTime)
    }

    @Test
    fun `dto toEntity maps all fields`() {
        val entity = fullDto.toEntity()
        assertEquals(1, entity.id)
        assertEquals("Hackathon", entity.title)
        assertEquals("Build stuff", entity.description)
        assertEquals("dev,fun", entity.tags)
        assertEquals(10, entity.maxThreadAmount)
        assertEquals(1711234567L, entity.startTime)
        assertEquals(1711239999L, entity.endTime)
    }

    @Test
    fun `entity toDomain maps all fields`() {
        val entity = EventEntity(
            id = 3, title = "Test", description = null, tags = null,
            maxThreadAmount = null, startTime = 500L, endTime = null
        )
        val domain = entity.toDomain()
        assertEquals(3, domain.id)
        assertEquals("Test", domain.title)
        assertNull(domain.description)
        assertEquals(500L, domain.startTime)
    }

    @Test
    fun `eventWithThreadsDto toDomain maps event and threads`() {
        val dto = EventWithThreadsDto(
            id = 1, title = "Event", description = null, tags = null,
            maxThreadAmount = null, startTime = 100L, endTime = null,
            threads = listOf(
                ThreadDto(id = 10, eventId = 1, title = "Thread A", createdAt = 200L, entryCount = 5),
                ThreadDto(id = 11, eventId = 1, title = "Thread B", createdAt = 300L, entryCount = null)
            )
        )
        val domain = dto.toDomain()
        assertEquals(1, domain.event.id)
        assertEquals("Event", domain.event.title)
        assertEquals(2, domain.threads.size)
        assertEquals("Thread A", domain.threads[0].title)
        assertEquals(5, domain.threads[0].entryCount)
        assertEquals(0, domain.threads[1].entryCount) // null maps to 0
    }

    @Test
    fun `eventWithThreadsDto toEventEntity maps correctly`() {
        val dto = EventWithThreadsDto(
            id = 5, title = "WithThreads", description = "desc", tags = "tag",
            maxThreadAmount = 3, startTime = 999L, endTime = 1000L,
            threads = emptyList()
        )
        val entity = dto.toEventEntity()
        assertEquals(5, entity.id)
        assertEquals("WithThreads", entity.title)
        assertEquals("desc", entity.description)
    }

    @Test
    fun `dto to entity to domain roundtrip preserves data`() {
        val domain = fullDto.toEntity().toDomain()
        assertEquals(fullDto.id, domain.id)
        assertEquals(fullDto.title, domain.title)
        assertEquals(fullDto.description, domain.description)
        assertEquals(fullDto.tags, domain.tags)
        assertEquals(fullDto.maxThreadAmount, domain.maxThreadAmount)
        assertEquals(fullDto.startTime, domain.startTime)
        assertEquals(fullDto.endTime, domain.endTime)
    }
}
