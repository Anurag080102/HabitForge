package com.habitforge.app.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

object ReminderScheduler {

    // Schedule reminder for a specific habit at a specific time (one day prior to habit startDate)
    fun scheduleHabitReminder(context: Context, habitId: Long, reminderTime: String, startDate: String) {
        try {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val habitStart = LocalDate.parse(startDate, formatter)
            val reminderDate = habitStart.minusDays(1)

            // Parse reminderTime (HH:mm)
            val parts = reminderTime.split(":")
            if (parts.size != 2) return
            val hour = parts[0].toIntOrNull() ?: return
            val minute = parts[1].toIntOrNull() ?: 0

            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.YEAR, reminderDate.year)
                set(Calendar.MONTH, reminderDate.monthValue - 1)
                set(Calendar.DAY_OF_MONTH, reminderDate.dayOfMonth)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val delay = target.timeInMillis - now.timeInMillis
            if (delay <= 0) return

            val data = Data.Builder()
                .putLong("habitId", habitId)
                .build()

            val request = OneTimeWorkRequestBuilder<HabitReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("habit_$habitId")
                .build()

            WorkManager.getInstance(context).enqueue(request)
        } catch (_: Exception) {
            // parsing failed or invalid date/time - ignore scheduling
        }
    }

    // Cancel all reminders for a habit (by tag)
    fun cancelHabitReminders(context: Context, habitId: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag("habit_$habitId")
    }

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
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // If target time has passed today, schedule for tomorrow
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
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
