package com.habitforge.app

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.habitforge.app.util.LocaleHelper
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
            if (_workManagerConfiguration == null) {
                if (!::workerFactory.isInitialized) {
                    android.util.Log.e("HabitForgeApp", "workerFactory not initialized! This should not happen.")
                    return Configuration.Builder().build()
                }
                android.util.Log.d("HabitForgeApp", "Initializing WorkManager configuration with HiltWorkerFactory")
                _workManagerConfiguration = Configuration.Builder()
                    .setWorkerFactory(workerFactory)
                    .build()
            }
            return _workManagerConfiguration!!
        }

    override fun attachBaseContext(base: Context) {
        val savedLanguage = LocaleHelper.getSavedLanguage(base)
        val contextToUse = if (savedLanguage != null && savedLanguage.isNotEmpty()) {
            LocaleHelper.setLocale(base, savedLanguage)
        } else {
            base
        }
        super.attachBaseContext(contextToUse)
    }

    override fun onCreate() {
        super.onCreate()
        
        android.util.Log.d("HabitForgeApp", "onCreate: Initializing WorkManager configuration")
        if (!::workerFactory.isInitialized) {
            android.util.Log.e("HabitForgeApp", "workerFactory not initialized in onCreate()!")
        } else {
            try {
                val config = workManagerConfiguration
                android.util.Log.d("HabitForgeApp", "WorkManager configuration initialized: $config")
                
                if (!WorkManager.isInitialized()) {
                    WorkManager.initialize(this, config)
                    android.util.Log.d("HabitForgeApp", "WorkManager initialized with HiltWorkerFactory")
                } else {
                    android.util.Log.w("HabitForgeApp", "WorkManager already initialized")
                }
            } catch (e: Exception) {
                android.util.Log.e("HabitForgeApp", "Error initializing WorkManager", e)
            }
        }
        
        ReminderScheduler.scheduleDailyReminder(this)
    }
}
