package com.example.myapplication1.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication1.R
import com.example.myapplication1.SleepViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun LogScreen(viewModel: SleepViewModel) {
    val logged by viewModel.selectedDayLogged.collectAsState()
    val hours by viewModel.selectedDayHours.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val today = LocalDate.now()

    val dateLabel = when (selectedDate) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Date navigation
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateDay(-1) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_left),
                    contentDescription = "Previous day"
                )
            }
            Text(text = dateLabel, style = MaterialTheme.typography.titleMedium)
            IconButton(
                onClick = { viewModel.navigateDay(1) },
                enabled = selectedDate < today
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right),
                    contentDescription = "Next day"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (logged) {
            null -> {
                // Still loading from DB — show nothing to prevent flicker
            }

            true -> {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = "Logged",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Logged: ${formatHours(hours)}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                FilterChip(
                    selected = false,
                    onClick = { viewModel.resetSelectedDay() },
                    label = { Text("Change") }
                )
            }

            false -> {
                Text(
                    text = "Hours slept?",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(24.dp))

                val options = listOf(
                    2f, 2.5f, 3f, 3.5f,
                    4f, 4.5f, 5f, 5.5f,
                    6f, 6.5f, 7f, 7.5f,
                    8f, 8.5f, 9f, 9.5f,
                    10f, 10.5f, 11f, 12f
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    items(options) { h ->
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.logSleepForSelectedDay(h) },
                            label = {
                                Box(
                                    modifier = Modifier.width(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = formatHours(h),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatHours(hours: Float): String {
    return if (hours % 1f == 0f) "${hours.toInt()}h" else "${hours}h"
}
