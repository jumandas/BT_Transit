package com.example.bt_transit.data.repository

import com.example.bt_transit.data.local.dao.RatingDao
import com.example.bt_transit.data.local.entity.RatingEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RatingRepository @Inject constructor(private val ratingDao: RatingDao) {

    suspend fun submitRating(tripId: String?, routeId: String?, stars: Int, comment: String) {
        ratingDao.insert(
            RatingEntity(
                tripId = tripId,
                routeId = routeId,
                stars = stars,
                comment = comment,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun getAverageForRoute(routeId: String): Float? =
        ratingDao.getAverageForRoute(routeId)
}
