package com.example.bt_transit.ui.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bt_transit.ui.components.AnimatedBusMarker
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

private val BLOOMINGTON = LatLng(39.1653, -86.5264)

@Composable
fun MapScreen(
    innerPadding: PaddingValues,
    vm: MapViewModel = hiltViewModel()
) {
    val routes by vm.routes.collectAsStateWithLifecycle()
    val vehicles by vm.vehicles.collectAsStateWithLifecycle()
    val shapes by vm.shapes.collectAsStateWithLifecycle()

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

        // Animated bus markers — glide between poll updates
        vehicles.forEach { vehicle ->
            AnimatedBusMarker(vehicle = vehicle)
        }
    }
}
