package com.habitforge.app.data.repository

import com.habitforge.app.data.local.dao.HabitDao
import com.habitforge.app.data.local.dao.HabitCompletionDao
import com.habitforge.app.data.local.entity.HabitEntity
import com.habitforge.app.data.local.entity.HabitCompletionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    // Get all active habits
    fun getAllHabits(): Flow<List<HabitEntity>> = habitDao.getAllHabits()

    // Get habit by ID
    suspend fun getHabitById(habitId: Long): HabitEntity? = habitDao.getHabitById(habitId)

    // Get habit as Flow
    fun getHabitByIdFlow(habitId: Long): Flow<HabitEntity?> = habitDao.getHabitByIdFlow(habitId)

    // Add new habit
    suspend fun addHabit(habit: HabitEntity): Long = habitDao.insertHabit(habit)

    // Update habit
    suspend fun updateHabit(habit: HabitEntity) = habitDao.updateHabit(habit)

    // Delete habit
    suspend fun deleteHabit(habit: HabitEntity) = habitDao.deleteHabit(habit)

    // Archive habit
    suspend fun archiveHabit(habitId: Long) = habitDao.archiveHabit(habitId)

    // Mark habit as complete for today
    suspend fun markHabitComplete(habitId: Long, note: String = "") {
        val today = LocalDate.now().format(dateFormatter)
        val completion = HabitCompletionEntity(
            habitId = habitId,
            date = today,
            isCompleted = true,
            note = note
        )
        completionDao.insertCompletion(completion)
    }

    // Mark habit as missed for today
    suspend fun markHabitMissed(habitId: Long) {
        val today = LocalDate.now().format(dateFormatter)
        val completion = HabitCompletionEntity(
            habitId = habitId,
            date = today,
            isCompleted = false
        )
        completionDao.insertCompletion(completion)
    }

    // Undo completion for today
    suspend fun undoCompletion(habitId: Long) {
        val today = LocalDate.now().format(dateFormatter)
        completionDao.deleteCompletion(habitId, today)
    }

    // Get completions for a habit
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletionEntity>> =
        completionDao.getCompletionsForHabit(habitId)

    // Get today's completions
    fun getTodayCompletions(): Flow<List<HabitCompletionEntity>> {
        val today = LocalDate.now().format(dateFormatter)
        return completionDao.getCompletionsForDate(today)
    }

    // Check if habit is completed today
    suspend fun isHabitCompletedToday(habitId: Long): Boolean {
        val today = LocalDate.now().format(dateFormatter)
        val completion = completionDao.getCompletion(habitId, today)
        return completion?.isCompleted == true
    }

    // Calculate current streak for a habit
    suspend fun calculateStreak(habitId: Long): Int {
        var streak = 0
        var currentDate = LocalDate.now()

        // Check if completed today first
        val todayCompletion = completionDao.getCompletion(habitId, currentDate.format(dateFormatter))
        if (todayCompletion?.isCompleted != true) {
            // If not completed today, start from yesterday
            currentDate = currentDate.minusDays(1)
        }

        // Count consecutive days
        while (true) {
            val dateStr = currentDate.format(dateFormatter)
            val completion = completionDao.getCompletion(habitId, dateStr)

            if (completion?.isCompleted == true) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }

    // Get total completions count
    suspend fun getTotalCompletions(habitId: Long): Int =
        completionDao.getTotalCompletions(habitId)

    // Get all active habits for a given date and day of week
    fun getHabitsForDate(date: LocalDate): Flow<List<HabitEntity>> {
        val dayOfWeek = date.dayOfWeek.name.take(3) // e.g., MON, TUE
        return habitDao.getHabitsForDate(date.format(dateFormatter), dayOfWeek)
    }

    // Get monthly completion stats for all habits
    fun getMonthlyCompletionStats() = completionDao.getMonthlyCompletionStats()
}
