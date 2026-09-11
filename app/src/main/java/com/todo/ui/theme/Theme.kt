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
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightPrimaryText,
    // 只有"最底层页面底色"用 background，其余一切仍用 surface
    background = Neumorph.LightBackground,
    onBackground = LightPrimaryText,
    surface = Neumorph.LightSurface,
    onSurface = LightPrimaryText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightSecondaryText,
    outline = LightOutline,
    secondary = LightSecondaryText,
    onSecondary = Color.White,
    error = LightDanger,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = Color(0xFF12141A),
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkPrimaryText,
    // 只有"最底层页面底色"用 background，其余一切仍用 surface
    background = Neumorph.DarkBackground,
    onBackground = DarkPrimaryText,
    surface = Neumorph.DarkSurface,
    onSurface = DarkPrimaryText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkSecondaryText,
    outline = DarkOutline,
    secondary = DarkSecondaryText,
    onSecondary = Color(0xFF12141A),
    error = DarkDanger,
    onError = Color(0xFF12141A)
)

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ThemePreferencesEntryPoint {
    fun themePreferences(): ThemePreferences
}

@Composable
fun TodoTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current.applicationContext
    // 主题偏好取 Hilt 单例（与 SettingsViewModel 共用同一个实例），避免在此重复构造
    val preferences = remember(context) {
        EntryPointAccessors.fromApplication(context, ThemePreferencesEntryPoint::class.java)
            .themePreferences()
    }
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
