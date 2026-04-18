package com.example.bt_transit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bt_transit.data.local.entity.StopTimeEntity
import com.example.bt_transit.data.local.projection.DirectTripResult
import com.example.bt_transit.data.local.projection.StopTimeWithStop

@Dao
interface StopTimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stopTimes: List<StopTimeEntity>)

    @Query("SELECT * FROM stop_times WHERE tripId = :tripId ORDER BY stopSequence")
    suspend fun getByTrip(tripId: String): List<StopTimeEntity>

    @Query("SELECT * FROM stop_times WHERE stopId = :stopId")
    suspend fun getByStop(stopId: String): List<StopTimeEntity>

    @Query("""
        SELECT s.stopId AS stopId,
               s.name AS stopName,
               s.lat AS lat,
               s.lng AS lng,
               st.stopSequence AS stopSequence,
               st.arrivalTime AS arrivalTime,
               st.departureTime AS departureTime
        FROM stop_times st
        INNER JOIN stops s ON s.stopId = st.stopId
        WHERE st.tripId = :tripId
        ORDER BY st.stopSequence
    """)
    suspend fun getTripStopsWithInfo(tripId: String): List<StopTimeWithStop>

    @Query("""
        SELECT st.departureTime
        FROM stop_times st
        INNER JOIN trips t ON st.tripId = t.tripId
        WHERE t.routeId = :routeId
        AND st.stopSequence = (
            SELECT MIN(stopSequence) FROM stop_times WHERE tripId = t.tripId
        )
        AND st.departureTime >= :currentTime
        ORDER BY st.departureTime ASC
        LIMIT :limit
    """)
    suspend fun getNextDeparturesForRoute(
        routeId: String,
        currentTime: String,
        limit: Int
    ): List<String>

    @Query("""
        SELECT DISTINCT st.departureTime
        FROM stop_times st
        INNER JOIN trips t ON st.tripId = t.tripId
        WHERE t.routeId = :routeId
        AND st.stopId = :stopId
        AND st.departureTime >= :currentTime
        ORDER BY st.departureTime ASC
        LIMIT :limit
    """)
    suspend fun getNextDeparturesForStop(
        routeId: String,
        stopId: String,
        currentTime: String,
        limit: Int
    ): List<String>

    @Query("""
        SELECT t.tripId FROM trips t
        INNER JOIN stop_times st ON st.tripId = t.tripId
        WHERE t.routeId = :routeId
        AND st.stopSequence = (
            SELECT MIN(stopSequence) FROM stop_times WHERE tripId = t.tripId
        )
        AND st.departureTime = :firstDepartureTime
        LIMIT 1
    """)
    suspend fun findTripByFirstDeparture(
        routeId: String,
        firstDepartureTime: String
    ): String?

    @Query("""
        SELECT DISTINCT st.tripId FROM stop_times st
        WHERE st.stopId IN (:stopIds)
        AND st.arrivalTime >= :afterTime AND st.arrivalTime <= :beforeTime
    """)
    suspend fun getActiveTripIdsNearStops(
        stopIds: List<String>,
        afterTime: String,
        beforeTime: String
    ): List<String>

    @Query("""
        SELECT st1.tripId AS tripId,
               st1.departureTime AS fromDepartureTime,
               st2.arrivalTime AS toArrivalTime
        FROM stop_times st1
        INNER JOIN stop_times st2 ON st1.tripId = st2.tripId
        WHERE st1.stopId = :fromStopId
          AND st2.stopId = :toStopId
          AND st1.stopSequence < st2.stopSequence
          AND st1.departureTime >= :afterTime
        ORDER BY st1.departureTime ASC
        LIMIT 1
    """)
    suspend fun findDirectTrip(
        fromStopId: String,
        toStopId: String,
        afterTime: String
    ): DirectTripResult?
}
