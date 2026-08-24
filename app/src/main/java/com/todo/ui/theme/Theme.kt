package com.todo.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.todo.data.local.datastore.ThemePreferences
import com.todo.util.Constants
import com.todo.ui.theme.MotionTokens

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = Color.White,
    background = Color.Transparent,
    onBackground = LightPrimaryText,
    surface = Color.Transparent,
    onSurface = LightPrimaryText,
    secondary = LightSecondaryText,
    error = LightDanger
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = Color.Black,
    background = Color.Transparent,
    onBackground = DarkPrimaryText,
    surface = Color.Transparent,
    onSurface = DarkPrimaryText,
    secondary = DarkSecondaryText,
    error = DarkDanger
)

@Composable
fun TodoTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val preferences = remember(context) { ThemePreferences(context) }
    val mode by preferences.themeModeFlow.collectAsState(initial = Constants.THEME_MODE_SYSTEM)
    val systemDark = isSystemInDarkTheme()
    val useDark = when (mode) {
        Constants.THEME_MODE_LIGHT -> false
        Constants.THEME_MODE_DARK -> true
        else -> systemDark
    }

    val contentAlpha = remember { Animatable(1f) }
    LaunchedEffect(useDark) {
        contentAlpha.snapTo(0.72f)
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = MotionTokens.ThemeCrossfade,
                easing = MotionTokens.StandardEasing
            )
        )
    }

    MaterialTheme(
        colorScheme = if (useDark) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = Shapes
    ) {
        Box(modifier = Modifier.alpha(contentAlpha.value)) {
            content()
        }
    }
}
