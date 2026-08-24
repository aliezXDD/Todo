package com.todo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.todo.data.local.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos WHERE date = :date ORDER BY sortOrder ASC")
    fun getTodosByDate(date: String): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE date = :date ORDER BY sortOrder ASC")
    suspend fun getTodosByDateSnapshot(date: String): List<TodoEntity>

    @Query("SELECT * FROM todos WHERE date IN (:dates) ORDER BY date DESC, sortOrder ASC")
    fun getTodosForDates(dates: List<String>): Flow<List<TodoEntity>>

    @Query("SELECT COALESCE(MAX(sortOrder), 0) + 1 FROM todos WHERE date = :date")
    suspend fun getNextSortOrder(date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoEntity): Long

    @Update
    suspend fun update(todo: TodoEntity)

    @Delete
    suspend fun delete(todo: TodoEntity)

    @Query("DELETE FROM todos WHERE date = :date")
    suspend fun deleteAllByDate(date: String)

    @Query("UPDATE todos SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompleted(id: Long, isCompleted: Boolean)

    @Query("UPDATE todos SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    @Query("UPDATE todos SET content = :content WHERE id = :id")
    suspend fun updateContent(id: Long, content: String)

    @Query("SELECT DISTINCT date FROM todos WHERE date < :today ORDER BY date DESC")
    suspend fun getHistoryDates(today: String): List<String>

    @Query("SELECT * FROM todos WHERE date < :cutoffDate")
    suspend fun getTodosBeforeDate(cutoffDate: String): List<TodoEntity>

    @Query("DELETE FROM todos WHERE date < :cutoffDate")
    suspend fun deleteTodosBeforeDate(cutoffDate: String)
}
