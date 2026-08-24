package com.todo.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

@Composable
fun UndoSnackbar(
    visible: Boolean,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val bgColor = if (isDark) DarkGlassSurface.copy(alpha = 0.80f) else LightGlassSurface.copy(alpha = 0.90f)
    val borderColor = if (isDark) DarkGlassBorder else LightGlassBorder
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    LaunchedEffect(visible) {
        if (visible) {
            delay(3000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
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
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(14.dp),
            color = bgColor,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "已删除", color = textColor)
                TextButton(onClick = {
                    onUndo()
                    onDismiss()
                }) {
                    Text(text = "撤销", color = textColor)
                }
            }
        }
    }
}
