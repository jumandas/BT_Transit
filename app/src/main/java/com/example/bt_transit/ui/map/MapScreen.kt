package com.example.bt_transit.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bt_transit.ui.components.AnimatedBusMarker
import com.example.bt_transit.ui.components.StopTimelineStrip
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

private val BLOOMINGTON = LatLng(39.1653, -86.5264)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    innerPadding: PaddingValues,
    vm: MapViewModel = hiltViewModel()
) {
    val routes by vm.routes.collectAsStateWithLifecycle()
    val vehicles by vm.vehicles.collectAsStateWithLifecycle()
    val shapes by vm.shapes.collectAsStateWithLifecycle()
    val selectedBus by vm.selectedBus.collectAsStateWithLifecycle()

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(BLOOMINGTON, 13f)
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        cameraPositionState = cameraState,
        properties = MapProperties(isTrafficEnabled = false)
    ) {
        // Route polylines
        routes.forEach { route ->
            val points = shapes[route.routeId]
            if (!points.isNullOrEmpty()) {
                Polyline(
                    points = points.map { LatLng(it.lat, it.lng) },
                    color = Color(route.color),
                    width = 8f
                )
            }
        }

        // Animated bus markers
        vehicles.forEach { vehicle ->
            AnimatedBusMarker(
                vehicle = vehicle,
                onClick = { vm.selectBus(vehicle) }
            )
        }
    }

    // Bottom sheet when a bus is tapped
    selectedBus?.let { info ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

        ModalBottomSheet(
            onDismissRequest = { vm.dismissBusSheet() },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Route header
                Text(
                    text = if (info.route != null)
                        "Route ${info.route.shortName} — ${info.route.longName}"
                    else
                        "Vehicle ${info.vehicle.vehicleId}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Bus ${info.vehicle.vehicleId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(16.dp))

                // Stop timeline
                if (info.stops.isNotEmpty()) {
                    val routeColor = info.route?.color?.let { Color(it) }
                        ?: MaterialTheme.colorScheme.primary
                    StopTimelineStrip(
                        stops = info.stops,
                        etaByStopId = info.etaByStopId,
                        currentStopIndex = info.currentStopIndex,
                        routeColor = routeColor,
                        modifier = Modifier.height(400.dp)
                    )
                } else {
                    Text(
                        text = "No stop information available for this trip.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
