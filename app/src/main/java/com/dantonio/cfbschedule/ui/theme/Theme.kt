package com.dantonio.cfbschedule.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FieldGreen = Color(0xFF1B4332)
private val FieldGreenLight = Color(0xFF2D6A4F)
private val Gold = Color(0xFFE9C46A)

private val DarkColors = darkColorScheme(
    primary = FieldGreenLight,
    secondary = Gold,
    background = Color(0xFF10151A),
    surface = Color(0xFF1A2128),
    surfaceVariant = Color(0xFF232C34),
    // Material's default outlineVariant is tuned for its stock purple palette and is nearly
    // invisible against this app's very dark background — a row divider needs real contrast.
    outlineVariant = Color(0xFF3E4B56)
)

private val LightColors = lightColorScheme(
    primary = FieldGreen,
    secondary = Gold,
    background = Color(0xFFF6F7F5),
    surface = Color.White,
    surfaceVariant = Color(0xFFE9ECEA),
    outlineVariant = Color(0xFFCFD4D1)
)

@Composable
fun CfbScheduleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
