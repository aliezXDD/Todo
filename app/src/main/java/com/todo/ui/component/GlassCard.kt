package com.todo.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes

/**
 * 新拟态卡片：与背景同色、无描边，靠一对亮/暗阴影"从背景里凸起"。
 * 底色由 [neumorph] 填充（因此这里 Surface 用透明色），否则凹陷时内阴影会被底色盖住。
 *
 * [onClick] 不为空时，按下会把卡片**按进去**（凸→凹），与按钮、FAB 是同一套反馈。
 * 这里刻意**不用** Material 的水波纹：它在小按钮上是局部涟漪，在整张卡片上却等于给整张卡
 * 糊一层 ~12% 的浅色，看起来就是"卡片颜色偶尔变浅了"——那不是新拟态的表达方式（深度才表达按压）。
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(NeumorphShapes.Large),
    depth: Float = 1f,
    elevation: NeumorphElevation = NeumorphElevation.Large,
    fillMaxHeight: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val cardModifier = modifier.neumorphPress(
        shape = shape,
        isDark = isDark,
        pressed = pressed && onClick != null,
        elevation = elevation,
        restDepth = depth
    )

    Surface(
        modifier = cardModifier,
        shape = shape,
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                // 点击层在卡片形状之内（Surface 自带裁剪），覆盖整张卡片（在 padding 之外）
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            role = Role.Button,
                            onClick = onClick
                        )
                    } else {
                        Modifier
                    }
                )
                // fillMaxHeight=true 时让内容撑满卡片高度（配合外部 weight 固定卡片高度）；默认仍按内容高度。
                // contentPadding 可传"只有纵向"的值：内部若有一条滚动列表，让它越出卡片的左右内边距、
                // 由列表自己把条目内缩（见 TodaySection），条目溢出的光影才不会被滚动视口硬切。
                .then(if (fillMaxHeight) Modifier.fillMaxHeight() else Modifier)
                .padding(contentPadding)
        ) {
            content()
        }
    }
}
