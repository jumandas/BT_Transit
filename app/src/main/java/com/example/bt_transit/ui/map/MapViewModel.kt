package com.example.bt_transit.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bt_transit.data.repository.RealtimeRepository
import com.example.bt_transit.data.repository.TransitRepository
import com.example.bt_transit.domain.model.GeoPoint
import com.example.bt_transit.domain.model.Route
import com.example.bt_transit.domain.model.Vehicle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val transitRepo: TransitRepository,
    realtimeRepo: RealtimeRepository
) : ViewModel() {

    val routes: StateFlow<List<Route>> = transitRepo.observeRoutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val vehicles: StateFlow<List<Vehicle>> = realtimeRepo.vehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // routeId -> list of shape points
    private val _shapes = MutableStateFlow<Map<String, List<GeoPoint>>>(emptyMap())
    val shapes: StateFlow<Map<String, List<GeoPoint>>> = _shapes.asStateFlow()

    init {
        // load shapes once routes are available
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
}
