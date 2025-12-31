package com.habitforge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.habitforge.ui.screens.CreateEditHabitScreen
import com.habitforge.ui.screens.DashboardScreen
import com.habitforge.ui.screens.HabitDetailScreen
import com.habitforge.ui.screens.JournalScreen
import com.habitforge.ui.screens.NotificationsScreen
import com.habitforge.ui.screens.ProgressScreen
import com.habitforge.ui.screens.ProfileScreen
import com.habitforge.ui.screens.SocialFeedScreen
import com.habitforge.ui.screens.SplashScreen

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onGetStarted = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onHabitClick = { habitId ->
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                },
                onAddHabit = {
                    navController.navigate(Screen.CreateHabit.route)
                },
                bottomNavBar = {
                    BottomNavigationBar(navController)
                }
            )
        }

        composable(Screen.CreateHabit.route) {
            CreateEditHabitScreen(
                habitId = null,
                onSave = {
                    navController.popBackStack()
                },
                onDelete = null,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EditHabit.route) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
            CreateEditHabitScreen(
                habitId = habitId,
                onSave = {
                    navController.popBackStack()
                },
                onDelete = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.HabitDetail.route) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
            HabitDetailScreen(
                habitId = habitId,
                onBack = {
                    navController.popBackStack()
                },
                onJournalClick = {
                    navController.navigate(Screen.Journal.route)
                }
            )
        }

        composable(Screen.Journal.route) {
            JournalScreen(
                bottomNavBar = {
                    BottomNavigationBar(navController)
                }
            )
        }

        composable(Screen.Progress.route) {
            ProgressScreen()
        }

        composable(Screen.SocialFeed.route) {
            SocialFeedScreen(
                bottomNavBar = {
                    BottomNavigationBar(navController)
                }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNotificationsClick = {
                    navController.navigate(Screen.Notifications.route)
                },
                bottomNavBar = {
                    BottomNavigationBar(navController)
                }
            )
        }
    }
}

