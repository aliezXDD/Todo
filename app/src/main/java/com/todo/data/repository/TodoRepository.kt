package com.todo.data.repository

import com.todo.data.local.dao.TodoDao
import com.todo.data.local.entity.TodoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepository @Inject constructor(
    private val todoDao: TodoDao
) {
    fun getTodosByDate(date: String): Flow<List<TodoEntity>> =
        todoDao.getTodosByDate(date).distinctUntilChanged()

    suspend fun getTodosByDateSnapshot(date: String): List<TodoEntity> = todoDao.getTodosByDateSnapshot(date)

    fun getTodosForDates(dates: List<String>): Flow<List<TodoEntity>> =
        todoDao.getTodosForDates(dates).distinctUntilChanged()

    /** 追加到某天末尾（含取号，单事务）。新增待办走这里，不用自己先取号。 */
    suspend fun insertAtEnd(todo: TodoEntity): Long = todoDao.insertAtEnd(todo)

    suspend fun delete(todo: TodoEntity) = todoDao.delete(todo)

    /** 勾选/取消完成，并（可选）把这条待办改到另一天的末尾（单事务，避免中间态）。 */
    suspend fun setCompleted(id: Long, isCompleted: Boolean, refileTo: String? = null) =
        todoDao.setCompleted(id, isCompleted, refileTo)

    /** 整组顺序一次写完（单事务），避免逐行写入导致界面闪出中间态 */
    suspend fun updateSortOrders(orderedIds: List<Long>) = todoDao.updateSortOrders(orderedIds)

    /** 保存编辑：内容 + 截止日期，并（可选）把这条待办改到另一天的末尾（单事务）。 */
    suspend fun applyEdit(id: Long, content: String, dueDate: String?, refileTo: String? = null) =
        todoDao.applyEdit(id, content, dueDate, refileTo)

    suspend fun getHistoryDates(today: String): List<String> = todoDao.getHistoryDates(today)

    /** 截止日已过、仍未完成的待办分别落在哪几天（跨天时要覆盖重算那几天的统计）。 */
    suspend fun getExpiredDeadlineDates(today: String): List<String> = todoDao.getExpiredDeadlineDates(today)

    suspend fun getTodosBeforeDate(cutoffDate: String): List<TodoEntity> = todoDao.getTodosBeforeDate(cutoffDate)

    suspend fun deleteTodosBeforeDate(cutoffDate: String) = todoDao.deleteTodosBeforeDate(cutoffDate)
}
