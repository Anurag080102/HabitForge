    // Get monthly completion counts for all habits
    @Query("SELECT SUBSTR(date, 1, 7) AS month, COUNT(*) AS completedCount FROM habit_completions WHERE isCompleted = 1 GROUP BY month ORDER BY month DESC")
    fun getMonthlyCompletionStats(): Flow<List<MonthlyCompletionStat>>
package com.habitforge.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.habitforge.app.data.local.entity.HabitCompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE date = :date")
    fun getCompletionsForDate(date: String): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getCompletion(habitId: Long, date: String): HabitCompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletionEntity)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletion(habitId: Long, date: String)

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getCompletionsInRange(habitId: Long, startDate: String, endDate: String): List<HabitCompletionEntity>

    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId AND isCompleted = 1")
    suspend fun getTotalCompletions(habitId: Long): Int
}
