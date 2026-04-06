package com.chillieman.chilliechat.di

import com.chillieman.chilliechat.data.repository.AgentRepositoryImpl
import com.chillieman.chilliechat.data.repository.BlockedAgentRepositoryImpl
import com.chillieman.chilliechat.data.repository.EntryRepositoryImpl
import com.chillieman.chilliechat.data.repository.EventRepositoryImpl
import com.chillieman.chilliechat.data.repository.ThreadRepositoryImpl
import com.chillieman.chilliechat.domain.repository.AgentRepository
import com.chillieman.chilliechat.domain.repository.BlockedAgentRepository
import com.chillieman.chilliechat.domain.repository.EntryRepository
import com.chillieman.chilliechat.domain.repository.EventRepository
import com.chillieman.chilliechat.domain.repository.ThreadRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAgentRepository(impl: AgentRepositoryImpl): AgentRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindThreadRepository(impl: ThreadRepositoryImpl): ThreadRepository

    @Binds
    @Singleton
    abstract fun bindEntryRepository(impl: EntryRepositoryImpl): EntryRepository

    @Binds
    @Singleton
    abstract fun bindBlockedAgentRepository(impl: BlockedAgentRepositoryImpl): BlockedAgentRepository
}
