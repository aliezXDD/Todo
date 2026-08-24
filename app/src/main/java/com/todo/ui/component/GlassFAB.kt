package com.todo.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
            .size(56.dp)
            .scale(fabScale.value),
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.94f),
        contentColor = Color.White,
        interactionSource = interactionSource,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 10.dp)
    ) {
        Text(
            text = "+",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
