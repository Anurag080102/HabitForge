package com.habitforge.app.data.repository

import com.habitforge.app.data.local.dao.JournalDao
import com.habitforge.app.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class JournalRepositoryTest {

    private lateinit var journalDao: JournalDao
    private lateinit var repository: JournalRepository

    @Before
    fun setup() {
        journalDao = mock()
        repository = JournalRepository(journalDao)
    }

    @Test
    fun `addEntry should insert entry and return id`() = runTest {
        // Given
        whenever(journalDao.insertEntry(any())).thenReturn(1L)

        // When
        val result = repository.addEntry(
            content = "Today was great!",
            mood = 4,
            habitId = null
        )

        // Then
        assertEquals(1L, result)
        verify(journalDao).insertEntry(argThat { entry ->
            entry.content == "Today was great!" &&
            entry.mood == 4 &&
            entry.habitId == null
        })
    }

    @Test
    fun `getAllEntries returns flow of entries`() = runTest {
        // Given
        val entries = listOf(
            JournalEntryEntity(id = 1, content = "Entry 1", date = "2026-01-10"),
            JournalEntryEntity(id = 2, content = "Entry 2", date = "2026-01-09")
        )
        whenever(journalDao.getAllEntries()).thenReturn(flowOf(entries))

        // When
        val result = repository.getAllEntries().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("Entry 1", result[0].content)
    }

    @Test
    fun `deleteEntry should call dao delete`() = runTest {
        // Given
        val entry = JournalEntryEntity(id = 1, content = "Test", date = "2026-01-10")

        // When
        repository.deleteEntry(entry)

        // Then
        verify(journalDao).deleteEntry(entry)
    }

    @Test
    fun `getEntryById returns correct entry`() = runTest {
        // Given
        val entry = JournalEntryEntity(id = 1, content = "Test entry", date = "2026-01-10")
        whenever(journalDao.getEntryById(1L)).thenReturn(entry)

        // When
        val result = repository.getEntryById(1L)

        // Then
        assertNotNull(result)
        assertEquals("Test entry", result?.content)
    }
}

