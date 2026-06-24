package com.example.myapplication1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: SleepEntry)

    @Query("SELECT * FROM sleep_entries WHERE date = :date LIMIT 1")
    suspend fun getEntryForDate(date: Long): SleepEntry?

    @Query("SELECT * FROM sleep_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<SleepEntry>>

    @Query("SELECT * FROM sleep_entries WHERE date >= :startDate ORDER BY date ASC")
    fun getEntriesSince(startDate: Long): Flow<List<SleepEntry>>
}
