package com.habitforge.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val frequency: String = "DAILY", // DAILY or WEEKLY
    val reminderTime: String? = null, // HH:mm format
    val startDate: String, // ISO format (yyyy-MM-dd)
    val endDate: String? = null, // ISO format or null for open-ended
    val daysOfWeek: String? = null, // Comma-separated (e.g., "MON,WED,FRI") or null for daily
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)

