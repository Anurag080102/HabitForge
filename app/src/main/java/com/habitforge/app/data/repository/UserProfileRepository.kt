package com.habitforge.app.data.repository

import com.habitforge.app.data.local.dao.UserProfileDao
import com.habitforge.app.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val userProfileDao: UserProfileDao
) {
    fun getProfile(): Flow<UserProfileEntity?> = userProfileDao.getProfile()
    suspend fun saveProfile(profile: UserProfileEntity) = userProfileDao.insertOrUpdate(profile)
    suspend fun clearProfile() = userProfileDao.clearProfile()
}
