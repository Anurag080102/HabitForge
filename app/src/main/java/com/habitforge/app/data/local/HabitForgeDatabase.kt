package com.habitforge.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.habitforge.app.data.local.dao.HabitDao
import com.habitforge.app.data.local.dao.HabitCompletionDao
import com.habitforge.app.data.local.dao.JournalDao
import com.habitforge.app.data.local.entity.HabitEntity
import com.habitforge.app.data.local.entity.HabitCompletionEntity
import com.habitforge.app.data.local.entity.JournalEntryEntity
import com.habitforge.app.data.local.entity.UserProfileEntity
import com.habitforge.app.data.local.dao.UserProfileDao

@Database(
    entities = [
        HabitEntity::class,
        HabitCompletionEntity::class,
        JournalEntryEntity::class,
        UserProfileEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class HabitForgeDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun journalDao(): JournalDao
    abstract fun userProfileDao(): UserProfileDao
}

