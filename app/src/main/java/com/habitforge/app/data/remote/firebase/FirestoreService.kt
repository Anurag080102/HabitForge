package com.habitforge.app.data.remote.firebase

import com.google.firebase.firestore.Query
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton
import com.habitforge.app.data.local.entity.UserProfileEntity
import com.habitforge.app.data.local.entity.HabitEntity
import com.habitforge.app.data.local.entity.JournalEntryEntity

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore?
) {
    // FirestoreService ready for profile/stats sync

    private fun firestoreOrNull(): FirebaseFirestore? = firestore

    // Stream community posts ordered by timestamp desc
    fun getCommunityPosts(): Flow<List<CommunityPost>> = callbackFlow {
        val firestore = firestoreOrNull()
        if (firestore == null) {
            close(IllegalStateException("Firebase is not configured (missing google-services.json)."))
            return@callbackFlow
        }

        val query = firestore.collection("community")
            .orderBy("timestamp", Query.Direction.DESCENDING)

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

    // Share a post and return the created document id
    suspend fun sharePost(post: CommunityPost): Result<String> {
        val firestore = firestoreOrNull()
            ?: return Result.failure(IllegalStateException("Firebase is not configured (missing google-services.json)."))

        return try {
            val ref = firestore.collection("community").add(post).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Increment likes for a post
    suspend fun likePost(postId: String): Result<Unit> {
        val firestore = firestoreOrNull()
            ?: return Result.failure(IllegalStateException("Firebase is not configured (missing google-services.json)."))

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

    // Save user profile to Firestore (single-user: document id = "main")
    suspend fun saveUserProfile(profile: UserProfileEntity): Result<Unit> {
        val firestore = firestoreOrNull()
            ?: return Result.failure(IllegalStateException("Firebase is not configured (missing google-services.json)."))

        return try {
            firestore.collection("profiles").document("main").set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Load user profile from Firestore (single-user: document id = "main")
    suspend fun loadUserProfile(): Result<UserProfileEntity?> {
        val firestore = firestoreOrNull()
            ?: return Result.failure(IllegalStateException("Firebase is not configured (missing google-services.json)."))

        return try {
            val doc = firestore.collection("profiles").document("main").get().await()
            if (doc.exists()) {
                val profile = doc.toObject(UserProfileEntity::class.java)
                Result.success(profile)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Save all habits to Firestore
    suspend fun saveHabits(habits: List<HabitEntity>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val habitsCollection = firestore.collection("habits")
            habits.forEach { habit ->
                val docRef = habitsCollection.document(habit.id.toString())
                batch.set(docRef, habit)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Load all habits from Firestore
    suspend fun loadHabits(): Result<List<HabitEntity>> {
        return try {
            val snapshot = firestore.collection("habits").get().await()
            val habits = snapshot.documents.mapNotNull { it.toObject(HabitEntity::class.java) }
            Result.success(habits)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Save all journal entries to Firestore
    suspend fun saveJournalEntries(entries: List<JournalEntryEntity>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val entriesCollection = firestore.collection("journal_entries")
            entries.forEach { entry ->
                val docRef = entriesCollection.document(entry.id.toString())
                batch.set(docRef, entry)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Load all journal entries from Firestore
    suspend fun loadJournalEntries(): Result<List<JournalEntryEntity>> {
        return try {
            val snapshot = firestore.collection("journal_entries").get().await()
            val entries = snapshot.documents.mapNotNull { it.toObject(JournalEntryEntity::class.java) }
            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
