package com.habitforge.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitforge.app.data.local.entity.HabitEntity
import com.habitforge.app.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitListItem(
    val habit: HabitEntity,
    val currentStreak: Int,
    val totalCompletions: Int,
    val isCompletedToday: Boolean
)

data class HabitsUiState(
    val isLoading: Boolean = true,
    val habits: List<HabitListItem> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitsUiState())
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    init {
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            combine(
                habitRepository.getAllHabits(),
                habitRepository.getTodayCompletions()
            ) { habits, completions ->
                val habitItems = habits.map { habit ->
                    val streak = habitRepository.calculateStreak(habit.id)
                    val total = habitRepository.getTotalCompletions(habit.id)
                    val isCompleted = completions.any { it.habitId == habit.id && it.isCompleted }
                    HabitListItem(habit, streak, total, isCompleted)
                }

                HabitsUiState(
                    isLoading = false,
                    habits = habitItems
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun deleteHabit(habit: HabitEntity, context: android.content.Context? = null) {
        viewModelScope.launch {
            if (context != null) {
                com.habitforge.app.worker.ReminderScheduler.cancelHabitReminders(context, habit.id)
            }
            habitRepository.deleteHabit(habit)
        }
    }

    fun archiveHabit(habitId: Long) {
        viewModelScope.launch {
            habitRepository.archiveHabit(habitId)
        }
    }

    fun markComplete(habitId: Long) {
        viewModelScope.launch {
            habitRepository.markHabitComplete(habitId)
            loadHabits()
        }
    }

    fun undoComplete(habitId: Long) {
        viewModelScope.launch {
            habitRepository.undoCompletion(habitId)
            loadHabits()
        }
    }

    fun syncHabitsToRemote() {
        viewModelScope.launch {
            habitRepository.saveHabitsToRemote()
        }
    }

    fun syncHabitsFromRemote() {
        viewModelScope.launch {
            habitRepository.syncHabitsFromRemote()
            loadHabits()
        }
    }
}
