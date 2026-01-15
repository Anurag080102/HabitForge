            // Reminder Time
            OutlinedTextField(
                value = uiState.reminderTime ?: "",
                onValueChange = { viewModel.updateReminderTime(if (it.isBlank()) null else it) },
                label = { Text("Reminder Time (HH:mm, optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
package com.habitforge.app.ui.screens.habits

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitforge.app.ui.screens.habits.AddEditHabitViewModel
import com.habitforge.app.R
import com.habitforge.app.util.HabitFrequency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    habitId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddEditHabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load habit if editing
    LaunchedEffect(habitId) {
        habitId?.let { viewModel.loadHabit(it) }
    }

    // Navigate back on successful save
    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditing) "Edit Habit"
                        else stringResource(R.string.add_habit)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Habit Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text(stringResource(R.string.habit_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.errorMessage != null && uiState.name.isBlank()
            )

            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text(stringResource(R.string.habit_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            // Frequency Selection
            Text(
                text = stringResource(R.string.habit_frequency),
                style = MaterialTheme.typography.labelLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HabitFrequency.entries.forEach { frequency ->
                    FilterChip(
                        selected = uiState.frequency == frequency,
                        onClick = { viewModel.updateFrequency(frequency) },
                        label = { Text(frequency.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }


            // Start Date
            OutlinedTextField(
                value = uiState.startDate,
                onValueChange = { viewModel.updateStartDate(it) },
                label = { Text("Start Date (yyyy-MM-dd)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // End Date
            OutlinedTextField(
                value = uiState.endDate ?: "",
                onValueChange = { viewModel.updateEndDate(if (it.isBlank()) null else it) },
                label = { Text("End Date (yyyy-MM-dd, optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Days of Week (for weekly habits)
            if (uiState.frequency == com.habitforge.app.util.HabitFrequency.WEEKLY) {
                val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    days.forEach { day ->
                        FilterChip(
                            selected = uiState.daysOfWeek.contains(day),
                            onClick = {
                                val newSet = if (uiState.daysOfWeek.contains(day))
                                    uiState.daysOfWeek - day else uiState.daysOfWeek + day
                                viewModel.updateDaysOfWeek(newSet)
                            },
                            label = { Text(day) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Error message
            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Save Button
            Button(
                onClick = { viewModel.saveHabit() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

