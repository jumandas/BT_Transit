package com.example.bt_transit.notifications

import android.location.Location
import com.example.bt_transit.data.repository.RealtimeRepository
import com.example.bt_transit.data.repository.WaypointRepository
import com.example.bt_transit.domain.model.Vehicle
import com.example.bt_transit.domain.model.Waypoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArrivalWatcher @Inject constructor(
    private val realtimeRepo: RealtimeRepository,
    private val waypointRepo: WaypointRepository,
    private val notifier: TransitNotificationManager
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // tracks which (vehicleId, waypointId) pairs already fired so we don't spam
    private val alreadyNotified = mutableSetOf<String>()

    fun start() {
        scope.launch {
            combine(
                realtimeRepo.vehicles,
                waypointRepo.waypoints
            ) { vehicles, waypoints -> vehicles to waypoints }
                .collectLatest { (vehicles, waypoints) ->
                    checkProximity(vehicles, waypoints)
                }
        }
    }

    private fun checkProximity(vehicles: List<Vehicle>, waypoints: List<Waypoint>) {
        if (waypoints.isEmpty()) return

        val activeKeys = mutableSetOf<String>()

        for (vehicle in vehicles) {
            val routeId = vehicle.routeId ?: continue
            for (waypoint in waypoints) {
                val distanceMeters = distanceBetween(vehicle.lat, vehicle.lng, waypoint.lat, waypoint.lng)
                val key = "${vehicle.vehicleId}:${waypoint.id}"
                activeKeys.add(key)

                if (distanceMeters <= waypoint.notifyRadiusMeters && key !in alreadyNotified) {
                    val minutesAway = (distanceMeters / AVERAGE_BUS_SPEED_MPS / 60).toInt()
                    notifier.notifyBusApproaching(waypoint.label, routeId, minutesAway)
                    alreadyNotified.add(key)
                }
            }
        }

        // clear keys for buses that have left the radius so they can re-trigger later
        alreadyNotified.retainAll(activeKeys)
    }

    private fun distanceBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0]
    }

    companion object {
        private const val AVERAGE_BUS_SPEED_MPS = 8.3f // ~30 km/h
    }
}
