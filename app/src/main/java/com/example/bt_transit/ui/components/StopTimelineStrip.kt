package com.example.bt_transit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.bt_transit.domain.model.Stop
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class StopState { PASSED, CURRENT, UPCOMING }

@Composable
fun StopTimelineStrip(
    stops: List<Stop>,
    etaByStopId: Map<String, Long?>,
    currentStopIndex: Int,
    routeColor: Color,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        itemsIndexed(stops, key = { _, s -> s.stopId }) { index, stop ->
            val state = when {
                index < currentStopIndex -> StopState.PASSED
                index == currentStopIndex -> StopState.CURRENT
                else -> StopState.UPCOMING
            }
            StopTimelineRow(
                stop = stop,
                eta = etaByStopId[stop.stopId],
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
    stop: Stop,
    eta: Long?,
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
            // upper line segment
            if (!isFirst) {
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .width(3.dp)
                        .height(28.dp)
                        .background(lineColor)
                )
            }
            // lower line segment
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
            // dot
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

        // Right: stop name + ETA
        Column(
            Modifier
                .padding(vertical = 12.dp, horizontal = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = stop.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (state == StopState.PASSED)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (state == StopState.CURRENT) FontWeight.Bold else FontWeight.Normal
            )
            eta?.let {
                Text(
                    text = formatEta(it),
                    style = MaterialTheme.typography.labelMedium,
                    color = routeColor
                )
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
