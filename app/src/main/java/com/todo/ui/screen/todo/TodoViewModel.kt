package com.todo.ui.screen.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todo.domain.model.DailyRecord
import com.todo.domain.model.DailyStats
import com.todo.domain.model.Preset
import com.todo.domain.model.Todo
import com.todo.domain.usecase.AddTodoUseCase
import com.todo.domain.usecase.DeleteTodoUseCase
import com.todo.domain.usecase.GetHistoryRecordsUseCase
import com.todo.domain.usecase.GetTodayTodosUseCase
import com.todo.domain.usecase.PresetUseCases
import com.todo.domain.usecase.ReorderTodosUseCase
import com.todo.domain.usecase.ToggleTodoUseCase
import com.todo.domain.usecase.UpdateTodoUseCase
import com.todo.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val addTodoUseCase: AddTodoUseCase,
    private val updateTodoUseCase: UpdateTodoUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase,
    private val reorderTodosUseCase: ReorderTodosUseCase,
    private val toggleTodoUseCase: ToggleTodoUseCase,
    private val getTodayTodosUseCase: GetTodayTodosUseCase,
    private val getHistoryRecordsUseCase: GetHistoryRecordsUseCase,
    private val presetUseCases: PresetUseCases
) : ViewModel() {

    data class UiState(
        val todayTodos: List<Todo> = emptyList(),
        val historyRecords: List<DailyRecord> = emptyList(),
        val todayStats: DailyStats = DailyStats("", 0, 0),
        val isLoading: Boolean = true,
        val allPresets: List<Preset> = emptyList(),
        val filteredPresets: List<Preset> = emptyList(),
        val addSheetVisible: Boolean = false,
        val editSheetVisible: Boolean = false,
        val editingTodo: Todo? = null,
        val presetSearchQuery: String = "",
        val presetMultiSelectMode: Boolean = false,
        val selectedPresetIds: Set<Long> = emptySet()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        observeTodayTodos()
        observeHistoryRecords()
        observePresets()
    }

    private fun observeTodayTodos() {
        viewModelScope.launch {
            getTodayTodosUseCase().collect { todos ->
                _uiState.update { state ->
                    state.copy(
                        todayTodos = todos,
                        todayStats = DailyStats(
                            date = DateUtils.today(),
                            totalCount = todos.size,
                            completedCount = todos.count { it.isCompleted }
                        ),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeHistoryRecords() {
        viewModelScope.launch {
            getHistoryRecordsUseCase().collect { records ->
                _uiState.update { it.copy(historyRecords = records) }
            }
        }
    }

    private fun observePresets() {
        viewModelScope.launch {
            combine(
                presetUseCases.getPresets(),
                _uiState
            ) { presets, state ->
                val filtered = if (state.presetSearchQuery.isBlank()) {
                    presets
                } else {
                    presets.filter { it.content.contains(state.presetSearchQuery, ignoreCase = true) }
                }
                presets to filtered
            }.collect { (all, filtered) ->
                _uiState.update {
                    it.copy(allPresets = all, filteredPresets = filtered)
                }
            }
        }
    }

    fun setAddSheetVisible(visible: Boolean) {
        _uiState.update {
            it.copy(
                addSheetVisible = visible,
                presetMultiSelectMode = if (visible) it.presetMultiSelectMode else false,
                selectedPresetIds = if (visible) it.selectedPresetIds else emptySet(),
                presetSearchQuery = if (visible) it.presetSearchQuery else ""
            )
        }
    }

    fun setEditSheetVisible(visible: Boolean) {
        _uiState.update {
            it.copy(
                editSheetVisible = visible,
                editingTodo = if (visible) it.editingTodo else null
            )
        }
    }

    fun onSearchPresetQueryChange(query: String) {
        _uiState.update { it.copy(presetSearchQuery = query) }
    }

    fun onEnterPresetMultiSelect(presetId: Long) {
        _uiState.update {
            it.copy(
                presetMultiSelectMode = true,
                selectedPresetIds = it.selectedPresetIds + presetId
            )
        }
    }

    fun onTogglePresetSelected(presetId: Long) {
        _uiState.update { state ->
            val updated = if (presetId in state.selectedPresetIds) {
                state.selectedPresetIds - presetId
            } else {
                state.selectedPresetIds + presetId
            }
            state.copy(selectedPresetIds = updated)
        }
    }

    fun onExitPresetMultiSelect() {
        _uiState.update { it.copy(presetMultiSelectMode = false, selectedPresetIds = emptySet()) }
    }

    fun addTodo(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            addTodoUseCase(content.trim())
        }
    }

    fun addPresetTodo(preset: Preset) {
        addTodo(preset.content)
    }

    fun addSelectedPresets() {
        val selectedPresets = _uiState.value.allPresets.filter { it.id in _uiState.value.selectedPresetIds }
        viewModelScope.launch {
            selectedPresets.forEach { preset ->
                addTodoUseCase(preset.content)
            }
            _uiState.update { it.copy(presetMultiSelectMode = false, selectedPresetIds = emptySet()) }
        }
    }

    fun toggleTodo(todo: Todo, checked: Boolean) {
        viewModelScope.launch {
            toggleTodoUseCase(todo, checked)
        }
    }

    fun startEdit(todo: Todo) {
        _uiState.update {
            it.copy(
                editingTodo = todo,
                editSheetVisible = true
            )
        }
    }

    fun saveEditedTodo(content: String) {
        val editing = _uiState.value.editingTodo ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            updateTodoUseCase(editing.copy(content = content.trim()))
            _uiState.update { it.copy(editSheetVisible = false, editingTodo = null) }
        }
    }

    /** 编辑待办界面点击「删除」：直接删除，不再弹二次确认框。 */
    fun deleteTodoNow(todo: Todo) {
        viewModelScope.launch {
            deleteTodoUseCase(todo)
            _uiState.update {
                it.copy(
                    editSheetVisible = false,
                    editingTodo = null
                )
            }
        }
    }

    fun commitReorder(reordered: List<Todo>) {
        if (reordered.isEmpty()) return
        _uiState.update { it.copy(todayTodos = reordered) }
        viewModelScope.launch {
            reorderTodosUseCase(reordered)
        }
    }
}
