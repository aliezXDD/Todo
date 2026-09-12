package com.todo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    /**
     * 追加到当天末尾：取下一个序号与插入**在同一个事务里**。
     *
     * 分开写（先 `getNextSortOrder` 再 `insert`）时两次并发添加会读到同一个最大值
     * → 两条 `sortOrder` 相同，而 `ORDER BY` 遇到并列时顺序由 SQLite 决定，
     * 列表顺序就会在两次读取之间跳变。
     */
    @Transaction
    suspend fun insertAtEnd(todo: TodoEntity): Long {
        val next = getNextSortOrder(todo.date)
        return insert(todo.copy(sortOrder = next))
    }

    @Delete
    suspend fun delete(todo: TodoEntity)

    @Query("UPDATE todos SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompleted(id: Long, isCompleted: Boolean)

    @Query("UPDATE todos SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    /**
     * 一次事务写完整组顺序。
     *
     * 拖动排序后如果逐行 [updateSortOrder]，每一行都是一次独立写入：Room 的观察者会收到
     * **多次**回调，而中间那些时刻 sortOrder 存在重复值（ORDER BY 遇到并列时顺序由 SQLite 决定），
     * 于是列表会先闪出一个"半更新"的乱序，再被最后一帧纠正。放到一个事务里，
     * 失效通知在事务提交后才统一发出，界面只会看到最终顺序。
     */
    @Transaction
    suspend fun updateSortOrders(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id -> updateSortOrder(id, index) }
    }

    @Query("UPDATE todos SET content = :content WHERE id = :id")
    suspend fun updateContent(id: Long, content: String)

    @Query("SELECT DISTINCT date FROM todos WHERE date < :today ORDER BY date DESC")
    suspend fun getHistoryDates(today: String): List<String>

    @Query("SELECT * FROM todos WHERE date < :cutoffDate")
    suspend fun getTodosBeforeDate(cutoffDate: String): List<TodoEntity>

    @Query("DELETE FROM todos WHERE date < :cutoffDate")
    suspend fun deleteTodosBeforeDate(cutoffDate: String)
}
