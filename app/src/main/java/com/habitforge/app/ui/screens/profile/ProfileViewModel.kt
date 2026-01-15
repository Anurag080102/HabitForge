package com.habitforge.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitforge.app.data.local.entity.UserProfileEntity
import com.habitforge.app.data.local.entity.MonthlyCompletionStat
import com.habitforge.app.data.repository.UserProfileRepository
import com.habitforge.app.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val profile: UserProfileEntity? = null,
    val monthlyStats: List<MonthlyCompletionStat> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        loadMonthlyStats()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            userProfileRepository.getProfile().collectLatest { profile ->
                _uiState.value = _uiState.value.copy(profile = profile, isLoading = false)
            }
        }
    }

    private fun loadMonthlyStats() {
        viewModelScope.launch {
            habitRepository.getMonthlyCompletionStats().collectLatest { stats ->
                _uiState.value = _uiState.value.copy(monthlyStats = stats)
            }
        }
    }

    fun saveProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            userProfileRepository.saveProfile(profile)
        }
    }
}
