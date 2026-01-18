package com.habitforge.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.habitforge.app.ui.navigation.HabitForgeNavGraph
import com.habitforge.app.ui.navigation.Screen
import com.habitforge.app.ui.theme.HabitForgeTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.content.ContextCompat
import com.habitforge.app.worker.ReminderScheduler
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.habitforge.app.data.repository.HabitRepository
import android.content.Context
import com.habitforge.app.util.LocaleHelper

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val labelResId: Int
) {
    object Dashboard : BottomNavItem(Screen.Dashboard.route, Icons.Default.Home, R.string.nav_dashboard)
    object Habits : BottomNavItem(Screen.Habits.route, Icons.Default.List, R.string.nav_habits)
    object Journal : BottomNavItem(Screen.Journal.route, Icons.Default.DateRange, R.string.nav_journal)
    object Profile : BottomNavItem(Screen.Profile.route, Icons.Default.Person, R.string.profile_tab)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var habitRepository: HabitRepository

    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = LocaleHelper.getSavedLanguage(newBase)
        android.util.Log.d("MainActivity", "attachBaseContext called, savedLanguage=$savedLanguage")
        val contextToUse = if (savedLanguage != null && savedLanguage.isNotEmpty()) {
            android.util.Log.d("MainActivity", "Applying locale: $savedLanguage")
            LocaleHelper.setLocale(newBase, savedLanguage)
        } else {
            android.util.Log.d("MainActivity", "No saved language, using default")
            newBase
        }
        super.attachBaseContext(contextToUse)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }

        lifecycleScope.launch {
            try {
                val habits = habitRepository.getAllHabitsOnce()
                habits.forEach { habit ->
                    habit.reminderTime?.let { time ->
                        ReminderScheduler.scheduleHabitReminder(
                            this@MainActivity,
                            habit.id,
                            time,
                            habit.startDate
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Failed to schedule habit reminders", e)
            }
        }

        enableEdgeToEdge()
        setContent {
            HabitForgeTheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Habits,
        BottomNavItem.Journal,
        BottomNavItem.Profile
    )

    val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    item.icon, 
                                    contentDescription = null,
                                    tint = if (isSelected) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                            },
                            label = { 
                                Text(
                                    stringResource(item.labelResId),
                                    color = if (isSelected) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                            },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        HabitForgeNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
