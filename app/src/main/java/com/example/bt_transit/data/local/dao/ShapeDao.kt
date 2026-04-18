package com.example.bt_transit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bt_transit.data.local.entity.ShapeEntity

@Dao
interface ShapeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(points: List<ShapeEntity>)

    @Query("SELECT * FROM shape_points WHERE shapeId = :shapeId ORDER BY sequence")
    suspend fun getByShapeId(shapeId: String): List<ShapeEntity>
}
