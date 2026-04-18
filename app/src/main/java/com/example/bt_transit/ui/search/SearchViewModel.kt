package com.example.bt_transit.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bt_transit.data.repository.RealtimeRepository
import com.example.bt_transit.data.repository.TransitRepository
import com.example.bt_transit.domain.model.TripUpdate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class SearchResult(
    val name: String,
    val address: String,
    val isStop: Boolean = false,
    val stopId: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)

data class RouteRecommendation(
    val routeId: String,
    val shortName: String,
    val longName: String,
    val color: Int,
    val nearestStopName: String,
    val nextEtaLabel: String,
    val hasRealtime: Boolean
)

data class RecommendationState(
    val destination: SearchResult,
    val recommendations: List<RouteRecommendation>,
    val isLoading: Boolean
)

// Handful of well-known Bloomington landmarks so the search feels useful even
// before users learn the exact stop names.
private val BLOOMINGTON_PLACES = listOf(
    SearchResult("Indiana University", "107 S Indiana Ave, Bloomington",
        lat = 39.1684, lng = -86.5227),
    SearchResult("BT Transit Center", "130 W Grimes Ln, Bloomington",
        lat = 39.1533, lng = -86.5382),
    SearchResult("Sample Gates", "Kirkwood Ave & Indiana Ave",
        lat = 39.1671, lng = -86.5264),
    SearchResult("College Mall", "2815 E 3rd St, Bloomington",
        lat = 39.1640, lng = -86.4900),
    SearchResult("Bloomington Hospital", "601 W 2nd St, Bloomington",
        lat = 39.1646, lng = -86.5438),
    SearchResult("Monroe County Library", "303 E Kirkwood Ave",
        lat = 39.1668, lng = -86.5287),
    SearchResult("IMU (Indiana Memorial Union)", "900 E 7th St",
        lat = 39.1712, lng = -86.5200),
    SearchResult("IU Assembly Hall", "1001 E 17th St",
        lat = 39.1808, lng = -86.5225),
    SearchResult("Switchyard Park", "1601 S Rogers St",
        lat = 39.1493, lng = -86.5374),
    SearchResult("Eastland Plaza", "E 3rd St & Clarizz Blvd",
        lat = 39.1638, lng = -86.4855),
    SearchResult("IU Sample Gates", "Kirkwood Ave & Indiana Ave",
        lat = 39.1671, lng = -86.5264),
    SearchResult("Kelley School of Business", "1309 E 10th St",
        lat = 39.1736, lng = -86.5158),
    SearchResult("IU Stadium", "701 E 17th St",
        lat = 39.1810, lng = -86.5260)
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val transitRepo: TransitRepository,
    private val realtimeRepo: RealtimeRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<SearchResult>>(emptyList())
    val results: StateFlow<List<SearchResult>> = _results.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _recommendation = MutableStateFlow<RecommendationState?>(null)
    val recommendation: StateFlow<RecommendationState?> = _recommendation.asStateFlow()

    init {
        viewModelScope.launch {
            _query.debounce(250).collectLatest { q -> executeSearch(q) }
        }
    }

    fun onQueryChange(q: String) { _query.value = q }

    fun dismissRecommendation() {
        _recommendation.value = null
    }

    /**
     * User picked a destination — figure out which BT routes serve the stop (or nearest
     * stop for a landmark) and pull live ETAs from trip_updates if available.
     */
    fun selectDestination(dest: SearchResult) {
        _recommendation.value = RecommendationState(dest, emptyList(), isLoading = true)

        viewModelScope.launch {
            val stop = when {
                dest.isStop && dest.stopId != null -> transitRepo.getStopById(dest.stopId)
                dest.lat != null && dest.lng != null ->
                    transitRepo.nearestStop(dest.lat, dest.lng)
                else -> null
            }

            if (stop == null) {
                _recommendation.value = RecommendationState(dest, emptyList(), isLoading = false)
                return@launch
            }

            val routes = transitRepo.routesServingStop(stop.stopId)
            if (routes.isEmpty()) {
                _recommendation.value = RecommendationState(dest, emptyList(), isLoading = false)
                return@launch
            }

            val currentTime = currentTimeStr()
            val tripUpdates = realtimeRepo.tripUpdates.first()
            val now = System.currentTimeMillis() / 1000

            val recs = routes.map { route ->
                val realtimeEpoch = tripUpdates
                    .filter { it.routeId == route.routeId }
                    .flatMap { tu -> tu.updates.filter { it.stopId == stop.stopId } }
                    .mapNotNull { it.arrivalEpochSec }
                    .filter { it > now }
                    .minOrNull()

                val etaLabel: String
                val hasRealtime: Boolean
                if (realtimeEpoch != null) {
                    etaLabel = formatLiveEta(realtimeEpoch - now)
                    hasRealtime = true
                } else {
                    val nextDep = transitRepo.getNextDeparturesForStop(
                        route.routeId, stop.stopId, currentTime, 1
                    ).firstOrNull()
                    etaLabel = nextDep?.let { formatScheduledEta(it) } ?: "No more today"
                    hasRealtime = false
                }

                RouteRecommendation(
                    routeId = route.routeId,
                    shortName = route.shortName,
                    longName = route.longName,
                    color = route.color,
                    nearestStopName = stop.name,
                    nextEtaLabel = etaLabel,
                    hasRealtime = hasRealtime
                )
            }.sortedBy { it.shortName }

            _recommendation.value = RecommendationState(dest, recs, isLoading = false)
        }
    }

    private suspend fun executeSearch(q: String) {
        if (q.length < 2) { _results.value = emptyList(); return }
        _isSearching.value = true

        val stops = transitRepo.searchStops(q).map { stop ->
            SearchResult(
                name = stop.name,
                address = "BT Stop",
                isStop = true,
                stopId = stop.stopId,
                lat = stop.lat,
                lng = stop.lng
            )
        }
        val places = BLOOMINGTON_PLACES.filter {
            it.name.contains(q, ignoreCase = true) || it.address.contains(q, ignoreCase = true)
        }
        _results.value = stops + places
        _isSearching.value = false
    }

    private fun currentTimeStr(): String {
        val cal = Calendar.getInstance()
        return "%02d:%02d:%02d".format(
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            cal.get(Calendar.SECOND)
        )
    }

    private fun formatLiveEta(seconds: Long): String {
        val min = (seconds / 60).toInt()
        return when {
            min <= 0 -> "Now"
            min == 1 -> "1 min"
            else -> "$min min"
        }
    }

    private fun formatScheduledEta(time: String): String {
        val parts = time.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: return time
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val cal = Calendar.getInstance()
        val nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val schedMin = h * 60 + m
        val diff = schedMin - nowMin
        return when {
            diff < 0 -> "—"
            diff == 0 -> "Now"
            diff == 1 -> "1 min"
            diff < 60 -> "$diff min"
            else -> {
                val hour12 = h % 12
                val displayH = if (hour12 == 0) 12 else hour12
                val amPm = if (h < 12) "AM" else "PM"
                "%d:%02d %s".format(displayH, m, amPm)
            }
        }
    }
}
