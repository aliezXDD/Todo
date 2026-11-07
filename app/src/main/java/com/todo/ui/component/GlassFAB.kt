package com.todo.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.MotionTokens
import com.todo.ui.theme.Neumorph
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes

/**
 * 新拟态悬浮按钮：边长 52dp 的大圆角矩形，按下时凹入。
 * 表面对比度高（主题色），所以按下除了凹入只做很轻微的缩放，避免"又缩又凹"过度。
 *
 * [accent] = true 是主操作（主题色表面，如「+」）；
 * false 得到与卡片同色的次级按钮（内容色用 onSurface），用于「+」旁边的笔记入口。
 */
@Composable
fun GlassFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    accent: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val shape = RoundedCornerShape(NeumorphShapes.Medium)

    val surfaceColor = if (accent) MaterialTheme.colorScheme.primary else Neumorph.surface(isDark)
    // 图标自己的颜色：两种按钮各自取对比色（主色上用 onPrimary，玻璃面上用 onSurface）
    val iconColor = if (accent) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    // 按下高亮的颜色**不跟图标走**，两种按钮统一用 onSurface。
    // M3 的 FloatingActionButton 内部是 Surface(onClick)，它拿 contentColor 当涟漪颜色：
    // 要是让涟漪跟着图标，主色那颗按下去是"中间发暗的一团"，跟旁边两颗"整块变亮"完全不是一回事。
    val rippleColor = MaterialTheme.colorScheme.onSurface
    // 「按进去」那两张内阴影：同色表面沿用页面那对灰影子；主色表面换成同色系（见 tonalShadowDark），
    // 否则深灰压在橙色上会糊成一块黑，跟旁边两颗轻轻沉下去的样子对不上
    val innerShadowDark = if (accent) Neumorph.tonalShadowDark(surfaceColor) else Neumorph.shadowDark(isDark)
    val innerShadowLight = if (accent) Neumorph.tonalShadowLight(surfaceColor) else Neumorph.shadowLight(isDark)

    val fabScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = MotionTokens.SpringLow
        ),
        label = "fabScale"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(52.dp)
            .scale(fabScale)
            .neumorphPress(
                shape = shape,
                isDark = isDark,
                pressed = isPressed,
                surface = surfaceColor,
                elevation = NeumorphElevation.Medium,
                innerShadowDark = innerShadowDark,
                innerShadowLight = innerShadowLight
            ),
        shape = shape,
        containerColor = Color.Transparent,
        // 这里给的是「涟漪颜色」：按下高亮由它决定（见 rippleColor 的说明），
        // 图标自己的颜色下面单独给
        contentColor = rippleColor,
        interactionSource = interactionSource,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor
        )
    }
}
