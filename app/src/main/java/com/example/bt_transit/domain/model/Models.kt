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

data class StopTimeUpdate(
    val stopId: String,
    val stopSequence: Int,
    val arrivalEpochSec: Long?,
    val departureEpochSec: Long?,
    val scheduleRelationship: String
)

data class TripUpdate(
    val tripId: String,
    val routeId: String,
    val vehicleId: String?,
    val updates: List<StopTimeUpdate>
)

data class ServiceAlert(
    val id: String,
    val cause: String,
    val effect: String,
    val header: String,
    val description: String,
    val affectedRouteIds: List<String>
)
