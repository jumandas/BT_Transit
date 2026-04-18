package com.example.bt_transit.ui.theme

import com.example.bt_transit.domain.model.Route

/**
 * Fallback palette used when a GTFS route has no `route_color` field (or it matches
 * our default). We pick deterministically by route ID so a given route always gets
 * the same color across app launches.
 */
private val ROUTE_PALETTE = intArrayOf(
    0xFFE53935.toInt(), // red
    0xFF1E88E5.toInt(), // blue
    0xFF43A047.toInt(), // green
    0xFFFB8C00.toInt(), // orange
    0xFF8E24AA.toInt(), // purple
    0xFF00ACC1.toInt(), // teal
    0xFFD81B60.toInt(), // pink
    0xFF546E7A.toInt(), // blue-grey
    0xFF6D4C41.toInt(), // brown
    0xFF3949AB.toInt(), // indigo
    0xFF7CB342.toInt(), // light-green
    0xFFFFB300.toInt()  // amber
)

private const val GTFS_DEFAULT_BLUE = 0xFF2196F3.toInt()

fun Route.displayColor(): Int =
    if (color != GTFS_DEFAULT_BLUE && color != 0) color
    else ROUTE_PALETTE[(routeId.hashCode() and 0x7FFFFFFF) % ROUTE_PALETTE.size]

fun displayColorForRouteId(routeId: String?, routes: List<Route>): Int {
    if (routeId == null) return GTFS_DEFAULT_BLUE
    val route = routes.firstOrNull { it.routeId == routeId }
    return route?.displayColor()
        ?: ROUTE_PALETTE[(routeId.hashCode() and 0x7FFFFFFF) % ROUTE_PALETTE.size]
}
