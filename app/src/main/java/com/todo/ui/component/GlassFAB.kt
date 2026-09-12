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
    val contentColor = if (accent) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

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
                elevation = NeumorphElevation.Medium
            ),
        shape = shape,
        containerColor = Color.Transparent,
        contentColor = contentColor,
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
            tint = contentColor
        )
    }
}
