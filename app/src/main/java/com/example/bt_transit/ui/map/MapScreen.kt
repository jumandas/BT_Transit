package com.example.bt_transit.ui.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState

private val BLOOMINGTON = LatLng(39.1653, -86.5264)

@Composable
fun MapScreen(
    innerPadding: PaddingValues,
    vm: MapViewModel = hiltViewModel()
) {
    val vehicles by vm.vehicles.collectAsStateWithLifecycle()
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
        vehicles.forEach { vehicle ->
            Marker(
                state = rememberMarkerState(position = LatLng(vehicle.lat, vehicle.lng)),
                title = "Route ${vehicle.routeId ?: "Unknown"}",
                snippet = "Vehicle ${vehicle.vehicleId}",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )
        }
    }
}
