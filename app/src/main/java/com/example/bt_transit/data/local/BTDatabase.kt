package com.example.bt_transit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.bt_transit.data.local.dao.RatingDao
import com.example.bt_transit.data.local.dao.RouteDao
import com.example.bt_transit.data.local.dao.ShapeDao
import com.example.bt_transit.data.local.dao.StopDao
import com.example.bt_transit.data.local.dao.StopTimeDao
import com.example.bt_transit.data.local.dao.TripDao
import com.example.bt_transit.data.local.dao.WaypointDao
import com.example.bt_transit.data.local.entity.RatingEntity
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
        WaypointEntity::class,
        RatingEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class BTDatabase : RoomDatabase() {
    abstract fun stopDao(): StopDao
    abstract fun routeDao(): RouteDao
    abstract fun tripDao(): TripDao
    abstract fun stopTimeDao(): StopTimeDao
    abstract fun shapeDao(): ShapeDao
    abstract fun waypointDao(): WaypointDao
    abstract fun ratingDao(): RatingDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `ratings` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `tripId` TEXT,
                        `routeId` TEXT,
                        `stars` INTEGER NOT NULL,
                        `comment` TEXT NOT NULL DEFAULT '',
                        `timestamp` INTEGER NOT NULL
                    )"""
                )
            }
        }
    }
}
