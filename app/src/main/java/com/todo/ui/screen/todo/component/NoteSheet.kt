package com.todo.ui.screen.todo.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
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
 * 面板自身上下要占掉的高度：拖拽把手、标题与提示、面板内外边距，外加系统导航栏那一条。
 *
 * 取 220dp 是**上界**（三键导航比手势导航宽，再叠上放大的字体更是如此），这里故意留了余量：
 * 面板内容一旦高过可用高度，M3 会把超出的部分从**顶部**裁掉 —— 第一个被切掉的正好是那块拖拽把手。
 */
private val NoteSheetChromeHeight = 220.dp

/**
 * 输入区的最小高度。
 *
 * 取小一点是有意的：输入区的高度里已经减掉了键盘那一段（见 [noteFieldHeight] 的说明），
 * 若这里的地板留得太大，矮屏 + 超高键盘时面板会比可用高度还高、上边界又被顶上去 —— 那正是要避免的事。
 * 80dp 约三行，只有极端的"矮屏 + 超高键盘"才碰得到它。
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
 * 键盘弹起 / 收起时面板的**上边界不动**：底边被键盘顶上来的那一段，正好由输入区让出同样多的高度抵消
 * （推导见 [noteFieldHeight]）。
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
        // 高度必须在**面板自己的窗口**里算。ModalBottomSheet 是一个独立 Dialog（M3 用子组合把它挂到自己的
        // ComposeView 上），而下面这段正是在那个子组合里展开的：这里读到的是"面板这个窗口"的 WindowInsets，
        // 与 M3 那层 .imePadding() 同一个 WindowInsetsHolder —— 同一个键盘、同一帧更新。
        // 若把它们写在 [NoteSheet] 的函数体里（也就是 Activity 的组合里），读到的就是 **Activity 窗口**的那一份：
        // 同一个键盘，却不是同一份数据在同一帧更新 —— 面板已经按新的键盘高度抬起来了，输入区却还按旧值撑着，
        // 结果就是"先升高一点再回落"。
        //
        // 基准取 LocalWindowInfo 的容器高度：它是窗口的 MATCH_PARENT 尺寸、**不扣任何 insets**
        //（平台文档原话：The WindowInsets are not deducted from the bounds），所以键盘弹起/收起都不影响它。
        // 也正因如此，不能换成 Configuration.screenHeightDp 之类"看起来更稳"的值：那些在多窗口/折叠屏下
        // 不保证等于本窗口的可用高度（lint 的 ConfigurationScreenWidthHeight 说的就是这件事）。
        val containerHeightPx = LocalWindowInfo.current.containerSize.height
        // 组合阶段只拿这个 WindowInsets **对象**（`WindowInsets.ime` 是 @Composable 的，只有组合阶段取得到），
        // 键盘高度本身留到测量阶段再读 —— 这是"上边界不再先升高一点再回落"的另一半关键（见 [noteFieldHeight]）。
        val imeInsets = WindowInsets.ime

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
                    userTyped = true
                    fieldValue = updated
                    // 文本本身仍由调用方持有：它只关心字符串，光标/选区留在本组件内
                    onContentChange(updated.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .noteFieldHeight(containerHeightPx, imeInsets)
                    .focusRequester(focusRequester),
                placeholder = "写点什么…",
                shape = RoundedCornerShape(NeumorphShapes.Corner),
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * 把输入区的高度钉成"容器高度 − 键盘高度 − 面板自身占用"。
 *
 * **为什么要减掉键盘**：M3 的 ModalBottomSheet 会给整个面板加一层 `.imePadding()`，键盘一弹出，面板底边就被
 * 整块抬到键盘上沿。面板高度若不变，"上边界"就会跟着往上跑（面板越高跑得越狠，高到一定程度还会顶出屏幕、
 * 连拖拽把手都被切掉）。把键盘那一段从输入区里减掉后，面板总高度就少掉同样多：底边被抬高多少、高度就少
 * 多少，两者正好抵消 —— 上边界纹丝不动，而输入区仍然完整地待在键盘上方（不会被键盘盖住半截）。
 *
 * **为什么键盘高度必须在这里（测量阶段）读**：M3 那层 `.imePadding()` 读的是**同一个** [WindowInsets]
 * 实例，而且是在 measure 里读的：键盘动画每推进一帧，它当帧就生效（`ime` 是动画插值出来的当前值，见 Compose
 * 源码 `imeAnimationSource` / `imeAnimationTarget` 的说明）。而组合阶段读到的值要等**下一帧**重组才落实到
 * 布局。两者错开一帧，键盘上升期间就会出现"面板的可用高度已经少了一截、输入区却还按上一帧的高度撑着"，
 * 面板被多顶高一截，等键盘停稳、重组追上来才落回原位 —— 也就是"打开备注页时窗口先升高一点再回落"。
 * 放进 measure 后两者同处一次测量流程、看到的永远是同一个值，上边界才真正不动（前提是调用点就在**面板自己的
 * 窗口**里，见 [NoteSheet] 中 GlassBottomSheet 的 content）。
 *
 * 所以 [NoteSheet] 的组合阶段只把 [WindowInsets] **对象**（而不是它的值）交给这里，真正的值在这里读。
 *
 * 高度照 `Modifier.height(...)` 的老规矩收进父级给的约束里（这里上方套了 `fillMaxWidth()`，宽度原样透传），
 * 绝不越界把面板撑破；只有"矮屏 + 超高键盘"这种极端情况才会被父级上限压到 [NoteFieldMinHeight] 地板以下。
 */
private fun Modifier.noteFieldHeight(
    containerHeightPx: Int,
    imeInsets: WindowInsets
): Modifier = layout { measurable, constraints ->
    val imePx = imeInsets.getBottom(this)
    val heightPx = (containerHeightPx - imePx - NoteSheetChromeHeight.roundToPx())
        .coerceAtLeast(NoteFieldMinHeight.roundToPx())
        .coerceAtLeast(constraints.minHeight)
        .coerceAtMost(constraints.maxHeight)
    val placeable = measurable.measure(
        constraints.copy(minHeight = heightPx, maxHeight = heightPx)
    )
    // 与 Modifier.height(...) 的收尾逐字一致：尺寸取被测量者的实测值、放在左上角
    layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
}
