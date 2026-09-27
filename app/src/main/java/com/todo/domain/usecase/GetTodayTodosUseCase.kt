package com.todo.domain.usecase

import com.todo.data.local.entity.TodoEntity
import com.todo.data.repository.TodoRepository
import com.todo.domain.model.Todo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 取某一天的待办。
 *
 * **日期必须由调用方传入**：原先在用例内部取 `DateUtils.today()`，而它是在流被构建的那一刻求值的，
 * 于是收集开始后这一整天都不会再重新求值——进程活过零点后列表仍停在昨天，而零点后新增的待办
 * 用新日期入库，因此永远不显示。现在由 ViewModel 持有"当前逻辑日"，随日期变化重新换绑。
 *
 * 返回的列表除了那一天自己的待办，还包含**还在期限内的预留待办**（设了截止日期、未完成、
 * 期限没到），它们在清单里排在今天那些之后；是否计入统计由 `Todo.isDeferred` 单独判定。
 */
class GetTodayTodosUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    operator fun invoke(date: String): Flow<List<Todo>> =
        todoRepository.getTodosByDate(date).map { entities ->
            entities.map(TodoEntity::toDomain)
        }
}

private fun TodoEntity.toDomain(): Todo = Todo(
    id = id,
    content = content,
    isCompleted = isCompleted,
    date = date,
    sortOrder = sortOrder,
    createdAt = createdAt,
    dueDate = dueDate
)
