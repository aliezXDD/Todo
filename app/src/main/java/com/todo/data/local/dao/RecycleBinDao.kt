package com.todo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.todo.data.local.entity.RecycleBinEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecycleBinDao {
    @Query("SELECT * FROM recycle_bin ORDER BY deletedAt DESC")
    fun getAll(): Flow<List<RecycleBinEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RecycleBinEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RecycleBinEntity>)

    @Query("DELETE FROM recycle_bin WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM recycle_bin WHERE deletedAt < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long)
}
