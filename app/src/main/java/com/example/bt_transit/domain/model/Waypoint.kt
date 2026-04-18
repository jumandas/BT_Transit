package com.example.bt_transit.domain.model

data class Waypoint(
    val id: Long = 0,
    val label: String,
    val lat: Double,
    val lng: Double,
    val notifyRadiusMeters: Int = 400
)
