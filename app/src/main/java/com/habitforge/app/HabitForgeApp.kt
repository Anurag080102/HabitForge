package com.habitforge.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.habitforge.app.worker.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HabitForgeApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private var _workManagerConfiguration: Configuration? = null

    override val workManagerConfiguration: Configuration
        get() {
            // Initialize configuration on first access, ensuring workerFactory is ready
            if (_workManagerConfiguration == null) {
                // Hilt injection should complete before onCreate(), but add defensive check
                if (!::workerFactory.isInitialized) {
                    // Return default configuration if workerFactory not ready (should not happen)
                    return Configuration.Builder().build()
                }
                _workManagerConfiguration = Configuration.Builder()
                    .setWorkerFactory(workerFactory)
                    .build()
            }
            return _workManagerConfiguration!!
        }

    override fun onCreate() {
        super.onCreate()
        // Hilt injection completes before onCreate(), so workerFactory should be initialized
        // Schedule daily reminders after ensuring WorkManager is properly configured
        ReminderScheduler.scheduleDailyReminder(this)
    }
}
