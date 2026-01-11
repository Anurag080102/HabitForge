package com.habitforge.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitforge.app.data.local.entity.HabitEntity
import com.habitforge.app.data.repository.HabitRepository
import com.habitforge.app.util.HabitFrequency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            habitRepository.getAllHabits().collect { habits ->
                val habitItems = habits.map { habit ->
                    val streak = habitRepository.calculateStreak(habit.id)
                    val total = habitRepository.getTotalCompletions(habit.id)
                    val isCompleted = habitRepository.isHabitCompletedToday(habit.id)
                    HabitListItem(habit, streak, total, isCompleted)
                }

                _uiState.value = HabitsUiState(
                    isLoading = false,
                    habits = habitItems
                )
            }
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
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
}

// ViewModel for Add/Edit Habit screen
data class AddEditHabitUiState(
    val name: String = "",
    val description: String = "",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val reminderTime: String? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddEditHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditHabitUiState())
    val uiState: StateFlow<AddEditHabitUiState> = _uiState.asStateFlow()

    private var editingHabitId: Long? = null

    fun loadHabit(habitId: Long) {
        viewModelScope.launch {
            val habit = habitRepository.getHabitById(habitId)
            habit?.let {
                editingHabitId = it.id
                _uiState.value = AddEditHabitUiState(
                    name = it.name,
                    description = it.description,
                    frequency = HabitFrequency.fromValue(it.frequency),
                    reminderTime = it.reminderTime,
                    isEditing = true
                )
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateFrequency(frequency: HabitFrequency) {
        _uiState.value = _uiState.value.copy(frequency = frequency)
    }

    fun updateReminderTime(time: String?) {
        _uiState.value = _uiState.value.copy(reminderTime = time)
    }

    fun saveHabit() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Habit name is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)

            val habit = HabitEntity(
                id = editingHabitId ?: 0,
                name = state.name.trim(),
                description = state.description.trim(),
                frequency = state.frequency.value,
                reminderTime = state.reminderTime
            )

            if (editingHabitId != null) {
                habitRepository.updateHabit(habit)
            } else {
                habitRepository.addHabit(habit)
            }

            _uiState.value = state.copy(
                isSaving = false,
                savedSuccessfully = true
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

