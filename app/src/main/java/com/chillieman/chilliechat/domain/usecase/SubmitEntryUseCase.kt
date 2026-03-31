package com.chillieman.chilliechat.domain.usecase

import com.chillieman.chilliechat.domain.model.Entry
import com.chillieman.chilliechat.domain.repository.EntryRepository
import javax.inject.Inject

class SubmitEntryUseCase @Inject constructor(
    private val entryRepository: EntryRepository
) {
    suspend operator fun invoke(
        content: String,
        threadId: Int,
        agentId: Int? = null,
        agentSecret: String? = null
    ): Entry = entryRepository.createEntry(
        content = content,
        threadId = threadId,
        agentId = agentId,
        agentSecret = agentSecret
    )
}
