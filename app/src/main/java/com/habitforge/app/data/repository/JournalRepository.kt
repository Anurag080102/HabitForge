package com.habitforge.app.data.repository

import com.habitforge.app.data.local.dao.JournalDao
import com.habitforge.app.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRepository @Inject constructor(
    private val journalDao: JournalDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    // Get all journal entries
    fun getAllEntries(): Flow<List<JournalEntryEntity>> = journalDao.getAllEntries()

    // Get entry by ID
    suspend fun getEntryById(entryId: Long): JournalEntryEntity? = journalDao.getEntryById(entryId)

    // Get entries for today
    fun getTodayEntries(): Flow<List<JournalEntryEntity>> {
        val today = LocalDate.now().format(dateFormatter)
        return journalDao.getEntriesForDate(today)
    }

    // Get entries for a specific date
    fun getEntriesForDate(date: LocalDate): Flow<List<JournalEntryEntity>> =
        journalDao.getEntriesForDate(date.format(dateFormatter))

    // Get entries linked to a habit
    fun getEntriesForHabit(habitId: Long): Flow<List<JournalEntryEntity>> =
        journalDao.getEntriesForHabit(habitId)

    // Add new entry
    suspend fun addEntry(content: String, mood: Int = 3, habitId: Long? = null): Long {
        val entry = JournalEntryEntity(
            content = content,
            mood = mood,
            date = LocalDate.now().format(dateFormatter),
            habitId = habitId
        )
        return journalDao.insertEntry(entry)
    }

    // Update entry
    suspend fun updateEntry(entry: JournalEntryEntity) = journalDao.updateEntry(entry)

    // Delete entry
    suspend fun deleteEntry(entry: JournalEntryEntity) = journalDao.deleteEntry(entry)

    // Get entries for date range
    fun getEntriesInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<JournalEntryEntity>> =
        journalDao.getEntriesInRange(
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        )
}

