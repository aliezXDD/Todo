package com.todo.ui.screen.preset.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.dp
import com.todo.domain.model.Preset
import com.todo.ui.component.GlassListItem
import com.todo.ui.component.glassOverlay
import com.todo.ui.theme.MotionTokens

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PresetItemRow(
    preset: Preset,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
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
    val shape = RoundedCornerShape(16.dp)
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f

    GlassListItem(
        modifier = modifier
            .fillMaxWidth()
            .glassOverlay(
                shape = shape,
                isDark = isDark,
                topAlphaLight = 0.18f,
                topAlphaDark = 0.11f,
                bottomAlphaLight = 0.06f,
                bottomAlphaDark = 0.14f
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
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
                            modifier = Modifier.offset(y = 1.dp).size(16.dp),
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }
        }
    }
}
