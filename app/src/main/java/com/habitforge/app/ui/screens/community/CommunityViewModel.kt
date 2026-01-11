package com.habitforge.app.ui.screens.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitforge.app.data.remote.firebase.CommunityPost
import com.habitforge.app.data.repository.CommunityRepository
import com.habitforge.app.data.repository.HabitRepository
import com.habitforge.app.util.MilestoneType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CommunityUiState(
    val isLoading: Boolean = true,
    val posts: List<CommunityPost> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            try {
                communityRepository.getCommunityPosts().collect { posts ->
                    _uiState.value = CommunityUiState(
                        isLoading = false,
                        posts = posts
                    )
                }
            } catch (e: Exception) {
                _uiState.value = CommunityUiState(
                    isLoading = false,
                    errorMessage = "Failed to load community posts"
                )
            }
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            communityRepository.likePost(postId)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

// ViewModel for sharing a milestone
data class ShareMilestoneUiState(
    val selectedMilestoneType: MilestoneType = MilestoneType.STREAK,
    val habitName: String = "",
    val milestoneValue: Int = 0,
    val message: String = "",
    val isSharing: Boolean = false,
    val sharedSuccessfully: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ShareMilestoneViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareMilestoneUiState())
    val uiState: StateFlow<ShareMilestoneUiState> = _uiState.asStateFlow()

    // Generate anonymous user badge
    private val userBadge: String = "User${UUID.randomUUID().toString().take(6)}"

    fun updateMilestoneType(type: MilestoneType) {
        _uiState.value = _uiState.value.copy(selectedMilestoneType = type)
    }

    fun updateHabitName(name: String) {
        _uiState.value = _uiState.value.copy(habitName = name)
    }

    fun updateMilestoneValue(value: Int) {
        _uiState.value = _uiState.value.copy(milestoneValue = value)
    }

    fun updateMessage(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }

    fun shareMilestone() {
        val state = _uiState.value

        if (state.habitName.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please enter a habit name")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSharing = true)

            val result = communityRepository.shareMilestone(
                odge = userBadge,
                milestoneType = state.selectedMilestoneType.value,
                milestoneValue = state.milestoneValue,
                habitName = state.habitName,
                message = state.message
            )

            result.onSuccess {
                _uiState.value = state.copy(
                    isSharing = false,
                    sharedSuccessfully = true
                )
            }.onFailure { e ->
                _uiState.value = state.copy(
                    isSharing = false,
                    errorMessage = "Failed to share: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

