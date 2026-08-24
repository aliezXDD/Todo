package com.todo.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class RecycleBinItem(
    val id: Long,
    val originalDate: String,
    val content: String,
    val wasCompleted: Boolean,
    val deletedAt: Long
)
