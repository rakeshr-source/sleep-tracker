package com.example.myapplication1.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_entries")
data class SleepEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hours: Float,
    val date: Long // epoch millis, start of day
)
