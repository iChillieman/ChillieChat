package com.chillieman.chilliechat.domain.usecase

import com.chillieman.chilliechat.domain.model.Event
import com.chillieman.chilliechat.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class GetEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(tag: String? = null): Flow<List<Event>> =
        eventRepository.getEvents()
            .onStart {
                try { eventRepository.refreshEvents(tag) }
                catch (_: Exception) { /* Cached data still flows */ }
            }

    suspend fun refresh(tag: String? = null) {
        eventRepository.refreshEvents(tag)
    }
}
