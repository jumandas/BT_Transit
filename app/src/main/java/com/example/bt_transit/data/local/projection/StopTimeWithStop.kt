package com.example.bt_transit.data.local.projection

data class StopTimeWithStop(
    val stopId: String,
    val stopName: String,
    val lat: Double,
    val lng: Double,
    val stopSequence: Int,
    val arrivalTime: String,
    val departureTime: String
)
