package com.example.bt_transit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bt_transit.data.local.entity.RouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routes: List<RouteEntity>)

    @Query("SELECT * FROM routes ORDER BY shortName")
    fun observeAll(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE routeId = :id")
    suspend fun getById(id: String): RouteEntity?
}
