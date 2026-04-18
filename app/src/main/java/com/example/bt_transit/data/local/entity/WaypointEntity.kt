package com.example.bt_transit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "waypoints")
data class WaypointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val lat: Double,
    val lng: Double,
    val notifyRadiusMeters: Int = 400
)
