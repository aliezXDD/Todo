package com.todo.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.Neumorph
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes

/**
 * 新拟态按钮：平时凸起，**按下时凹进去**（凸→凹的连续形变，这是新拟态最具标志性的手感）。
 *
 * [glassSurface] = true 得到"与背景同色"的次级按钮；false 得到以主题色为表面的主操作按钮。
 * 表面由 [neumorphPress] 填充，因此 Button 自身容器色透明。
 *
 * [recessed] = true 表示"这一颗是当前选中项"：静止时就凹着（凹陷 = 已选中），
 * 与底部导航选中项、列表里的选中条目同一种语义。二选一的分段按钮因此不必另造组件。
 *
 * [contentPadding] 直接交给 M3 的 `Button`，但**纵向值会决定点击波纹能不能和按钮形状重合**：
 * M3 的 Button 会把不足最小触摸目标（48dp）的内容用**透明留白**补到 48dp，而那块留白落在这颗按钮的
 * neumorph 形状**之内**、按钮自身裁剪**之外**——波纹画在裁剪之内，于是比按钮本体小一圈，
 * 凹下去时那一圈高光就会与边框错开。把纵向内边距垫到内容正好 48dp（labelLarge 行高 20dp，
 * 纵向 14dp × 2）即可让两者严丝合缝：按钮本体尺寸与文字位置都不变，只有波纹对齐到边缘。
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    glassSurface: Boolean = false,
    recessed: Boolean = false,
    shape: Shape = RoundedCornerShape(NeumorphShapes.Small),
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val accent = MaterialTheme.colorScheme.primary
    val surfaceColor = if (glassSurface) Neumorph.surface(isDark) else accent
    val contentColor = if (glassSurface) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary

    Button(
        onClick = onClick,
        modifier = modifier.neumorphPress(
            shape = shape,
            isDark = isDark,
            pressed = pressed && enabled && !recessed,
            surface = if (enabled) surfaceColor else surfaceColor.copy(alpha = 0.45f),
            elevation = NeumorphElevation.Medium,
            restDepth = if (recessed) 0f else 1f
        ),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = contentColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = contentColor.copy(alpha = 0.45f)
        ),
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp,
            hoveredElevation = 0.dp,
            focusedElevation = 0.dp
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
