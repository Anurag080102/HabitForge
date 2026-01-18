package com.habitforge.app.data.repository

import com.habitforge.app.data.local.dao.HabitDao
import com.habitforge.app.data.local.dao.HabitCompletionDao
import com.habitforge.app.data.local.entity.HabitEntity
import com.habitforge.app.data.local.entity.HabitCompletionEntity
import com.habitforge.app.data.remote.firebase.FirestoreService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
    private val firestoreService: FirestoreService
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    // Get all active habits
    fun getAllHabits(): Flow<List<HabitEntity>> = habitDao.getAllHabits()

    // Get habit by ID
    suspend fun getHabitById(habitId: Long): HabitEntity? = habitDao.getHabitById(habitId)

    // Get habit as Flow
    fun getHabitByIdFlow(habitId: Long): Flow<HabitEntity?> = habitDao.getHabitByIdFlow(habitId)

    // Add new habit and sync to Firestore
    suspend fun addHabit(habit: HabitEntity): Long {
        val id = habitDao.insertHabit(habit)
        // Try to sync to Firestore, but don't fail if it doesn't work
        try {
            val habits = habitDao.getAllHabitsOnce()
            firestoreService.saveHabits(habits).getOrNull()
        } catch (e: Exception) {
            // Firestore sync failed, but local save succeeded - this is OK
            android.util.Log.d("HabitRepository", "Firestore sync failed for addHabit (non-critical)", e)
        }
        return id
    }

    // Update habit and sync to Firestore
    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
        // Try to sync to Firestore, but don't fail if it doesn't work
        try {
            val habits = habitDao.getAllHabitsOnce()
            firestoreService.saveHabits(habits).getOrNull()
        } catch (e: Exception) {
            // Firestore sync failed, but local update succeeded - this is OK
            android.util.Log.d("HabitRepository", "Firestore sync failed for updateHabit (non-critical)", e)
        }
    }

    // Delete habit
    suspend fun deleteHabit(habit: HabitEntity) {
        habitDao.deleteHabit(habit)
        // Try to sync to Firestore, but don't fail if it doesn't work
        try {
            val habits = habitDao.getAllHabitsOnce()
            firestoreService.saveHabits(habits).getOrNull()
        } catch (e: Exception) {
            // Firestore sync failed, but local delete succeeded - this is OK
            android.util.Log.d("HabitRepository", "Firestore sync failed for deleteHabit (non-critical)", e)
        }
    }

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

    // Save all habits to Firestore
    suspend fun saveHabitsToRemote() {
        val habits = habitDao.getAllHabitsOnce()
        firestoreService.saveHabits(habits)
    }

    // Load all habits from Firestore and update local
    suspend fun syncHabitsFromRemote() {
        val remoteResult = firestoreService.loadHabits()
        if (remoteResult.isSuccess) {
            val remoteHabits: List<HabitEntity> = remoteResult.getOrNull() ?: emptyList()
            for (habit in remoteHabits) {
                habitDao.insertHabit(habit)
            }
        }
    }

    // Get all habits as a list (not Flow)
    suspend fun getAllHabitsOnce(): List<HabitEntity> = habitDao.getAllHabitsOnce()
}
