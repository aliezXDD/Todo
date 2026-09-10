package com.todo.ui.component

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes

/**
 * 新拟态列表项：默认凸起；[depth] 传 0 表示"被按进去"（用于已完成/选中态）。
 * [elevation] 传零阴影（offset/blur 均为 0）可得到"与背景齐平"的平面行。
 */
@Composable
fun GlassListItem(
    modifier: Modifier = Modifier,
    depth: Float = 1f,
    elevation: NeumorphElevation = NeumorphElevation.Medium,
    shape: Shape = RoundedCornerShape(NeumorphShapes.Medium),
    minHeight: Int = 56,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val itemModifier = modifier.neumorph(
        shape = shape,
        isDark = isDark,
        depth = depth,
        elevation = elevation
    )
    val inner: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = minHeight.dp)
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }

    // 同 GlassCard：可点击时用 Surface 的点击重载裁水波纹，调用方不要再自己 clip()
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = itemModifier,
            shape = shape,
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            content = inner
        )
    } else {
        Surface(
            modifier = itemModifier,
            shape = shape,
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            content = inner
        )
    }
}
