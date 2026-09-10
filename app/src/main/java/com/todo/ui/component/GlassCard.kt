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
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f

    Surface(
        modifier = modifier.neumorph(
            shape = shape,
            isDark = isDark,
            depth = depth,
            elevation = elevation
        ),
        shape = shape,
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        // fillMaxHeight=true 时让内容撑满卡片高度（配合外部 weight 固定卡片高度）；默认仍按内容高度
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
}
