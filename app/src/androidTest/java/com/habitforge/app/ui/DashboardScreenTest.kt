package com.habitforge.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.habitforge.app.ui.screens.dashboard.DashboardUiState
import com.habitforge.app.ui.screens.dashboard.HabitWithStatus
import com.habitforge.app.ui.screens.dashboard.ProgressCard
import com.habitforge.app.ui.screens.dashboard.QuoteCard
import com.habitforge.app.ui.theme.HabitForgeTheme
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun quoteCard_displaysQuoteAndAuthor() {
        // Given
        composeTestRule.setContent {
            HabitForgeTheme {
                QuoteCard(
                    quote = "Test quote",
                    author = "Test Author",
                    onRefresh = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("\"Test quote\"").assertIsDisplayed()
        composeTestRule.onNodeWithText("— Test Author").assertIsDisplayed()
    }

    @Test
    fun progressCard_displaysProgress() {
        // Given
        composeTestRule.setContent {
            HabitForgeTheme {
                ProgressCard(completed = 3, total = 5)
            }
        }

        // Then
        composeTestRule.onNodeWithText("3 / 5 habits completed today").assertIsDisplayed()
    }

    @Test
    fun progressCard_handlesZeroTotal() {
        // Given
        composeTestRule.setContent {
            HabitForgeTheme {
                ProgressCard(completed = 0, total = 0)
            }
        }

        // Then
        composeTestRule.onNodeWithText("0 / 0 habits completed today").assertIsDisplayed()
    }
}

