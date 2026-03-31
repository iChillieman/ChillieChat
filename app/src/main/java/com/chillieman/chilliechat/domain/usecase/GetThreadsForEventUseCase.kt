package com.chillieman.chilliechat.domain.usecase

import com.chillieman.chilliechat.domain.model.ChatThread
import com.chillieman.chilliechat.domain.repository.ThreadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class GetThreadsForEventUseCase @Inject constructor(
    private val threadRepository: ThreadRepository
) {
    operator fun invoke(eventId: Int): Flow<List<ChatThread>> =
        threadRepository.getThreadsByEventId(eventId)
            .onStart { threadRepository.refreshThreadsForEvent(eventId) }
}
