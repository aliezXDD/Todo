package com.todo.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.DarkGlassBorder
import com.todo.ui.theme.DarkGlassSurface
import com.todo.ui.theme.LightGlassBorder
import com.todo.ui.theme.LightGlassSurface

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    highlightScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val containerColor = if (isDark) DarkGlassSurface.copy(alpha = 0.82f) else LightGlassSurface.copy(alpha = 0.92f)
    val borderColor = if (isDark) DarkGlassBorder else LightGlassBorder
    val shape = RoundedCornerShape(16.dp)

    Surface(
        modifier = modifier.glassOverlay(
            shape = shape,
            isDark = isDark,
            topAlphaLight = 0.18f * highlightScale,
            topAlphaDark = 0.11f * highlightScale,
            bottomAlphaLight = 0.06f * highlightScale,
            bottomAlphaDark = 0.14f * highlightScale
        ),
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

