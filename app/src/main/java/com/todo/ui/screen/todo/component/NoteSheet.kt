package com.todo.ui.screen.todo.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.todo.ui.component.GlassBottomSheet
import com.todo.ui.component.NeumorphTextField
import com.todo.ui.theme.NeumorphShapes

/**
 * 笔记面板：一块**很大**的凹陷输入区（占据屏幕高度的一半左右），用来随手写东西。
 *
 * 内容不在这里保存，也不在这里判断"要不要保存"：编辑只更新草稿，
 * **关闭面板时由调用方一次性写回存储**（拖动关闭、点遮罩、返回键都会走 [onDismiss]）。
 * 所以面板上没有"保存"按钮——那会让人以为不点就不保存。
 */
@Composable
fun NoteSheet(
    visible: Boolean,
    content: String,
    onContentChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    // 按屏幕高度取，而不是写死 dp：小屏上不顶到状态栏，大屏上也不会只占一小条。
    // 0.38 是"够大"与"完整可见"的平衡点：加上标题、提示与面板内边距，整块面板约占屏幕一半，
    // 且键盘弹出（约 300dp）后仍不会把输入区挤到屏幕外（0.45 时会溢出底部）。
    val fieldHeight = LocalConfiguration.current.screenHeightDp.dp * 0.38f

    GlassBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "笔记",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "关闭窗口时自动保存",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            NeumorphTextField(
                value = content,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fieldHeight),
                placeholder = "写点什么…",
                shape = RoundedCornerShape(NeumorphShapes.Corner),
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
