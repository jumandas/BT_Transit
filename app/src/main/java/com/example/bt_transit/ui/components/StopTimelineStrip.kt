package com.example.bt_transit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bt_transit.ui.map.TimelineStop
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class StopState { PASSED, CURRENT, UPCOMING }

@Composable
fun StopTimelineStrip(
    stops: List<TimelineStop>,
    currentStopIndex: Int,
    routeColor: Color,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LaunchedEffect(currentStopIndex) {
        if (currentStopIndex >= 0 && stops.isNotEmpty()) {
            listState.animateScrollToItem(currentStopIndex.coerceAtMost(stops.lastIndex))
        }
    }
    LazyColumn(state = listState, modifier = modifier.fillMaxWidth()) {
        itemsIndexed(stops, key = { _, s -> "${s.stop.stopId}_${s.stopSequence}" }) { index, stop ->
            val state = when {
                index < currentStopIndex -> StopState.PASSED
                index == currentStopIndex -> StopState.CURRENT
                else -> StopState.UPCOMING
            }
            StopTimelineRow(
                stop = stop,
                state = state,
                routeColor = routeColor,
                isFirst = index == 0,
                isLast = index == stops.lastIndex
            )
        }
    }
}

@Composable
private fun StopTimelineRow(
    stop: TimelineStop,
    state: StopState,
    routeColor: Color,
    isFirst: Boolean,
    isLast: Boolean
) {
    val dotColor = when (state) {
        StopState.PASSED -> Color.Gray.copy(alpha = 0.5f)
        StopState.CURRENT -> routeColor
        StopState.UPCOMING -> routeColor.copy(alpha = 0.85f)
    }
    val lineColor = Color.Gray.copy(alpha = 0.3f)

    Row(
        Modifier
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp)
    ) {
        // Left: vertical line + dot
        Box(
            Modifier
                .width(32.dp)
                .fillMaxHeight()
        ) {
            if (!isFirst) {
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .width(3.dp)
                        .height(28.dp)
                        .background(lineColor)
                )
            }
            if (!isLast) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .width(3.dp)
                        .fillMaxHeight()
                        .padding(top = 28.dp)
                        .background(lineColor)
                )
            }
            val dotSize = if (state == StopState.CURRENT) 18.dp else 12.dp
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 22.dp)
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(dotColor)
                    .then(
                        if (state == StopState.CURRENT)
                            Modifier.border(3.dp, routeColor.copy(alpha = 0.4f), CircleShape)
                        else Modifier
                    )
            )
        }

        // Right: stop name + ETA + scheduled time
        Row(
            Modifier
                .padding(vertical = 12.dp, horizontal = 12.dp)
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stop.stop.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (state == StopState.PASSED)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (state == StopState.CURRENT) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = "Scheduled ${formatGtfsTime(stop.scheduledTime)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val label = when (state) {
                    StopState.PASSED -> "Passed"
                    StopState.CURRENT -> "At stop"
                    StopState.UPCOMING -> stop.realtimeArrivalEpoch?.let { formatEta(it) }
                        ?: formatGtfsTime(stop.scheduledTime)
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = when (state) {
                        StopState.PASSED -> Color.Gray.copy(alpha = 0.6f)
                        StopState.CURRENT -> routeColor
                        StopState.UPCOMING ->
                            if (stop.realtimeArrivalEpoch != null) routeColor
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
                if (state == StopState.UPCOMING && stop.realtimeArrivalEpoch != null) {
                    Text(
                        text = "Live",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

private fun formatEta(epochSec: Long): String {
    val now = System.currentTimeMillis() / 1000
    val diffMin = ((epochSec - now) / 60).toInt()
    return when {
        diffMin <= 0 -> "Now"
        diffMin == 1 -> "1 min"
        diffMin < 60 -> "$diffMin min"
        else -> SimpleDateFormat("h:mm a", Locale.US).format(Date(epochSec * 1000))
    }
}

private fun formatGtfsTime(time: String): String {
    if (time.isBlank()) return "—"
    val parts = time.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: return time
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val hour12 = h % 12
    val displayH = if (hour12 == 0) 12 else hour12
    val amPm = if (h < 12 || h >= 24) "AM" else "PM"
    return "%d:%02d %s".format(displayH, m, amPm)
}
