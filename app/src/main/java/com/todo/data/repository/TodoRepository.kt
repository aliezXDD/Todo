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

    suspend fun insert(todo: TodoEntity): Long = todoDao.insert(todo)

    /** 追加到某天末尾（含取号，单事务）。新增待办走这里，不用自己先取号。 */
    suspend fun insertAtEnd(todo: TodoEntity): Long = todoDao.insertAtEnd(todo)

    suspend fun delete(todo: TodoEntity) = todoDao.delete(todo)

    suspend fun updateCompleted(id: Long, isCompleted: Boolean) = todoDao.updateCompleted(id, isCompleted)

    /** 整组顺序一次写完（单事务），避免逐行写入导致界面闪出中间态 */
    suspend fun updateSortOrders(orderedIds: List<Long>) = todoDao.updateSortOrders(orderedIds)

    suspend fun updateContent(id: Long, content: String) = todoDao.updateContent(id, content)

    suspend fun getHistoryDates(today: String): List<String> = todoDao.getHistoryDates(today)

    suspend fun getTodosBeforeDate(cutoffDate: String): List<TodoEntity> = todoDao.getTodosBeforeDate(cutoffDate)

    suspend fun deleteTodosBeforeDate(cutoffDate: String) = todoDao.deleteTodosBeforeDate(cutoffDate)
}
