package com.todo.ui.screen.todo.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.todo.ui.component.GlassBottomSheet
import com.todo.ui.component.NeumorphTextField
import com.todo.ui.theme.NeumorphShapes
import kotlinx.coroutines.delay

/**
 * 备注面板：一块**固定高度**的凹陷输入区，用来随手写东西。
 *
 * 内容不在这里保存，也不在这里判断"要不要保存"：编辑只更新草稿，
 * **关闭面板时由调用方一次性写回存储**（拖动关闭、点遮罩、返回键都会走 [onDismiss]）。
 * 所以面板上没有"保存"按钮——那会让人以为不点就不保存。
 *
 * 输入区只占屏幕高度的两成左右，因此**长文本靠框内滚动查看**（多行输入框自带滚动，
 * 光标移动、点击定位都会自动把它带进可视区）；高度写死在这儿是有意的：
 * 文本再长也不该把面板顶得满屏都是。
 */
@Composable
fun NoteSheet(
    visible: Boolean,
    content: String,
    onContentChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    // 光标落在**文本末尾**：面板一开就能接着往下写。
    // 只用 requestFocus() 的话光标会停在开头（实测过：新输入会被插到已有内容前面），
    // 所以这里自己持有 TextFieldValue，并在打开时把 selection 放到末尾。
    // 面板关闭时本组件会退出组合，下次打开是全新的状态，无需手动清理。
    var fieldValue by remember {
        mutableStateOf(TextFieldValue(content, selection = TextRange(content.length)))
    }

    // 点按钮后直接弹出键盘（与「编辑待办」「添加待办」同一做法：等 sheet 的节点挂上再请求焦点）
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(80)
        focusRequester.requestFocus()
    }

    // 高度按**实际容器高度**取，不写死 dp、也不用 Configuration.screenHeightDp：后者在多窗口/折叠屏下
    // 不保证随 Compose 容器变化（lint 的 ConfigurationScreenWidthHeight）。
    // 0.19 ≈ 输入的七八行；再长就靠框内上下拖动查看（见类注释）。
    val density = LocalDensity.current
    val containerHeightDp = with(density) { LocalWindowInfo.current.containerSize.height.toDp() }
    val fieldHeight = containerHeightDp * 0.19f

    GlassBottomSheet(
        onDismissRequest = onDismiss,
        // 面板自己的下滑关闭手势会让给文本框：长文本要在框内上下拖动查看，
        // 两者抢的是同一个纵向拖动（拖下去会把面板拖走）。关闭改为点面板外或按返回键。
        dragEnabled = false
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "备注",
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
                value = fieldValue,
                onValueChange = { updated ->
                    fieldValue = updated
                    // 文本本身仍由调用方持有：它只关心字符串，光标/选区留在本组件内
                    onContentChange(updated.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fieldHeight)
                    .focusRequester(focusRequester),
                placeholder = "写点什么…",
                shape = RoundedCornerShape(NeumorphShapes.Corner),
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
