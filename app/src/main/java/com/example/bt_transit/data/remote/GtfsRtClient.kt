package com.example.bt_transit.data.remote

import android.util.Log
import com.example.bt_transit.domain.model.ServiceAlert
import com.example.bt_transit.domain.model.StopTimeUpdate
import com.example.bt_transit.domain.model.TripUpdate
import com.example.bt_transit.domain.model.Vehicle
import com.google.transit.realtime.GtfsRealtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GtfsRtClient @Inject constructor(
    private val http: OkHttpClient
) {

    companion object {
        private const val BASE = "https://s3.amazonaws.com/etatransit.gtfs/bloomingtontransit.etaspot.net"
        const val POSITIONS = "$BASE/position_updates.pb"
        const val TRIP_UPDATES = "$BASE/trip_updates.pb"
        const val ALERTS = "$BASE/alerts.pb"
        private const val TAG = "GtfsRtClient"
    }

    suspend fun fetchVehicles(): List<Vehicle> = withContext(Dispatchers.IO) {
        val feed = fetchFeed(POSITIONS) ?: return@withContext emptyList()
        feed.entityList.mapNotNull { entity ->
            if (!entity.hasVehicle()) return@mapNotNull null
            val v = entity.vehicle
            Vehicle(
                vehicleId = v.vehicle.id,
                tripId = v.trip.tripId.ifEmpty { null },
                routeId = v.trip.routeId.ifEmpty { null },
                lat = v.position.latitude.toDouble(),
                lng = v.position.longitude.toDouble(),
                bearing = if (v.position.hasBearing()) v.position.bearing else null,
                speed = if (v.position.hasSpeed()) v.position.speed else null,
                timestamp = v.timestamp
            )
        }
    }

    suspend fun fetchTripUpdates(): List<TripUpdate> = withContext(Dispatchers.IO) {
        val feed = fetchFeed(TRIP_UPDATES) ?: return@withContext emptyList()
        feed.entityList.mapNotNull { entity ->
            if (!entity.hasTripUpdate()) return@mapNotNull null
            val tu = entity.tripUpdate
            TripUpdate(
                tripId = tu.trip.tripId,
                routeId = tu.trip.routeId,
                vehicleId = tu.vehicle.id.ifEmpty { null },
                updates = tu.stopTimeUpdateList.map { stu ->
                    StopTimeUpdate(
                        stopId = stu.stopId,
                        stopSequence = stu.stopSequence,
                        arrivalEpochSec = if (stu.hasArrival()) stu.arrival.time else null,
                        departureEpochSec = if (stu.hasDeparture()) stu.departure.time else null,
                        scheduleRelationship = stu.scheduleRelationship.name
                    )
                }
            )
        }
    }

    suspend fun fetchAlerts(): List<ServiceAlert> = withContext(Dispatchers.IO) {
        val feed = fetchFeed(ALERTS) ?: return@withContext emptyList()
        feed.entityList.mapNotNull { entity ->
            if (!entity.hasAlert()) return@mapNotNull null
            val a = entity.alert
            ServiceAlert(
                id = entity.id,
                cause = a.cause.name,
                effect = a.effect.name,
                header = a.headerText.translationList.firstOrNull()?.text.orEmpty(),
                description = a.descriptionText.translationList.firstOrNull()?.text.orEmpty(),
                affectedRouteIds = a.informedEntityList.mapNotNull {
                    it.routeId.ifEmpty { null }
                }
            )
        }
    }

    private fun fetchFeed(url: String): GtfsRealtime.FeedMessage? {
        return try {
            val req = Request.Builder().url(url).build()
            http.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.w(TAG, "HTTP ${resp.code} for $url")
                    return null
                }
                GtfsRealtime.FeedMessage.parseFrom(resp.body!!.byteStream())
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to fetch $url: ${t.message}")
            null
        }
    }
}
