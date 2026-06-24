package com.example.myapplication1.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.myapplication1.R
import com.example.myapplication1.SleepViewModel
import com.example.myapplication1.data.SleepEntry
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun StatsScreen(viewModel: SleepViewModel) {
    val allEntries by viewModel.allEntries.collectAsState()
    val context = LocalContext.current

    val now = LocalDate.now()
    val weekAgo = now.minusDays(6)
    val monthAgo = now.minusDays(29)

    val weekEntries = allEntries.filter { entryDate(it) >= weekAgo }
    val monthEntries = allEntries.filter { entryDate(it) >= monthAgo }

    val weekAvg = if (weekEntries.isNotEmpty()) weekEntries.map { it.hours }.average().toFloat() else 0f
    val monthAvg = if (monthEntries.isNotEmpty()) monthEntries.map { it.hours }.average().toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sleep Stats", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = { exportAndShare(context, allEntries) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_share),
                    contentDescription = "Export"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Summary cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Week avg",
                value = if (weekAvg > 0) "%.1fh".format(weekAvg) else "—",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Month avg",
                value = if (monthAvg > 0) "%.1fh".format(monthAvg) else "—",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Entries",
                value = "${allEntries.size}",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Weekly chart
        if (weekEntries.isNotEmpty()) {
            Text("Last 7 Days", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            WeeklyChart(entries = weekEntries.sortedBy { it.date })
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Monthly chart
        if (monthEntries.isNotEmpty()) {
            Text("Last 30 Days", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            SleepChart(entries = monthEntries.sortedBy { it.date })
        }

        if (allEntries.isEmpty()) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                "No data yet. Log some sleep!",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SleepChart(entries: List<SleepEntry>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val dotColor = MaterialTheme.colorScheme.onPrimaryContainer

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        if (entries.isEmpty()) return@Canvas

        val maxH = 12f
        val minH = 0f
        val w = size.width
        val h = size.height
        val padding = 8.dp.toPx()

        val chartW = w - padding * 2
        val chartH = h - padding * 2

        val path = Path()
        var first = true

        entries.forEachIndexed { index, entry ->
            val x = padding + (index.toFloat() / (entries.size - 1).coerceAtLeast(1)) * chartW
            val y = padding + chartH - ((entry.hours - minH) / (maxH - minH)) * chartH

            if (first) {
                path.moveTo(x, y)
                first = false
            } else {
                path.lineTo(x, y)
            }

            // Draw dot
            drawCircle(
                color = dotColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw baseline at 8h
        val baselineY = padding + chartH - ((8f - minH) / (maxH - minH)) * chartH
        drawLine(
            color = Color.Gray.copy(alpha = 0.4f),
            start = Offset(padding, baselineY),
            end = Offset(w - padding, baselineY),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
private fun WeeklyChart(entries: List<SleepEntry>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val dotColor = MaterialTheme.colorScheme.onPrimaryContainer
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        if (entries.isEmpty()) return@Canvas

        val maxH = 12f
        val minH = 0f
        val w = size.width
        val padding = 8.dp.toPx()
        val topPadding = 24.dp.toPx()

        val chartW = w - padding * 2
        val chartH = size.height - topPadding - padding

        val path = Path()
        var first = true

        entries.forEachIndexed { index, entry ->
            val x = padding + (index.toFloat() / (entries.size - 1).coerceAtLeast(1)) * chartW
            val y = topPadding + chartH - ((entry.hours - minH) / (maxH - minH)) * chartH

            if (first) {
                path.moveTo(x, y)
                first = false
            } else {
                path.lineTo(x, y)
            }

            drawCircle(
                color = dotColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )

            // Weekday label on top
            val dayName = entryDate(entry).dayOfWeek.name.take(3)
            val textResult = textMeasurer.measure(
                text = dayName,
                style = TextStyle(fontSize = 10.sp, color = labelColor)
            )
            drawText(
                textLayoutResult = textResult,
                topLeft = Offset(x - textResult.size.width / 2f, 4.dp.toPx())
            )
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw baseline at 8h
        val baselineY = topPadding + chartH - ((8f - minH) / (maxH - minH)) * chartH
        drawLine(
            color = Color.Gray.copy(alpha = 0.4f),
            start = Offset(padding, baselineY),
            end = Offset(w - padding, baselineY),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun entryDate(entry: SleepEntry): LocalDate {
    return Instant.ofEpochMilli(entry.date)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

private fun exportAndShare(context: Context, entries: List<SleepEntry>) {
    if (entries.isEmpty()) return

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val csv = buildString {
        appendLine("Date,Hours")
        entries.sortedBy { it.date }.forEach { entry ->
            val date = Instant.ofEpochMilli(entry.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(formatter)
            appendLine("$date,${entry.hours}")
        }
    }

    val file = File(context.cacheDir, "sleep_log_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.csv")
    file.writeText(csv)

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "My Sleep Log")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share sleep data"))
}
