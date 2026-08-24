package com.todo.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.DarkGlassBorder
import com.todo.ui.theme.DarkGlassScrim
import com.todo.ui.theme.DarkGlassSurface
import com.todo.ui.theme.LightGlassBorder
import com.todo.ui.theme.LightGlassScrim
import com.todo.ui.theme.LightGlassSurface
import com.todo.ui.theme.MotionTokens

@Composable
fun GlassDialog(
    visible: Boolean = true,
    title: String,
    message: String,
    confirmText: String = "确定",
    cancelText: String = "取消",
    confirmButtonRole: DialogButtonRole = DialogButtonRole.DANGER,
    cancelButtonRole: DialogButtonRole = DialogButtonRole.SECONDARY,
    confirmTextColor: Color? = null,
    cancelTextColor: Color? = null,
    surfaceAlphaDelta: Float = 0f,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val baseAlpha = if (isDark) 0.92f else 0.94f
    val bg = if (isDark) {
        DarkGlassSurface.copy(alpha = (baseAlpha + surfaceAlphaDelta).coerceIn(0f, 1f))
    } else {
        LightGlassSurface.copy(alpha = (baseAlpha + surfaceAlphaDelta).coerceIn(0f, 1f))
    }
    val border = if (isDark) DarkGlassBorder else LightGlassBorder
    val titleColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val messageColor = if (isDark) Color.White.copy(alpha = 0.92f) else MaterialTheme.colorScheme.onSurface
    val scrimColor = if (isDark) DarkGlassScrim else LightGlassScrim
    val noRipple = remember { MutableInteractionSource() }
    val transitionState = remember { MutableTransitionState(false) }
    val confirmRoleColor by animateColorAsState(
        targetValue = when (confirmButtonRole) {
            DialogButtonRole.DANGER -> MaterialTheme.colorScheme.error
            DialogButtonRole.PRIMARY -> MaterialTheme.colorScheme.primary
            DialogButtonRole.SECONDARY -> messageColor
        },
        animationSpec = tween(durationMillis = MotionTokens.DialogEnter, easing = MotionTokens.StandardEasing),
        label = "dialogConfirmRoleColor"
    )
    val cancelRoleColor by animateColorAsState(
        targetValue = when (cancelButtonRole) {
            DialogButtonRole.DANGER -> MaterialTheme.colorScheme.error
            DialogButtonRole.PRIMARY -> MaterialTheme.colorScheme.primary
            DialogButtonRole.SECONDARY -> messageColor
        },
        animationSpec = tween(durationMillis = MotionTokens.DialogEnter, easing = MotionTokens.StandardEasing),
        label = "dialogCancelRoleColor"
    )

    LaunchedEffect(visible) {
        transitionState.targetState = visible
    }
    if (!transitionState.currentState && !transitionState.targetState) return

    AnimatedVisibility(
        visibleState = transitionState,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = MotionTokens.DialogEnter,
                easing = MotionTokens.StandardEasing
            )
        ) + scaleIn(
            initialScale = 0.96f,
            animationSpec = tween(
                durationMillis = MotionTokens.Standard,
                easing = MotionTokens.StandardEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = MotionTokens.DialogExit,
                easing = MotionTokens.StandardEasing
            )
        ) + scaleOut(
            targetScale = 0.96f,
            animationSpec = tween(
                durationMillis = MotionTokens.DialogExit,
                easing = MotionTokens.StandardEasing
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor)
                .clickable(
                    interactionSource = noRipple,
                    indication = null,
                    onClick = onCancel
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .glassOverlay(
                        shape = RoundedCornerShape(20.dp),
                        isDark = isDark,
                        topAlphaLight = 0.18f,
                        topAlphaDark = 0.10f,
                        bottomAlphaLight = 0.06f,
                        bottomAlphaDark = 0.14f
                    )
                    .fillMaxWidth(0.85f)
                    .clickable(
                        interactionSource = noRipple,
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(20.dp),
                color = bg,
                border = BorderStroke(1.dp, border),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, color = titleColor)
                    Text(text = message, style = MaterialTheme.typography.bodyMedium, color = messageColor)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        DialogActionTextButton(
                            text = cancelText,
                            role = cancelButtonRole,
                            fallbackColor = cancelTextColor ?: cancelRoleColor,
                            onClick = onCancel
                        )
                        DialogActionTextButton(
                            text = confirmText,
                            role = confirmButtonRole,
                            fallbackColor = confirmTextColor ?: confirmRoleColor,
                            onClick = onConfirm
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DialogActionTextButton(
    text: String,
    role: DialogButtonRole,
    fallbackColor: Color? = null,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val animatedColor by animateColorAsState(
        targetValue = when (role) {
            DialogButtonRole.PRIMARY -> MaterialTheme.colorScheme.primary
            DialogButtonRole.DANGER -> MaterialTheme.colorScheme.error
            DialogButtonRole.SECONDARY -> if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(durationMillis = MotionTokens.DialogEnter, easing = MotionTokens.StandardEasing),
        label = "dialogActionColor"
    )

    TextButton(onClick = onClick) {
        Text(text = text, color = fallbackColor ?: animatedColor)
    }
}

enum class DialogButtonRole {
    PRIMARY,
    DANGER,
    SECONDARY
}

