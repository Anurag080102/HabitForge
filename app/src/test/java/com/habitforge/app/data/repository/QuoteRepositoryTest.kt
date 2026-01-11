package com.habitforge.app.data.repository

import com.habitforge.app.data.remote.api.QuoteResponse
import com.habitforge.app.data.remote.api.QuotesApiService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class QuoteRepositoryTest {

    private lateinit var quotesApi: QuotesApiService
    private lateinit var repository: QuoteRepository

    @Before
    fun setup() {
        quotesApi = mock()
        repository = QuoteRepository(quotesApi)
    }

    @Test
    fun `getTodayQuote returns quote on success`() = runTest {
        // Given
        val response = listOf(
            QuoteResponse(
                q = "The only way to do great work is to love what you do.",
                a = "Steve Jobs",
                h = ""
            )
        )
        whenever(quotesApi.getTodayQuote()).thenReturn(response)

        // When
        val result = repository.getTodayQuote()

        // Then
        assertTrue(result.isSuccess)
        assertEquals("The only way to do great work is to love what you do.", result.getOrNull()?.text)
        assertEquals("Steve Jobs", result.getOrNull()?.author)
    }

    @Test
    fun `getTodayQuote returns fallback on error`() = runTest {
        // Given
        whenever(quotesApi.getTodayQuote()).thenThrow(RuntimeException("Network error"))

        // When
        val result = repository.getTodayQuote()

        // Then
        assertTrue(result.isSuccess) // Should return fallback
        assertEquals("Mark Twain", result.getOrNull()?.author)
    }

    @Test
    fun `getTodayQuote caches quote for same day`() = runTest {
        // Given
        val response = listOf(
            QuoteResponse(q = "Quote 1", a = "Author 1", h = "")
        )
        whenever(quotesApi.getTodayQuote()).thenReturn(response)

        // When - call twice
        repository.getTodayQuote()
        repository.getTodayQuote()

        // Then - API should only be called once due to caching
        verify(quotesApi, times(1)).getTodayQuote()
    }

    @Test
    fun `getRandomQuote returns quote on success`() = runTest {
        // Given
        val response = listOf(
            QuoteResponse(q = "Random quote", a = "Random author", h = "")
        )
        whenever(quotesApi.getRandomQuote()).thenReturn(response)

        // When
        val result = repository.getRandomQuote()

        // Then
        assertTrue(result.isSuccess)
        assertEquals("Random quote", result.getOrNull()?.text)
    }

    @Test
    fun `getRandomQuote returns failure on error`() = runTest {
        // Given
        whenever(quotesApi.getRandomQuote()).thenThrow(RuntimeException("Network error"))

        // When
        val result = repository.getRandomQuote()

        // Then
        assertTrue(result.isFailure)
    }
}

