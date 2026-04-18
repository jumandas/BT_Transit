package com.example.bt_transit.ui.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class RouteStop(val name: String, val time: String)

private data class BtRoute(
    val number: String,
    val name: String,
    val color: Color,
    val stops: List<RouteStop>
)

private val btRoutes = listOf(
    BtRoute(
        "1", "Crosstown", Color(0xFF2E7D32),
        listOf(
            RouteStop("Transit Center", "7:00 AM"),
            RouteStop("3rd & College Ave", "7:08 AM"),
            RouteStop("10th & College Ave", "7:15 AM"),
            RouteStop("17th & College Ave", "7:22 AM"),
            RouteStop("Eastland Plaza", "7:30 AM")
        )
    ),
    BtRoute(
        "2", "South", Color(0xFF00838F),
        listOf(
            RouteStop("Transit Center", "7:05 AM"),
            RouteStop("3rd & Walnut", "7:10 AM"),
            RouteStop("Curry Pike & 2nd", "7:18 AM"),
            RouteStop("Whitehall Crossing", "7:28 AM")
        )
    ),
    BtRoute(
        "3", "North", Color(0xFF6A1B9A),
        listOf(
            RouteStop("Transit Center", "7:00 AM"),
            RouteStop("4th & Rogers", "7:07 AM"),
            RouteStop("10th & Rogers", "7:14 AM"),
            RouteStop("Landmark Ave & 17th", "7:22 AM"),
            RouteStop("North Park", "7:30 AM")
        )
    ),
    BtRoute(
        "4", "West", Color(0xFFC62828),
        listOf(
            RouteStop("Transit Center", "7:10 AM"),
            RouteStop("3rd & Madison", "7:16 AM"),
            RouteStop("Bloomington Hospital", "7:24 AM"),
            RouteStop("West Side", "7:32 AM")
        )
    ),
    BtRoute(
        "5", "Southeast", Color(0xFFAD6F00),
        listOf(
            RouteStop("Transit Center", "7:15 AM"),
            RouteStop("3rd & Lincoln", "7:20 AM"),
            RouteStop("Tapp Road", "7:30 AM"),
            RouteStop("College Mall", "7:38 AM")
        )
    ),
    BtRoute(
        "6", "Campus Shuttle", Color(0xFF1565C0),
        listOf(
            RouteStop("Transit Center", "7:00 AM"),
            RouteStop("Indiana Ave & 3rd", "7:05 AM"),
            RouteStop("Sample Gates", "7:10 AM"),
            RouteStop("IMU", "7:13 AM"),
            RouteStop("10th & Fee Lane", "7:18 AM"),
            RouteStop("Stadium", "7:22 AM")
        )
    ),
    BtRoute(
        "7", "East", Color(0xFF37474F),
        listOf(
            RouteStop("Transit Center", "7:20 AM"),
            RouteStop("3rd & Dunn", "7:26 AM"),
            RouteStop("10th & Dunn", "7:33 AM"),
            RouteStop("Smallwood Plaza", "7:40 AM")
        )
    ),
    BtRoute(
        "9", "East-West", Color(0xFFE65100),
        listOf(
            RouteStop("Westside Park & Ride", "7:00 AM"),
            RouteStop("3rd & Madison", "7:10 AM"),
            RouteStop("Transit Center", "7:18 AM"),
            RouteStop("3rd & Dunn", "7:24 AM"),
            RouteStop("Smallwood Plaza", "7:35 AM")
        )
    ),
    BtRoute(
        "9L", "9 Limited", Color(0xFFBF360C),
        listOf(
            RouteStop("Westside Park & Ride", "7:00 AM"),
            RouteStop("Transit Center", "7:15 AM"),
            RouteStop("Smallwood Plaza", "7:28 AM")
        )
    ),
    BtRoute(
        "11", "IU Campus Loop", Color(0xFF880E4F),
        listOf(
            RouteStop("IMU", "7:00 AM"),
            RouteStop("10th & Fee Lane", "7:06 AM"),
            RouteStop("Stadium & 17th", "7:12 AM"),
            RouteStop("Jordan Ave & 10th", "7:18 AM"),
            RouteStop("IMU", "7:25 AM")
        )
    ),
    BtRoute(
        "12", "South Rogers", Color(0xFF1B5E20),
        listOf(
            RouteStop("Transit Center", "7:05 AM"),
            RouteStop("Rogers & 4th", "7:12 AM"),
            RouteStop("Rogers & 17th", "7:20 AM"),
            RouteStop("South Rogers Park", "7:28 AM")
        )
    ),
    BtRoute(
        "13", "North Dunn", Color(0xFF4A148C),
        listOf(
            RouteStop("Transit Center", "7:10 AM"),
            RouteStop("Dunn & 4th", "7:15 AM"),
            RouteStop("Dunn & 10th", "7:22 AM"),
            RouteStop("Dunn & 17th", "7:28 AM")
        )
    ),
    BtRoute(
        "14", "Winslow", Color(0xFF006064),
        listOf(
            RouteStop("Transit Center", "7:30 AM"),
            RouteStop("3rd & Winslow", "7:36 AM"),
            RouteStop("Winslow & Rhino's", "7:42 AM"),
            RouteStop("Winslow South End", "7:50 AM")
        )
    )
)

@Composable
fun ScheduleScreen(innerPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        ScheduleHeader()
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(btRoutes, key = { it.number }) { route ->
                RouteRow(route)
            }
        }
    }
}

@Composable
private fun ScheduleHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Schedule",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = "All active BT routes",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun RouteRow(route: BtRoute) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(route.color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = route.number,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(14.dp))

            Text(
                text = route.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                              else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                route.stops.forEachIndexed { index, stop ->
                    StopTimeRow(stop, isLast = index == route.stops.lastIndex)
                }
            }
        }
    }
}

@Composable
private fun StopTimeRow(stop: RouteStop, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 70.dp, end = 16.dp, top = 10.dp, bottom = if (isLast) 14.dp else 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stop.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stop.time,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
