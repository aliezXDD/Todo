package com.todo.data.repository

import com.todo.data.local.dao.PresetDao
import com.todo.data.local.entity.PresetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresetRepository @Inject constructor(
    private val presetDao: PresetDao
) {
    fun getAllPresets(): Flow<List<PresetEntity>> =
        presetDao.getAllPresets().distinctUntilChanged()

    suspend fun insert(preset: PresetEntity): Long = presetDao.insert(preset)

    suspend fun update(preset: PresetEntity) = presetDao.update(preset)

    suspend fun deleteByIds(ids: List<Long>) = presetDao.deleteByIds(ids)
}
