package com.habitforge.app.ui.navigation

// Define all navigation routes
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Habits : Screen("habits")
    object AddHabit : Screen("add_habit")
    object EditHabit : Screen("edit_habit/{habitId}") {
        fun createRoute(habitId: Long) = "edit_habit/$habitId"
    }
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: Long) = "habit_detail/$habitId"
    }
    object Journal : Screen("journal")
    object AddJournalEntry : Screen("add_journal_entry")
    object Profile : Screen("profile")
}

