package com.example.bt_transit.domain.model

data class TripUpdate(
    val tripId: String,
    val routeId: String,
    val vehicleId: String?,
    val updates: List<StopTimeUpdate>
)

data class StopTimeUpdate(
    val stopId: String,
    val stopSequence: Int,
    val arrivalEpochSec: Long?,
    val departureEpochSec: Long?,
    val scheduleRelationship: String
)
