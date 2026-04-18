package com.example.bt_transit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bt_transit.data.local.entity.StopTimeEntity
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
}
