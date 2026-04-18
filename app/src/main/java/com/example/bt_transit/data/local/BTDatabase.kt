package com.example.bt_transit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bt_transit.data.local.dao.RouteDao
import com.example.bt_transit.data.local.dao.ShapeDao
import com.example.bt_transit.data.local.dao.StopDao
import com.example.bt_transit.data.local.dao.StopTimeDao
import com.example.bt_transit.data.local.dao.TripDao
import com.example.bt_transit.data.local.dao.WaypointDao
import com.example.bt_transit.data.local.entity.RouteEntity
import com.example.bt_transit.data.local.entity.ShapeEntity
import com.example.bt_transit.data.local.entity.StopEntity
import com.example.bt_transit.data.local.entity.StopTimeEntity
import com.example.bt_transit.data.local.entity.TripEntity
import com.example.bt_transit.data.local.entity.WaypointEntity

@Database(
    entities = [
        StopEntity::class,
        RouteEntity::class,
        TripEntity::class,
        StopTimeEntity::class,
        ShapeEntity::class,
        WaypointEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class BTDatabase : RoomDatabase() {
    abstract fun stopDao(): StopDao
    abstract fun routeDao(): RouteDao
    abstract fun tripDao(): TripDao
    abstract fun stopTimeDao(): StopTimeDao
    abstract fun shapeDao(): ShapeDao
    abstract fun waypointDao(): WaypointDao
}
