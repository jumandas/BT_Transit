package com.example.bt_transit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bt_transit.data.local.entity.StopEntity
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
}
