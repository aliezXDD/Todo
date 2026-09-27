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
    /**
     * 某一天的清单 = 那天自己的待办 + **所有还在期限内的预留待办**。
     *
     * 预留待办的 `date` 是它的截止日（在将来），靠 `date = :date` 是取不到的：第二条判断把它们显式
     * 带进来（判定与 `com.todo.domain.model.isDeferred` 同一条：设了截止日期、未完成、今天仍在期限内），
     * 它们才会在期限内的每一天都出现在今日清单里。
     *
     * 排序是"两档 + 档内顺序"：
     * 1. **归属日（`date`）升序**：今天那一档在前，预留档在后。今天到期的预留待办归属日就是今天，
     *    所以"到截止日期当天"它会自动出现在今日那一档里；预留档则按截止日从近到远排。
     * 2. **同一归属日内**按 `sortOrder`：今天档的手动拖动排序靠它。预留档的位置完全由截止日决定，
     *    界面上也**不给**预留条目排序拖拽（拖它只会回弹，见 `TodaySection`），
     *    因此预留档的 `sortOrder` 只用来给"同一天到期"的几条定个先后。
     */
    @Query(
        """
        SELECT * FROM todos
        WHERE date = :date
           OR (dueDate IS NOT NULL AND isCompleted = 0 AND dueDate >= :date)
        ORDER BY date ASC, sortOrder ASC
        """
    )
    fun getTodosByDate(date: String): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE date = :date ORDER BY sortOrder ASC")
    suspend fun getTodosByDateSnapshot(date: String): List<TodoEntity>

    @Query("SELECT * FROM todos WHERE date IN (:dates) ORDER BY date DESC, sortOrder ASC")
    fun getTodosForDates(dates: List<String>): Flow<List<TodoEntity>>

    /**
     * 某个归属日的下一个序号（追加到该日末尾）。
     *
     * 今天档与预留档各按自己的归属日编号：新待办落在今天末尾；新设的截止日期条目落在
     * **它那个截止日**的末尾 —— 也就是预留档始终按截止日从近到远排（见 [getTodosByDate]）。
     */
    @Query("SELECT COALESCE(MAX(sortOrder), 0) + 1 FROM todos WHERE date = :date")
    suspend fun getNextSortOrder(date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoEntity): Long

    /**
     * 追加到**当天**的末尾：取下一个序号与插入**在同一个事务里**。
     *
     * 分开写（先 `getNextSortOrder` 再 `insert`）时两次并发添加会读到同一个最大值
     * → 两条 `sortOrder` 相同，而 `ORDER BY` 遇到并列时顺序由 SQLite 决定，
     * 列表顺序就会在两次读取之间跳变。
     *
     * 新建的待办与从回收站还原的条目都没有截止日期（后者不带 dueDate），所以都落在归属日那天。
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

    @Query("UPDATE todos SET content = :content, dueDate = :dueDate WHERE id = :id")
    suspend fun updateContentAndDueDate(id: Long, content: String, dueDate: String?)

    @Query("UPDATE todos SET date = :date, sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateDate(id: Long, date: String, sortOrder: Int)

    /**
     * 保存编辑：内容、截止日期，以及（可选）这条待办归属的那一天，**同一个事务**写完。
     *
     * 分几次写会让界面先看到"内容改了、归属日还没改"的中间态：那条待办会短暂地同时出现在
     * 今天和截止日那天的清单里（或两天都不出现）。[refileTo] 传 null 表示归属日不动。
     *
     * 改到另一天时序号取**目标归属日**的末尾（与 [insertAtEnd] 同一套取号，且在同一事务内完成）。
     */
    @Transaction
    suspend fun applyEdit(id: Long, content: String, dueDate: String?, refileTo: String?) {
        updateContentAndDueDate(id, content, dueDate)
        if (refileTo != null) updateDate(id, refileTo, getNextSortOrder(refileTo))
    }

    /**
     * 勾选 / 取消完成，并（可选）把它改到另一天的末尾 —— 同样是单事务。
     *
     * 有截止日期的待办，完成状态一变，它归属的那一天也跟着变（完成 → 完成那天；取消完成 → 截止日那天），
     * 否则中间态会让它出现在错误的那一天里。
     */
    @Transaction
    suspend fun setCompleted(id: Long, isCompleted: Boolean, refileTo: String?) {
        updateCompleted(id, isCompleted)
        if (refileTo != null) updateDate(id, refileTo, getNextSortOrder(refileTo))
    }

    /**
     * 截止日已过、仍未完成的待办都落在哪几天。
     *
     * 那些天的统计需要在跨天时**覆盖重算**一次：当天算统计时它们还是预留（被排除在外），
     * 过了期限就该按"未完成"补进去。这里取的是 `date`（= 它们的截止日，见 `applyEdit` 的说明），
     * 因此只会拿到今天以前的日期，不会给未来写出统计行。
     */
    @Query("SELECT DISTINCT date FROM todos WHERE date < :today AND dueDate IS NOT NULL AND isCompleted = 0")
    suspend fun getExpiredDeadlineDates(today: String): List<String>

    @Query("SELECT DISTINCT date FROM todos WHERE date < :today ORDER BY date DESC")
    suspend fun getHistoryDates(today: String): List<String>

    @Query("SELECT * FROM todos WHERE date < :cutoffDate")
    suspend fun getTodosBeforeDate(cutoffDate: String): List<TodoEntity>

    @Query("DELETE FROM todos WHERE date < :cutoffDate")
    suspend fun deleteTodosBeforeDate(cutoffDate: String)
}
