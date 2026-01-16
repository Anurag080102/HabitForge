package com.habitforge.app.data.repository

import com.habitforge.app.data.remote.firebase.CommunityPost
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // Get community posts
    fun getCommunityPosts(): Flow<List<CommunityPost>> = callbackFlow {
        val query = firestore.collection("community").orderBy("timestamp", Query.Direction.DESCENDING)
        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val posts = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(CommunityPost::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(posts)
            }
        }
        awaitClose { subscription.remove() }
    }

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
        return try {
            val ref = firestore.collection("community").add(post).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Like a post
    suspend fun likePost(postId: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val ref = firestore.collection("community").document(postId)
                val snapshot = transaction.get(ref)
                val current = snapshot.getLong("likes") ?: 0L
                transaction.update(ref, "likes", current + 1)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
