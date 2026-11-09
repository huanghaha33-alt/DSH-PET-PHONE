package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BigFishColorScheme = darkColorScheme(
    primary = SkyBlue,
    onPrimary = Black,
    primaryContainer = OceanBlue,
    onPrimaryContainer = Color.White,
    secondary = SoftCyan,
    onSecondary = Black,
    background = Black,
    onBackground = TextWhite,
    surface = Black,
    onSurface = TextWhite,
    surfaceVariant = DarkNavy,
    onSurfaceVariant = TextWhite
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BigFishColorScheme,
        typography = Typography,
        content = content
    )
}
