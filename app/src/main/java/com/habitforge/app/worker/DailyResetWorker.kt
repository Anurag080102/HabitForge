package com.habitforge.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.habitforge.app.data.repository.HabitRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Worker that runs at midnight to reset daily habits to "undone" status
 */
@HiltWorker
class DailyResetWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val habitRepository: HabitRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "daily_reset_work"
        private const val TAG = "DailyResetWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "DailyResetWorker started - resetting daily habits")
        return try {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val yesterday = LocalDate.now().minusDays(1)
            val yesterdayStr = yesterday.format(formatter)
            
            val habits = habitRepository.getAllHabitsOnce()
            val dailyHabits = habits.filter { it.frequency == "DAILY" }
            
            Log.d(TAG, "Found ${dailyHabits.size} daily habits to reset")
            
            val today = LocalDate.now().format(formatter)
            var resetCount = 0
            for (habit in dailyHabits) {
                val isCompleted = habitRepository.isHabitCompletedToday(habit.id)
                if (isCompleted) {
                    habitRepository.undoCompletion(habit.id)
                    resetCount++
                    Log.d(TAG, "Reset habit: ${habit.name}")
                }
            }
            
            Log.d(TAG, "DailyResetWorker completed - reset $resetCount habits")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in DailyResetWorker", e)
            Result.retry()
        }
    }
}
