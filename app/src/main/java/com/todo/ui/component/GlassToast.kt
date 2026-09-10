package com.todo.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.DarkGlassBorder
import com.todo.ui.theme.DarkGlassSurface
import com.todo.ui.theme.LightGlassBorder
import com.todo.ui.theme.LightGlassSurface
import com.todo.ui.theme.MotionTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/**
 * 玻璃质感的一次性轻提示：订阅 [messageFlow]，每次收到新消息显示一段时间后自动消失。
 * 用计数 [tick] 重置定时器，因此连续收到相同内容的消息也能正确重新计时。
 */
@Composable
fun GlassToast(
    messageFlow: Flow<String>,
    modifier: Modifier = Modifier,
    durationMillis: Long = 2500L
) {
    var message by remember { mutableStateOf<String?>(null) }
    var tick by remember { mutableIntStateOf(0) }

    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val bgColor = if (isDark) DarkGlassSurface.copy(alpha = 0.88f) else LightGlassSurface.copy(alpha = 0.94f)
    val borderColor = if (isDark) DarkGlassBorder else LightGlassBorder
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    LaunchedEffect(Unit) {
        messageFlow.collect { value ->
            message = value
            tick++
        }
    }

    LaunchedEffect(tick) {
        if (tick > 0) {
            delay(durationMillis)
            message = null
        }
    }

    AnimatedVisibility(
        visible = message != null,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(
                durationMillis = MotionTokens.Medium,
                easing = MotionTokens.StandardEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = MotionTokens.Medium,
                easing = MotionTokens.StandardEasing
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { it / 2 },
            animationSpec = tween(
                durationMillis = MotionTokens.DialogExit,
                easing = MotionTokens.StandardEasing
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = MotionTokens.DialogExit,
                easing = MotionTokens.StandardEasing
            )
        )
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = bgColor,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Text(
                text = message ?: "",
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}
