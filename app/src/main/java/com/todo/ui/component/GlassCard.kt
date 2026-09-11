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
import androidx.compose.material3.ripple
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
import com.todo.ui.theme.dropOnly

/**
 * 卡片涟漪的半径上限。
 *
 * Material 默认的涟漪会一路铺满整张卡片，在大表面上看起来就是"整张卡变浅了一档"（深色下尤其像
 * 颜色出错）。给一个固定半径后，它只是触点附近的一圈水波，卡片其余部分颜色始终不变。
 */
private val CardRippleRadius = 48.dp

/**
 * 新拟态卡片：与背景同色、无描边，靠一对亮/暗阴影"从背景里凸起"。
 * 底色由 [neumorph] 填充（因此这里 Surface 用透明色），否则凹陷时内阴影会被底色盖住。
 *
 * [onClick] 不为空时有两种反馈，和按钮、FAB 保持一致：
 * 1. 按下把卡片**按进去**（凸→凹）—— 新拟态里表达按压的方式；
 * 2. 触点处一圈水波纹（半径见 [CardRippleRadius]，不会铺满整张卡）。
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(NeumorphShapes.Large),
    depth: Float = 1f,
    elevation: NeumorphElevation = NeumorphElevation.Large,
    fillMaxHeight: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    dropOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    // 二级界面的卡片只留投影（不带左上高光），见 NeumorphElevation.dropOnly
    val effectiveElevation = if (dropOnly) elevation.dropOnly(isDark) else elevation
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val cardModifier = modifier.neumorphPress(
        shape = shape,
        isDark = isDark,
        pressed = pressed && onClick != null,
        elevation = effectiveElevation,
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
                            indication = ripple(radius = CardRippleRadius),
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
