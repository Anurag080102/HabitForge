package com.habitforge.app.data.remote.firebase

// Model for community feed posts stored in Firestore
data class CommunityPost(
    val id: String = "",
    val odge: String = "",  // Anonymized user badge/ID
    val milestoneType: String = "", // STREAK, COMPLETION, JOURNAL
    val milestoneValue: Int = 0,
    val habitName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0
)

