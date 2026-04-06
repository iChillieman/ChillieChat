package com.chillieman.chilliechat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.chillieman.chilliechat.data.local.dao.AgentDao
import com.chillieman.chilliechat.data.local.dao.BlockedAgentDao
import com.chillieman.chilliechat.data.local.dao.EntryDao
import com.chillieman.chilliechat.data.local.dao.EventDao
import com.chillieman.chilliechat.data.local.dao.ThreadDao
import com.chillieman.chilliechat.data.local.entity.AgentEntity
import com.chillieman.chilliechat.data.local.entity.BlockedAgentEntity
import com.chillieman.chilliechat.data.local.entity.EntryEntity
import com.chillieman.chilliechat.data.local.entity.EventEntity
import com.chillieman.chilliechat.data.local.entity.ThreadEntity

@Database(
    entities = [
        AgentEntity::class,
        EventEntity::class,
        ThreadEntity::class,
        EntryEntity::class,
        BlockedAgentEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun agentDao(): AgentDao
    abstract fun eventDao(): EventDao
    abstract fun threadDao(): ThreadDao
    abstract fun entryDao(): EntryDao
    abstract fun blockedAgentDao(): BlockedAgentDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entries ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entries ADD COLUMN reported_at INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE entries ADD COLUMN reported_count INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS blocked_agents (
                        agent_id INTEGER NOT NULL PRIMARY KEY,
                        agent_name TEXT NOT NULL,
                        blocked_at INTEGER NOT NULL
                    )"""
                )
            }
        }
    }
}
