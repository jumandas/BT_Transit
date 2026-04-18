package com.example.bt_transit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary          = BTBluePrimary,
    onPrimary        = BTOnPrimaryLight,
    secondary        = BTBlueSecondary,
    onSecondary      = BTOnPrimaryLight,
    tertiary         = BTTealAccent,
    background       = BTSurfaceLight,
    surface          = BTSurfaceLight,
    error            = BTError,
)

private val DarkColorScheme = darkColorScheme(
    primary          = BTBlueSecondary,
    onPrimary        = BTOnPrimaryLight,
    secondary        = BTTealAccent,
    onSecondary      = BTOnPrimaryLight,
    tertiary         = BTBluePrimary,
    background       = BTSurfaceDark,
    surface          = BTSurfaceDark,
    onSurface        = BTOnSurfaceDark,
    error            = BTError,
)

// Note: dynamicColor intentionally disabled — we want consistent BT brand colors
// regardless of the device's wallpaper-based Material You palette.
@Composable
fun BT_TransitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = BTTypography,
        content     = content
    )
}