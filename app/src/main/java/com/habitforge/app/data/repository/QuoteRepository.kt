package com.habitforge.app.data.repository

import com.habitforge.app.data.remote.api.QuotesApiService
import javax.inject.Inject
import javax.inject.Singleton

data class Quote(
    val text: String,
    val author: String
)

@Singleton
class QuoteRepository @Inject constructor(
    private val quotesApi: QuotesApiService
) {
    // Cache the daily quote
    private var cachedQuote: Quote? = null
    private var cacheDate: String? = null

    // Get today's quote
    suspend fun getTodayQuote(): Result<Quote> {
        val today = java.time.LocalDate.now().toString()

        // Return cached quote if same day
        if (cacheDate == today && cachedQuote != null) {
            return Result.success(cachedQuote!!)
        }

        return try {
            val response = quotesApi.getTodayQuote()
            if (response.isNotEmpty()) {
                val quote = Quote(
                    text = response[0].q,
                    author = response[0].a
                )
                cachedQuote = quote
                cacheDate = today
                Result.success(quote)
            } else {
                Result.failure(Exception("No quote received"))
            }
        } catch (e: Exception) {
            // Return fallback quote on error
            val fallback = Quote(
                text = "The secret of getting ahead is getting started.",
                author = "Mark Twain"
            )
            Result.success(fallback)
        }
    }

    // Get a random quote
    suspend fun getRandomQuote(): Result<Quote> {
        return try {
            val response = quotesApi.getRandomQuote()
            if (response.isNotEmpty()) {
                Result.success(Quote(
                    text = response[0].q,
                    author = response[0].a
                ))
            } else {
                Result.failure(Exception("No quote received"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

