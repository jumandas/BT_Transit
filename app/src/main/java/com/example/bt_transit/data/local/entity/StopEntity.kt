package com.example.bt_transit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stops")
data class StopEntity(
    @PrimaryKey val stopId: String,
    val name: String,
    val lat: Double,
    val lng: Double
)
