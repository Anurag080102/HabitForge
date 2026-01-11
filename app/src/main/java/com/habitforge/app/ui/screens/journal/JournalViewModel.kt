package com.habitforge.app.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitforge.app.data.local.entity.JournalEntryEntity
import com.habitforge.app.data.repository.JournalRepository
import com.habitforge.app.util.Mood
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JournalUiState(
    val isLoading: Boolean = true,
    val entries: List<JournalEntryEntity> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    init {
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            journalRepository.getAllEntries().collect { entries ->
                _uiState.value = JournalUiState(
                    isLoading = false,
                    entries = entries
                )
            }
        }
    }

    fun deleteEntry(entry: JournalEntryEntity) {
        viewModelScope.launch {
            journalRepository.deleteEntry(entry)
        }
    }
}

// ViewModel for Add Journal Entry screen
data class AddJournalEntryUiState(
    val content: String = "",
    val mood: Mood = Mood.NEUTRAL,
    val linkedHabitId: Long? = null,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddJournalEntryViewModel @Inject constructor(
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddJournalEntryUiState())
    val uiState: StateFlow<AddJournalEntryUiState> = _uiState.asStateFlow()

    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
    }

    fun updateMood(mood: Mood) {
        _uiState.value = _uiState.value.copy(mood = mood)
    }

    fun linkToHabit(habitId: Long?) {
        _uiState.value = _uiState.value.copy(linkedHabitId = habitId)
    }

    fun saveEntry() {
        val state = _uiState.value

        if (state.content.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please write something")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)

            journalRepository.addEntry(
                content = state.content.trim(),
                mood = state.mood.value,
                habitId = state.linkedHabitId
            )

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

