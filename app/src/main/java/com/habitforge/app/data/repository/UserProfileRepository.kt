package com.habitforge.app.data.repository

import com.habitforge.app.data.local.dao.UserProfileDao
import com.habitforge.app.data.local.entity.UserProfileEntity
import com.habitforge.app.data.remote.firebase.FirestoreService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val firestoreService: FirestoreService
) {
    fun getProfile(): Flow<UserProfileEntity?> = userProfileDao.getProfile()

    // Save to both local and remote
    suspend fun saveProfile(profile: UserProfileEntity) {
        println("[UserProfileRepository] saveProfile called with: $profile")
        userProfileDao.insertOrUpdate(profile)
        val result = firestoreService.saveUserProfile(profile)
        if (result.isSuccess) {
            println("[UserProfileRepository] Firestore save successful")
        } else {
            println("[UserProfileRepository] Firestore save failed: ${result.exceptionOrNull()}")
        }
    }

    // Fetch from Firestore and update local if remote data is newer or local is empty
    suspend fun syncProfileFromRemote() {
        val remoteResult = firestoreService.loadUserProfile()
        if (remoteResult.isSuccess) {
            val remoteProfile = remoteResult.getOrNull()
            if (remoteProfile != null) {
                userProfileDao.insertOrUpdate(remoteProfile)
            }
        }
        // else: ignore errors, keep local data
    }

    suspend fun clearProfile() {
        userProfileDao.clearProfile()
        // Attempt to delete remote profile as well
        val result = firestoreService.deleteUserProfile()
        if (result.isSuccess) {
            println("[UserProfileRepository] remote profile deleted")
        } else {
            println("[UserProfileRepository] failed to delete remote profile: ${result.exceptionOrNull()}")
        }
    }
}
