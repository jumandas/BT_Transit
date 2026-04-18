package com.example.bt_transit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bt_transit.data.local.entity.StopTimeEntity

@Dao
interface StopTimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stopTimes: List<StopTimeEntity>)

    @Query("SELECT * FROM stop_times WHERE tripId = :tripId ORDER BY stopSequence")
    suspend fun getByTrip(tripId: String): List<StopTimeEntity>

    @Query("SELECT * FROM stop_times WHERE stopId = :stopId")
    suspend fun getByStop(stopId: String): List<StopTimeEntity>
}
