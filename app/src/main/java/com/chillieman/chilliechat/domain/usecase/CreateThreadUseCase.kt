package com.chillieman.chilliechat.domain.usecase

import com.chillieman.chilliechat.domain.model.ChatThread
import com.chillieman.chilliechat.domain.repository.ThreadRepository
import javax.inject.Inject

class CreateThreadUseCase @Inject constructor(
    private val threadRepository: ThreadRepository
) {
    suspend operator fun invoke(
        title: String,
        eventId: Int,
        tags: String? = null
    ): ChatThread = threadRepository.createThread(title, eventId, tags)
}
