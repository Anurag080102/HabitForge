package com.habitforge.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitforge.app.data.local.entity.HabitEntity
import com.habitforge.app.data.repository.HabitRepository
import com.habitforge.app.data.repository.Quote
import com.habitforge.app.data.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitWithStatus(
    val habit: HabitEntity,
    val isCompletedToday: Boolean,
    val currentStreak: Int
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val quote: Quote? = null,
    val todayHabits: List<HabitWithStatus> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            loadQuote()
            loadHabitsWithStatus()
        }
    }

    private suspend fun loadQuote(forceRefresh: Boolean = false) {
        val result = quoteRepository.getTodayQuote(forceRefresh = forceRefresh)
        result.onSuccess { quote ->
            _uiState.value = _uiState.value.copy(quote = quote)
        }
    }

    private fun loadHabitsWithStatus() {
        viewModelScope.launch {
            combine(
                habitRepository.getAllHabits(),
                habitRepository.getTodayCompletions()
            ) { habits, completions ->
                val habitsWithStatus = habits.map { habit ->
                    val isCompleted = completions.any { it.habitId == habit.id && it.isCompleted }
                    val streak = habitRepository.calculateStreak(habit.id)
                    HabitWithStatus(habit, isCompleted, streak)
                }

                val completedCount = habitsWithStatus.count { it.isCompletedToday }

                _uiState.value.copy(
                    isLoading = false,
                    todayHabits = habitsWithStatus,
                    completedCount = completedCount,
                    totalCount = habits.size
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun markHabitComplete(habitId: Long) {
        viewModelScope.launch {
            habitRepository.markHabitComplete(habitId)
            loadHabitsWithStatus()
        }
    }

    fun undoHabitCompletion(habitId: Long) {
        viewModelScope.launch {
            habitRepository.undoCompletion(habitId)
            loadHabitsWithStatus()
        }
    }

    fun refreshQuote() {
        viewModelScope.launch {
            loadQuote(forceRefresh = true)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
