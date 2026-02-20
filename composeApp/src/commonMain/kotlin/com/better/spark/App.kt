package com.better.spark

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.better.spark.presentation.navigation.TaskNavigation
import org.koin.compose.KoinContext

/**
 * Main app composable.
 * Sets up the theme and navigation.
 * Wraps in KoinContext to ensure Koin is available in Preview/Desktop if needed.
 */
@Composable
fun App() {
    val darkColorScheme = darkColorScheme(
        primary = Color(0xFFBB86FC),
        secondary = Color(0xFF03DAC6),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
        surfaceVariant = Color(0xFF2C2C2C)
    )

    KoinContext {
        MaterialTheme(
            colorScheme = darkColorScheme
        ) {
            TaskNavigation()
        }
    }
}
