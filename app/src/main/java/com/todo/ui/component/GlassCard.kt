package com.todo.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes

/**
 * 新拟态卡片：与背景同色、无描边，靠一对亮/暗阴影"从背景里凸起"。
 * 底色由 [neumorph] 填充（因此这里 Surface 用透明色），否则凹陷时内阴影会被底色盖住。
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(NeumorphShapes.Large),
    depth: Float = 1f,
    elevation: NeumorphElevation = NeumorphElevation.Large,
    fillMaxHeight: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val cardModifier = modifier.neumorph(
        shape = shape,
        isDark = isDark,
        depth = depth,
        elevation = elevation
    )
    // fillMaxHeight=true 时让内容撑满卡片高度（配合外部 weight 固定卡片高度）；默认仍按内容高度
    val inner: @Composable () -> Unit = {
        Box(
            modifier = if (fillMaxHeight) {
                Modifier.fillMaxHeight().padding(16.dp)
            } else {
                Modifier.padding(16.dp)
            }
        ) {
            content()
        }
    }

    // 可点击时走 Surface 的点击重载：水波纹会被裁到圆角内，调用方因此**不需要**再自己 clip()
    // （在 neumorph 之前调用 clip() 会把外阴影一起裁掉，这是阴影显得很浅的常见原因）
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = cardModifier,
            shape = shape,
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            content = inner
        )
    } else {
        Surface(
            modifier = cardModifier,
            shape = shape,
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            content = inner
        )
    }
}
