package com.todo.ui.screen.preset.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todo.domain.model.Preset
import com.todo.ui.theme.DarkScrim
import com.todo.ui.theme.LightScrim
import com.todo.ui.theme.MotionTokens
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes
import com.todo.ui.component.NeumorphTextField
import com.todo.ui.component.neumorph
import kotlinx.coroutines.delay

@Composable
fun PresetEditDialog(
    visible: Boolean,
    editingPreset: Preset?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val transitionState = remember { MutableTransitionState(false) }
    LaunchedEffect(visible) {
        transitionState.targetState = visible
    }
    if (!transitionState.currentState && !transitionState.targetState) return

    var input by remember(editingPreset?.id) { mutableStateOf(editingPreset?.content.orEmpty()) }
    val isCreateMode = editingPreset == null
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f

    // 弹窗打开即弹出键盘（与「添加待办」「编辑待办」「备注」同一做法：等节点挂上再请求焦点）
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(visible) {
        if (visible) {
            delay(80)
            focusRequester.requestFocus()
        }
    }

    val titleColor = MaterialTheme.colorScheme.onSurface
    val contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f)
    val scrimColor = if (isDark) DarkScrim else LightScrim
    val noRipple = remember { MutableInteractionSource() }

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
                // 键盘弹出时向上让位：弹窗是垂直居中的，不加这个内边距时键盘会盖住输入框与按钮
                .imePadding()
                .background(scrimColor)
                .clickable(
                    interactionSource = noRipple,
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .neumorph(
                        shape = RoundedCornerShape(NeumorphShapes.Large),
                        isDark = isDark,
                        depth = 1f,
                        // 弹窗不带上高光：只用投影表达"浮在遮罩之上"
                        elevation = NeumorphElevation.Dialog
                    ),
                shape = RoundedCornerShape(NeumorphShapes.Large),
                color = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .clickable(
                            interactionSource = noRipple,
                            indication = null
                        ) {},
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isCreateMode) "新建预设" else "编辑预设",
                        style = MaterialTheme.typography.titleLarge,
                        color = titleColor
                    )

                    NeumorphTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = "输入预设内容...",
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        // 与对话框同形（默认 TextButton 是胶囊水波纹）
                        TextButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(NeumorphShapes.Corner)
                        ) {
                            Text(text = "取消", color = contentColor)
                        }
                        TextButton(
                            onClick = { if (input.isNotBlank()) onSave(input) },
                            shape = RoundedCornerShape(NeumorphShapes.Corner)
                        ) {
                            Text(text = if (isCreateMode) "创建" else "保存", color = contentColor)
                        }
                    }
                }
            }
        }
    }
}
