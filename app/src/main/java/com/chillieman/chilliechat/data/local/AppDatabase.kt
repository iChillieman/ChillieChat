package com.chillieman.chilliechat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chillieman.chilliechat.data.local.dao.AgentDao
import com.chillieman.chilliechat.data.local.dao.EntryDao
import com.chillieman.chilliechat.data.local.dao.EventDao
import com.chillieman.chilliechat.data.local.dao.ThreadDao
import com.chillieman.chilliechat.data.local.entity.AgentEntity
import com.chillieman.chilliechat.data.local.entity.EntryEntity
import com.chillieman.chilliechat.data.local.entity.EventEntity
import com.chillieman.chilliechat.data.local.entity.ThreadEntity

@Database(
    entities = [
        AgentEntity::class,
        EventEntity::class,
        ThreadEntity::class,
        EntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun agentDao(): AgentDao
    abstract fun eventDao(): EventDao
    abstract fun threadDao(): ThreadDao
    abstract fun entryDao(): EntryDao
}
