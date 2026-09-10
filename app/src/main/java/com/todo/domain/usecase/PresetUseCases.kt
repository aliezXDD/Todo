package com.todo.domain.usecase

import com.todo.data.local.entity.PresetEntity
import com.todo.data.repository.PresetRepository
import com.todo.domain.model.Preset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPresetsUseCase @Inject constructor(
    private val presetRepository: PresetRepository
) {
    operator fun invoke(): Flow<List<Preset>> =
        presetRepository.getAllPresets().map { items -> items.map(PresetEntity::toDomain) }
}

class AddPresetUseCase @Inject constructor(
    private val presetRepository: PresetRepository
) {
    suspend operator fun invoke(content: String): Long =
        presetRepository.insert(PresetEntity(content = content))
}

class UpdatePresetUseCase @Inject constructor(
    private val presetRepository: PresetRepository
) {
    suspend operator fun invoke(preset: Preset) {
        presetRepository.update(
            PresetEntity(
                id = preset.id,
                content = preset.content,
                createdAt = preset.createdAt
            )
        )
    }
}

class DeletePresetsByIdsUseCase @Inject constructor(
    private val presetRepository: PresetRepository
) {
    suspend operator fun invoke(ids: List<Long>) {
        if (ids.isEmpty()) return
        presetRepository.deleteByIds(ids)
    }
}

data class PresetUseCases @Inject constructor(
    val getPresets: GetPresetsUseCase,
    val addPreset: AddPresetUseCase,
    val updatePreset: UpdatePresetUseCase,
    val deletePresetsByIds: DeletePresetsByIdsUseCase
)

private fun PresetEntity.toDomain(): Preset = Preset(
    id = id,
    content = content,
    createdAt = createdAt
)
