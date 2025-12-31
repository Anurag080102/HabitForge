package com.habitforge.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard")
    object CreateHabit : Screen("create_habit")
    object EditHabit : Screen("edit_habit/{habitId}") {
        fun createRoute(habitId: String) = "edit_habit/$habitId"
    }
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: String) = "habit_detail/$habitId"
    }
    object Journal : Screen("journal")
    object Progress : Screen("progress")
    object SocialFeed : Screen("social_feed")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
}

