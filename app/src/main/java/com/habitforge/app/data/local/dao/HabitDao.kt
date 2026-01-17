package com.habitforge.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habitforge.app.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: Long): HabitEntity?

    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun getHabitByIdFlow(habitId: Long): Flow<HabitEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("UPDATE habits SET isArchived = 1 WHERE id = :habitId")
    suspend fun archiveHabit(habitId: Long)

    @Query("SELECT * FROM habits WHERE frequency = :frequency AND isArchived = 0")
    fun getHabitsByFrequency(frequency: String): Flow<List<HabitEntity>>

    // Get all active habits for a given date (ISO yyyy-MM-dd) and dayOfWeek (e.g., 'MON')
    @Query("SELECT * FROM habits WHERE isArchived = 0 AND startDate <= :date AND (endDate IS NULL OR endDate >= :date) AND (frequency = 'DAILY' OR (frequency = 'WEEKLY' AND daysOfWeek LIKE '%' || :dayOfWeek || '%')) ORDER BY createdAt DESC")
    fun getHabitsForDate(date: String, dayOfWeek: String): Flow<List<HabitEntity>>

    // Get all habits as a list (not Flow)
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAt DESC")
    suspend fun getAllHabitsOnce(): List<HabitEntity>
}
