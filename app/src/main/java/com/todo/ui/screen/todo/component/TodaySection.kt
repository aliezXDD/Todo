package com.todo.ui.screen.todo.component

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.todo.domain.model.Todo
import com.todo.ui.component.EmptyState
import com.todo.ui.component.GlassCard
import com.todo.ui.component.NeumorphScrollFade
import com.todo.ui.component.NeumorphScrollFadeHeight
import com.todo.ui.theme.Neumorph
import com.todo.util.DateUtils
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState

/**
 * 「今日」卡片：标题、日期，以及可拖动排序的待办列表。
 *
 * 列表用**懒加载版**的拖动排序（`ReorderableItem` + 真正的 `key`）：
 * - 条目按 id 做 key，框架按"条目身份"跟踪它们，交换时行**整体移动**，
 *   不会出现"原地换内容 + 状态从旧条目形变到新条目"的闪动；
 * - 位移动画交给 `Modifier.animateItem()`，拖动中是实时回调 `onMove`，
 *   松手后再把顺序写库（一次事务），所以中间态根本不会进入画面。
 *
 * 本地列表仍然只把数据库当"内容来源"：回调只就地刷新内容，绝不改变顺序。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodaySection(
    todos: List<Todo>,
    onToggleTodo: (Todo, Boolean) -> Unit,
    onEditTodo: (Todo) -> Unit,
    onReorderFinished: (List<Todo>) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var localTodos by remember { mutableStateOf(todos) }
    val listState = rememberLazyListState()

    // 拖动过程中实时重排本地列表（库的懒加载版本就是这么用的：镜头里始终是新顺序）
    val reorderState = rememberReorderableLazyColumnState(listState) { from, to ->
        if (from.index in localTodos.indices && to.index in localTodos.indices && from.index != to.index) {
            localTodos = localTodos.toMutableList().apply { add(to.index, removeAt(from.index)) }
        }
    }

    // 数据库回调只刷内容、不重排（拖动进行中整段跳过）
    LaunchedEffect(todos, reorderState.isAnyItemDragging) {
        if (reorderState.isAnyItemDragging) return@LaunchedEffect
        val fresh = todos.associateBy { it.id }
        val merged = buildList {
            localTodos.forEach { local -> fresh[local.id]?.let { add(it) } }
            todos.forEach { todo -> if (none { it.id == todo.id }) add(todo) }
        }
        if (merged != localTodos) localTodos = merged
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
                    text = DateUtils.formatDisplayDate(DateUtils.today()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (localTodos.isEmpty()) {
                EmptyState(
                    text = "点击右下角 + 添加第一条待办",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 24.dp)
                )
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
                        items(localTodos, key = { it.id }) { todo ->
                            ReorderableItem(reorderableLazyListState = reorderState, key = todo.id) { isDragging ->
                                TodoItemRow(
                                    todo = todo,
                                    isDragging = isDragging,
                                    onCheckedChange = { checked -> onToggleTodo(todo, checked) },
                                    onEdit = { onEditTodo(todo) },
                                    dragHandleModifier = Modifier.draggableHandle(
                                        onDragStarted = {
                                            view.performHapticFeedback(HapticFeedbackConstants.DRAG_START)
                                        },
                                        onDragStopped = {
                                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                            // 松手时把最终顺序写库（单事务，不会产生中间态）
                                            onReorderFinished(localTodos)
                                        }
                                    ),
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
