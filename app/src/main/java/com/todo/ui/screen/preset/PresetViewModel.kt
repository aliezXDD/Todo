package com.todo.ui.screen.preset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todo.domain.model.Preset
import com.todo.domain.usecase.AddTodoUseCase
import com.todo.domain.usecase.PresetUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PresetViewModel @Inject constructor(
    private val presetUseCases: PresetUseCases,
    private val addTodoUseCase: AddTodoUseCase
) : ViewModel() {

    data class PresetUiState(
        val presets: List<Preset> = emptyList(),
        /** 首次发射前为 true：界面据此显示"加载中"，而不是先闪一句"暂无预设"。 */
        val isLoading: Boolean = true,
        val isMultiSelectMode: Boolean = false,
        val selectedIds: Set<Long> = emptySet()
    )

    private val _uiState = MutableStateFlow(PresetUiState())
    val uiState: StateFlow<PresetUiState> = _uiState.asStateFlow()

    private val _editingPreset = MutableStateFlow<Preset?>(null)
    val editingPreset: StateFlow<Preset?> = _editingPreset.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    private val _showDeleteConfirm = MutableStateFlow(false)
    val showDeleteConfirm: StateFlow<Boolean> = _showDeleteConfirm.asStateFlow()

    // 双击预设条目成功加入「今日待办」后的一次性提示消息。
    private val _addedToTodayMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val addedToTodayMessage: SharedFlow<String> = _addedToTodayMessage

    init {
        observePresets()
    }

    private fun observePresets() {
        viewModelScope.launch {
            presetUseCases.getPresets().collect { presets ->
                _uiState.update { state ->
                    state.copy(
                        presets = presets,
                        isLoading = false,
                        selectedIds = state.selectedIds.intersect(presets.map { it.id }.toSet())
                    )
                }
            }
        }
    }

    fun onCreateClick() {
        _editingPreset.value = null
        _showEditDialog.value = true
    }

    fun onPresetClick(preset: Preset) {
        if (_uiState.value.isMultiSelectMode) {
            toggleSelected(preset.id)
        } else {
            _editingPreset.value = preset
            _showEditDialog.value = true
        }
    }

    fun onPresetLongClick(preset: Preset) {
        _uiState.update {
            it.copy(
                isMultiSelectMode = true,
                selectedIds = it.selectedIds + preset.id
            )
        }
    }

    /** 双击预设条目：多选模式下当作一次选中切换；否则将预设内容加入今日待办。 */
    fun onPresetDoubleClick(preset: Preset) {
        if (_uiState.value.isMultiSelectMode) {
            toggleSelected(preset.id)
        } else {
            addPresetToToday(preset)
        }
    }

    /** 将预设内容作为一条「今日待办」写入，并触发一次性提示。 */
    fun addPresetToToday(preset: Preset) {
        if (preset.content.isBlank()) return
        viewModelScope.launch {
            addTodoUseCase(preset.content)
            _addedToTodayMessage.tryEmit("已添加到今日待办")
        }
    }

    fun toggleSelected(id: Long) {
        _uiState.update { state ->
            val selected = if (id in state.selectedIds) state.selectedIds - id else state.selectedIds + id
            state.copy(selectedIds = selected)
        }
    }

    fun selectAll() {
        _uiState.update { it.copy(selectedIds = it.presets.map { preset -> preset.id }.toSet()) }
    }

    fun clearMultiSelect() {
        _uiState.update { it.copy(isMultiSelectMode = false, selectedIds = emptySet()) }
    }

    fun requestDeleteSelected() {
        if (_uiState.value.selectedIds.isNotEmpty()) {
            _showDeleteConfirm.value = true
        }
    }

    fun dismissDeleteConfirm() {
        _showDeleteConfirm.value = false
    }

    fun confirmDeleteSelected() {
        val ids = _uiState.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            presetUseCases.deletePresetsByIds(ids)
            _showDeleteConfirm.value = false
            _uiState.update { it.copy(isMultiSelectMode = false, selectedIds = emptySet()) }
        }
    }

    /**
     * 只关窗、**不清 editingPreset**。
     *
     * 弹窗有退场动画（fade+scale），若这里立刻把编辑对象置空，动画期间弹窗会以 `editingPreset == null`
     * 重新组合——标题瞬间变回"新建预设"、输入框也被重置成空，看起来就是"取消的一瞬间闪了一下"。
     * 打开时本来就会重新赋值（新建是显式置 null，编辑是赋目标预设），所以留着上一次的值没有副作用。
     */
    fun dismissEditDialog() {
        _showEditDialog.value = false
    }

    fun savePreset(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val editing = _editingPreset.value
            if (editing == null) {
                presetUseCases.addPreset(content.trim())
            } else {
                presetUseCases.updatePreset(editing.copy(content = content.trim()))
            }
            dismissEditDialog()
        }
    }
}
