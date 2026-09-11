package com.todo.ui.screen.todo.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.todo.domain.model.Todo
import com.todo.ui.component.GlassBottomSheet
import com.todo.ui.component.GlassButton
import com.todo.ui.component.GlassCard
import com.todo.ui.component.NeumorphTextField
import com.todo.ui.theme.NeumorphElevation

@Composable
fun EditTodoSheet(
    visible: Boolean,
    todo: Todo?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: (Todo) -> Unit
) {
    if (!visible || todo == null) return

    val focusRequester = remember { FocusRequester() }
    var input by remember(todo.id) { mutableStateOf(TextFieldValue(todo.content, TextRange(todo.content.length))) }
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f

    LaunchedEffect(todo.id) {
        // Delay to let the bottom-sheet popup attach its node before requesting focus.
        delay(80)
        focusRequester.requestFocus()
    }

    GlassBottomSheet(
        onDismissRequest = onDismiss
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            // 与「添加待办」同一规格：卡片只当布局容器，不画光影，
            // 免得面板外围出现一圈深色边框、与内侧按钮的光影打架
            elevation = NeumorphElevation.None
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "编辑待办",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                NeumorphTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        text = "删除",
                        onClick = { onDelete(todo) },
                        glassSurface = true, // 与添加待办页面中的“完成”按钮同色（玻璃/次要）
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "保存",
                        onClick = { if (input.text.isNotBlank()) onSave(input.text) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
