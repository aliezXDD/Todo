package com.todo.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Preset(
    val id: Long,
    val content: String,
    val createdAt: Long
)
