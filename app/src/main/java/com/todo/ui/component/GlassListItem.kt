package com.todo.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.DarkGlassBorder
import com.todo.ui.theme.DarkGlassSurface
import com.todo.ui.theme.LightGlassBorder
import com.todo.ui.theme.LightGlassSurface

@Composable
fun GlassListItem(
    modifier: Modifier = Modifier,
    minHeight: Int = 56,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val bgColor = if (isDark) DarkGlassSurface.copy(alpha = 0.72f) else LightGlassSurface.copy(alpha = 0.78f)
    val borderColor = if (isDark) DarkGlassBorder else LightGlassBorder
    val shape = RoundedCornerShape(16.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight.dp),
        shape = shape,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

