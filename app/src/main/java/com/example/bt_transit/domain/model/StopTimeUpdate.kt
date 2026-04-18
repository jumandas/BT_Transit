package com.example.bt_transit.domain.model

data class StopTimeUpdate(
    val stopId: String,
    val stopSequence: Int,
    val arrivalEpochSec: Long?,
    val departureEpochSec: Long?,
    val scheduleRelationship: String
)
