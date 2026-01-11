package com.habitforge.app.data.remote.api

import retrofit2.http.GET

// Response model for ZenQuotes API
data class QuoteResponse(
    val q: String,  // Quote text
    val a: String,  // Author
    val h: String   // HTML formatted quote
)

interface QuotesApiService {

    @GET("api/today")
    suspend fun getTodayQuote(): List<QuoteResponse>

    @GET("api/random")
    suspend fun getRandomQuote(): List<QuoteResponse>
}

