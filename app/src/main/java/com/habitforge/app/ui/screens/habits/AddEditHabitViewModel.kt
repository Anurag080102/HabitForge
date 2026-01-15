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
import java.time.LocalDate
import javax.inject.Inject

data class AddEditHabitUiState(
    val name: String = "",
    val description: String = "",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val startDate: String = LocalDate.now().toString(),
    val endDate: String? = null,
    val daysOfWeek: Set<String> = emptySet(), // e.g., {"MON", "WED"}
    val reminderTime: String? = null,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val isEditing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddEditHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddEditHabitUiState())
    val uiState: StateFlow<AddEditHabitUiState> = _uiState.asStateFlow()

    fun updateName(name: String) { _uiState.value = _uiState.value.copy(name = name) }
    fun updateDescription(desc: String) { _uiState.value = _uiState.value.copy(description = desc) }
    fun updateFrequency(freq: HabitFrequency) { _uiState.value = _uiState.value.copy(frequency = freq) }
    fun updateStartDate(date: String) { _uiState.value = _uiState.value.copy(startDate = date) }
    fun updateEndDate(date: String?) { _uiState.value = _uiState.value.copy(endDate = date) }
    fun updateDaysOfWeek(days: Set<String>) { _uiState.value = _uiState.value.copy(daysOfWeek = days) }
    fun updateReminderTime(time: String?) { _uiState.value = _uiState.value.copy(reminderTime = time) }

    fun loadHabit(habitId: Long) {
        viewModelScope.launch {
            val habit = habitRepository.getHabitById(habitId)
            habit?.let {
                _uiState.value = _uiState.value.copy(
                    name = it.name,
                    description = it.description,
                    frequency = HabitFrequency.fromValue(it.frequency),
                    startDate = it.startDate,
                    endDate = it.endDate,
                    daysOfWeek = it.daysOfWeek?.split(",")?.toSet() ?: emptySet(),
                    reminderTime = it.reminderTime,
                    isEditing = true
                )
            }
        }
    }

    fun saveHabit() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Name cannot be empty")
            return
        }
        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            val entity = HabitEntity(
                name = state.name,
                description = state.description,
                frequency = state.frequency.value,
                startDate = state.startDate,
                endDate = state.endDate,
                daysOfWeek = if (state.daysOfWeek.isEmpty()) null else state.daysOfWeek.joinToString(","),
                reminderTime = state.reminderTime
            )
            if (state.isEditing) {
                habitRepository.updateHabit(entity)
            } else {
                habitRepository.addHabit(entity)
            }
            _uiState.value = state.copy(isSaving = false, savedSuccessfully = true)
        }
    }
}
