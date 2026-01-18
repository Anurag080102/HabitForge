package com.habitforge.app.di

import android.content.Context
import androidx.room.Room
import com.habitforge.app.data.local.HabitForgeDatabase
import com.habitforge.app.data.local.dao.HabitDao
import com.habitforge.app.data.local.dao.HabitCompletionDao
import com.habitforge.app.data.local.dao.JournalDao

import com.habitforge.app.data.local.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HabitForgeDatabase {
        return Room.databaseBuilder(
            context,
            HabitForgeDatabase::class.java,
            "habitforge_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideHabitDao(database: HabitForgeDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    fun provideHabitCompletionDao(database: HabitForgeDatabase): HabitCompletionDao {
        return database.habitCompletionDao()
    }

    @Provides
    fun provideJournalDao(database: HabitForgeDatabase): JournalDao {
        return database.journalDao()
    }

    @Provides
    fun provideUserProfileDao(database: HabitForgeDatabase): UserProfileDao {
        return database.userProfileDao()
    }
}
