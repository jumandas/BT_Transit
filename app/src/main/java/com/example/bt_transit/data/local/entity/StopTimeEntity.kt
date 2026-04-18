package com.example.bt_transit.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "stop_times",
    primaryKeys = ["tripId", "stopSequence"],
    indices = [Index("stopId"), Index("tripId")]
)
data class StopTimeEntity(
    val tripId: String,
    val stopSequence: Int,
    val stopId: String,
    val arrivalTime: String,
    val departureTime: String
)
