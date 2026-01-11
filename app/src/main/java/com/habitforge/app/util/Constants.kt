package com.habitforge.app.util

// Frequency options for habits
enum class HabitFrequency(val value: String, val displayName: String) {
    DAILY("DAILY", "Daily"),
    WEEKLY("WEEKLY", "Weekly");

    companion object {
        fun fromValue(value: String): HabitFrequency {
            return entries.find { it.value == value } ?: DAILY
        }
    }
}

// Milestone types for community sharing
enum class MilestoneType(val value: String) {
    STREAK("STREAK"),
    COMPLETION("COMPLETION"),
    JOURNAL("JOURNAL")
}

// Mood levels for journal entries
enum class Mood(val value: Int, val emoji: String, val label: String) {
    VERY_SAD(1, "😢", "Very Sad"),
    SAD(2, "😔", "Sad"),
    NEUTRAL(3, "😐", "Neutral"),
    HAPPY(4, "🙂", "Happy"),
    VERY_HAPPY(5, "😄", "Very Happy");

    companion object {
        fun fromValue(value: Int): Mood {
            return entries.find { it.value == value } ?: NEUTRAL
        }
    }
}

