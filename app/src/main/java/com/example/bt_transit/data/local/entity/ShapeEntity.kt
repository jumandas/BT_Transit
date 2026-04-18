package com.example.bt_transit.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "shape_points",
    primaryKeys = ["shapeId", "sequence"]
)
data class ShapeEntity(
    val shapeId: String,
    val sequence: Int,
    val lat: Double,
    val lng: Double
)
