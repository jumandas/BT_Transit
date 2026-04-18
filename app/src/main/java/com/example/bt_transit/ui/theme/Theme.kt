package com.example.bt_transit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary             = BTBluePrimary,
    onPrimary           = BTOnPrimary,
    primaryContainer    = BTBlueContainer,
    onPrimaryContainer  = BTBlueOnContainer,
    secondary           = BTBlueSecondary,
    onSecondary         = BTOnPrimary,
    tertiary            = BTTealAccent,
    background          = BTSurfaceLight,
    onBackground        = BTOnBackground,
    surface             = BTSurface,
    onSurface           = BTOnSurface,
    surfaceVariant      = BTSurfaceVariant,
    onSurfaceVariant    = BTOnSurfaceVariant,
    outline             = BTOutline,
    outlineVariant      = BTOutlineVariant,
    error               = BTError,
    onError             = BTOnPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary             = BTBlueSecondary,
    onPrimary           = BTOnPrimary,
    primaryContainer    = BTBluePrimaryDark,
    onPrimaryContainer  = BTBlueContainer,
    secondary           = BTTealAccent,
    onSecondary         = BTOnPrimary,
    tertiary             = BTBluePrimary,
    background          = BTSurfaceDark,
    onBackground        = BTOnSurfaceDark,
    surface             = BTSurfaceDarkElev,
    onSurface           = BTOnSurfaceDark,
    surfaceVariant      = BTSurfaceDarkElev,
    onSurfaceVariant    = BTOnSurfaceDark,
    outline             = BTOutline,
    error               = BTError
)

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
