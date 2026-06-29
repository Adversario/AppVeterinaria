package com.example.veterinariaapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = BrandOnPrimary,
    secondary = BrandSecondary,
    tertiary = BrandTertiary,
    background = Bg,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onSurface = OnSurface,
    outline = Outline,
    error = Error,
    onError = OnError
)

@Composable
fun VeterinariaAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}