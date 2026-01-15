import com.habitforge.app.ui.screens.profile.ProfileScreen
package com.habitforge.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.habitforge.app.ui.screens.community.CommunityScreen
import com.habitforge.app.ui.screens.dashboard.DashboardScreen
import com.habitforge.app.ui.screens.habits.AddEditHabitScreen
import com.habitforge.app.ui.screens.habits.HabitsScreen
import com.habitforge.app.ui.screens.journal.AddJournalEntryScreen
import com.habitforge.app.ui.screens.journal.JournalScreen

@Composable
fun HabitForgeNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToHabits = {
                    navController.navigate(Screen.Habits.route)
                }
            )
        }

        composable(Screen.Habits.route) {
            HabitsScreen(
                onNavigateToAddHabit = {
                    navController.navigate(Screen.AddHabit.route)
                },
                onNavigateToEditHabit = { habitId ->
                    navController.navigate(Screen.EditHabit.createRoute(habitId))
                }
            )
        }

        composable(Screen.AddHabit.route) {
            AddEditHabitScreen(
                habitId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditHabit.route,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: 0L
            AddEditHabitScreen(
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Journal.route) {
            JournalScreen(
                onNavigateToAddEntry = {
                    navController.navigate(Screen.AddJournalEntry.route)
                }
            )
        }

        composable(Screen.AddJournalEntry.route) {
            AddJournalEntryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen()
        }
    }
}
