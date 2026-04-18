package com.example.bt_transit.data.repository

import com.example.bt_transit.data.local.dao.WaypointDao
import com.example.bt_transit.data.local.entity.WaypointEntity
import com.example.bt_transit.domain.model.Waypoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaypointRepository @Inject constructor(
    private val dao: WaypointDao
) {

    val waypoints: Flow<List<Waypoint>> = dao.observeAll().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun add(waypoint: Waypoint): Long =
        dao.insert(waypoint.toEntity())

    suspend fun update(waypoint: Waypoint) =
        dao.update(waypoint.toEntity())

    suspend fun remove(id: Long) =
        dao.deleteById(id)
}

private fun WaypointEntity.toDomain() = Waypoint(
    id = id,
    label = label,
    lat = lat,
    lng = lng,
    notifyRadiusMeters = notifyRadiusMeters
)

private fun Waypoint.toEntity() = WaypointEntity(
    id = id,
    label = label,
    lat = lat,
    lng = lng,
    notifyRadiusMeters = notifyRadiusMeters
)
