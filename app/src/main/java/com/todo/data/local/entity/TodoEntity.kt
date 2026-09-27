package com.todo.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "todos",
    indices = [Index(value = ["date"])]
)
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val isCompleted: Boolean = false,
    val date: String,
    val sortOrder: Int,
    val createdAt: Long = System.currentTimeMillis(),
    /**
     * 截止日期（`yyyy-MM-dd`，逻辑日）。
     *
     * 非空表示这条待办是**预留**的：它"归属"的那一天（[date]）就是截止日，一直跟在今天的清单后面，
     * 却暂时不计入任何一天的统计、也不进往日记录（判定见 `com.todo.domain.model.isDeferred`）。
     * 可空 + 默认值让老数据与原有插入路径都不用改。
     */
    val dueDate: String? = null
)
