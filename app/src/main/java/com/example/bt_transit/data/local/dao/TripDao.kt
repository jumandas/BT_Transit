package com.example.bt_transit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bt_transit.data.local.entity.TripEntity

@Dao
interface TripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(trips: List<TripEntity>)

    @Query("SELECT * FROM trips WHERE tripId = :id")
    suspend fun getById(id: String): TripEntity?

    @Query("SELECT * FROM trips WHERE routeId = :routeId")
    suspend fun getByRoute(routeId: String): List<TripEntity>

    @Query("SELECT DISTINCT shapeId FROM trips WHERE routeId = :routeId AND shapeId IS NOT NULL")
    suspend fun getShapeIdsForRoute(routeId: String): List<String>
}
