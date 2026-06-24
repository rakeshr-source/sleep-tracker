package com.example.myapplication1

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication1.data.SleepDatabase
import com.example.myapplication1.data.SleepEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class SleepViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = SleepDatabase.getInstance(application).sleepDao()

    val allEntries: StateFlow<List<SleepEntry>> = dao.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    // null = still loading, true/false = loaded
    private val _selectedDayLogged = MutableStateFlow<Boolean?>(null)
    val selectedDayLogged: StateFlow<Boolean?> = _selectedDayLogged

    private val _selectedDayHours = MutableStateFlow(0f)
    val selectedDayHours: StateFlow<Float> = _selectedDayHours

    init {
        loadSelectedDay()
    }

    fun navigateDay(offset: Int) {
        val newDate = _selectedDate.value.plusDays(offset.toLong())
        if (newDate <= LocalDate.now()) {
            _selectedDate.value = newDate
            _selectedDayLogged.value = null // show loading while fetching
            loadSelectedDay()
        }
    }

    fun logSleepForSelectedDay(hours: Float) {
        viewModelScope.launch {
            val epoch = dateToEpoch(_selectedDate.value)
            val existing = dao.getEntryForDate(epoch)
            if (existing != null) {
                dao.insert(existing.copy(hours = hours))
            } else {
                dao.insert(SleepEntry(hours = hours, date = epoch))
            }
            _selectedDayLogged.value = true
            _selectedDayHours.value = hours
        }
    }

    fun resetSelectedDay() {
        _selectedDayLogged.value = false
        _selectedDayHours.value = 0f
    }

    private fun loadSelectedDay() {
        viewModelScope.launch {
            val epoch = dateToEpoch(_selectedDate.value)
            val entry = dao.getEntryForDate(epoch)
            if (entry != null) {
                _selectedDayLogged.value = true
                _selectedDayHours.value = entry.hours
            } else {
                _selectedDayLogged.value = false
                _selectedDayHours.value = 0f
            }
        }
    }

    private fun dateToEpoch(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}
