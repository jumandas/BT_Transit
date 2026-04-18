package com.example.bt_transit.ui.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bt_transit.domain.model.Route
import com.example.bt_transit.domain.model.ScheduledStop
import com.example.bt_transit.domain.model.Stop

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    innerPadding: PaddingValues,
    vm: ScheduleViewModel = hiltViewModel()
) {
    val routes by vm.routes.collectAsStateWithLifecycle()
    val departures by vm.departures.collectAsStateWithLifecycle()
    val tripDetail by vm.tripDetail.collectAsStateWithLifecycle()
    val selectedDay by vm.selectedDay.collectAsStateWithLifecycle()
    val plannerFrom by vm.plannerFrom.collectAsStateWithLifecycle()
    val plannerTo by vm.plannerTo.collectAsStateWithLifecycle()
    val plannerResult by vm.plannerResult.collectAsStateWithLifecycle()
    val plannerLoading by vm.plannerLoading.collectAsStateWithLifecycle()
    val stopSearchResults by vm.stopSearchResults.collectAsStateWithLifecycle()
    val scheduledReminders by vm.scheduledReminders.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        ScheduleHeader()
        if (routes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            DayChipRow(selectedDay = selectedDay, onDaySelected = { vm.setDay(it) })
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ArrivalPlannerSection(
                        fromStop = plannerFrom,
                        toStop = plannerTo,
                        result = plannerResult,
                        isLoading = plannerLoading,
                        searchResults = stopSearchResults,
                        onSearch = { vm.searchStopsFor(it) },
                        onClearSearch = { vm.clearSearchResults() },
                        onFromSelected = { vm.setFromStop(it) },
                        onToSelected = { vm.setToStop(it) },
                        onClear = { vm.clearPlanner() }
                    )
                }
                items(routes, key = { it.routeId }) { route ->
                    RouteRow(
                        route = route,
                        departureTimes = departures["${route.routeId}:$selectedDay"],
                        selectedDay = selectedDay,
                        scheduledReminders = scheduledReminders,
                        onExpand = { vm.loadDeparturesFor(route.routeId) },
                        onDepartureClick = { timeStr -> vm.openTripDetail(route, timeStr) },
                        onReminderToggle = { timeStr -> vm.toggleReminder(route.shortName, timeStr, selectedDay) }
                    )
                }
            }
        }
    }

    // Trip detail bottom sheet with full stop list
    tripDetail?.let { detail ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { vm.dismissTripDetail() },
            sheetState = sheetState
        ) {
            TripScheduleSheet(detail)
        }
    }
}

@Composable
private fun ScheduleHeader() {
    val primary = MaterialTheme.colorScheme.primary
    val gradient = androidx.compose.ui.graphics.Brush.verticalGradient(
        colors = listOf(primary, primary.copy(alpha = 0.85f))
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient)
            .padding(horizontal = 22.dp, vertical = 24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.size(12.dp))
                Column {
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Pick a route, then a time to see every stop",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayChipRow(selectedDay: Int, onDaySelected: (Int) -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedDay == 0,
                onClick = { onDaySelected(0) },
                label = { Text("Today") }
            )
            FilterChip(
                selected = selectedDay == 1,
                onClick = { onDaySelected(1) },
                label = { Text("Tomorrow") }
            )
        }
        if (selectedDay == 1) {
            Text(
                text = "Schedule may vary on holidays and special service days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RouteRow(
    route: Route,
    departureTimes: List<String>?,
    selectedDay: Int,
    scheduledReminders: Set<String>,
    onExpand: () -> Unit,
    onDepartureClick: (String) -> Unit,
    onReminderToggle: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = !expanded
                    if (expanded) onExpand()
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(route.color)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = route.shortName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.longName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

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
            Column(Modifier.padding(bottom = 12.dp)) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(12.dp))

                when {
                    departureTimes == null -> {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Loading departures…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    departureTimes.isEmpty() -> {
                        Text(
                            text = if (selectedDay == 0) "No more departures today" else "No departures scheduled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = "Tap a time to see every stop",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            departureTimes.forEach { timeStr ->
                                val cal = java.util.Calendar.getInstance()
                                cal.add(java.util.Calendar.DAY_OF_YEAR, selectedDay)
                                val dateStr = "%04d%02d%02d".format(
                                    cal.get(java.util.Calendar.YEAR),
                                    cal.get(java.util.Calendar.MONTH) + 1,
                                    cal.get(java.util.Calendar.DAY_OF_MONTH)
                                )
                                DepartureChipWithBell(
                                    timeStr = timeStr,
                                    routeColor = Color(route.color),
                                    hasReminder = scheduledReminders.contains("${route.shortName}|$timeStr|$dateStr"),
                                    onClick = { onDepartureClick(timeStr) },
                                    onBellClick = { onReminderToggle(timeStr) }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun DepartureChip(
    timeStr: String,
    routeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = routeColor.copy(alpha = 0.12f),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.AccessTime,
                contentDescription = null,
                tint = routeColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = formatGtfsTime(timeStr),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = routeColor
            )
        }
    }
}

@Composable
private fun DepartureChipWithBell(
    timeStr: String,
    routeColor: Color,
    hasReminder: Boolean,
    onClick: () -> Unit,
    onBellClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = routeColor.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.clickable { onClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = routeColor,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = formatGtfsTime(timeStr),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = routeColor
                )
            }
            IconButton(
                onClick = onBellClick
            ) {
                Icon(
                    imageVector = if (hasReminder) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                    contentDescription = if (hasReminder) "Cancel reminder" else "Set reminder",
                    tint = if (hasReminder) routeColor else routeColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun TripScheduleSheet(detail: TripScheduleDetail) {
    val routeColor = Color(detail.route.color)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(routeColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = detail.route.shortName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = detail.route.longName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "Departs ${formatGtfsTime(detail.firstDepartureTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        when {
            detail.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = routeColor)
                }
            }
            detail.stops.isEmpty() -> {
                Text(
                    text = "No stop data for this trip.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
            else -> {
                Text(
                    text = "${detail.stops.size} stops",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.height(480.dp)) {
                    itemsIndexed(
                        items = detail.stops,
                        key = { _, s -> "${s.stop.stopId}_${s.stopSequence}" }
                    ) { index, scheduled ->
                        TripStopRow(
                            scheduled = scheduled,
                            routeColor = routeColor,
                            isFirst = index == 0,
                            isLast = index == detail.stops.lastIndex
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun TripStopRow(
    scheduled: ScheduledStop,
    routeColor: Color,
    isFirst: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left rail: dot + connecting lines
        Box(
            modifier = Modifier
                .size(width = 28.dp, height = 56.dp)
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(width = 2.dp, height = 28.dp)
                        .background(routeColor.copy(alpha = 0.5f))
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(width = 2.dp, height = 28.dp)
                        .background(routeColor.copy(alpha = 0.5f))
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(routeColor)
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = scheduled.stop.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = 2
        )
        Text(
            text = formatGtfsTime(
                scheduled.arrivalTime.ifBlank { scheduled.departureTime }
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = routeColor
        )
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

@Composable
private fun ArrivalPlannerSection(
    fromStop: Stop?,
    toStop: Stop?,
    result: PlannerResult?,
    isLoading: Boolean,
    searchResults: List<Stop>,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    onFromSelected: (Stop) -> Unit,
    onToSelected: (Stop) -> Unit,
    onClear: () -> Unit
) {
    var expandedPlanner by remember { mutableStateOf(false) }
    var pickingFrom by remember { mutableStateOf(false) }
    var pickingTo by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedPlanner = !expandedPlanner }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Arrival Planner",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expandedPlanner) Icons.Default.KeyboardArrowUp
                                  else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            AnimatedVisibility(
                visible = expandedPlanner,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // From stop picker
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pickingFrom = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = fromStop?.name ?: "From stop…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (fromStop == null)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Icon(
                        Icons.Default.SwapVert,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(4.dp))

                    // To stop picker
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pickingTo = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00BFA5))
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = toStop?.name ?: "To stop…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (toStop == null)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    when {
                        isLoading -> CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(24.dp),
                            strokeWidth = 2.dp
                        )
                        result != null -> PlannerResultCard(result)
                        fromStop != null && toStop != null ->
                            Text(
                                text = "No direct route — try searching nearby stops",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                    }

                    if (fromStop != null || toStop != null) {
                        TextButton(onClick = onClear) { Text("Clear") }
                    }
                }
            }
        }
    }

    // Stop search dialog
    if (pickingFrom || pickingTo) {
        Dialog(
            onDismissRequest = {
                pickingFrom = false
                pickingTo = false
                searchQuery = ""
                onClearSearch()
            }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = if (pickingFrom) "Pick From Stop" else "Pick To Stop",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { q ->
                            searchQuery = q
                            onSearch(q)
                        },
                        label = { Text("Stop name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        items(searchResults, key = { it.stopId }) { stop ->
                            Text(
                                text = stop.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (pickingFrom) onFromSelected(stop)
                                        else onToSelected(stop)
                                        pickingFrom = false
                                        pickingTo = false
                                        searchQuery = ""
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlannerResultCard(result: PlannerResult) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = primary.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Depart ${formatGtfsTime(result.departureTime)}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = primary
                )
                Text(
                    text = "Arrive ${formatGtfsTime(result.arrivalTime)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Text(
                text = "${result.durationMin} min",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = primary
            )
        }
    }
}
