package com.todo.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.MotionTokens

@Composable
fun GlassFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    val fabScale = animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
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
            .scale(fabScale.value),
        // 大圆角矩形：边长 52dp，圆角用大半径；+ 图标保持默认尺寸不变
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.94f),
        contentColor = Color.White,
        interactionSource = interactionSource,
        // 阴影再淡一点（5.6dp → 4dp）
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "添加",
            tint = Color.White
        )
    }
}
