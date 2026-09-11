package com.todo.ui.screen.preset.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.todo.domain.model.Preset
import com.todo.ui.component.GlassListItem
import com.todo.ui.theme.MotionTokens
import com.todo.ui.theme.NeumorphShapes

@Composable
fun PresetItemRow(
    preset: Preset,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val indicatorScale = animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.86f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = MotionTokens.SpringMediumLow
        ),
        label = "presetSelectScale"
    )
    val indicatorColor = animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
        },
        label = "presetSelectColor"
    )

    // 圆角/行高/点击反馈（含长按与双击）全部走 GlassListItem 的统一规格：
    // 自己挂 pointerInput + indication 会让波纹落在填充之下、且不按圆角裁剪，只从四个圆角漏出方形亮块
    GlassListItem(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        onLongClick = onLongClick,
        onDoubleClick = onDoubleClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = preset.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (isMultiSelectMode) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .scale(indicatorScale.value)
                        // 多选标记用"小方块圆角"，与待办勾选框、回收站标记一致
                        .background(
                            color = indicatorColor.value,
                            shape = RoundedCornerShape(NeumorphShapes.Marker)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier
                                .offset(y = with(LocalDensity.current) { (-1.dp.toPx() + 1f).toDp() })
                                .size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
