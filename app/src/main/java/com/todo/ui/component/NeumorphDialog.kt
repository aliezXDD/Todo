package com.todo.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.todo.ui.theme.DarkScrim
import com.todo.ui.theme.LightScrim
import com.todo.ui.theme.MotionTokens
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes

/**
 * 铺满整屏的新拟态对话框外壳：遮罩 + 居中的凸起面板，内容由调用方给。
 *
 * 与 [GlassDialog] 的区别只有一处、但很关键：这里是**真正的 `Dialog` 窗口**，因此能盖住底部导航
 * 那种"叠在内容之上的兄弟节点"。[GlassDialog] 是就地铺一层 `fillMaxSize` 的遮罩，只能盖住它所在
 * 的导航内容区，底部导航仍露在遮罩外面、还能点 —— 对"选图 / 裁剪"这种要用户先处理完的模态操作，
 * 那是不能接受的。[GlassDialog] 的既有用法（回收站、预设页）行为保持不变，本组件不动它。
 *
 * 动画、配色、层级与 [GlassDialog]、[com.todo.ui.screen.todo.component.DeadlinePickerDialog] 一致。
 */
@Composable
fun NeumorphDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    widthFraction: Float = 0.86f,
    content: @Composable ColumnScope.() -> Unit
) {
    val transitionState = remember { MutableTransitionState(false) }
    LaunchedEffect(visible) {
        transitionState.targetState = visible
    }
    if (!transitionState.currentState && !transitionState.targetState) return

    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val scrimColor = if (isDark) DarkScrim else LightScrim
    val noRipple = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(NeumorphShapes.Large)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            // 遮罩与面板要铺满整屏：不受平台默认宽度限制
            usePlatformDefaultWidth = false,
            // 点面板外由下面的遮罩自己处理，免得两套"点外面关闭"打架
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false
        )
    ) {
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
                        onClick = onDismiss
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = modifier
                        .fillMaxWidth(widthFraction)
                        .neumorph(
                            shape = shape,
                            isDark = isDark,
                            depth = 1f,
                            // 弹窗不带上高光：只用投影表达"浮在遮罩之上"
                            elevation = NeumorphElevation.Dialog
                        )
                        // 吃掉落在面板上的点击，否则会穿透到遮罩把自己关掉
                        .clickable(
                            interactionSource = noRipple,
                            indication = null,
                            onClick = {}
                        ),
                    shape = shape,
                    color = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        content = content
                    )
                }
            }
        }
    }
}
