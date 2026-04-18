package com.example.bt_transit.ui.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bt_transit.data.repository.FavoritesRepository
import com.example.bt_transit.data.repository.RealtimeRepository
import com.example.bt_transit.data.repository.TransitRepository
import com.example.bt_transit.data.repository.WeatherRepository
import com.example.bt_transit.domain.model.TripUpdate
import com.example.bt_transit.domain.model.Vehicle
import com.example.bt_transit.domain.model.WeatherInfo
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Calendar
import javax.inject.Inject
import kotlin.coroutines.resume

data class NearbyRouteUiModel(
    val routeId: String,
    val shortName: String,
    val longName: String,
    val color: Int,
    val nextArrivalLabel: String,
    val stopName: String,
    val stopId: String
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transitRepo: TransitRepository,
    private val realtimeRepo: RealtimeRepository,
    private val weatherRepo: WeatherRepository,
    private val favoritesRepo: FavoritesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val BLOOMINGTON_LAT = 39.1653
        private const val BLOOMINGTON_LNG = -86.5264
    }

    val vehicles: StateFlow<List<Vehicle>> = realtimeRepo.vehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val weather: StateFlow<WeatherInfo?> = weatherRepo.weather
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val favorites: StateFlow<Set<String>> = favoritesRepo.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _nearbyRoutes = MutableStateFlow<List<NearbyRouteUiModel>>(emptyList())
    val nearbyRoutes: StateFlow<List<NearbyRouteUiModel>> = _nearbyRoutes.asStateFlow()

    private val _isLoadingNearby = MutableStateFlow(true)
    val isLoadingNearby: StateFlow<Boolean> = _isLoadingNearby.asStateFlow()

    init {
        viewModelScope.launch { loadNearbyRoutes(BLOOMINGTON_LAT, BLOOMINGTON_LNG) }
        viewModelScope.launch {
            realtimeRepo.tripUpdates.collect { updates ->
                if (_nearbyRoutes.value.isNotEmpty()) refreshRealtimeEtas(updates)
            }
        }
    }

    fun onLocationGranted(lat: Double, lng: Double) {
        viewModelScope.launch { loadNearbyRoutes(lat, lng) }
    }

    @SuppressLint("MissingPermission")
    fun tryLoadWithDeviceLocation() {
        viewModelScope.launch {
            val (lat, lng) = try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                suspendCancellableCoroutine { cont ->
                    client.lastLocation
                        .addOnSuccessListener { loc ->
                            cont.resume(
                                if (loc != null) Pair(loc.latitude, loc.longitude)
                                else Pair(BLOOMINGTON_LAT, BLOOMINGTON_LNG)
                            )
                        }
                        .addOnFailureListener { cont.resume(Pair(BLOOMINGTON_LAT, BLOOMINGTON_LNG)) }
                }
            } catch (e: Exception) {
                Pair(BLOOMINGTON_LAT, BLOOMINGTON_LNG)
            }
            loadNearbyRoutes(lat, lng)
        }
    }

    private suspend fun loadNearbyRoutes(lat: Double, lng: Double) {
        _isLoadingNearby.value = true
        val nearbyStops = transitRepo.findStopsNear(lat, lng, limit = 10)
        if (nearbyStops.isEmpty()) { _isLoadingNearby.value = false; return }

        val stopRouteMap = transitRepo.stopRouteIndex()
        val allRoutes = transitRepo.observeRoutes().first()
        val routeById = allRoutes.associateBy { it.routeId }

        val added = mutableSetOf<String>()
        val result = mutableListOf<NearbyRouteUiModel>()
        val currentTime = currentTimeStr()

        for (stop in nearbyStops) {
            val routeIds = stopRouteMap[stop.stopId] ?: continue
            for (routeId in routeIds) {
                if (routeId in added) continue
                val route = routeById[routeId] ?: continue
                added.add(routeId)

                val nextDep = transitRepo.getNextDeparturesForStop(routeId, stop.stopId, currentTime, 1)
                val etaLabel = nextDep.firstOrNull()?.let { staticEtaLabel(it) } ?: "—"

                result.add(
                    NearbyRouteUiModel(
                        routeId = routeId,
                        shortName = route.shortName,
                        longName = route.longName,
                        color = route.color,
                        nextArrivalLabel = etaLabel,
                        stopName = stop.name,
                        stopId = stop.stopId
                    )
                )
                if (result.size >= 5) break
            }
            if (result.size >= 5) break
        }

        _nearbyRoutes.value = sortWithFavorites(result)
        _isLoadingNearby.value = false
    }

    private fun sortWithFavorites(routes: List<NearbyRouteUiModel>): List<NearbyRouteUiModel> {
        val favs = favoritesRepo.favorites.value
        return routes.sortedWith(compareByDescending { it.routeId in favs })
    }

    fun toggleFavorite(routeId: String): Boolean {
        val result = favoritesRepo.toggleFavorite(routeId)
        _nearbyRoutes.value = sortWithFavorites(_nearbyRoutes.value)
        return result
    }

    private fun refreshRealtimeEtas(updates: List<TripUpdate>) {
        val now = System.currentTimeMillis() / 1000
        val updated = _nearbyRoutes.value.map { item ->
            val realtimeEpoch = updates
                .filter { it.routeId == item.routeId }
                .flatMap { tu -> tu.updates.filter { it.stopId == item.stopId } }
                .mapNotNull { it.arrivalEpochSec }
                .filter { it > now }
                .minOrNull()

            if (realtimeEpoch != null) {
                val diffMin = ((realtimeEpoch - now) / 60).toInt()
                item.copy(nextArrivalLabel = when {
                    diffMin <= 0 -> "Now"
                    diffMin == 1 -> "1 min"
                    else -> "$diffMin min"
                })
            } else item
        }
        _nearbyRoutes.value = sortWithFavorites(updated)
    }

    private fun currentTimeStr(): String {
        val cal = Calendar.getInstance()
        return "%02d:%02d:%02d".format(
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            cal.get(Calendar.SECOND)
        )
    }

    private fun staticEtaLabel(timeStr: String): String {
        val parts = timeStr.split(":")
        val schedMin = (parts.getOrNull(0)?.toIntOrNull() ?: return "—") * 60 +
                       (parts.getOrNull(1)?.toIntOrNull() ?: 0)
        val cal = Calendar.getInstance()
        val nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val diff = schedMin - nowMin
        return when {
            diff < 0 -> "—"
            diff == 0 -> "Now"
            diff == 1 -> "1 min"
            else -> "$diff min"
        }
    }
}
