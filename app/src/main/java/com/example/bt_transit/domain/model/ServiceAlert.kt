package com.example.bt_transit.domain.model

data class ServiceAlert(
    val id: String,
    val cause: String,
    val effect: String,
    val header: String,
    val description: String,
    val affectedRouteIds: List<String>
)
