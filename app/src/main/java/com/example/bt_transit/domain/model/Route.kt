package com.example.bt_transit.domain.model

data class Route(
    val routeId: String,
    val shortName: String,
    val longName: String,
    val color: Int,
    val textColor: Int
)
