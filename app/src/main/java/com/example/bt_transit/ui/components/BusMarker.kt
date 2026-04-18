package com.example.bt_transit.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.example.bt_transit.domain.model.Vehicle
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun AnimatedBusMarker(
    vehicle: Vehicle,
    onClick: () -> Unit = {}
) {
    // smoothly animate between old and new positions over ~9s
    // so the bus glides instead of jumping each 10s poll
    val lat by animateFloatAsState(
        targetValue = vehicle.lat.toFloat(),
        animationSpec = tween(durationMillis = 9000),
        label = "busLat_${vehicle.vehicleId}"
    )
    val lng by animateFloatAsState(
        targetValue = vehicle.lng.toFloat(),
        animationSpec = tween(durationMillis = 9000),
        label = "busLng_${vehicle.vehicleId}"
    )

    Marker(
        state = rememberMarkerState(
            key = vehicle.vehicleId,
            position = LatLng(lat.toDouble(), lng.toDouble())
        ),
        title = "Route ${vehicle.routeId ?: "Unknown"}",
        snippet = "Vehicle ${vehicle.vehicleId}",
        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
        rotation = vehicle.bearing ?: 0f,
        onClick = { onClick(); true }
    )
}
