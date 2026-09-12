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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.todo.data.local.datastore.ThemePreferences
import com.todo.util.Constants
import com.todo.util.StartupGate
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    // 主题色表面上的白色（按钮图标/文字、勾选标记）：不用纯白，见 LightOnAccent 的说明
    onPrimary = LightOnAccent,
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
    fun startupGate(): StartupGate
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
    val startupGate = remember(context) {
        EntryPointAccessors.fromApplication(context, ThemePreferencesEntryPoint::class.java)
            .startupGate()
    }
    // 初值刻意用 null 表示"**还没读盘**"：如果用 THEME_MODE_SYSTEM 当占位初值，首次组合就会带着
    // 它触发下面的 LaunchedEffect，于是主题闸门在 DataStore 真正读出来之前就被放行了
    // ——那个"等主题就绪"的条件等于没写（正确主题只是碰巧抢在数据之前读完）。
    val storedMode by preferences.themeModeFlow.collectAsState(initial = null)
    val mode = storedMode ?: Constants.THEME_MODE_SYSTEM
    val systemDark = isSystemInDarkTheme()
    val useDark = when (mode) {
        Constants.THEME_MODE_LIGHT -> false
        Constants.THEME_MODE_DARK -> true
        else -> systemDark
    }

    // 真正读到偏好后才通知启动闸门（见 StartupGate）
    LaunchedEffect(storedMode) {
        if (storedMode != null) {
            startupGate.markThemeResolved()
        }
    }

    val contentAlpha = remember { Animatable(1f) }
    var isFirstComposition by remember { mutableStateOf(true) }
    LaunchedEffect(useDark) {
        // 首帧直接满不透明度：冷启动时"淡入"会被看成界面闪了一下，
        // 而主题切换（用户手动切深浅色）才需要这次过渡
        if (isFirstComposition) {
            isFirstComposition = false
            contentAlpha.snapTo(1f)
            return@LaunchedEffect
        }
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
