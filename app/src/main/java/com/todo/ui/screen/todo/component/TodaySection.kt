package com.todo.ui.screen.todo.component

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.todo.domain.model.Todo
import com.todo.ui.component.EmptyState
import com.todo.ui.component.GlassCard
import com.todo.util.DateUtils
import sh.calvin.reorderable.ReorderableColumn

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodaySection(
    todos: List<Todo>,
    onToggleTodo: (Todo, Boolean) -> Unit,
    onLongPressTodo: (Todo) -> Unit,
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

    GlassCard(
        modifier = modifier
            .fillMaxWidth(),
        fillMaxHeight = true
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "今日",
                    style = MaterialTheme.typography.titleLarge,
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
                        .padding(vertical = 24.dp)
                )
            } else {
                ReorderableColumn(
                    list = localTodos,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                        onLongPress = { onLongPressTodo(todo) },
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

