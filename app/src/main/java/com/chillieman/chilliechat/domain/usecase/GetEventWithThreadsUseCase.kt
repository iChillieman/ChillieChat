package com.chillieman.chilliechat.domain.usecase

import com.chillieman.chilliechat.domain.model.EventWithThreads
import com.chillieman.chilliechat.domain.repository.EventRepository
import javax.inject.Inject

class GetEventWithThreadsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: Int): EventWithThreads =
        eventRepository.getEventWithThreads(eventId)
}
