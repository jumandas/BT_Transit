package com.example.bt_transit.domain.model

data class Vehicle(
    val vehicleId: String,
    val tripId: String?,
    val routeId: String?,
    val lat: Double,
    val lng: Double,
    val bearing: Float?,
    val speed: Float?,
    val timestamp: Long
)
