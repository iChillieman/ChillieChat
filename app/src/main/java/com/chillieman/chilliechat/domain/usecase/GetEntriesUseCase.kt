package com.chillieman.chilliechat.domain.usecase

import com.chillieman.chilliechat.domain.model.Entry
import com.chillieman.chilliechat.domain.repository.EntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class GetEntriesUseCase @Inject constructor(
    private val entryRepository: EntryRepository
) {
    operator fun invoke(threadId: Int): Flow<List<Entry>> =
        entryRepository.getEntriesByThreadId(threadId)
            .onStart { entryRepository.refreshEntries(threadId) }
}
