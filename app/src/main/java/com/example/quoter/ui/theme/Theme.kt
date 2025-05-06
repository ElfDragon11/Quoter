package com.example.quoter.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MutedCyan,       // Use the less bright cyan
    secondary = MutedCyan,     // Use muted cyan for secondary too
    tertiary = MutedCyan,      // Use muted cyan for tertiary too
    background = DarkBackground, // Use the very dark grey background
    surface = DarkSurface,       // Use slightly lighter dark grey for surfaces
    onPrimary = OnPrimaryColor,  // White text on muted cyan
    onSecondary = OnPrimaryColor, // White text on muted cyan
    onTertiary = OnPrimaryColor,  // White text on muted cyan
    onBackground = OnDarkColor,  // Light grey text on dark background
    onSurface = OnDarkColor    // Light grey text on dark surface
)

// Update LightColorScheme to use the new Blue palette and define container colors
private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,       // Use the new base blue
    secondary = PrimaryBlueLight, // Use a lighter blue for secondary
    tertiary = PrimaryBlueDark,  // Use a darker blue for tertiary
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,      // Text on primary blue
    onSecondary = Color.White,    // Text on secondary blue
    onTertiary = Color.White,     // Text on tertiary blue
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),

    // Explicitly define container colors for NavigationBar indicator
    secondaryContainer = PrimaryBlueLight, // Use light blue for the indicator background
    onSecondaryContainer = Color.White     // Use white for the icon/text on the indicator
)

@Composable
fun QuoterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep dynamic color disabled for now
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> { ... }
        darkTheme -> DarkColorScheme // Ensure DarkColorScheme is applied
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar color to match the background
            window.statusBarColor = colorScheme.background.toArgb()
            // Adjust status bar icon colors based on theme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}