package com.example.bt_transit.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.bt_transit.R
import com.example.bt_transit.domain.model.Vehicle
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

/**
 * Bus marker that glides between 10-second poll updates and shows a directional arrow.
 * The circular backdrop is tinted by route color and the route short name is drawn
 * on top so each route is unambiguously identifiable at a glance.
 */
@Composable
fun AnimatedBusMarker(
    vehicle: Vehicle,
    routeColor: Int,
    routeShortName: String,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val icon = remember(routeColor, routeShortName) {
        createBusArrowDescriptor(context, routeColor, routeShortName)
    }

    val markerState = remember(vehicle.vehicleId) {
        MarkerState(position = LatLng(vehicle.lat, vehicle.lng))
    }

    // Glide from last position to newly polled position over ~9 s
    LaunchedEffect(vehicle.vehicleId, vehicle.lat, vehicle.lng) {
        val startLat = markerState.position.latitude
        val startLng = markerState.position.longitude
        val targetLat = vehicle.lat
        val targetLng = vehicle.lng
        if (startLat == targetLat && startLng == targetLng) return@LaunchedEffect

        val anim = Animatable(0f)
        anim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 9000)
        ) {
            val f = value
            markerState.position = LatLng(
                startLat + (targetLat - startLat) * f,
                startLng + (targetLng - startLng) * f
            )
        }
    }

    key(vehicle.vehicleId) {
        Marker(
            state = markerState,
            title = "Route $routeShortName — Bus ${vehicle.vehicleId}",
            snippet = vehicle.speed?.let { "Speed: %.1f m/s".format(it) } ?: "Tap for trip detail",
            icon = icon,
            rotation = vehicle.bearing ?: 0f,
            flat = true,
            anchor = Offset(0.5f, 0.5f),
            zIndex = 2f,
            onClick = { onClick(); true }
        )
    }
}

/**
 * Render a compact route-colored bus badge: colored disc + bus glyph + route short name
 * overlay. Returned as a BitmapDescriptor for use as a Google Maps marker icon.
 */
private fun createBusArrowDescriptor(
    context: Context,
    @ColorInt routeColor: Int,
    routeShortName: String
): BitmapDescriptor {
    val size = 140
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Shadow ring (subtle drop shadow)
    val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x33000000
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f + 1f, size / 2f + 3f, size / 2f - 4f, shadow)

    // White halo
    val halo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 6f, halo)

    // Route-colored inner circle
    val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = routeColor
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 12f, inner)

    // Small bus glyph in upper half
    val glyph = ContextCompat.getDrawable(context, R.drawable.ic_bus_arrow)!!.mutate()
    val glyphW = 60
    val glyphH = 75
    val left = (size - glyphW) / 2
    val top = 10
    glyph.setBounds(left, top, left + glyphW, top + glyphH)
    glyph.draw(canvas)

    // Route short name centered in lower area
    if (routeShortName.isNotBlank()) {
        val label = routeShortName.take(3)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = if (label.length > 2) 28f else 36f
            textAlign = Paint.Align.CENTER
            setShadowLayer(3f, 0f, 1f, 0x66000000)
        }
        val bounds = Rect()
        textPaint.getTextBounds(label, 0, label.length, bounds)
        val x = size / 2f
        val y = size - 26f
        canvas.drawText(label, x, y, textPaint)
    }

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
