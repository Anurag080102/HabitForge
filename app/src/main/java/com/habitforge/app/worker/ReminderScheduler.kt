package com.habitforge.app.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

object ReminderScheduler {

    private const val TAG = "ReminderScheduler"

    fun scheduleHabitReminder(context: Context, habitId: Long, reminderTime: String, startDate: String) {
        try {
            Log.d(TAG, "scheduleHabitReminder called for habitId=$habitId reminderTime=$reminderTime startDate=$startDate")
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val habitStart = LocalDate.parse(startDate, formatter)
            val today = LocalDate.now()
            
            val parts = reminderTime.split(":")
            if (parts.size != 2) {
                Log.w(TAG, "Invalid reminderTime format: $reminderTime (expected HH:mm)")
                return
            }
            val hour = parts[0].toIntOrNull()
            val minute = parts[1].toIntOrNull()
            if (hour == null || hour !in 0..23 || minute == null || minute !in 0..59) {
                Log.w(TAG, "Invalid reminderTime values: hour=$hour, minute=$minute")
                return
            }

            val now = Calendar.getInstance()
            
            val reminderDate = when {
                habitStart == today -> {
                    val todayAtReminderTime = Calendar.getInstance().apply {
                        set(Calendar.YEAR, today.year)
                        set(Calendar.MONTH, today.monthValue - 1)
                        set(Calendar.DAY_OF_MONTH, today.dayOfMonth)
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    if (todayAtReminderTime.after(now)) {
                        Log.d(TAG, "Habit starts today, reminder time hasn't passed yet, scheduling for today at $reminderTime")
                        today
                    } else {
                        Log.d(TAG, "Habit starts today but reminder time ($reminderTime) has already passed. Scheduling for tomorrow at $reminderTime")
                        today.plusDays(1)
                    }
                }
                habitStart == today.plusDays(1) -> {
                    val todayAtReminderTime = Calendar.getInstance().apply {
                        set(Calendar.YEAR, today.year)
                        set(Calendar.MONTH, today.monthValue - 1)
                        set(Calendar.DAY_OF_MONTH, today.dayOfMonth)
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    if (todayAtReminderTime.after(now)) {
                        Log.d(TAG, "Habit starts tomorrow, reminder time hasn't passed today, scheduling for today at $reminderTime")
                        today
                    } else {
                        Log.d(TAG, "Habit starts tomorrow, reminder time has passed today, scheduling for tomorrow at $reminderTime")
                        today.plusDays(1)
                    }
                }
                habitStart.isAfter(today) -> {
                    val date = habitStart.minusDays(1)
                    Log.d(TAG, "Habit starts in future ($startDate), scheduling reminder for 1 day before: $date")
                    date
                }
                else -> {
                    Log.w(TAG, "Habit start date ($startDate) is in the past. Not scheduling reminder.")
                    return
                }
            }

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
            Log.d(TAG, "Computed delayMillis=$delay (${delay / 1000 / 60} minutes) for target=$target, habitStart=$habitStart")
            
            if (delay <= 0) {
                Log.w(TAG, "Not scheduling reminder: computed delay <= 0 (target time is in the past)")
                return
            }

            val data = Data.Builder()
                .putLong("habitId", habitId)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .build()

            val request = OneTimeWorkRequestBuilder<HabitReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .setConstraints(constraints)
                .addTag("habit_$habitId")
                .build()

            val workInfo = WorkManager.getInstance(context).enqueue(request)
            Log.d(TAG, "WorkManager enqueued OneTimeWorkRequest for habit_$habitId")
            Log.d(TAG, "  - Scheduled for: ${target.time}")
            Log.d(TAG, "  - Delay: ${delay / 1000 / 60} minutes (${delay / 1000 / 60 / 60} hours)")
            Log.d(TAG, "  - Reminder date: $reminderDate, Reminder time: $reminderTime")
            Log.d(TAG, "  - WorkRequest ID: ${request.id}")
            android.util.Log.d(TAG, "WorkManager instance: ${WorkManager.getInstance(context)}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule reminder for habitId=$habitId", e)
        }
    }

    fun cancelHabitReminders(context: Context, habitId: Long) {
        Log.d(TAG, "cancelHabitReminders called for habitId=$habitId")
        WorkManager.getInstance(context).cancelAllWorkByTag("habit_$habitId")
    }

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
        
        // Also schedule daily reset worker at midnight
        scheduleDailyReset(context)
    }
    
    // Schedule daily reset worker to run at midnight
    fun scheduleDailyReset(context: Context) {
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        if (midnight.before(now) || midnight.equals(now)) {
            midnight.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        val delay = midnight.timeInMillis - now.timeInMillis
        Log.d(TAG, "Scheduling daily reset worker for midnight, delay=${delay / 1000 / 60} minutes")
        
        val resetRequest = PeriodicWorkRequestBuilder<DailyResetWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyResetWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            resetRequest
        )
        Log.d(TAG, "Daily reset worker scheduled")
    }

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
            set(Calendar.MILLISECOND, 0)
        }

        if (target.before(now) || target.equals(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        val delay = target.timeInMillis - now.timeInMillis
        Log.d(TAG, "calculateInitialDelay: now=$now, target=$target, delay=${delay}ms")
        return delay
    }

    // Schedule one-time reminder for testing
    fun scheduleOneTimeReminder(context: Context, delayMinutes: Long = 1) {
        val reminderRequest = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueue(reminderRequest)
    }

    fun scheduleImmediateHabitReminder(context: Context, habitId: Long, delayMinutes: Long = 1) {
        val data = Data.Builder().putLong("habitId", habitId).build()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .build()
        val request = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setInputData(data)
            .setConstraints(constraints)
            .addTag("habit_$habitId")
            .build()
        WorkManager.getInstance(context).enqueue(request)
        Log.d(TAG, "Enqueued immediate OneTimeWorkRequest for habit_$habitId with delayMinutes=$delayMinutes")
    }
    
    fun logScheduledWork(context: Context, habitId: Long) {
        Log.d(TAG, "Scheduled work check requested for habit_$habitId")
        Log.d(TAG, "To verify scheduled work, run: adb shell dumpsys jobscheduler | grep habit_$habitId")
    }
}
