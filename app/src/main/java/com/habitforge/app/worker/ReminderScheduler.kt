    // Schedule reminder for a specific habit at a specific time (one day prior)
    fun scheduleHabitReminder(context: Context, habitId: Long, reminderTime: String) {
        // Parse reminderTime (HH:mm)
        val parts = reminderTime.split(":")
        if (parts.size != 2) return
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: 0

        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_MONTH, 1) // one day prior
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
        }
        val delay = target.timeInMillis - now.timeInMillis
        if (delay <= 0) return

        val data = androidx.work.Data.Builder()
            .putLong("habitId", habitId)
            .build()

        val request = androidx.work.OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        androidx.work.WorkManager.getInstance(context).enqueue(request)
    }

    // Cancel all reminders for a habit (by tag)
    fun cancelHabitReminders(context: Context, habitId: Long) {
        androidx.work.WorkManager.getInstance(context).cancelAllWorkByTag("habit_$habitId")
    }
package com.habitforge.app.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    // Schedule daily reminder at a specific time
    fun scheduleDailyReminder(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val reminderRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            HabitReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }

    // Cancel scheduled reminders
    fun cancelReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(HabitReminderWorker.WORK_NAME)
    }

    // Calculate delay until 8 PM (20:00)
    private fun calculateInitialDelay(): Long {
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 20)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }

        // If target time has passed today, schedule for tomorrow
        if (target.before(now)) {
            target.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }

    // Schedule one-time reminder for testing
    fun scheduleOneTimeReminder(context: Context, delayMinutes: Long = 1) {
        val reminderRequest = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueue(reminderRequest)
    }
}

