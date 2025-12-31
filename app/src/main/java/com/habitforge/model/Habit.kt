package com.habitforge.model

data class Habit(
    val id: String,
    val name: String,
    val frequency: HabitFrequency,
    val streak: Int,
    val totalCompletions: Int,
    val reminderEnabled: Boolean
)

enum class HabitFrequency {
    DAILY, WEEKLY
}

