package com.example.bt_transit

import android.app.Application
import android.util.Log
import com.example.bt_transit.data.local.BTDatabase
import com.example.bt_transit.data.repository.RealtimeRepository
import com.example.bt_transit.data.repository.TransitRepository
import com.example.bt_transit.notifications.ArrivalWatcher
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltAndroidApp
class BTApplication : Application() {

    @Inject lateinit var transitRepo: TransitRepository
    @Inject lateinit var realtimeRepo: RealtimeRepository
    @Inject lateinit var db: BTDatabase
    @Inject lateinit var arrivalWatcher: ArrivalWatcher

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // TEMP: verifies the pipeline end-to-end until M3 wires a proper first-launch flow.
        scope.launch {
            try {
                if (!transitRepo.isSynced()) {
                    val ms = measureTimeMillis { transitRepo.syncStaticFeed() }
                    Log.i(TAG, "Static sync complete in ${ms}ms")
                } else {
                    Log.i(TAG, "Static feed already cached; skipping sync")
                }
                Log.i(
                    TAG,
                    "Room counts -> stops=${db.stopDao().count()} " +
                        "(expect ~300 for BT)"
                )
            } catch (t: Throwable) {
                Log.e(TAG, "Static sync failed", t)
            }
        }
        realtimeRepo.vehicles
            .take(3)
            .onEach { list ->
                Log.i(TAG, "vehicles tick: ${list.size}")
                val v = list.firstOrNull { it.tripId != null } ?: return@onEach
                val route = transitRepo.getRouteForTrip(v.tripId!!)
                val schedule = transitRepo.getScheduledStopsForTrip(v.tripId!!)
                Log.i(
                    TAG,
                    "bus ${v.vehicleId} trip=${v.tripId} -> " +
                        "route=${route?.shortName ?: "?"} " +
                        "schedule=${schedule.size} stops; " +
                        "next=${schedule.firstOrNull()?.stop?.name}"
                )

                val rid = route?.routeId ?: return@onEach
                val stopsOnRoute = transitRepo.stopsOnRoute(rid)
                val shapes = transitRepo.getShapesForRoute(rid)
                Log.i(
                    TAG,
                    "route $rid -> stopsOnRoute=${stopsOnRoute.size}, " +
                        "shapes=${shapes.size} (points: ${shapes.map { it.size }})"
                )
                val stop0 = stopsOnRoute.firstOrNull()
                if (stop0 != null) {
                    val byId = transitRepo.getStopById(stop0.stopId)
                    val routesForStop = transitRepo.stopRouteIndex()[stop0.stopId].orEmpty()
                    Log.i(
                        TAG,
                        "getStopById(${stop0.stopId}) -> ${byId?.name}; " +
                            "routes serving this stop: $routesForStop"
                    )
                }
            }
            .launchIn(scope)

        arrivalWatcher.start()
    }

    companion object {
        private const val TAG = "BTBoot"
    }
}
