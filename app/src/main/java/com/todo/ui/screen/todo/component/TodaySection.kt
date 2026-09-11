package com.todo.ui.screen.todo.component

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
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
import sh.calvin.reorderable.ReorderableColumn

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
    var isDragging by remember { mutableStateOf(false) }
    var holdExternalSync by remember { mutableStateOf(false) }
    var localTodos by remember { mutableStateOf(todos) }
    LaunchedEffect(todos, isDragging, holdExternalSync) {
        if (isDragging) return@LaunchedEffect

        if (holdExternalSync) {
            val sameOrder = todos.size == localTodos.size &&
                todos.indices.all { index -> todos[index].id == localTodos[index].id }
            if (!sameOrder) return@LaunchedEffect
            holdExternalSync = false
        }

        localTodos = todos
    }
    val scrollState = rememberScrollState()
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
                    ReorderableColumn(
                        list = localTodos,
                        // 内衬：纵向与渐隐带同高（静止时首尾条目的光影完整，只有滚过边界的才被淡化）；
                        // 横向 16dp 让条目与卡片内边距对齐，同时给溢出的光影留出不被裁掉的空间。
                        // padding 必须挂在 verticalScroll 之后（滚动内容内部），才会随内容一起滚动
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = NeumorphScrollFadeHeight),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        onSettle = { fromIndex, toIndex ->
                            if (fromIndex !in localTodos.indices || toIndex !in localTodos.indices || fromIndex == toIndex) {
                                return@ReorderableColumn
                            }
                            val reordered = localTodos.toMutableList().apply {
                                add(toIndex, removeAt(fromIndex))
                            }
                            localTodos = reordered
                            onReorderFinished(reordered)
                        }
                    ) {
                        _, todo, rowDragging ->
                        TodoItemRow(
                            todo = todo,
                            isDragging = rowDragging,
                            onCheckedChange = { checked -> onToggleTodo(todo, checked) },
                            onEdit = { onEditTodo(todo) },
                            dragHandleModifier = with(this) {
                                Modifier.draggableHandle(
                                    onDragStarted = {
                                        holdExternalSync = true
                                        isDragging = true
                                        view.performHapticFeedback(HapticFeedbackConstants.DRAG_START)
                                    },
                                    onDragStopped = {
                                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                        isDragging = false
                                    }
                                )
                            },
                            modifier = Modifier
                        )
                    }
                }
            }
        }
    }
}

