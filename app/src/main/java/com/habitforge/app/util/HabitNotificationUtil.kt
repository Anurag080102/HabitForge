package com.habitforge.app.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import android.Manifest
import com.habitforge.app.worker.ReminderScheduler

object HabitNotificationUtil {
    private const val TAG = "HabitNotificationUtil"
    
    fun scheduleHabitNotification(context: Context, habitId: Long, reminderTime: String?, startDate: String) {
        if (reminderTime == null || reminderTime.isBlank()) {
            Log.d(TAG, "Not scheduling notification for habitId=$habitId: reminderTime is null or blank")
            return
        }
        
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            
            if (!hasPermission) {
                Log.w(TAG, "Notification permission not granted for habitId=$habitId. Please grant POST_NOTIFICATIONS permission.")
                // Still schedule the work - permission might be granted later
            } else {
                Log.d(TAG, "Notification permission granted")
            }
        }
        
        Log.d(TAG, "Scheduling notification for habitId=$habitId, reminderTime=$reminderTime, startDate=$startDate")
        ReminderScheduler.scheduleHabitReminder(context, habitId, reminderTime, startDate)
        Log.d(TAG, "Notification scheduling completed for habitId=$habitId")
    }
}
