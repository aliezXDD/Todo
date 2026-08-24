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

    suspend fun getNextSortOrder(date: String): Int = todoDao.getNextSortOrder(date)

    suspend fun insert(todo: TodoEntity): Long = todoDao.insert(todo)

    suspend fun update(todo: TodoEntity) = todoDao.update(todo)

    suspend fun delete(todo: TodoEntity) = todoDao.delete(todo)

    suspend fun deleteAllByDate(date: String) = todoDao.deleteAllByDate(date)

    suspend fun updateCompleted(id: Long, isCompleted: Boolean) = todoDao.updateCompleted(id, isCompleted)

    suspend fun updateSortOrder(id: Long, sortOrder: Int) = todoDao.updateSortOrder(id, sortOrder)

    suspend fun updateContent(id: Long, content: String) = todoDao.updateContent(id, content)

    suspend fun getHistoryDates(today: String): List<String> = todoDao.getHistoryDates(today)

    suspend fun getTodosBeforeDate(cutoffDate: String): List<TodoEntity> = todoDao.getTodosBeforeDate(cutoffDate)

    suspend fun deleteTodosBeforeDate(cutoffDate: String) = todoDao.deleteTodosBeforeDate(cutoffDate)
}
