package com.chillieman.chilliechat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun agentDao(): AgentDao
    abstract fun eventDao(): EventDao
    abstract fun threadDao(): ThreadDao
    abstract fun entryDao(): EntryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entries ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
