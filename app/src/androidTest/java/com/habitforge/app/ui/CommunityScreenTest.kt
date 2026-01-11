package com.habitforge.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.habitforge.app.data.remote.firebase.CommunityPost
import com.habitforge.app.ui.screens.community.CommunityPostCard
import com.habitforge.app.ui.theme.HabitForgeTheme
import org.junit.Rule
import org.junit.Test

class CommunityScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createTestPost(
        id: String = "post1",
        habitName: String = "Exercise",
        milestoneValue: Int = 7,
        milestoneType: String = "STREAK"
    ) = CommunityPost(
        id = id,
        odge = "User123",
        habitName = habitName,
        milestoneType = milestoneType,
        milestoneValue = milestoneValue,
        message = "Feeling great!",
        timestamp = System.currentTimeMillis(),
        likes = 5
    )

    @Test
    fun communityPostCard_displaysHabitName() {
        val post = createTestPost(habitName = "Morning Meditation")

        composeTestRule.setContent {
            HabitForgeTheme {
                CommunityPostCard(
                    post = post,
                    onLike = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Morning Meditation").assertIsDisplayed()
    }

    @Test
    fun communityPostCard_displaysMilestoneValue() {
        val post = createTestPost(milestoneValue = 30)

        composeTestRule.setContent {
            HabitForgeTheme {
                CommunityPostCard(
                    post = post,
                    onLike = {}
                )
            }
        }

        composeTestRule.onNodeWithText("30 day streak!").assertIsDisplayed()
    }

    @Test
    fun communityPostCard_displaysUserBadge() {
        val post = createTestPost()

        composeTestRule.setContent {
            HabitForgeTheme {
                CommunityPostCard(
                    post = post,
                    onLike = {}
                )
            }
        }

        composeTestRule.onNodeWithText("User123").assertIsDisplayed()
    }

    @Test
    fun communityPostCard_displaysStreakEmoji() {
        val post = createTestPost(milestoneType = "STREAK")

        composeTestRule.setContent {
            HabitForgeTheme {
                CommunityPostCard(
                    post = post,
                    onLike = {}
                )
            }
        }

        composeTestRule.onNodeWithText("🔥").assertIsDisplayed()
    }

    @Test
    fun communityPostCard_likeButtonIsClickable() {
        var likeClicked = false
        val post = createTestPost()

        composeTestRule.setContent {
            HabitForgeTheme {
                CommunityPostCard(
                    post = post,
                    onLike = { likeClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Like").performClick()

        assert(likeClicked)
    }
}
