package com.habitforge.app.data.repository

import com.habitforge.app.data.remote.firebase.CommunityPost
import kotlinx.coroutines.flow.Flow
import com.habitforge.app.data.remote.firebase.FirestoreService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepository @Inject constructor(
    private val firestoreService: FirestoreService
) {
    // Get community posts
    fun getCommunityPosts(): Flow<List<CommunityPost>> = firestoreService.getCommunityPosts()

    // Share a milestone
    suspend fun shareMilestone(
        odge: String,
        milestoneType: String,
        milestoneValue: Int,
        habitName: String,
        message: String
    ): Result<String> {
        val post = CommunityPost(
            odge = odge,
            milestoneType = milestoneType,
            milestoneValue = milestoneValue,
            habitName = habitName,
            message = message
        )
        return firestoreService.sharePost(post)
    }

    // Like a post
    suspend fun likePost(postId: String): Result<Unit> = firestoreService.likePost(postId)
}
