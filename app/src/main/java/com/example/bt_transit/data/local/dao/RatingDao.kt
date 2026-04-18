package com.example.bt_transit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bt_transit.data.local.entity.RatingEntity

@Dao
interface RatingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rating: RatingEntity)

    @Query("SELECT AVG(stars) FROM ratings WHERE routeId = :routeId")
    suspend fun getAverageForRoute(routeId: String): Float?

    @Query("SELECT * FROM ratings ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<RatingEntity>
}
