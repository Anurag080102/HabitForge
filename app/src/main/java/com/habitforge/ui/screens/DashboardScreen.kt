package com.habitforge.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.unit.dp
import com.habitforge.model.Habit
import com.habitforge.model.HabitFrequency
import com.habitforge.ui.components.HabitCard
import com.habitforge.ui.navigation.BottomNavigationBar

@Composable
fun DashboardScreen(
    onHabitClick: (String) -> Unit,
    onAddHabit: () -> Unit,
    bottomNavBar: @Composable () -> Unit
) {
    val mockHabits = remember {
        listOf(
            Habit("1", "Morning Meditation", HabitFrequency.DAILY, 15, 45, true),
            Habit("2", "Exercise", HabitFrequency.DAILY, 7, 21, true),
            Habit("3", "Read 30 Minutes", HabitFrequency.DAILY, 3, 9, false),
            Habit("4", "Drink 8 Glasses Water", HabitFrequency.DAILY, 12, 36, true),
            Habit("5", "Weekly Review", HabitFrequency.WEEKLY, 4, 16, false)
        )
    }

    val completedHabits = remember { mutableStateOf<Set<String>>(emptySet()) }

    Scaffold(
        bottomBar = { bottomNavBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabit,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Habit")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Column {
                    Text(
                        text = "Hello! 👋",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Let's build great habits today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(mockHabits) { habit ->
                    var isCompleted by remember { mutableStateOf(completedHabits.value.contains(habit.id)) }
                    
                    HabitCard(
                        habit = habit,
                        isCompleted = isCompleted,
                        onCompletedChange = { completed ->
                            isCompleted = completed
                            if (completed) {
                                completedHabits.value = completedHabits.value + habit.id
                            } else {
                                completedHabits.value = completedHabits.value - habit.id
                            }
                        },
                        onClick = { onHabitClick(habit.id) }
                    )
                }
            }
        }
    }
}

