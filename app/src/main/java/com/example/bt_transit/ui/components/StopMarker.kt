package com.example.bt_transit.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import com.example.bt_transit.domain.model.Stop
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

/**
 * Small colored dot rendered at a bus stop location along a focused route.
 * Tap to get arrivals for this stop.
 */
@Composable
fun StopMarker(
    stop: Stop,
    @ColorInt routeColor: Int,
    onClick: () -> Unit = {}
) {
    val icon = remember(routeColor) { stopDotDescriptor(routeColor) }
    val state = remember(stop.stopId) { MarkerState(position = LatLng(stop.lat, stop.lng)) }
    Marker(
        state = state,
        title = stop.name,
        snippet = "Tap for arrivals",
        icon = icon,
        anchor = Offset(0.5f, 0.5f),
        zIndex = 1f,
        onClick = { onClick(); true }
    )
}

private fun stopDotDescriptor(@ColorInt routeColor: Int): BitmapDescriptor {
    val size = 44
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Subtle shadow
    val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x33000000
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f + 0.5f, size / 2f + 1.5f, size / 2f - 2f, shadow)

    // Route-colored ring
    val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = routeColor
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 3f, ring)

    // White fill
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 7f, fill)

    // Inner colored dot
    val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = routeColor
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 12f, dot)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
