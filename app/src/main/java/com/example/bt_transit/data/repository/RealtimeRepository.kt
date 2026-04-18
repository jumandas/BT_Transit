package com.example.bt_transit.data.repository

import com.example.bt_transit.data.remote.GtfsRtClient
import com.example.bt_transit.domain.model.ServiceAlert
import com.example.bt_transit.domain.model.TripUpdate
import com.example.bt_transit.domain.model.Vehicle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeRepository @Inject constructor(private val client: GtfsRtClient) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val vehicles: Flow<List<Vehicle>> = flow {
        while (true) {
            emit(client.fetchVehicles())
            delay(10_000L)
        }
    }.flowOn(Dispatchers.IO).shareIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000),
        replay = 1
    )

    val tripUpdates: Flow<List<TripUpdate>> = flow {
        while (true) {
            emit(client.fetchTripUpdates())
            delay(10_000L)
        }
    }.flowOn(Dispatchers.IO).shareIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000),
        replay = 1
    )

    // Alerts change rarely — poll every 30 seconds
    val alerts: Flow<List<ServiceAlert>> = flow {
        while (true) {
            emit(client.fetchAlerts())
            delay(30_000L)
        }
    }.flowOn(Dispatchers.IO).shareIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000),
        replay = 1
    )
}
