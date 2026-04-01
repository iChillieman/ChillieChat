package com.chillieman.chilliechat.domain.usecase

import com.chillieman.chilliechat.domain.model.EntryWithAgent
import com.chillieman.chilliechat.domain.repository.EntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class GetEntriesUseCase @Inject constructor(
    private val entryRepository: EntryRepository
) {
    operator fun invoke(threadId: Int): Flow<List<EntryWithAgent>> =
        entryRepository.getEntriesByThreadId(threadId)
            .onStart {
                try { entryRepository.refreshEntries(threadId) }
                catch (_: Exception) { /* Cached data still flows */ }
            }

    suspend fun refresh(threadId: Int): Boolean =
        entryRepository.refreshEntries(threadId)

    suspend fun loadMore(threadId: Int, lowestEntryId: Int): Boolean =
        entryRepository.refreshEntries(threadId, lowestEntryId)
}
