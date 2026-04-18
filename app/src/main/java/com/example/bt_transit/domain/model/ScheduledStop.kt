package com.example.bt_transit.domain.model

data class ScheduledStop(
    val stop: Stop,
    val stopSequence: Int,
    val arrivalTime: String,
    val departureTime: String
)
