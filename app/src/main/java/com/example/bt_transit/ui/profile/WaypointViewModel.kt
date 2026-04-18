package com.example.bt_transit.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bt_transit.data.repository.WaypointRepository
import com.example.bt_transit.domain.model.Waypoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WaypointViewModel @Inject constructor(
    private val repo: WaypointRepository
) : ViewModel() {

    val waypoints: StateFlow<List<Waypoint>> = repo.waypoints.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun addWaypoint(label: String, lat: Double, lng: Double) {
        if (label.isBlank()) return
        viewModelScope.launch {
            repo.add(Waypoint(label = label.trim(), lat = lat, lng = lng))
        }
    }

    fun removeWaypoint(waypoint: Waypoint) {
        viewModelScope.launch {
            repo.remove(waypoint.id)
        }
    }
}
