package com.todo.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes

/**
 * 卡片涟漪的半径上限。
 *
 * Material 默认的涟漪会一路铺满整张卡片，在大表面上看起来就是"整张卡变浅了一档"（深色下尤其像
 * 颜色出错）。给一个固定半径后，它只是触点附近的一圈水波，卡片其余部分颜色始终不变。
 */
private val CardRippleRadius = 48.dp

/**
 * 新拟态卡片：与背景同色、无描边，靠一对亮/暗阴影"从背景里凸起"。
 * 底色由 [neumorphPress]/[neumorph] 填充（因此这里不铺任何底色），否则凹陷时内阴影会被底色盖住。
 *
 * **卡片不裁剪内容**。这里刻意不用 M3 的 `Surface`：它会按卡片形状裁掉超出边界的一切，而卡片内侧
 * 的按钮、条目本身就靠溢出的光影表达凸起（偏移 7dp + 模糊 11dp ≈ 18dp），于是卡片那圈"框"会在
 * 边缘把它们的阴影切掉一刀。卡片与背景同色，光影多出来那几 dp 落在同色表面上根本看不见，
 * 所以不需要裁剪。
 *
 * 需要裁剪的只有卡片自己的点击涟漪：它单独铺一层（尺寸跟随卡片、被裁成卡片形状）挂在内容**之下**，
 * 这样既不会从圆角外露出方块，也不会挡住内容的绘制与点击。内容自己的形状/涟漪各自由其自身裁剪。
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

    // Surface 原来还负责把 onSurface 注入 LocalContentColor，这里补上
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        Box(modifier = cardModifier) {
            if (onClick != null) {
                // 点击层：铺满卡片、裁成卡片形状，但排在内容之前（因此波纹在内容之下，
                // 且不会抢走内容里按钮的点击）
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(shape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(radius = CardRippleRadius),
                            role = Role.Button,
                            onClick = onClick
                        )
                )
            }

            Box(
                modifier = Modifier
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
}
