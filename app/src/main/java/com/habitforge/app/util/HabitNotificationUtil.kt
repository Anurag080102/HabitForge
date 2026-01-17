package com.habitforge.app.util

import android.content.Context
import com.habitforge.app.worker.ReminderScheduler

object HabitNotificationUtil {
    fun scheduleHabitNotification(context: Context, habitId: Long, reminderTime: String?) {
        if (reminderTime != null) {
            ReminderScheduler.scheduleHabitReminder(context, habitId, reminderTime)
        }
    }
}

