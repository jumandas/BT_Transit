package com.example.bt_transit.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trips",
    indices = [Index("routeId"), Index("shapeId")]
)
data class TripEntity(
    @PrimaryKey val tripId: String,
    val routeId: String,
    val serviceId: String,
    val headsign: String,
    val directionId: Int,
    val shapeId: String?
)
