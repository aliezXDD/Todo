package com.todo.ui.screen.todo.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.todo.util.DateUtils

/**
 * 截止日期那颗按钮的高度。
 *
 * 比常规按钮矮一档：常规按钮的内容是 labelLarge 行高 20dp + 上下各 12dp = 44dp，随后又被 M3 的最小触摸目标
 * 垫到 48dp。40dp 正好是 M3 自己给小号按钮的高度（`ButtonDefaults.MinHeight`），也是它被当作"次要行动"时
 * 该有的分量：和「删除」一样站次要一侧，不抢「保存」的位置。
 *
 * 用 [LocalMinimumInteractiveComponentSize] 把它同时声明成触摸目标，内容高度就与触摸目标相等：
 * 波纹/高光依旧和新拟态形状严丝合缝（见 [GlassButton] 的说明），不会像默认那样差出那 2dp。
 */
private val DueDateButtonHeight = 40.dp

/**
 * 截止日期那颗按钮的光影档位：比常规按钮淡一半。
 *
 * 新拟态里按钮的"颜色"其实就是这层暗影 —— 表面色与面板底色完全相同（浅色下还刻意不留左上高光），
 * 所以暗影一收，按钮立刻显得轻。常规档是按列表项那类大元素标定的（偏移 7dp / 模糊 11dp、满强度），
 * 压在一颗 40dp 高的小按钮上，它就显得比按钮承载的内容还重；这里几何不动，只把暗影强度减半。
 */
private val DueDateButtonElevation = NeumorphElevation.Medium.copy(darkAlpha = 0.5f)

/**
 * 编辑待办面板：内容 + 截止日期，排布与「添加待办」同一套（标题 → 输入区 → 动作按钮）。
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
                // 两者各占一半宽度：按钮的宽度因此与下面的「保存」完全对齐，两行成为同一套两列网格。
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "截止日期",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    // 这颗按钮矮一档：把触摸目标也声明成 [DueDateButtonHeight]，
                    // 于是内容高度 = 触摸目标 = 新拟态形状，三者重合（见 [GlassButton] 的说明）。
                    CompositionLocalProvider(
                        LocalMinimumInteractiveComponentSize provides DueDateButtonHeight
                    ) {
                        GlassButton(
                            text = dueDate?.let { DateUtils.formatShortDate(it) } ?: "选择日期",
                            onClick = { pickerVisible = true },
                            // 玻璃面 = 次要行动：不抢「保存」的位置；暗影再淡一半，比「删除」更轻
                            glassSurface = true,
                            elevation = DueDateButtonElevation,
                            modifier = Modifier.weight(1f),
                            // 纵向内边距 = (目标高度 − labelLarge 行高 20dp) / 2：
                            // 内容高度正好等于目标高度，改 [DueDateButtonHeight] 也不会与内边距脱节。
                            // 横向压到 12dp：宽度已经被"与「保存」对齐"钉死，这点内边距只影响文字能摊多宽 ——
                            // 留窄些，免得窄屏或大字体下「2026年10月2日」被挤成两行（文字居中，平时看不出差别）
                            contentPadding = PaddingValues(
                                horizontal = 12.dp,
                                vertical = (DueDateButtonHeight - 20.dp) / 2
                            )
                        )
                    }
                }
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
