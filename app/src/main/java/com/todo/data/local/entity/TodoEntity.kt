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
    val createdAt: Long = System.currentTimeMillis()
)
