package com.oceanwalls.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val OceanColors = darkColorScheme(
    primary = Aqua,
    onPrimary = Color(0xFF00201D),
    primaryContainer = DeepTeal,
    onPrimaryContainer = AquaGlow,
    secondary = AquaDeep,
    onSecondary = Color(0xFF001512),
    tertiary = SunsetWarm,
    onTertiary = Color(0xFF241505),
    background = AmoledBlack,
    onBackground = Foam,
    surface = AmoledBlack,
    onSurface = Foam,
    surfaceVariant = SurfaceMid,
    onSurfaceVariant = Mist,
    surfaceTint = Aqua,
    outline = Hairline,
    outlineVariant = SurfaceHigh,
    error = Color(0xFFFF8A80),
    onError = Color(0xFF2B0000),
    scrim = Color(0xCC000000)
)

@Composable
fun OceanWallsTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(colorScheme = OceanColors, typography = OceanTypography, content = content)
}
