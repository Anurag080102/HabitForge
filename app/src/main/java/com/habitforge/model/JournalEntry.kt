package com.habitforge.model

data class JournalEntry(
    val id: String,
    val date: String,
    val content: String,
    val mood: String? = null
)

