package com.todo.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Todo(
    val id: Long,
    val content: String,
    val isCompleted: Boolean,
    val date: String,
    val sortOrder: Int,
    val createdAt: Long
)
