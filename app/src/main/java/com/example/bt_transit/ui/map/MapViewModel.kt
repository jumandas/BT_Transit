package com.example.bt_transit.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bt_transit.data.repository.RatingRepository
import com.example.bt_transit.data.repository.RealtimeRepository
import com.example.bt_transit.data.repository.TransitRepository
import com.example.bt_transit.domain.model.GeoPoint
import com.example.bt_transit.domain.model.Route
import com.example.bt_transit.domain.model.Stop
import com.example.bt_transit.domain.model.TripUpdate
import com.example.bt_transit.domain.model.Vehicle
import com.example.bt_transit.ui.theme.displayColor
import com.example.bt_transit.ui.theme.displayColorForRouteId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimelineStop(
    val stop: Stop,
    val scheduledTime: String,
    val realtimeArrivalEpoch: Long?,
    val stopSequence: Int
)

data class SelectedBusInfo(
    val vehicle: Vehicle,
    val route: Route?,
    val stops: List<TimelineStop>,
    val currentStopIndex: Int
)

data class StopArrivalLine(
    val route: Route,
    val etaLabel: String,
    val hasRealtime: Boolean
)

data class SelectedStopInfo(
    val stop: Stop,
    val arrivals: List<StopArrivalLine>,
    val isLoading: Boolean
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val transitRepo: TransitRepository,
    private val realtimeRepo: RealtimeRepository,
    private val ratingRepo: RatingRepository
) : ViewModel() {

    val routes: StateFlow<List<Route>> = transitRepo.observeRoutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val vehicles: StateFlow<List<Vehicle>> = realtimeRepo.vehicles
        .map { list ->
            list.map { v ->
                if (v.routeId != null) v
                else v.copy(
                    routeId = transitRepo.getRouteForTrip(v.tripId ?: return@map v)?.routeId
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tripUpdates: StateFlow<List<TripUpdate>> = realtimeRepo.tripUpdates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _shapes = MutableStateFlow<Map<String, List<GeoPoint>>>(emptyMap())
    val shapes: StateFlow<Map<String, List<GeoPoint>>> = _shapes.asStateFlow()

    private val _selectedBus = MutableStateFlow<SelectedBusInfo?>(null)
    val selectedBus: StateFlow<SelectedBusInfo?> = _selectedBus.asStateFlow()

    private val _selectedStop = MutableStateFlow<SelectedStopInfo?>(null)
    val selectedStop: StateFlow<SelectedStopInfo?> = _selectedStop.asStateFlow()

    private val _focusedRouteId = MutableStateFlow<String?>(null)
    val focusedRouteId: StateFlow<String?> = _focusedRouteId.asStateFlow()

    private val _focusedStops = MutableStateFlow<List<Stop>>(emptyList())
    val focusedStops: StateFlow<List<Stop>> = _focusedStops.asStateFlow()

    private val _selectedDirection = MutableStateFlow<Int?>(null)
    val selectedDirection: StateFlow<Int?> = _selectedDirection.asStateFlow()

    private val _directionShape = MutableStateFlow<List<GeoPoint>>(emptyList())
    val directionShape: StateFlow<List<GeoPoint>> = _directionShape.asStateFlow()

    private val _directionTripIds = MutableStateFlow<Set<String>>(emptySet())
    val directionTripIds: StateFlow<Set<String>> = _directionTripIds.asStateFlow()

    private val _arrivingFilterEnabled = MutableStateFlow(false)
    val arrivingFilterEnabled: StateFlow<Boolean> = _arrivingFilterEnabled.asStateFlow()

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)

    private val _arrivingTripIds = MutableStateFlow<Set<String>>(emptySet())
    val arrivingTripIds: StateFlow<Set<String>> = _arrivingTripIds.asStateFlow()

    private val _onBoardTripId = MutableStateFlow<String?>(null)
    val onBoardTripId: StateFlow<String?> = _onBoardTripId.asStateFlow()

    private val _ratingSubmitted = MutableStateFlow(false)
    val ratingSubmitted: StateFlow<Boolean> = _ratingSubmitted.asStateFlow()

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

        viewModelScope.launch {
            combine(focusedRouteId, _selectedDirection) { routeId, dir ->
                Pair(routeId, dir)
            }.collect { (routeId, dir) ->
                _focusedStops.value = when {
                    routeId == null -> emptyList()
                    dir != null -> transitRepo.stopsOnRouteAndDirection(routeId, dir)
                    else -> transitRepo.stopsOnRoute(routeId)
                }
                _directionShape.value = when {
                    routeId != null && dir != null ->
                        transitRepo.getShapeForRouteAndDirection(routeId, dir)
                    else -> emptyList()
                }
                _directionTripIds.value = when {
                    routeId != null && dir != null ->
                        transitRepo.getTripIdsForRouteAndDirection(routeId, dir)
                    else -> emptySet()
                }
            }
        }
    }

    fun setFocusedRoute(routeId: String?) {
        _focusedRouteId.value = routeId
        if (routeId == null) _selectedDirection.value = null
    }

    fun setDirection(directionId: Int?) {
        _selectedDirection.value = directionId
    }

    fun toggleFocusedRoute(routeId: String) {
        _focusedRouteId.value = if (_focusedRouteId.value == routeId) null else routeId
    }

    fun colorForRoute(routeId: String?): Int =
        displayColorForRouteId(routeId, routes.value)

    fun routeColorFor(vehicle: Vehicle): Int = colorForRoute(vehicle.routeId)

    fun shortNameFor(routeId: String?): String {
        if (routeId == null) return "?"
        return routes.value.firstOrNull { it.routeId == routeId }
            ?.shortName?.ifBlank { routeId } ?: routeId
    }

    fun selectBus(vehicle: Vehicle) {
        viewModelScope.launch {
            val tripId = vehicle.tripId
            if (tripId == null) {
                _selectedBus.value = SelectedBusInfo(vehicle, null, emptyList(), -1)
                return@launch
            }

            val route = transitRepo.getRouteForTrip(tripId)
            val scheduled = transitRepo.getScheduledStopsForTrip(tripId)
            val tripUpdate = tripUpdates.value.find { it.tripId == tripId }
            val timeline = scheduled.map { s ->
                val realtimeEta = tripUpdate?.updates?.find { it.stopId == s.stop.stopId }
                TimelineStop(
                    stop = s.stop,
                    scheduledTime = s.arrivalTime.ifBlank { s.departureTime },
                    realtimeArrivalEpoch = realtimeEta?.arrivalEpochSec,
                    stopSequence = s.stopSequence
                )
            }

            val busLat = vehicle.lat
            val busLng = vehicle.lng
            val currentIdx = if (timeline.isEmpty()) -1 else {
                timeline.indices.minByOrNull { i ->
                    val s = timeline[i].stop
                    (s.lat - busLat) * (s.lat - busLat) + (s.lng - busLng) * (s.lng - busLng)
                } ?: -1
            }

            _selectedBus.value = SelectedBusInfo(vehicle, route, timeline, currentIdx)
        }
    }

    fun dismissBusSheet() {
        _selectedBus.value = null
        _onBoardTripId.value = null
        _ratingSubmitted.value = false
    }

    fun boardBus(tripId: String) {
        _onBoardTripId.value = tripId
        _ratingSubmitted.value = false
    }

    fun alightBus() {
        _onBoardTripId.value = null
        _ratingSubmitted.value = false
    }

    fun submitRating(tripId: String?, routeId: String?, stars: Int, comment: String) {
        viewModelScope.launch {
            try {
                ratingRepo.submitRating(tripId, routeId, stars, comment)
                _ratingSubmitted.value = true
            } catch (_: Exception) {
                // insert failed; leave ratingSubmitted false so user can retry
            }
        }
    }

    fun selectStop(stop: Stop) {
        _selectedStop.value = SelectedStopInfo(stop, emptyList(), isLoading = true)
        viewModelScope.launch {
            val stopRoutes = transitRepo.routesServingStop(stop.stopId)
            val now = System.currentTimeMillis() / 1000
            val updates = tripUpdates.value
            val currentTime = nowHhMmSs()

            val arrivals = stopRoutes.map { route ->
                val realtimeEpoch = updates
                    .filter { it.routeId == route.routeId }
                    .flatMap { tu -> tu.updates.filter { it.stopId == stop.stopId } }
                    .mapNotNull { it.arrivalEpochSec }
                    .filter { it > now }
                    .minOrNull()

                val label: String
                val hasRealtime: Boolean
                if (realtimeEpoch != null) {
                    val diffMin = ((realtimeEpoch - now) / 60).toInt()
                    label = when {
                        diffMin <= 0 -> "Now"
                        diffMin == 1 -> "1 min"
                        else -> "$diffMin min"
                    }
                    hasRealtime = true
                } else {
                    val nextDep = transitRepo.getNextDeparturesForStop(
                        route.routeId, stop.stopId, currentTime, 1
                    ).firstOrNull()
                    label = nextDep?.let { scheduleLabel(it) } ?: "No more today"
                    hasRealtime = false
                }
                StopArrivalLine(route, label, hasRealtime)
            }.sortedBy { it.route.shortName }

            _selectedStop.value = SelectedStopInfo(stop, arrivals, isLoading = false)
        }
    }

    fun dismissStopSheet() {
        _selectedStop.value = null
    }

    private fun nowHhMmSs(): String {
        val cal = java.util.Calendar.getInstance()
        return "%02d:%02d:%02d".format(
            cal.get(java.util.Calendar.HOUR_OF_DAY),
            cal.get(java.util.Calendar.MINUTE),
            cal.get(java.util.Calendar.SECOND)
        )
    }

    private fun scheduleLabel(time: String): String {
        val parts = time.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: return time
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val cal = java.util.Calendar.getInstance()
        val nowMin = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
        val diff = h * 60 + m - nowMin
        return when {
            diff < 0 -> "—"
            diff == 0 -> "Now"
            diff == 1 -> "1 min"
            diff < 60 -> "$diff min"
            else -> {
                val h12 = h % 12
                val dh = if (h12 == 0) 12 else h12
                val amPm = if (h < 12) "AM" else "PM"
                "%d:%02d %s".format(dh, m, amPm)
            }
        }
    }

    fun setUserLocation(lat: Double, lng: Double) {
        _userLocation.value = Pair(lat, lng)
        if (_arrivingFilterEnabled.value) refreshArrivingFilter()
    }

    fun toggleArrivingFilter(): Boolean {
        _userLocation.value ?: return false
        _arrivingFilterEnabled.value = !_arrivingFilterEnabled.value
        if (_arrivingFilterEnabled.value) refreshArrivingFilter()
        else _arrivingTripIds.value = emptySet()
        return true
    }

    private fun refreshArrivingFilter() {
        val loc = _userLocation.value ?: return
        viewModelScope.launch {
            val cal = java.util.Calendar.getInstance()
            val nowStr = "%02d:%02d:%02d".format(
                cal.get(java.util.Calendar.HOUR_OF_DAY),
                cal.get(java.util.Calendar.MINUTE),
                cal.get(java.util.Calendar.SECOND)
            )
            cal.add(java.util.Calendar.MINUTE, 30)
            val plusStr = "%02d:%02d:%02d".format(
                cal.get(java.util.Calendar.HOUR_OF_DAY),
                cal.get(java.util.Calendar.MINUTE),
                cal.get(java.util.Calendar.SECOND)
            )
            _arrivingTripIds.value = transitRepo.getActiveTripIdsNear(
                loc.first, loc.second, 500.0, nowStr, plusStr
            )
        }
    }
}

fun Route.mapDisplayColor(): Int = displayColor()
