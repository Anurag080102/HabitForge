package com.habitforge.app.data.remote.firebase

import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.habitforge.app.data.local.entity.UserProfileEntity
import com.habitforge.app.data.local.entity.HabitEntity
import com.habitforge.app.data.local.entity.JournalEntryEntity

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore?
) {
    private fun firestoreOrNull(): FirebaseFirestore? = firestore
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
                if (error.message?.contains("PERMISSION_DENIED") != true) {
                    android.util.Log.d("FirestoreService", "Community posts error (non-critical): ${error.message}")
                }
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

    suspend fun saveUserProfile(profile: UserProfileEntity): Result<Unit> {
        val firestore = firestoreOrNull()
            ?: return Result.failure(IllegalStateException("Firebase is not configured (missing google-services.json)."))

        return try {
            firestore.collection("profiles").document("main").set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.d("FirestoreService", "Firestore profile save failed (non-critical, app works offline): ${e.message}")
            Result.failure(e)
        }
    }

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

    suspend fun saveHabits(habits: List<HabitEntity>): Result<Unit> {
        val firestore = firestoreOrNull()
            ?: return Result.failure(IllegalStateException("Firebase is not configured (missing google-services.json)."))
        
        return try {
            val batch = firestore.batch()
            val habitsCollection = firestore.collection("habits")

            val existingSnapshot = habitsCollection.get().await()
            val existingIds = existingSnapshot.documents.mapNotNull { it.id }.toSet()
            val localIds = habits.map { it.id.toString() }.toSet()

            val idsToDelete = existingIds - localIds
            idsToDelete.forEach { id ->
                val docRef = habitsCollection.document(id)
                batch.delete(docRef)
            }

            habits.forEach { habit ->
                val docRef = habitsCollection.document(habit.id.toString())
                batch.set(docRef, habit)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.d("FirestoreService", "Firestore sync failed (non-critical, app works offline): ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteUserProfile(): Result<Unit> {
        val firestore = firestoreOrNull()
            ?: return Result.failure(IllegalStateException("Firebase is not configured (missing google-services.json)."))

        return try {
            firestore.collection("profiles").document("main").delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveJournalEntries(entries: List<JournalEntryEntity>): Result<Unit> {
        val firestore = firestoreOrNull()
            ?: return Result.failure(IllegalStateException("Firebase is not configured (missing google-services.json)."))
        
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

    suspend fun loadJournalEntries(): Result<List<JournalEntryEntity>> {
        val firestore = firestoreOrNull()
            ?: return Result.failure(IllegalStateException("Firebase is not configured (missing google-services.json)."))
        
        return try {
            val snapshot = firestore.collection("journal_entries").get().await()
            val entries = snapshot.documents.mapNotNull { it.toObject(JournalEntryEntity::class.java) }
            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadHabits(): Result<List<HabitEntity>> {
        val firestore = firestoreOrNull()
            ?: return Result.failure(IllegalStateException("Firebase is not configured (missing google-services.json)."))

        return try {
            val snapshot = firestore.collection("habits").get().await()
            val habits = snapshot.documents.mapNotNull { it.toObject(HabitEntity::class.java) }
            Result.success(habits)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
