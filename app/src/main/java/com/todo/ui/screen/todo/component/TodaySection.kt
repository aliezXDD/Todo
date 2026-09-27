package com.todo.ui.screen.todo.component

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.todo.domain.model.Todo
import com.todo.ui.component.EmptyState
import com.todo.ui.component.GlassCard
import com.todo.ui.component.LoadingState
import com.todo.ui.component.NeumorphScrollFade
import com.todo.ui.component.NeumorphScrollFadeHeight
import com.todo.ui.theme.Neumorph
import com.todo.util.DateUtils
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * 拖动开始/结束的触感反馈。
 *
 * `DRAG_START` 与 `GESTURE_END` 都是 **API 34** 才加入的常量，而本项目 minSdk 31：
 * 它们是编译期常量、会被内联进字节码，所以能编过，但在 31–33 上这个值并非系统认得的触感类型
 * （轻则无声无感，重则在部分机型上抛异常）。因此在低版本回退到语义最接近的旧常量。
 */
private fun View.hapticDragStart() {
    performHapticFeedback(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HapticFeedbackConstants.DRAG_START
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
    )
}

private fun View.hapticDragEnd() {
    performHapticFeedback(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HapticFeedbackConstants.GESTURE_END
        } else {
            HapticFeedbackConstants.CLOCK_TICK
        }
    )
}

/**
 * "今天那一组"与"预留组"之间那条分隔线在列表里的 key。
 *
 * 用字符串是为了和条目 key（都是待办 id，Long）区分开，保证它在整个列表里唯一。
 */
private const val TodayGroupsDividerKey = "today-groups-divider"

/**
 * 「今日」卡片：标题、日期，以及可拖动排序的待办列表。
 *
 * 列表用**懒加载版**的拖动排序（`ReorderableItem` + 真正的 `key`）：
 * - 条目按 id 做 key，框架按"条目身份"跟踪它们，交换时行**整体移动**，
 *   不会出现"原地换内容 + 状态从旧条目形变到新条目"的闪动；
 * - 位移动画交给 `Modifier.animateItem()`，拖动中是实时回调 `onMove`，
 *   松手后再把顺序写库（一次事务），所以中间态根本不会进入画面。
 *
 * 本地列表仍然只把数据库当"内容来源"：回调只就地刷新内容与增删，**同一归属日内**的顺序绝不改动；
 * 组与组之间则跟着数据库走 —— 归属日升序：今天那一组在前、预留的按截止日从近到远。
 *
 * 手动换位只发生在**同一天之内**：今天那一组自然全在同一天，预留里只有"同一天到期"的几条能互换；
 * 跨天拖不动（松手弹回原位），因为位置本来就由归属日（= 截止日）决定。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodaySection(
    todos: List<Todo>,
    date: String,
    isLoading: Boolean,
    onToggleTodo: (Todo, Boolean) -> Unit,
    onEditTodo: (Todo) -> Unit,
    onReorderFinished: (List<Todo>) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var localTodos by remember { mutableStateOf(todos) }

    // 正在被拖动的那一条的归属日（null = 没有拖动）。
    // 换位范围就靠它控制：只有"和它同一天"的条目才留在库的 reorderableKeys 里（见下面的 renderRow），
    // 于是今天那一组内部可以随意排，预留里只有**同一天到期**的几条之间能换位，跨天拖不动。
    var draggingDate by remember { mutableStateOf<String?>(null) }

    // 兜底清理：数据一变就说明不可能还有没结束的拖动（拖动过程中不会有新的数据发射），
    // 于是把它清掉。没有这一条时，"正被拖的那一行中途被销毁"（被删除、跨凌晨 4 点被移出清单）
    // 会让两个拖拽结束回调都不来，draggingDate 残留 —— 其它归属日的条目就既不是换位目标、
    // 连拖拽把手都被禁用，直到这个界面被重建为止。
    LaunchedEffect(todos) { draggingDate = null }

    val listState = rememberLazyListState()

    // 拖动过程中实时重排本地列表（库的懒加载版本就是这么用的：镜头里始终是新顺序）。
    //
    // 这里照单全收：库在每次 onMove 之后都要等布局真的变了才继续（其 moveItems 里
    // `layoutInfoFlow.take(2)`），不接受移动会让拖动卡住一下、条目位置还会跳。
    // "能不能换位"由库的 reorderableKeys 决定 —— 上面那行 draggingDate 已经把它限制在"同一天"之内。
    //
    // 这里按 **key（待办 id）** 找位置，而不是拿 LazyList 的下标直接用：列表里还夹着一条分隔线
    //（它自己占一个位置但不参与排序），下标会差一位；分隔线的 key 是字符串、永远匹配不到待办 id。
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        val fromIndex = localTodos.indexOfFirst { it.id == from.key }
        val toIndex = localTodos.indexOfFirst { it.id == to.key }
        if (fromIndex >= 0 && toIndex >= 0 && fromIndex != toIndex) {
            localTodos = localTodos.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        }
    }

    // 数据库是"内容来源"，但**顺序**始终以本地为准：回调只就地刷新内容、增删条目，
    // 不改变同一归属日内部的顺序 —— 拖动排序的权威在 localTodos 上。
    //
    // 组与组之间的顺序则跟着数据库走，按归属日升序：今天那一组在前（当天到期的预留待办归属日就是今天，
    // 于是"到截止日期当天"它自动落到这一组里），预留的按截止日从近到远排在后面。
    //
    // 同步必须在**组合期**完成，不能放进 LaunchedEffect：LaunchedEffect 要等这一帧画完才跑，
    // 于是启动时会出现"统计卡已显示 7/10、列表还停在'添加第一条待办'"的一帧错位
    // （启动卡顿期这一帧能被拉长到几百毫秒，肉眼可见）。这里用输入引用做闸门，
    // 只在入参真的换了新列表时同步一次，拖动进行中整段跳过。
    var syncedInput by remember { mutableStateOf(todos) }
    if (syncedInput !== todos && !reorderState.isAnyItemDragging) {
        syncedInput = todos
        val fresh = todos.associateBy { it.id }
        // 归属日**没变**的那些：保持用户手动排出来的顺序（消失的条目自然被丢掉）。
        // 按新数据判断"没变"，所以设/清截止日期而换了归属日的条目不会留在这儿。
        val kept = localTodos.mapNotNull { local ->
            fresh[local.id]?.takeIf { it.date == local.date }
        }
        // 组序 = 归属日升序（与 TodoDao.getTodosByDate 的 `ORDER BY date ASC` 同一条规则）
        val groups = todos.map { it.date }.distinct().sorted()
        val merged = buildList {
            groups.forEach { group ->
                addAll(kept.filter { it.date == group })
                // 新出现的、以及刚换了归属日的：按数据库给的顺序（也就是它自己的 sortOrder）接到这一组的末尾
                addAll(
                    todos.filter { todo ->
                        todo.date == group && none { added -> added.id == todo.id }
                    }
                )
            }
        }
        if (merged != localTodos) localTodos = merged
    }

    // 每一行的渲染：下面两组各调一次 items()，中间夹那条分隔线，所以把这一行提出来。
    //
    // 拖动换位的范围由**归属日**决定：`enabled` 只在"没有拖动、或与正在拖的那一条同一天"时为真，
    // 而库的 findTargetItem 过滤的 reorderableKeys 正是这个集合 —— 于是
    // 「同一天之内可以随意换位（含预留里同一天到期的几条），跨天拖不动、松手弹回原位」。
    // 跨天为什么不能排：顺序键是 `(date, sortOrder)`（见 TodoDao.getTodosByDate），
    // 预留档的位置本来就由截止日决定。
    val renderRow: @Composable LazyItemScope.(Todo) -> Unit = { todo ->
        ReorderableItem(
            state = reorderState,
            key = todo.id,
            enabled = draggingDate == null || draggingDate == todo.date
        ) { isDragging ->
            TodoItemRow(
                todo = todo,
                isDragging = isDragging,
                onCheckedChange = { checked -> onToggleTodo(todo, checked) },
                onEdit = { onEditTodo(todo) },
                dragHandleModifier = Modifier.draggableHandle(
                    onDragStarted = {
                        // 记住"拖的是哪一天"：同一天的条目随之成为唯一的换位目标
                        draggingDate = todo.date
                        view.hapticDragStart()
                    },
                    onDragStopped = {
                        draggingDate = null
                        view.hapticDragEnd()
                        // 松手时把最终顺序写库（单事务，不会产生中间态）。
                        // 库已经把可换位的条目限制在"同一天"之内，所以这份顺序就是数据库认得的顺序。
                        onReorderFinished(localTodos)
                    }
                ),
                modifier = Modifier.animateItem()
            )
        }
    }

    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f

    GlassCard(
        modifier = modifier
            .fillMaxWidth(),
        fillMaxHeight = true,
        // 左右内边距交给列表自己（列表视口因此更宽，条目溢出的光影不会被视口硬切）；
        // 卡片内的文字类内容各自补 16dp，位置与原来完全一致
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        Column(
            // 不再叠加纵向间距：列表自己带 16dp 顶部内衬（与渐隐带同高），间距由它提供
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "今日",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = DateUtils.formatDisplayDate(date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (localTodos.isEmpty()) {
                // 加载中不能显示"添加第一条待办"：此时列表为空只是因为数据还没到，
                // 显示引导文案会谎报"你还没有待办"（+ 在今日卡片右上角，文案也按实际位置写）
                if (isLoading) {
                    LoadingState(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                } else {
                    EmptyState(
                        text = "点击右上角 + 添加第一条待办",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(vertical = 24.dp)
                    )
                }
            } else {
                // 列表上下缘各一条常驻渐隐：条目滚出列表边界时，溢出的光影会被容器硬切。
                // 这条列表在**卡片内部**，所以渐隐色要用表面色（不是页面底色）
                NeumorphScrollFade(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    fadeColor = Neumorph.surface(isDark)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        // 上下内衬与渐隐带同高（静止时首尾条目的光影完整）；
                        // 横向 16dp 让条目与卡片内边距对齐，同时给溢出的光影留出不被裁掉的空间
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = NeumorphScrollFadeHeight,
                            bottom = NeumorphScrollFadeHeight
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 两组：今天那一组（归属日 = 今天）与预留组（归属日在将来）。
                        // 组序由 `(date, sortOrder)` 决定（见 TodoDao.getTodosByDate），这条线就画在两组的交界上；
                        // 只有一组时不画 —— 没有交界可言。
                        val todayRowCount = localTodos
                            .indexOfFirst { it.date != date }
                            .let { if (it < 0) localTodos.size else it }
                        val todayRows = localTodos.take(todayRowCount)
                        val reservedRows = localTodos.drop(todayRowCount)

                        items(todayRows, key = { it.id }) { todo -> renderRow(todo) }
                        if (todayRows.isNotEmpty() && reservedRows.isNotEmpty()) {
                            item(key = TodayGroupsDividerKey) {
                                HorizontalDivider(
                                    modifier = Modifier.animateItem(),
                                    // 一条同色系的浅线：新拟态里不用描边，这里只表达"两组之间的分隔"，
                                    // 所以取主题的 outline 再压淡，别抢条目的光影
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
                                )
                            }
                        }
                        items(reservedRows, key = { it.id }) { todo -> renderRow(todo) }
                    }
                }
            }
        }
    }
}
