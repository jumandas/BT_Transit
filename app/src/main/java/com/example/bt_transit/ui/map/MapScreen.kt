package com.example.bt_transit.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.example.bt_transit.domain.model.Route
import com.example.bt_transit.ui.components.AnimatedBusMarker
import com.example.bt_transit.ui.components.StopMarker
import com.example.bt_transit.ui.components.StopTimelineStrip
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private val BLOOMINGTON = LatLng(39.1653, -86.5264)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    innerPadding: PaddingValues,
    focusedRouteId: String? = null,
    vm: MapViewModel = hiltViewModel()
) {
    val routes by vm.routes.collectAsStateWithLifecycle()
    val vehicles by vm.vehicles.collectAsStateWithLifecycle()
    val shapes by vm.shapes.collectAsStateWithLifecycle()
    val selectedBus by vm.selectedBus.collectAsStateWithLifecycle()
    val selectedStop by vm.selectedStop.collectAsStateWithLifecycle()
    val currentFocus by vm.focusedRouteId.collectAsStateWithLifecycle()
    val focusedStops by vm.focusedStops.collectAsStateWithLifecycle()
    val selectedDirection by vm.selectedDirection.collectAsStateWithLifecycle()
    val directionShape by vm.directionShape.collectAsStateWithLifecycle()
    val directionTripIds by vm.directionTripIds.collectAsStateWithLifecycle()
    val arrivingFilterEnabled by vm.arrivingFilterEnabled.collectAsStateWithLifecycle()
    val arrivingTripIds by vm.arrivingTripIds.collectAsStateWithLifecycle()
    val onBoardTripId by vm.onBoardTripId.collectAsStateWithLifecycle()
    val ratingSubmitted by vm.ratingSubmitted.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var locationEnabled by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> locationEnabled = granted }
    LaunchedEffect(Unit) {
        if (!locationEnabled) permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(locationEnabled) {
        if (!locationEnabled) return@LaunchedEffect
        try {
            @Suppress("MissingPermission")
            val loc = suspendCancellableCoroutine<android.location.Location?> { cont ->
                LocationServices.getFusedLocationProviderClient(context).lastLocation
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(null) }
            }
            if (loc != null) vm.setUserLocation(loc.latitude, loc.longitude)
        } catch (_: Exception) {}
    }

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(BLOOMINGTON, 13f)
    }

    LaunchedEffect(focusedRouteId) { vm.setFocusedRoute(focusedRouteId) }

    // When focus or vehicles change, pan to the focused bus
    LaunchedEffect(currentFocus, vehicles) {
        val focus = currentFocus ?: return@LaunchedEffect
        val bus = vehicles.firstOrNull { it.routeId == focus } ?: return@LaunchedEffect
        cameraState.animate(
            update = CameraUpdateFactory.newLatLngZoom(LatLng(bus.lat, bus.lng), 15f),
            durationMs = 900
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        RouteFilterBar(
            routes = routes,
            selectedRouteId = currentFocus,
            onRouteClick = { id -> vm.toggleFocusedRoute(id) },
            onClear = { vm.setFocusedRoute(null) },
            colorFor = { id -> vm.colorForRoute(id) }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraState,
                properties = MapProperties(isTrafficEnabled = false, isMyLocationEnabled = locationEnabled),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false
                )
            ) {
                // Polylines — focused route renders twice (wide glow + narrow bold)
                routes.forEach { route ->
                    val points = shapes[route.routeId] ?: return@forEach
                    val ll = if (currentFocus == route.routeId && selectedDirection != null && directionShape.isNotEmpty())
                        directionShape.map { LatLng(it.lat, it.lng) }
                    else
                        points.map { LatLng(it.lat, it.lng) }
                    val color = Color(vm.colorForRoute(route.routeId))

                    when {
                        currentFocus == null -> {
                            Polyline(points = ll, color = color.copy(alpha = 0.8f), width = 7f, zIndex = 0f)
                        }
                        currentFocus == route.routeId -> {
                            // outer glow
                            Polyline(
                                points = ll,
                                color = color.copy(alpha = 0.25f),
                                width = 28f,
                                zIndex = 0f
                            )
                            // inner bold line
                            Polyline(
                                points = ll,
                                color = color,
                                width = 12f,
                                zIndex = 1f
                            )
                        }
                        else -> { /* hidden while filtered */ }
                    }
                }

                // Stops along focused route (clickable)
                if (currentFocus != null) {
                    val stopColor = vm.colorForRoute(currentFocus)
                    focusedStops.forEach { stop ->
                        StopMarker(
                            stop = stop,
                            routeColor = stopColor,
                            onClick = { vm.selectStop(stop) }
                        )
                    }
                }

                // Direction arrows — chevrons placed along route every N shape points
                if (currentFocus != null && selectedDirection != null && directionShape.size >= 2) {
                    val arrowIcon = remember(currentFocus) {
                        directionArrowDescriptor()
                    }
                    val step = maxOf(4, directionShape.size / 12)
                    for (i in 0 until directionShape.size - 1 step step) {
                        val from = directionShape[i]
                        val to = directionShape[i + 1]
                        val bearing = bearingBetween(from.lat, from.lng, to.lat, to.lng)
                        Marker(
                            state = remember(i, currentFocus) { MarkerState(LatLng(from.lat, from.lng)) },
                            icon = arrowIcon,
                            rotation = bearing,
                            flat = true,
                            anchor = Offset(0.5f, 0.5f),
                            zIndex = 1.5f,
                            alpha = 0.85f,
                            onClick = { false }
                        )
                    }
                }

                // Live buses — filtered to focused route + direction if set
                vehicles.forEach { vehicle ->
                    val visible = when {
                        arrivingFilterEnabled && arrivingTripIds.isNotEmpty() ->
                            vehicle.tripId != null && vehicle.tripId in arrivingTripIds
                        arrivingFilterEnabled && arrivingTripIds.isEmpty() -> false
                        currentFocus == null -> true
                        directionTripIds.isNotEmpty() ->
                            vehicle.routeId == currentFocus &&
                            (vehicle.tripId == null || vehicle.tripId in directionTripIds)
                        else -> vehicle.routeId == currentFocus
                    }
                    if (!visible) return@forEach
                    AnimatedBusMarker(
                        vehicle = vehicle,
                        routeColor = vm.routeColorFor(vehicle),
                        routeShortName = vm.shortNameFor(vehicle.routeId),
                        onClick = { vm.selectBus(vehicle) }
                    )
                }
            }

            // Focused route banner (overlay)
            currentFocus?.let { rid ->
                val route = routes.firstOrNull { it.routeId == rid }
                val busCount = vehicles.count { it.routeId == rid }
                if (route != null) {
                    FocusedRouteBanner(
                        route = route,
                        color = Color(vm.colorForRoute(rid)),
                        busCount = busCount,
                        stopCount = focusedStops.size,
                        selectedDirection = selectedDirection,
                        onDirectionSelect = { vm.setDirection(it) },
                        onClear = { vm.setFocusedRoute(null) },
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }

            if (currentFocus == null) {
                FilterChip(
                    selected = arrivingFilterEnabled,
                    onClick = { vm.toggleArrivingFilter() },
                    label = { Text("Arriving near me") },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 12.dp, bottom = 16.dp)
                )
            }
        }
    }

    // --- Bus detail sheet ---
    selectedBus?.let { info ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            onDismissRequest = { vm.dismissBusSheet() },
            sheetState = sheetState
        ) {
            val routeColor = info.route?.let { Color(vm.colorForRoute(it.routeId)) }
                ?: MaterialTheme.colorScheme.primary
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(routeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = info.route?.shortName ?: "?",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = info.route?.longName ?: "Vehicle ${info.vehicle.vehicleId}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Bus ${info.vehicle.vehicleId} · ${info.stops.size} stops",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LegendDot(Color.Gray.copy(alpha = 0.5f), "Passed")
                    LegendDot(routeColor, "Current / Upcoming")
                }
                Spacer(Modifier.height(12.dp))
                if (info.stops.isNotEmpty()) {
                    StopTimelineStrip(
                        stops = info.stops,
                        currentStopIndex = info.currentStopIndex,
                        routeColor = routeColor,
                        modifier = Modifier.height(420.dp)
                    )
                } else {
                    Text(
                        text = "No stop information available for this trip.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                val isOnBoard = info.vehicle.tripId != null && onBoardTripId == info.vehicle.tripId
                val routeId = info.route?.routeId

                if (!isOnBoard && !ratingSubmitted) {
                    OutlinedButton(
                        onClick = { info.vehicle.tripId?.let { vm.boardBus(it) } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("I'm on this bus")
                    }
                } else if (isOnBoard && !ratingSubmitted) {
                    var selectedStars by remember { mutableIntStateOf(0) }
                    var commentText by remember { mutableStateOf("") }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Rate this trip",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { selectedStars = star }) {
                                Icon(
                                    imageVector = if (star <= selectedStars) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star stars",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("Comment (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { vm.alightBus() }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (selectedStars > 0) {
                                    vm.submitRating(info.vehicle.tripId, routeId, selectedStars, commentText)
                                }
                            },
                            enabled = selectedStars > 0,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Submit")
                        }
                    }
                } else if (ratingSubmitted) {
                    Text(
                        text = "Thanks for your feedback!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // --- Stop arrivals sheet ---
    selectedStop?.let { info ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { vm.dismissStopSheet() },
            sheetState = sheetState
        ) {
            StopArrivalsSheet(info = info, colorFor = { id -> vm.colorForRoute(id) })
        }
    }
}

@Composable
private fun FocusedRouteBanner(
    route: Route,
    color: Color,
    busCount: Int,
    stopCount: Int,
    selectedDirection: Int?,
    onDirectionSelect: (Int?) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 6.dp,
        color = Color.White
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = route.shortName,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = route.longName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A),
                        maxLines = 1
                    )
                    Text(
                        text = "$busCount live · $stopCount stops",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1A1A1A).copy(alpha = 0.55f)
                    )
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onClear, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear filter",
                        tint = Color(0xFF1A1A1A).copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DirectionChip(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    label = "Westbound",
                    selected = selectedDirection == 1,
                    color = color,
                    onClick = { onDirectionSelect(if (selectedDirection == 1) null else 1) },
                    modifier = Modifier.weight(1f)
                )
                DirectionChip(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    label = "Eastbound",
                    selected = selectedDirection == 0,
                    color = color,
                    onClick = { onDirectionSelect(if (selectedDirection == 0) null else 0) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DirectionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) color else color.copy(alpha = 0.08f),
        modifier = modifier
            .clickable { onClick() }
            .border(1.dp, color.copy(alpha = if (selected) 0f else 0.35f), RoundedCornerShape(50))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color.White else color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else color
            )
        }
    }
}

@Composable
private fun RouteFilterBar(
    routes: List<Route>,
    selectedRouteId: String?,
    onRouteClick: (String) -> Unit,
    onClear: () -> Unit,
    colorFor: (String) -> Int
) {
    val headerGradient = Brush.horizontalGradient(
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)
        )
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().background(headerGradient)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Live Map",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = if (selectedRouteId == null) "Showing every active route"
                            else {
                                val r = routes.firstOrNull { it.routeId == selectedRouteId }
                                "Filtered to Route ${r?.shortName ?: selectedRouteId}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                }
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    AllRoutesChip(
                        selected = selectedRouteId == null,
                        onClick = onClear
                    )
                }
                items(routes, key = { it.routeId }) { route ->
                    RouteChip(
                        route = route,
                        color = Color(colorFor(route.routeId)),
                        selected = selectedRouteId == route.routeId,
                        onClick = { onRouteClick(route.routeId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AllRoutesChip(selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .clickable { onClick() }
            .then(
                if (!selected) Modifier.border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(50)
                ) else Modifier
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.DirectionsBus,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "All",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RouteChip(
    route: Route,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) color else MaterialTheme.colorScheme.surface,
        shadowElevation = if (selected) 4.dp else 0.dp,
        modifier = Modifier
            .clickable { onClick() }
            .border(
                width = if (selected) 0.dp else 1.5.dp,
                color = if (selected) Color.Transparent else color.copy(alpha = 0.6f),
                shape = RoundedCornerShape(50)
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (selected) Color.White.copy(alpha = 0.3f) else color)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = route.shortName.ifBlank { route.routeId },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else color
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun StopArrivalsSheet(
    info: SelectedStopInfo,
    colorFor: (String) -> Int
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bus Stop",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
                Text(
                    text = info.stop.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        when {
            info.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            info.arrivals.isEmpty() -> {
                Text(
                    text = "No routes serve this stop right now.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
            else -> {
                Text(
                    text = "Upcoming arrivals",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.height(
                        (56 * info.arrivals.size.coerceAtMost(8)).dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(info.arrivals, key = { it.route.routeId }) { line ->
                        ArrivalRow(line, Color(colorFor(line.route.routeId)))
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private fun bearingBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
    val dLng = Math.toRadians(lng2 - lng1)
    val lat1r = Math.toRadians(lat1)
    val lat2r = Math.toRadians(lat2)
    val y = sin(dLng) * cos(lat2r)
    val x = cos(lat1r) * sin(lat2r) - sin(lat1r) * cos(lat2r) * cos(dLng)
    return ((Math.toDegrees(atan2(y, x)).toFloat() + 360f) % 360f)
}

private fun directionArrowDescriptor(): BitmapDescriptor {
    val size = 40
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
        setShadowLayer(3f, 0f, 1f, 0x88000000.toInt())
    }
    val path = android.graphics.Path()
    path.moveTo(size / 2f, 2f)
    path.lineTo(size - 4f, size - 4f)
    path.lineTo(size / 2f, size * 0.68f)
    path.lineTo(4f, size - 4f)
    path.close()
    canvas.drawPath(path, paint)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@Composable
private fun ArrivalRow(line: StopArrivalLine, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = line.route.shortName.ifBlank { line.route.routeId },
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = line.route.longName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (line.hasRealtime) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32))
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "Live",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            text = "Scheduled",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            Text(
                text = line.etaLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
