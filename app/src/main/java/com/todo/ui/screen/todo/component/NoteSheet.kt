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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.todo.ui.component.GlassBottomSheet
import com.todo.ui.component.NeumorphTextField
import com.todo.ui.theme.NeumorphShapes
import kotlinx.coroutines.delay

/**
 * 面板顶部（距窗口上沿）要留出的那一段。
 *
 * 面板高度 = **可用高度 − 这一段**，而"可用高度"直接取父级在**这次测量**里给出的约束
 * （M3 面板外层那层 `.imePadding()` 已经把键盘扣掉了，而且和我们在同一次测量流程里拿到的约束是同一份）。
 * 于是：键盘抬起多少，面板就矮多少，上边界始终钉在同一个位置。
 *
 * 这里刻意**不读窗口高度、也不单独读键盘高度**：那两条都是"面板之外"的数值，一旦与面板这一帧拿到的约束
 * 不一致（时机差一帧、或窗口尺寸口径不同），上边界就会抖一下、或者在键盘停稳后突然跳一格。
 * 只用一个固定量做减法，就没有这类风险。
 *
 * 取 56dp：比状态栏 + 面板内边距再高一点点，面板几乎占满整屏；想更高/更矮只改这一个数。
 */
private val NoteSheetTopOffset = 56.dp

/**
 * 兜底高度：只有父级给的约束量不出有界高度时才会用到（正常路径走不到）。
 * 80dp 约三行。
 */
private val NoteFieldMinHeight = 80.dp

/**
 * 备注面板：一块**尽量高**的凹陷输入区，用来随手写东西。
 *
 * 内容不在这里保存，也不在这里判断"要不要保存"：编辑只更新草稿，
 * **关闭面板时由调用方一次性写回存储**（拖动关闭、点遮罩、返回键都会走 [onDismiss]）。
 * 所以面板上没有"保存"按钮——那会让人以为不点就不保存。
 *
 * 上方与「添加待办」一样有拖拽把手、也可以下滑关闭：输入区已经高到能装下一整屏草稿，
 * 框内滚动只在极长文本时才用得上；而关闭即保存，误拖也不会丢内容。
 *
 * 键盘弹起 / 收起时面板的**上边界不动**：底边被键盘顶上来的那一段，正好由面板自己让出同样多的高度抵消
 * （推导见 [noteSheetHeight]）。
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

    // 存储层的值可能在面板打开**之后**才到（冷启动时 DataStore 首次读盘是异步的）。
    // 只在用户还没敲过字时回灌：既让迟到的内容显示出来，又不会覆盖正在输入的内容。
    // 没有这道回灌时，界面会一直渲染空框；用户一旦输入，ViewModel 的 noteEdited 就被置位、
    // 存储值再也不会回灌，关闭时只写回新输入的那点字——**已存备注被静默覆盖**。
    var userTyped by remember { mutableStateOf(false) }
    LaunchedEffect(content) {
        if (!userTyped) {
            fieldValue = TextFieldValue(content, selection = TextRange(content.length))
        }
    }

    // 点按钮后直接弹出键盘（与「编辑待办」「添加待办」同一做法：等 sheet 的节点挂上再请求焦点）
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(80)
        focusRequester.requestFocus()
    }

    GlassBottomSheet(
        onDismissRequest = onDismiss,
        // 面板很高：跳过"半展开"那一档，一打开就给全高度（否则会先停在屏幕一半，还要再往上拖一次）
        skipPartiallyExpanded = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // 面板高度 = 这次测量拿到的可用高度（里面已经扣掉了键盘）− 顶部要留出的那一段。
                // 不读窗口高度、也不单独读键盘高度，推导见 [noteSheetHeight]。
                .noteSheetHeight()
        ) {
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
                    userTyped = true
                    fieldValue = updated
                    // 文本本身仍由调用方持有：它只关心字符串，光标/选区留在本组件内
                    onContentChange(updated.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    // 输入区吃掉面板剩下的全部高度：面板矮多少（键盘抬起多少），它就矮多少
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = "写点什么…",
                shape = RoundedCornerShape(NeumorphShapes.Corner),
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * 把面板高度钉成"这次测量拿到的可用高度 − [NoteSheetTopOffset]"。
 *
 * **为什么这样上边界就不动**：M3 的 ModalBottomSheet 会给整个面板加一层 `.imePadding()`，键盘一弹出，
 * 面板底边就被整块抬到键盘上沿 —— 也就是说，父级在**同一次测量**里给我们的可用高度，当帧就少掉了键盘那一段。
 * 我们只在这段可用高度里再减掉一个固定量：
 *
 *     面板底边 = 可用高度的下沿 = 键盘上沿（由 M3 保证）
 *     面板高度 = 可用高度 − 固定量
 *     ⇒ 面板上沿 = 固定量（与键盘高低无关）
 *
 * 并且"可用高度"是从父级约束里取来的，与 M3 那层 `.imePadding()` 读的是同一帧的同一个键盘高度，
 * 不存在"面板已经按新键盘高度抬起来了、输入区还按上一帧撑着"这种错位；
 * 也不再有"窗口高度"和"键盘高度"这两个来自面板外部的数值需要互相对齐 —— 那两个正是抖动的来源。
 *
 * 于是整段键盘动画里只有底边跟着键盘走（这一层交给 M3），上边界是一个常数：键盘升起不追帧、收起不弹跳。
 *
 * 高度照 `Modifier.height(...)` 的老规矩收进父级给的约束里（上方套了 `fillMaxWidth()`，宽度原样透传），
 * 绝不越界把面板撑破。正常路径上父级约束一定有界（理由见 [NoteSheetTopOffset]），
 * 只有拿不到有界高度时才退回 [NoteFieldMinHeight]。
 */
private fun Modifier.noteSheetHeight(): Modifier = layout { measurable, constraints ->
    val heightPx = if (constraints.hasBoundedHeight) {
        (constraints.maxHeight - NoteSheetTopOffset.roundToPx()).coerceAtLeast(0)
    } else {
        NoteFieldMinHeight.roundToPx()
    }
    val placeable = measurable.measure(
        constraints.copy(minHeight = heightPx, maxHeight = heightPx)
    )
    // 与 Modifier.height(...) 的收尾逐字一致：尺寸取被测量者的实测值、放在左上角
    layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
}
