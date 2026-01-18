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
    val errorMessage: String? = null,
    val showConfirmation: Boolean = false // Show confirmation after save
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Sync profile from Firestore to local database
        syncProfileFromRemote()
        loadProfile()
        loadMonthlyStats()
    }

    // Sync profile from Firestore and update local database
    private fun syncProfileFromRemote() {
        viewModelScope.launch {
            userProfileRepository.syncProfileFromRemote()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            userProfileRepository.getProfile().collect { profile ->
                // Only update if profile actually changed to prevent unnecessary recompositions
                val currentProfile = _uiState.value.profile
                if (currentProfile?.id != profile?.id || 
                    currentProfile?.name != profile?.name ||
                    currentProfile?.email != profile?.email ||
                    currentProfile?.preferredLanguage != profile?.preferredLanguage) {
                    _uiState.value = _uiState.value.copy(profile = profile, isLoading = false)
                } else if (_uiState.value.isLoading) {
                    // Still update isLoading flag if it was true
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    private fun loadMonthlyStats() {
        viewModelScope.launch {
            habitRepository.getMonthlyCompletionStats().collect { stats ->
                // Only update if stats actually changed
                val currentStats = _uiState.value.monthlyStats
                if (currentStats != stats) {
                    _uiState.value = _uiState.value.copy(monthlyStats = stats)
                }
            }
        }
    }

    fun saveProfile(profile: UserProfileEntity) {
        // Save profile to both local and remote database
        println("[ProfileViewModel] saveProfile called with: $profile")
        viewModelScope.launch {
            userProfileRepository.saveProfile(profile)
            _uiState.value = _uiState.value.copy(showConfirmation = true)
        }
    }

    fun resetConfirmation() {
        _uiState.value = _uiState.value.copy(showConfirmation = false)
    }
}
