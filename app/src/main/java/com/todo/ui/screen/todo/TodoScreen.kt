package com.todo.ui.screen.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import com.todo.ui.component.DialogButtonRole
import com.todo.ui.component.GlassDialog
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todo.ui.component.GlassFAB
import com.todo.ui.component.GlassButton
import com.todo.ui.component.glassOverlay
import com.todo.ui.screen.todo.component.AddTodoSheet
import com.todo.ui.screen.todo.component.EditTodoSheet
import com.todo.ui.screen.todo.component.MiniStatsCard
import com.todo.ui.screen.todo.component.TodaySection

@Composable
fun TodoScreen(
    onNavigateToChart: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val historyButtonShape = RoundedCornerShape(18.dp)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 84.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MiniStatsCard(
                completedCount = uiState.todayStats.completedCount,
                totalCount = uiState.todayStats.totalCount,
                onClick = onNavigateToChart
            )

            GlassButton(
                text = "往日记录",
                onClick = onNavigateToHistory,
                glassSurface = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(historyButtonShape)
                    .glassOverlay(
                        shape = historyButtonShape,
                        isDark = isDark,
                        topAlphaLight = 0.18f,
                        topAlphaDark = 0.11f,
                        bottomAlphaLight = 0.06f,
                        bottomAlphaDark = 0.14f
                    )
            )

            TodaySection(
                todos = uiState.todayTodos,
                onToggleTodo = viewModel::toggleTodo,
                onLongPressTodo = viewModel::startEdit,
                onReorderFinished = viewModel::commitReorder
            )
        }

        GlassFAB(
            onClick = { viewModel.setAddSheetVisible(true) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 16.dp)
        )

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
        onDelete = viewModel::confirmDelete
    )

    GlassDialog(
        visible = uiState.showDeleteDialog,
        title = "删除待办",
        message = "确定删除这条待办吗？",
        confirmText = "确定",
        cancelText = "取消",
        confirmButtonRole = DialogButtonRole.DANGER,
        cancelButtonRole = DialogButtonRole.SECONDARY,
        onConfirm = viewModel::deleteConfirmed,
        onCancel = viewModel::dismissDeleteDialog
    )
}
