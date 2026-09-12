package com.todo.ui.screen.recyclebin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todo.data.local.entity.RecycleBinEntity
import com.todo.data.repository.RecycleBinRepository
import com.todo.domain.model.RecycleBinItem
import com.todo.domain.usecase.RestoreRecycleBinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val recycleBinRepository: RecycleBinRepository,
    private val restoreRecycleBinUseCase: RestoreRecycleBinUseCase
) : ViewModel() {

    data class UiState(
        val items: List<RecycleBinItem> = emptyList(),
        /** 首次发射前为 true：界面据此显示"加载中"，而不是先闪一句"回收站为空"。 */
        val isLoading: Boolean = true,
        val isMultiSelectMode: Boolean = false,
        val selectedIds: Set<Long> = emptySet(),
        val showDeleteConfirm: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        observeRecycleBin()
    }

    private fun observeRecycleBin() {
        viewModelScope.launch {
            recycleBinRepository.getAll().collect { entities ->
                _uiState.update { state ->
                    val items = entities.map(RecycleBinEntity::toDomain)
                    val itemIds = items.asSequence().map { it.id }.toSet()
                    state.copy(
                        items = items,
                        isLoading = false,
                        selectedIds = state.selectedIds.intersect(itemIds)
                    )
                }
            }
        }
    }

    fun onItemClick(item: RecycleBinItem) {
        if (_uiState.value.isMultiSelectMode) {
            toggleSelected(item.id)
        }
    }

    fun onItemLongClick(item: RecycleBinItem) {
        _uiState.update {
            it.copy(
                isMultiSelectMode = true,
                selectedIds = it.selectedIds + item.id
            )
        }
    }

    fun toggleSelected(id: Long) {
        _uiState.update { state ->
            val selected = if (id in state.selectedIds) state.selectedIds - id else state.selectedIds + id
            state.copy(selectedIds = selected)
        }
    }

    fun selectAll() {
        _uiState.update { it.copy(selectedIds = it.items.asSequence().map { item -> item.id }.toSet()) }
    }

    fun clearMultiSelect() {
        _uiState.update {
            it.copy(
                isMultiSelectMode = false,
                selectedIds = emptySet(),
                showDeleteConfirm = false
            )
        }
    }

    fun requestDeleteSelected() {
        if (_uiState.value.selectedIds.isNotEmpty()) {
            _uiState.update { it.copy(showDeleteConfirm = true) }
        }
    }

    fun dismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
    }

    fun confirmDeleteSelected() {
        val ids = _uiState.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            recycleBinRepository.deleteByIds(ids)
            _uiState.update {
                it.copy(
                    isMultiSelectMode = false,
                    selectedIds = emptySet(),
                    showDeleteConfirm = false
                )
            }
        }
    }

    fun restoreSelected() {
        val selectedItems = _uiState.value.items.filter { it.id in _uiState.value.selectedIds }
        if (selectedItems.isEmpty()) return
        viewModelScope.launch {
            restoreRecycleBinUseCase(selectedItems)
            _uiState.update {
                it.copy(
                    isMultiSelectMode = false,
                    selectedIds = emptySet()
                )
            }
        }
    }
}

private fun RecycleBinEntity.toDomain(): RecycleBinItem = RecycleBinItem(
    id = id,
    originalDate = originalDate,
    content = content,
    wasCompleted = wasCompleted,
    deletedAt = deletedAt
)
