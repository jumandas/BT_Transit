package com.example.bt_transit.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bt_transit.data.repository.RealtimeRepository
import com.example.bt_transit.data.repository.TransitRepository
import com.example.bt_transit.domain.model.GeoPoint
import com.example.bt_transit.domain.model.Route
import com.example.bt_transit.domain.model.ScheduledStop
import com.example.bt_transit.domain.model.Stop
import com.example.bt_transit.domain.model.TripUpdate
import com.example.bt_transit.domain.model.Vehicle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SelectedBusInfo(
    val vehicle: Vehicle,
    val route: Route?,
    val stops: List<Stop>,
    val etaByStopId: Map<String, Long?>,
    val currentStopIndex: Int
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val transitRepo: TransitRepository,
    private val realtimeRepo: RealtimeRepository
) : ViewModel() {

    val routes: StateFlow<List<Route>> = transitRepo.observeRoutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val vehicles: StateFlow<List<Vehicle>> = realtimeRepo.vehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tripUpdates: StateFlow<List<TripUpdate>> = realtimeRepo.tripUpdates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // routeId -> list of shape points
    private val _shapes = MutableStateFlow<Map<String, List<GeoPoint>>>(emptyMap())
    val shapes: StateFlow<Map<String, List<GeoPoint>>> = _shapes.asStateFlow()

    // selected bus bottom sheet
    private val _selectedBus = MutableStateFlow<SelectedBusInfo?>(null)
    val selectedBus: StateFlow<SelectedBusInfo?> = _selectedBus.asStateFlow()

    init {
        viewModelScope.launch {
            transitRepo.observeRoutes().collect { routeList ->
                val loaded = _shapes.value.toMutableMap()
                for (route in routeList) {
                    if (route.routeId in loaded) continue
                    val points = transitRepo.getShapeForRoute(route.routeId)
                    if (points.isNotEmpty()) {
                        loaded[route.routeId] = points
                    }
                }
                _shapes.value = loaded
            }
        }
    }

    fun selectBus(vehicle: Vehicle) {
        viewModelScope.launch {
            val tripId = vehicle.tripId
            if (tripId == null) {
                _selectedBus.value = SelectedBusInfo(vehicle, null, emptyList(), emptyMap(), -1)
                return@launch
            }

            val route = transitRepo.getRouteForTrip(tripId)
            val scheduled = transitRepo.getScheduledStopsForTrip(tripId)
            val stops = scheduled.map { it.stop }

            // merge realtime ETAs from trip_updates
            val tripUpdate = tripUpdates.value.find { it.tripId == tripId }
            val etaMap = mutableMapOf<String, Long?>()
            for (s in scheduled) {
                val realtimeEta = tripUpdate?.updates?.find { it.stopId == s.stop.stopId }
                etaMap[s.stop.stopId] = realtimeEta?.arrivalEpochSec
            }

            // figure out which stop the bus is currently at
            val busLat = vehicle.lat
            val busLng = vehicle.lng
            val currentIdx = if (stops.isEmpty()) -1 else {
                stops.indices.minByOrNull { i ->
                    val s = stops[i]
                    (s.lat - busLat) * (s.lat - busLat) + (s.lng - busLng) * (s.lng - busLng)
                } ?: -1
            }

            _selectedBus.value = SelectedBusInfo(vehicle, route, stops, etaMap, currentIdx)
        }
    }

    fun dismissBusSheet() {
        _selectedBus.value = null
    }
}
