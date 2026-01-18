package com.habitforge.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1L, // Single user profile
    val name: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val preferredLanguage: String = "en"
)
