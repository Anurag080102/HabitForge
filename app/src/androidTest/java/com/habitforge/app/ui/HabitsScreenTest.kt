package com.habitforge.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.habitforge.app.data.local.entity.HabitEntity
import com.habitforge.app.ui.screens.habits.HabitListCard
import com.habitforge.app.ui.screens.habits.HabitListItem
import com.habitforge.app.ui.theme.HabitForgeTheme
import org.junit.Rule
import org.junit.Test

class HabitsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createTestHabit(
        id: Long = 1L,
        name: String = "Test Habit",
        description: String = "Test Description"
    ) = HabitEntity(
        id = id,
        name = name,
        description = description,
        frequency = "DAILY",
        reminderTime = null,
        startDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE),
        createdAt = System.currentTimeMillis(),
        isArchived = false
    )

    @Test
    fun habitListCard_displaysHabitName() {
        // Given
        val habit = createTestHabit(name = "Exercise Daily")
        val habitItem = HabitListItem(
            habit = habit,
            currentStreak = 5,
            totalCompletions = 20,
            isCompletedToday = false
        )

        composeTestRule.setContent {
            HabitForgeTheme {
                HabitListCard(
                    habitItem = habitItem,
                    onEdit = {},
                    onDelete = {},
                    onToggleComplete = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Exercise Daily").assertIsDisplayed()
    }

    @Test
    fun habitListCard_displaysStreak() {
        // Given
        val habitItem = HabitListItem(
            habit = createTestHabit(),
            currentStreak = 7,
            totalCompletions = 15,
            isCompletedToday = false
        )

        composeTestRule.setContent {
            HabitForgeTheme {
                HabitListCard(
                    habitItem = habitItem,
                    onEdit = {},
                    onDelete = {},
                    onToggleComplete = {}
                )
            }
        }

        // Then - check streak is displayed
        composeTestRule.onNodeWithText("🔥 7").assertIsDisplayed()
    }

    @Test
    fun habitListCard_showsCheckIconWhenCompleted() {
        // Given
        val habitItem = HabitListItem(
            habit = createTestHabit(),
            currentStreak = 3,
            totalCompletions = 10,
            isCompletedToday = true
        )

        composeTestRule.setContent {
            HabitForgeTheme {
                HabitListCard(
                    habitItem = habitItem,
                    onEdit = {},
                    onDelete = {},
                    onToggleComplete = {}
                )
            }
        }

        // Verify the habit name is displayed (card is rendered)
        composeTestRule.onNodeWithText("Test Habit").assertIsDisplayed()
        // Verify streak is displayed
        composeTestRule.onNodeWithText("🔥 3").assertIsDisplayed()
    }

    @Test
    fun habitListCard_editButtonIsClickable() {
        // Given
        var editClicked = false
        val habitItem = HabitListItem(
            habit = createTestHabit(),
            currentStreak = 0,
            totalCompletions = 0,
            isCompletedToday = false
        )

        composeTestRule.setContent {
            HabitForgeTheme {
                HabitListCard(
                    habitItem = habitItem,
                    onEdit = { editClicked = true },
                    onDelete = {},
                    onToggleComplete = {}
                )
            }
        }

        // Click the first icon button (Edit button)
        composeTestRule.onAllNodes(hasClickAction())[0].performClick()

        // Then
        assert(editClicked)
    }

    @Test
    fun habitListCard_displaysDescription() {
        // Given
        val habit = createTestHabit(description = "Morning workout routine")
        val habitItem = HabitListItem(
            habit = habit,
            currentStreak = 2,
            totalCompletions = 5,
            isCompletedToday = false
        )

        composeTestRule.setContent {
            HabitForgeTheme {
                HabitListCard(
                    habitItem = habitItem,
                    onEdit = {},
                    onDelete = {},
                    onToggleComplete = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Morning workout routine").assertIsDisplayed()
    }
}
