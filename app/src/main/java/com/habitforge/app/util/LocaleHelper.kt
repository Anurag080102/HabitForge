package com.habitforge.app.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "habitforge_prefs"
    private const val KEY_LANGUAGE = "preferred_language"
    /**
     * Sets the locale for a context and returns a new context with the locale applied.
     * This should be used to wrap contexts that need locale-specific resources.
     */
    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            return context
        }
    }

    /**
     * Applies locale to an activity and recreates it.
     * Use this when the user explicitly changes language in UI.
     * The locale is saved to SharedPreferences, and when the activity is recreated,
     * attachBaseContext will apply the new locale.
     */
    fun applyLocale(activity: Activity, language: String) {
        android.util.Log.d("LocaleHelper", "applyLocale called with language=$language")
        // Save language preference first (synchronously to ensure it's saved before recreation)
        saveLanguagePreference(activity, language)
        android.util.Log.d("LocaleHelper", "Language saved to SharedPreferences, recreating activity")
        // Recreate activity - attachBaseContext will be called and will apply the saved locale
        activity.recreate()
    }

    /**
     * Saves language preference to SharedPreferences for quick access
     * Uses commit() instead of apply() to ensure synchronous write before activity recreation
     */
    private fun saveLanguagePreference(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language).commit() // Use commit() for synchronous write
    }

    /**
     * Gets saved language preference from SharedPreferences
     */
    fun getSavedLanguage(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, null)
    }

}
