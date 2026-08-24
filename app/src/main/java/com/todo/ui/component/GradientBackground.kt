package com.todo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.MaterialTheme
import com.todo.ui.theme.GradientDarkBottom
import com.todo.ui.theme.GradientDarkTop
import com.todo.ui.theme.GradientLightBottom
import com.todo.ui.theme.GradientLightTop

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f

    val baseGradient = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(colors = listOf(GradientDarkTop, GradientDarkBottom))
        } else {
            Brush.verticalGradient(colors = listOf(GradientLightTop, GradientLightBottom))
        }
    }

    val highlightOverlay = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.04f),
                    Color.Transparent
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.08f),
                    Color.Transparent
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseGradient)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(highlightOverlay)
        )
        content()
    }
}
