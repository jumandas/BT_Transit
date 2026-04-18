package com.example.bt_transit.data.repository

import android.graphics.Color
import android.util.Log
import androidx.room.withTransaction
import com.example.bt_transit.data.local.BTDatabase
import com.example.bt_transit.data.local.projection.DirectTripResult
import com.example.bt_transit.data.local.entity.RouteEntity
import com.example.bt_transit.data.local.entity.ShapeEntity
import com.example.bt_transit.data.local.entity.StopEntity
import com.example.bt_transit.data.local.entity.StopTimeEntity
import com.example.bt_transit.data.local.entity.TripEntity
import com.example.bt_transit.data.remote.GtfsStaticClient
import com.example.bt_transit.domain.model.GeoPoint
import com.example.bt_transit.domain.model.Route
import com.example.bt_transit.domain.model.ScheduledStop
import com.example.bt_transit.domain.model.Stop
import kotlin.math.pow
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransitRepository @Inject constructor(
    private val staticClient: GtfsStaticClient,
    private val db: BTDatabase
) {

    suspend fun syncStaticFeed() = withContext(Dispatchers.IO) {
        val files = staticClient.download()
        db.withTransaction {
            files["stops.txt"]?.let { rows ->
                db.stopDao().insertAll(rows.mapNotNull { it.toStopEntity() })
            }
            files["routes.txt"]?.let { rows ->
                db.routeDao().insertAll(rows.mapNotNull { it.toRouteEntity() })
            }
            files["trips.txt"]?.let { rows ->
                db.tripDao().insertAll(rows.mapNotNull { it.toTripEntity() })
            }
            files["stop_times.txt"]?.let { rows ->
                db.stopTimeDao().insertAll(rows.mapNotNull { it.toStopTimeEntity() })
            }
            files["shapes.txt"]?.let { rows ->
                db.shapeDao().insertAll(rows.mapNotNull { it.toShapeEntity() })
            }
        }
    }

    suspend fun isSynced(): Boolean = db.stopDao().count() > 0

    fun observeRoutes(): Flow<List<Route>> =
        db.routeDao().observeAll().map { list -> list.map { it.toDomain() } }

    fun observeStops(): Flow<List<Stop>> =
        db.stopDao().observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getShape(shapeId: String): List<GeoPoint> =
        db.shapeDao().getByShapeId(shapeId).map { GeoPoint(it.lat, it.lng) }

    suspend fun findStopsNear(lat: Double, lng: Double, limit: Int = 10): List<Stop> =
        db.stopDao().findNearest(lat, lng, limit).map { it.toDomain() }

    suspend fun getRouteForTrip(tripId: String): Route? =
        db.routeDao().findByTripId(tripId)?.toDomain()

    suspend fun getScheduledStopsForTrip(tripId: String): List<ScheduledStop> =
        db.stopTimeDao().getTripStopsWithInfo(tripId).map {
            ScheduledStop(
                stop = Stop(it.stopId, it.stopName, it.lat, it.lng),
                stopSequence = it.stopSequence,
                arrivalTime = it.arrivalTime,
                departureTime = it.departureTime
            )
        }

    suspend fun stopsOnRoute(routeId: String): List<Stop> =
        db.stopDao().getStopsOnRoute(routeId).map { it.toDomain() }

    suspend fun getShapesForRoute(routeId: String): List<List<GeoPoint>> {
        val shapeIds = db.tripDao().getShapeIdsForRoute(routeId)
        return shapeIds.map { getShape(it) }
    }

    suspend fun getStopById(stopId: String): Stop? =
        db.stopDao().getById(stopId)?.toDomain()

    suspend fun stopRouteIndex(): Map<String, List<String>> =
        db.stopDao().getStopRouteIndex()
            .groupBy({ it.stopId }, { it.routeId })

    suspend fun getShapeForRoute(routeId: String): List<GeoPoint> {
        val trip = db.tripDao().getByRoute(routeId).firstOrNull { it.shapeId != null }
            ?: return emptyList()
        return getShape(trip.shapeId!!)
    }

    suspend fun getShapeForRouteAndDirection(routeId: String, directionId: Int): List<GeoPoint> {
        val shapeId = db.tripDao().getShapeIdForRouteAndDirection(routeId, directionId)
            ?: return emptyList()
        return getShape(shapeId)
    }

    suspend fun stopsOnRouteAndDirection(routeId: String, directionId: Int): List<Stop> =
        db.stopDao().getStopsOnRouteAndDirection(routeId, directionId).map { it.toDomain() }

    suspend fun getTripIdsForRouteAndDirection(routeId: String, directionId: Int): Set<String> =
        db.tripDao().getByRouteAndDirection(routeId, directionId).map { it.tripId }.toSet()

    suspend fun searchStops(query: String): List<Stop> =
        db.stopDao().searchByName("%$query%").map { it.toDomain() }

    suspend fun getNextDeparturesForRoute(
        routeId: String,
        currentTime: String,
        limit: Int = 5
    ): List<String> = db.stopTimeDao().getNextDeparturesForRoute(routeId, currentTime, limit)

    suspend fun getNextDeparturesForStop(
        routeId: String,
        stopId: String,
        currentTime: String,
        limit: Int = 1
    ): List<String> = db.stopTimeDao().getNextDeparturesForStop(routeId, stopId, currentTime, limit)

    /** Nearest BT stop to an arbitrary lat/lng (used when the user picks a landmark). */
    suspend fun nearestStop(lat: Double, lng: Double): Stop? =
        db.stopDao().findNearest(lat, lng, 1).firstOrNull()?.toDomain()

    /** Full stop-by-stop schedule for the trip whose first stop leaves at [firstDepartureTime]. */
    suspend fun getTripScheduleByFirstDeparture(
        routeId: String,
        firstDepartureTime: String
    ): List<ScheduledStop> {
        val tripId = db.stopTimeDao().findTripByFirstDeparture(routeId, firstDepartureTime)
            ?: return emptyList()
        return getScheduledStopsForTrip(tripId)
    }

    /** All routes that serve a given stopId. */
    suspend fun routesServingStop(stopId: String): List<Route> {
        val routeIdsForStop = stopRouteIndex()[stopId] ?: return emptyList()
        if (routeIdsForStop.isEmpty()) return emptyList()
        val allRoutes = db.routeDao().observeAll().first()
        val byId = allRoutes.associateBy { it.routeId }
        return routeIdsForStop.mapNotNull { byId[it]?.toDomain() }
    }

    suspend fun getActiveTripIdsNear(
        lat: Double,
        lng: Double,
        radiusMeters: Double = 500.0,
        afterTime: String,
        beforeTime: String
    ): Set<String> {
        val nearbyStops = db.stopDao().findNearest(lat, lng, 20)
            .filter { stop -> haversineMeters(lat, lng, stop.lat, stop.lng) <= radiusMeters }
        if (nearbyStops.isEmpty()) return emptySet()
        return db.stopTimeDao()
            .getActiveTripIdsNearStops(nearbyStops.map { it.stopId }, afterTime, beforeTime)
            .toSet()
    }

    suspend fun findDirectTrip(
        fromStopId: String,
        toStopId: String,
        afterTime: String
    ): DirectTripResult? =
        db.stopTimeDao().findDirectTrip(fromStopId, toStopId, afterTime)

    private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2).pow(2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2).pow(2)
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }
}

private fun Map<String, String>.toStopEntity(): StopEntity? {
    val id = this["stop_id"] ?: return null
    val lat = this["stop_lat"]?.toDoubleOrNull() ?: return null
    val lng = this["stop_lon"]?.toDoubleOrNull() ?: return null
    return StopEntity(
        stopId = id,
        name = this["stop_name"].orEmpty(),
        lat = lat,
        lng = lng
    )
}

private fun Map<String, String>.toRouteEntity(): RouteEntity? {
    val id = this["route_id"] ?: return null
    return RouteEntity(
        routeId = id,
        shortName = this["route_short_name"].orEmpty(),
        longName = this["route_long_name"].orEmpty(),
        color = parseGtfsColor(this["route_color"], default = DEFAULT_ROUTE_COLOR),
        textColor = parseGtfsColor(this["route_text_color"], default = DEFAULT_TEXT_COLOR)
    )
}

private fun Map<String, String>.toTripEntity(): TripEntity? {
    val id = this["trip_id"] ?: return null
    val routeId = this["route_id"] ?: return null
    return TripEntity(
        tripId = id,
        routeId = routeId,
        serviceId = this["service_id"].orEmpty(),
        headsign = this["trip_headsign"].orEmpty(),
        directionId = this["direction_id"]?.toIntOrNull() ?: 0,
        shapeId = this["shape_id"]?.takeIf { it.isNotBlank() }
    )
}

private fun Map<String, String>.toStopTimeEntity(): StopTimeEntity? {
    val tripId = this["trip_id"] ?: return null
    val stopId = this["stop_id"] ?: return null
    val seq = this["stop_sequence"]?.toIntOrNull() ?: return null
    return StopTimeEntity(
        tripId = tripId,
        stopSequence = seq,
        stopId = stopId,
        arrivalTime = this["arrival_time"].orEmpty(),
        departureTime = this["departure_time"].orEmpty()
    )
}

private fun Map<String, String>.toShapeEntity(): ShapeEntity? {
    val id = this["shape_id"] ?: return null
    val seq = this["shape_pt_sequence"]?.toIntOrNull() ?: return null
    val lat = this["shape_pt_lat"]?.toDoubleOrNull() ?: return null
    val lng = this["shape_pt_lon"]?.toDoubleOrNull() ?: return null
    return ShapeEntity(shapeId = id, sequence = seq, lat = lat, lng = lng)
}

private fun StopEntity.toDomain() = Stop(stopId, name, lat, lng)

private fun RouteEntity.toDomain() = Route(routeId, shortName, longName, color, textColor)

private fun parseGtfsColor(hex: String?, default: Int): Int {
    if (hex.isNullOrBlank()) return default
    return try {
        Color.parseColor("#${hex.trimStart('#')}")
    } catch (t: Throwable) {
        Log.w("TransitRepository", "Bad GTFS color '$hex'; using default")
        default
    }
}

private const val DEFAULT_ROUTE_COLOR = 0xFF2196F3.toInt()
private const val DEFAULT_TEXT_COLOR = 0xFFFFFFFF.toInt()
