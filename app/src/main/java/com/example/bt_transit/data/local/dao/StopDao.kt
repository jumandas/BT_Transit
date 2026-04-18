package com.example.bt_transit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bt_transit.data.local.entity.StopEntity
import com.example.bt_transit.data.local.projection.StopRoutePair
import kotlinx.coroutines.flow.Flow

@Dao
interface StopDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stops: List<StopEntity>)

    @Query("SELECT * FROM stops")
    fun observeAll(): Flow<List<StopEntity>>

    @Query("SELECT * FROM stops WHERE stopId = :id")
    suspend fun getById(id: String): StopEntity?

    @Query("""
        SELECT *
        FROM stops
        ORDER BY (ABS(lat - :lat) + ABS(lng - :lng)) ASC
        LIMIT :limit
    """)
    suspend fun findNearest(lat: Double, lng: Double, limit: Int): List<StopEntity>

    @Query("SELECT COUNT(*) FROM stops")
    suspend fun count(): Int

    @Query("""
        SELECT DISTINCT s.* FROM stops s
        INNER JOIN stop_times st ON st.stopId = s.stopId
        INNER JOIN trips t ON t.tripId = st.tripId
        WHERE t.routeId = :routeId
    """)
    suspend fun getStopsOnRoute(routeId: String): List<StopEntity>

    @Query("""
        SELECT DISTINCT s.stopId AS stopId, t.routeId AS routeId
        FROM stops s
        INNER JOIN stop_times st ON st.stopId = s.stopId
        INNER JOIN trips t ON t.tripId = st.tripId
    """)
    suspend fun getStopRouteIndex(): List<StopRoutePair>

    @Query("SELECT * FROM stops WHERE name LIKE :query ORDER BY name ASC LIMIT 20")
    suspend fun searchByName(query: String): List<StopEntity>

    @Query("""
        SELECT DISTINCT s.* FROM stops s
        INNER JOIN stop_times st ON st.stopId = s.stopId
        INNER JOIN trips t ON t.tripId = st.tripId
        WHERE t.routeId = :routeId AND t.directionId = :directionId
    """)
    suspend fun getStopsOnRouteAndDirection(routeId: String, directionId: Int): List<StopEntity>
}
