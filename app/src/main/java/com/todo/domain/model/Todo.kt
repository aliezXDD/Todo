package com.todo.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Todo(
    val id: Long,
    val content: String,
    val isCompleted: Boolean,
    val date: String,
    val sortOrder: Int,
    val createdAt: Long,
    /**
     * 截止日期（`yyyy-MM-dd`，逻辑日）；为空 = 普通待办。
     *
     * 非空的含义与生效条件见 [isDeferred]：它把这条待办"预留"到截止日那一天，
     * 到期前不计入任何统计、也不进往日记录。
     */
    val dueDate: String? = null
)

/**
 * 预留：**设了截止日期、还没完成、今天仍在期限内**（截止日当天也算，期限一直到第二天凌晨 4 点 ——
 * 也就是软件统一定义的"一天"结束）。
 *
 * 这样的一条待办属于"还没到要算账的时候"：
 * - 它照旧出现在今日清单里（[Todo.date] 就是截止日那天，界面把它排在今天那些之后），可以随时提前完成；
 * - **不计入任何一天的完成统计**：今日进度百分比、平均完成率、连续全部完成、累计全部完成都不受它影响；
 * - 不进往日记录（往日记录只列 [Todo.date] 早于今天的待办，它的归属日在将来）；
 * - 也不会被「7 天前的待办进回收站」搬走（归属日在将来，不在 7 天前的范围里）。
 *
 * 出期限的两条路都要"算账"：完成 → 落到**完成的那一天**（今天）并变成普通已完成待办；
 * 过了截止日仍未完成 → 落到**截止日那一天**，按未完成计入那天的统计与往日记录。
 */
fun Todo.isDeferred(today: String): Boolean = isDeferred(dueDate, isCompleted, today)

/**
 * [Todo.isDeferred] 的原字段版本。
 *
 * 统计层拿到的是 Room 实体（`TodoEntity`）而不是领域模型，两处必须用**同一条判定**，
 * 否则"界面不算它、统计却算了它"这类错位就会出现。
 */
fun isDeferred(dueDate: String?, isCompleted: Boolean, today: String): Boolean =
    dueDate != null && !isCompleted && dueDate >= today
