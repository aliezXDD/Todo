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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.todo.domain.model.Todo
import com.todo.ui.component.GlassBottomSheet
import com.todo.ui.component.GlassButton
import com.todo.ui.component.GlassCard
import com.todo.ui.component.NeumorphTextField
import com.todo.ui.theme.NeumorphElevation

/**
 * 编辑待办面板：内容 + 截止日期，排布与「添加待办」同一套（标题 → 输入区 → 动作按钮）。
 *
 * 截止日期那一行共用 [DueDateField]（高度、光影、左右比例都在那里），
 * 「添加待办」面板用的是同一个组件，两处不会走偏。
 *
 * 截止日期不立即落盘：[dueDate] 与输入框里的内容一样只是这里的草稿，按「保存」才一起写回
 * （这个面板本来就有保存按钮，改成"选完即存"会让人分不清哪一步生效了）。
 *
 * 截止日期的含义见 `com.todo.domain.model.isDeferred`：设了它、未完成、期限没到的待办是**预留**的，
 * 到期前不影响完成统计、也不进往日记录 —— 所以这一行右边那颗按钮同时是"这条为什么不计入今天"的答案。
 *
 * [today] 是当前逻辑日，交给日历当"最早可选的一天"（今天之前的日期没有意义）。
 */
@Composable
fun EditTodoSheet(
    visible: Boolean,
    todo: Todo?,
    today: String,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit,
    onDelete: (Todo) -> Unit
) {
    if (!visible || todo == null) return

    val focusRequester = remember { FocusRequester() }
    var input by remember(todo.id) { mutableStateOf(TextFieldValue(todo.content, TextRange(todo.content.length))) }
    // 截止日期的草稿：初值就是这条待办当前的截止日期（null = 没有截止日期）
    var dueDate by remember(todo.id) { mutableStateOf(todo.dueDate) }
    var pickerVisible by remember(todo.id) { mutableStateOf(false) }

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
                // 截止日期：左边标签，右边那颗按钮显示当前值（未设置时写「选择日期」）。
                // 与「添加待办」面板共用同一个组件，宽度比例 / 高度 / 光影因此不可能走偏
                DueDateField(
                    label = "截止日期",
                    dueDate = dueDate,
                    onClick = { pickerVisible = true }
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
                        onClick = { if (input.text.isNotBlank()) onSave(input.text, dueDate) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    DeadlinePickerDialog(
        visible = pickerVisible,
        today = today,
        selectedDate = dueDate,
        onDismiss = { pickerVisible = false },
        onConfirm = { picked ->
            dueDate = picked
            pickerVisible = false
        },
        onClear = {
            dueDate = null
            pickerVisible = false
        }
    )
}
