package com.habitforge.app.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.habitforge.app.MainActivity
import com.habitforge.app.R
import com.habitforge.app.data.repository.HabitRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val habitRepository: HabitRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "habit_reminders"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "habit_reminder_work"
    }

    override suspend fun doWork(): Result {
        return try {
            val habitId = inputData.getLong("habitId", -1L)
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            if (habitId != -1L) {
                // One-time notification for a specific habit
                val habit = habitRepository.getHabitById(habitId)
                if (habit != null) {
                    val tomorrow = LocalDate.now().plusDays(1)
                    val habitStart = LocalDate.parse(habit.startDate, formatter)
                    if (habitStart == tomorrow) {
                        showTomorrowHabitNotification(habit.name)
                    }
                }
                return Result.success()
            }
            // Fallback: daily reminder logic
            val habits = habitRepository.getAllHabits().first()
            val tomorrow = LocalDate.now().plusDays(1)
            var sent = false
            for (habit in habits) {
                val habitStart = LocalDate.parse(habit.startDate, formatter)
                if (habitStart == tomorrow) {
                    showTomorrowHabitNotification(habit.name)
                    sent = true
                }
            }
            if (!sent) {
                // fallback to existing logic
                var incompleteCount = 0
                for (habit in habits) {
                    if (!habitRepository.isHabitCompletedToday(habit.id)) {
                        incompleteCount++
                    }
                }
                if (incompleteCount > 0) {
                    showNotification(incompleteCount)
                }
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun showTomorrowHabitNotification(habitName: String) {
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val title = "Get Ready!"
        val text = "Hey, a new habit '$habitName' is scheduled for tomorrow! Are you charged up?"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify((habitName.hashCode() and 0xFFFFFF), notification)
    }

    private fun showNotification(incompleteCount: Int) {
        createNotificationChannel()

        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.reminder_title)
        val text = if (incompleteCount == 1) {
            "You have 1 habit to complete today!"
        } else {
            "You have $incompleteCount habits to complete today!"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val name = "Habit Reminders"
        val descriptionText = "Reminders to complete your daily habits"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
