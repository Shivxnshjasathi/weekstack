package com.zincstate.hepta.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE dateEpochDays = :dateEpoch")
    fun getNotesForDate(dateEpoch: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE dateEpochDays = :dateEpoch AND category = :category LIMIT 1")
    fun getNoteForDateAndCategory(dateEpoch: Long, category: String): Flow<NoteEntity?>

    @Upsert
    suspend fun upsertNote(note: NoteEntity)
}
