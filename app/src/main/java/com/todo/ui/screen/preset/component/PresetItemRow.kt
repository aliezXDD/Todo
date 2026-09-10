package com.todo.ui.screen.preset.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.input.pointer.pointerInput
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
    val interactionSource = remember { MutableInteractionSource() }
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
    val shape = RoundedCornerShape(NeumorphShapes.Medium)
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f

    GlassListItem(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(preset.id) {
                detectTapGestures(
                    onDoubleTap = { onDoubleClick() },
                    onLongPress = { onLongClick() },
                    onPress = { pressPosition ->
                        val press = PressInteraction.Press(pressPosition)
                        interactionSource.tryEmit(press)
                        try {
                            tryAwaitRelease()
                        } finally {
                            interactionSource.tryEmit(PressInteraction.Release(press))
                        }
                    },
                    onTap = { onClick() }
                )
            }
            .indication(interactionSource, LocalIndication.current)
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
                        .background(indicatorColor.value, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.offset(y = with(LocalDensity.current) { (-1.dp.toPx() + 1f).toDp() }).size(16.dp),
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }
        }
    }
}
