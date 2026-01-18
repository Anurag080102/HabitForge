package com.habitforge.app.data.repository

import android.content.Context
import com.habitforge.app.data.remote.api.QuotesApiService
import com.habitforge.app.util.LocaleHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class Quote(
    val text: String,
    val author: String
)

@Singleton
class QuoteRepository @Inject constructor(
    private val quotesApi: QuotesApiService,
    @ApplicationContext private val context: Context
) {
    // Cache the daily quote per language
    private val cachedQuotes = mutableMapOf<String, Quote>()
    private val cacheDates = mutableMapOf<String, String>()
    private var lastLanguage: String? = null

    // Get today's quote - adapts to current language
    suspend fun getTodayQuote(forceRefresh: Boolean = false): Result<Quote> {
        val today = java.time.LocalDate.now().toString()
        val language = LocaleHelper.getSavedLanguage(context) ?: Locale.getDefault().language
        val cacheKey = "${today}_$language"

        // Invalidate cache if language changed
        if (lastLanguage != null && lastLanguage != language) {
            android.util.Log.d("QuoteRepository", "Language changed from $lastLanguage to $language, invalidating cache")
            cachedQuotes.clear()
            cacheDates.clear()
        }
        lastLanguage = language

        // Return cached quote if same day and language (unless force refresh)
        if (!forceRefresh && cacheDates[cacheKey] == today && cachedQuotes[cacheKey] != null) {
            android.util.Log.d("QuoteRepository", "Returning cached quote for language=$language")
            return Result.success(cachedQuotes[cacheKey]!!)
        }

        android.util.Log.d("QuoteRepository", "Fetching new quote for language=$language")
        
        // Use local quotes for supported languages (FR, HI, EN)
        // zenquotes.io API doesn't support language selection
        val supportedLanguages = listOf("fr", "hi", "en")
        val effectiveLanguage = if (language in supportedLanguages) language else "en"
        
        return try {
            // Try to get quote from local database first (for supported languages)
            val localQuote = LocalQuotes.getTodayQuote(effectiveLanguage)
            android.util.Log.d("QuoteRepository", "Using local quote for language=$effectiveLanguage")
            cachedQuotes[cacheKey] = localQuote
            cacheDates[cacheKey] = today
            Result.success(localQuote)
            
            // Fallback to API only if local quotes fail (shouldn't happen)
            // Commented out since we're using local quotes for all supported languages
            /*
            val response = quotesApi.getTodayQuote()
            if (response.isNotEmpty()) {
                val quote = Quote(
                    text = response[0].q,
                    author = response[0].a
                )
                cachedQuotes[cacheKey] = quote
                cacheDates[cacheKey] = today
                android.util.Log.d("QuoteRepository", "Quote cached for language=$language")
                Result.success(quote)
            } else {
                Result.failure(Exception("No quote received"))
            }
            */
        } catch (e: Exception) {
            android.util.Log.w("QuoteRepository", "Error getting quote, using fallback for language=$language", e)
            // Return fallback quote on error (translated based on language)
            val fallback = getFallbackQuote(language)
            Result.success(fallback)
        }
    }
    
    private fun getFallbackQuote(language: String): Quote {
        return when (language) {
            "fr" -> Quote(
                text = "Le secret pour avancer est de commencer.",
                author = "Mark Twain"
            )
            "hi" -> Quote(
                text = "आगे बढ़ने का रहस्य शुरू करना है।",
                author = "Mark Twain"
            )
            else -> Quote(
                text = "The secret of getting ahead is getting started.",
                author = "Mark Twain"
            )
        }
    }

    // Get a random quote
    suspend fun getRandomQuote(): Result<Quote> {
        val language = LocaleHelper.getSavedLanguage(context) ?: Locale.getDefault().language
        val supportedLanguages = listOf("fr", "hi", "en")
        val effectiveLanguage = if (language in supportedLanguages) language else "en"
        
        return try {
            // Use local quotes for supported languages
            val quote = LocalQuotes.getRandomQuote(effectiveLanguage)
            android.util.Log.d("QuoteRepository", "Using local random quote for language=$effectiveLanguage")
            Result.success(quote)
        } catch (e: Exception) {
            android.util.Log.w("QuoteRepository", "Error getting random quote", e)
            Result.failure(e)
        }
    }
}

