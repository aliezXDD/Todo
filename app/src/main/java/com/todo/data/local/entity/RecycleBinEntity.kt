package com.todo.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recycle_bin",
    indices = [Index(value = ["deletedAt"])]
)
data class RecycleBinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalDate: String,
    val content: String,
    val wasCompleted: Boolean,
    val deletedAt: Long = System.currentTimeMillis()
)
