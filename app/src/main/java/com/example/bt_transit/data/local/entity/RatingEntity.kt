package com.example.bt_transit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ratings")
data class RatingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: String?,
    val routeId: String?,
    val stars: Int,
    val comment: String = "",
    val timestamp: Long
)
