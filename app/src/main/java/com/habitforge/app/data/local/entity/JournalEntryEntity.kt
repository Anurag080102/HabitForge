package com.habitforge.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val mood: Int = 3, // 1-5 scale
    val date: String, // yyyy-MM-dd format
    val habitId: Long? = null, // Optional link to a habit
    val createdAt: Long = System.currentTimeMillis()
)

