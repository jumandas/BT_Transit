package com.example.bt_transit.domain.model

data class Stop(
    val stopId: String,
    val name: String,
    val lat: Double,
    val lng: Double
)
