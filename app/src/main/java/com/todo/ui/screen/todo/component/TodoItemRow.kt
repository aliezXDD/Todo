package com.todo.ui.screen.todo.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.todo.domain.model.Todo
import com.todo.ui.component.GlassListItem
import com.todo.ui.theme.MotionTokens
import com.todo.ui.theme.NeumorphShapes

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoItemRow(
    todo: Todo,
    isDragging: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier
) {
    val alpha = animateFloatAsState(
        targetValue = if (todo.isCompleted) 0.5f else 1f,
        animationSpec = tween(durationMillis = MotionTokens.ItemState, easing = MotionTokens.StandardEasing),
        label = "todoAlpha"
    )
    val checkboxScale = animateFloatAsState(
        targetValue = if (todo.isCompleted) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = MotionTokens.SpringMediumLow
        ),
        label = "checkboxScale"
    )
    val checkMarkScale = animateFloatAsState(
        targetValue = if (todo.isCompleted) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = MotionTokens.SpringMediumLow
        ),
        label = "checkMarkScale"
    )
    val strikeProgress = animateFloatAsState(
        targetValue = if (todo.isCompleted) 1f else 0f,
        animationSpec = tween(durationMillis = MotionTokens.ItemState, easing = MotionTokens.StandardEasing),
        label = "strikeProgress"
    )
    val rowScale = animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = MotionTokens.Fast, easing = MotionTokens.StandardEasing),
        label = "rowScale"
    )

    val strikeColor = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha.value)
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f

    // 勾选后整行从凸起按进去（"表面"的状态变化保留；勾选框本身仍是平面）
    val rowDepth by animateFloatAsState(
        targetValue = if (todo.isCompleted) 0f else 1f,
        animationSpec = tween(durationMillis = MotionTokens.ItemState, easing = MotionTokens.StandardEasing),
        label = "todoRowDepth"
    )

    // 圆角与行高都不在这里指定：统一走 GlassListItem 的默认规格，保证与预设/回收站条目完全一致
    GlassListItem(
        modifier = modifier
            .fillMaxWidth()
            .scale(rowScale.value)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
                onLongClick = onLongPress
            ),
        depth = rowDepth
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val handleSpace = 32.dp
            val contentWidth = (maxWidth - handleSpace).coerceAtLeast(0.dp)

            Row(
                modifier = Modifier
                    .width(contentWidth),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .scale(checkboxScale.value)
                        // 勾选框保持平面设计（状态指示类元素不进新拟态）
                        .background(
                            color = if (todo.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(NeumorphShapes.Corner)
                        )
                        .then(
                            if (todo.isCompleted) Modifier else Modifier.background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(NeumorphShapes.Corner)
                            )
                        )
                        .clickable { onCheckedChange(!todo.isCompleted) },
                    contentAlignment = Alignment.Center
                ) {
                    if (todo.isCompleted) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).scale(checkMarkScale.value),
                            tint = if (isDark) Color.White else MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Text(
                    text = todo.content,
                    modifier = Modifier
                        .width((contentWidth - 34.dp).coerceAtLeast(0.dp))
                        .drawWithContent {
                            drawContent()
                            if (strikeProgress.value > 0f) {
                                val lineY = size.height * 0.56f
                                drawLine(
                                    color = strikeColor,
                                    start = Offset(0f, lineY),
                                    end = Offset(size.width * strikeProgress.value, lineY),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        },
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = TextDecoration.None,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha.value)
                )
            }

            // 拖拽把手用真实图标（原来是文本字形 "≡"，字宽与基线会随字体漂移，不够专业）
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "拖动排序",
                modifier = dragHandleModifier
                    .align(Alignment.CenterEnd)
                    .size(20.dp)
                    .padding(horizontal = 2.dp),
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = if (isDragging) 0.95f else 0.7f)
            )
        }
    }
}
