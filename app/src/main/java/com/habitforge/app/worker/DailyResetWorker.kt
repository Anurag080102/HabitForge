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
            
            // Get all daily habits
            val habits = habitRepository.getAllHabitsOnce()
            val dailyHabits = habits.filter { it.frequency == "DAILY" }
            
            Log.d(TAG, "Found ${dailyHabits.size} daily habits to reset")
            
            // Remove completions for yesterday (they will be reset for today)
            // Actually, we don't need to delete yesterday's completions - they stay in history
            // We just need to ensure today's completions don't exist yet (which is automatic)
            // But if we want to explicitly reset, we can delete today's completions
            
            val today = LocalDate.now().format(formatter)
            var resetCount = 0
            for (habit in dailyHabits) {
                // Check if there's a completion for today and undo it
                val isCompleted = habitRepository.isHabitCompletedToday(habit.id)
                if (isCompleted) {
                    // This shouldn't happen at midnight, but just in case
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
