package com.todo.ui.screen.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todo.ui.component.GlassButton
import com.todo.ui.component.GlassFAB
import com.todo.ui.screen.todo.component.AddTodoSheet
import com.todo.ui.screen.todo.component.EditTodoSheet
import com.todo.ui.screen.todo.component.MiniStatsCard
import com.todo.ui.screen.todo.component.NoteSheet
import com.todo.ui.screen.todo.component.TodaySection
import com.todo.ui.theme.NeumorphShapes

@Composable
fun TodoScreen(
    onNavigateToChart: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 「往日记录」按钮：与卡片同一套圆角语言
    val historyButtonShape = RoundedCornerShape(NeumorphShapes.Small)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 150.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MiniStatsCard(
                completedCount = uiState.todayStats.completedCount,
                totalCount = uiState.todayStats.totalCount,
                isLoading = uiState.isLoading,
                onClick = onNavigateToChart
            )

            GlassButton(
                text = "往日记录",
                onClick = onNavigateToHistory,
                glassSurface = true,
                shape = historyButtonShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )

            // 「今日」卡片（标题 + 日期 + 待办列表）自己撑满这个 Box，
            // 所以 Box 的范围就等于卡片的范围，+ 按钮贴着它的右上角放即可
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                TodaySection(
                    todos = uiState.todayTodos,
                    isLoading = uiState.isLoading,
                    onToggleTodo = viewModel::toggleTodo,
                    onEditTodo = viewModel::startEdit,
                    onReorderFinished = viewModel::commitReorder,
                    modifier = Modifier.fillMaxSize()
                )

                // 两个入口并排贴在「今日」卡片的右上角：笔记在左、添加在右。
                // 「+」的右边界仍与待办条目右边界对齐（条目内缩 16dp），位置与之前完全一致。
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassFAB(
                        onClick = { viewModel.setNoteSheetVisible(true) },
                        icon = Icons.Filled.Edit,
                        contentDescription = "笔记",
                        accent = false
                    )
                    GlassFAB(
                        onClick = { viewModel.setAddSheetVisible(true) },
                        icon = Icons.Filled.Add,
                        contentDescription = "添加"
                    )
                }
            }
        }

    }

    AddTodoSheet(
        visible = uiState.addSheetVisible,
        presets = uiState.filteredPresets,
        isMultiSelectMode = uiState.presetMultiSelectMode,
        selectedPresetIds = uiState.selectedPresetIds,
        searchQuery = uiState.presetSearchQuery,
        onDismiss = { viewModel.setAddSheetVisible(false) },
        onSearchQueryChange = viewModel::onSearchPresetQueryChange,
        onAddManual = viewModel::addTodo,
        onPresetClick = viewModel::addPresetTodo,
        onPresetLongPress = { viewModel.onEnterPresetMultiSelect(it.id) },
        onTogglePresetSelect = { viewModel.onTogglePresetSelected(it.id) },
        onAddSelected = viewModel::addSelectedPresets,
        onExitMultiSelect = viewModel::onExitPresetMultiSelect
    )

    EditTodoSheet(
        visible = uiState.editSheetVisible,
        todo = uiState.editingTodo,
        onDismiss = { viewModel.setEditSheetVisible(false) },
        onSave = viewModel::saveEditedTodo,
        onDelete = viewModel::deleteTodoNow
    )

    NoteSheet(
        visible = uiState.noteSheetVisible,
        content = uiState.noteContent,
        onContentChange = viewModel::onNoteContentChange,
        onDismiss = { viewModel.setNoteSheetVisible(false) }
    )
}
