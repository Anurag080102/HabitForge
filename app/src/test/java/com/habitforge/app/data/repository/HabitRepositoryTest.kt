package com.habitforge.app.data.repository

import com.habitforge.app.data.local.dao.HabitCompletionDao
import com.habitforge.app.data.local.dao.HabitDao
import com.habitforge.app.data.local.entity.HabitCompletionEntity
import com.habitforge.app.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitRepositoryTest {

    private lateinit var habitDao: HabitDao
    private lateinit var completionDao: HabitCompletionDao
    private lateinit var repository: HabitRepository

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val today = LocalDate.now().format(dateFormatter)

    @Before
    fun setup() {
        habitDao = mock()
        completionDao = mock()
        repository = HabitRepository(habitDao, completionDao)
    }

    @Test
    fun `addHabit should insert habit and return id`() = runTest {
        // Given
        val habit = HabitEntity(name = "Exercise", frequency = "DAILY")
        whenever(habitDao.insertHabit(habit)).thenReturn(1L)

        // When
        val result = repository.addHabit(habit)

        // Then
        assertEquals(1L, result)
        verify(habitDao).insertHabit(habit)
    }

    @Test
    fun `markHabitComplete should insert completion for today`() = runTest {
        // Given
        val habitId = 1L

        // When
        repository.markHabitComplete(habitId, "Great job!")

        // Then
        verify(completionDao).insertCompletion(argThat { completion ->
            completion.habitId == habitId &&
            completion.date == today &&
            completion.isCompleted &&
            completion.note == "Great job!"
        })
    }

    @Test
    fun `isHabitCompletedToday returns true when completed`() = runTest {
        // Given
        val habitId = 1L
        val completion = HabitCompletionEntity(
            habitId = habitId,
            date = today,
            isCompleted = true
        )
        whenever(completionDao.getCompletion(habitId, today)).thenReturn(completion)

        // When
        val result = repository.isHabitCompletedToday(habitId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isHabitCompletedToday returns false when not completed`() = runTest {
        // Given
        val habitId = 1L
        whenever(completionDao.getCompletion(habitId, today)).thenReturn(null)

        // When
        val result = repository.isHabitCompletedToday(habitId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `calculateStreak returns correct count for consecutive days`() = runTest {
        // Given
        val habitId = 1L
        val yesterday = LocalDate.now().minusDays(1).format(dateFormatter)
        val twoDaysAgo = LocalDate.now().minusDays(2).format(dateFormatter)
        val threeDaysAgo = LocalDate.now().minusDays(3).format(dateFormatter)

        whenever(completionDao.getCompletion(habitId, today)).thenReturn(
            HabitCompletionEntity(habitId = habitId, date = today, isCompleted = true)
        )
        whenever(completionDao.getCompletion(habitId, yesterday)).thenReturn(
            HabitCompletionEntity(habitId = habitId, date = yesterday, isCompleted = true)
        )
        whenever(completionDao.getCompletion(habitId, twoDaysAgo)).thenReturn(
            HabitCompletionEntity(habitId = habitId, date = twoDaysAgo, isCompleted = true)
        )
        whenever(completionDao.getCompletion(habitId, threeDaysAgo)).thenReturn(null)

        // When
        val streak = repository.calculateStreak(habitId)

        // Then
        assertEquals(3, streak)
    }

    @Test
    fun `undoCompletion should delete completion for today`() = runTest {
        // Given
        val habitId = 1L

        // When
        repository.undoCompletion(habitId)

        // Then
        verify(completionDao).deleteCompletion(habitId, today)
    }
}
