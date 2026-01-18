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

    fun getAllHabits(): Flow<List<HabitEntity>> = habitDao.getAllHabits()

    suspend fun getHabitById(habitId: Long): HabitEntity? = habitDao.getHabitById(habitId)

    fun getHabitByIdFlow(habitId: Long): Flow<HabitEntity?> = habitDao.getHabitByIdFlow(habitId)

    suspend fun addHabit(habit: HabitEntity): Long {
        val id = habitDao.insertHabit(habit)
        try {
            val habits = habitDao.getAllHabitsOnce()
            firestoreService.saveHabits(habits).getOrNull()
        } catch (e: Exception) {
            android.util.Log.d("HabitRepository", "Firestore sync failed for addHabit (non-critical)", e)
        }
        return id
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
        try {
            val habits = habitDao.getAllHabitsOnce()
            firestoreService.saveHabits(habits).getOrNull()
        } catch (e: Exception) {
            android.util.Log.d("HabitRepository", "Firestore sync failed for updateHabit (non-critical)", e)
        }
    }

    suspend fun deleteHabit(habit: HabitEntity) {
        habitDao.deleteHabit(habit)
        try {
            val habits = habitDao.getAllHabitsOnce()
            firestoreService.saveHabits(habits).getOrNull()
        } catch (e: Exception) {
            android.util.Log.d("HabitRepository", "Firestore sync failed for deleteHabit (non-critical)", e)
        }
    }

    suspend fun archiveHabit(habitId: Long) = habitDao.archiveHabit(habitId)

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

    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletionEntity>> =
        completionDao.getCompletionsForHabit(habitId)

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

    suspend fun calculateStreak(habitId: Long): Int {
        var streak = 0
        var currentDate = LocalDate.now()

        val todayCompletion = completionDao.getCompletion(habitId, currentDate.format(dateFormatter))
        if (todayCompletion?.isCompleted != true) {
            currentDate = currentDate.minusDays(1)
        }

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

    suspend fun getTotalCompletions(habitId: Long): Int =
        completionDao.getTotalCompletions(habitId)

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

    suspend fun syncHabitsFromRemote() {
        val remoteResult = firestoreService.loadHabits()
        if (remoteResult.isSuccess) {
            val remoteHabits: List<HabitEntity> = remoteResult.getOrNull() ?: emptyList()
            for (habit in remoteHabits) {
                habitDao.insertHabit(habit)
            }
        }
    }

    suspend fun getAllHabitsOnce(): List<HabitEntity> = habitDao.getAllHabitsOnce()
}
