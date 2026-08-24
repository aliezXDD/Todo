package com.todo.domain.model

import androidx.compose.runtime.Stable

@Stable
data class DailyRecord(
    val date: String,
    val todos: List<Todo>
)
