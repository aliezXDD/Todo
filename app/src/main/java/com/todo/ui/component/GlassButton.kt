package com.todo.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.DarkAction
import com.todo.ui.theme.DarkActionDisabled
import com.todo.ui.theme.DarkGlassBorder
import com.todo.ui.theme.DarkGlassSurface
import com.todo.ui.theme.LightAction
import com.todo.ui.theme.LightActionDisabled
import com.todo.ui.theme.LightGlassBorder
import com.todo.ui.theme.LightGlassSurface

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    glassSurface: Boolean = false,
    shape: Shape = RoundedCornerShape(14.dp),
    borderShape: Shape = shape
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val targetContainer = when {
        glassSurface && isDark -> DarkGlassSurface.copy(alpha = 0.80f)
        glassSurface -> LightGlassSurface.copy(alpha = 0.84f)
        isDark -> DarkAction
        else -> LightAction
    }
    val targetBorder = when {
        glassSurface && isDark -> DarkGlassBorder
        glassSurface -> LightGlassBorder
        else -> Color.White.copy(alpha = if (isDark) 0.18f else 0.10f)
    }
    val targetContent = if (glassSurface) MaterialTheme.colorScheme.onSurface else Color.White
    // 颜色变化时平滑过渡：仅当某按钮的颜色实际变化时（如“添加”随输入内容切换主题色/玻璃色）才会触发动画，
    // 其余颜色恒定的按钮不会产生任何可见动画。
    val containerColor by animateColorAsState(
        targetValue = targetContainer,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "glassButtonContainer"
    )
    val borderColor by animateColorAsState(
        targetValue = targetBorder,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "glassButtonBorder"
    )
    val contentColor by animateColorAsState(
        targetValue = targetContent,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "glassButtonContent"
    )
    val disabledContainerColor = if (isDark) DarkActionDisabled else LightActionDisabled
    // 允许描边用与表面不同的圆角（如往日记录：表面 14dp、描边 16dp）；此时不带 Button 自带描边，改用 modifier 画描边
    val buttonModifier = if (borderShape != shape) {
        modifier.border(1.dp, borderColor, borderShape)
    } else {
        modifier
    }

    Button(
        onClick = onClick,
        modifier = buttonModifier,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor.copy(alpha = if (enabled) 0.95f else 0.82f),
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = contentColor.copy(alpha = 0.7f)
        ),
        border = if (borderShape != shape) null else androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(text = text)
    }
}
