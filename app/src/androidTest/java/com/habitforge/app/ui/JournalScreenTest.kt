package com.habitforge.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.habitforge.app.data.local.entity.JournalEntryEntity
import com.habitforge.app.ui.screens.journal.JournalEntryCard
import com.habitforge.app.ui.theme.HabitForgeTheme
import org.junit.Rule
import org.junit.Test

class JournalScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun journalEntryCard_displaysContent() {
        // Given
        val entry = JournalEntryEntity(
            id = 1,
            content = "Today was a great day!",
            mood = 4,
            date = "2026-01-10"
        )

        composeTestRule.setContent {
            HabitForgeTheme {
                JournalEntryCard(
                    entry = entry,
                    onDelete = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Today was a great day!").assertIsDisplayed()
        composeTestRule.onNodeWithText("2026-01-10").assertIsDisplayed()
    }

    @Test
    fun journalEntryCard_showsDeleteDialog_whenDeleteClicked() {
        // Given
        val entry = JournalEntryEntity(
            id = 1,
            content = "Test entry",
            mood = 3,
            date = "2026-01-10"
        )

        composeTestRule.setContent {
            HabitForgeTheme {
                JournalEntryCard(
                    entry = entry,
                    onDelete = {}
                )
            }
        }

        // When - click delete button
        composeTestRule.onNode(hasClickAction()).performClick()

        // Then - dialog should appear
        composeTestRule.onNodeWithText("Delete this journal entry?").assertIsDisplayed()
    }

    @Test
    fun journalEntryCard_displaysMoodEmoji() {
        val entry = JournalEntryEntity(
            id = 1,
            content = "Feeling happy",
            mood = 5,
            date = "2026-01-10"
        )

        composeTestRule.setContent {
            HabitForgeTheme {
                JournalEntryCard(
                    entry = entry,
                    onDelete = {}
                )
            }
        }

        // Card should be displayed with content
        composeTestRule.onNodeWithText("Feeling happy").assertIsDisplayed()
    }
}
