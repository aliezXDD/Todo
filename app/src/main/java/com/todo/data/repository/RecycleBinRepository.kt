package com.todo.data.repository

import com.todo.data.local.dao.RecycleBinDao
import com.todo.data.local.entity.RecycleBinEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecycleBinRepository @Inject constructor(
    private val recycleBinDao: RecycleBinDao
) {
    fun getAll(): Flow<List<RecycleBinEntity>> = recycleBinDao.getAll().distinctUntilChanged()

    suspend fun insert(item: RecycleBinEntity) = recycleBinDao.insert(item)

    suspend fun insertAll(items: List<RecycleBinEntity>) = recycleBinDao.insertAll(items)

    suspend fun deleteByIds(ids: List<Long>) = recycleBinDao.deleteByIds(ids)

    suspend fun deleteOlderThan(cutoffTimestamp: Long) = recycleBinDao.deleteOlderThan(cutoffTimestamp)
}
