package com.todo.ui.screen.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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

            TodaySection(
                todos = uiState.todayTodos,
                onToggleTodo = viewModel::toggleTodo,
                onEditTodo = viewModel::startEdit,
                onReorderFinished = viewModel::commitReorder,
                modifier = Modifier.weight(1f)
            )
        }

        GlassFAB(
            onClick = { viewModel.setAddSheetVisible(true) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                // 使 + 按钮底边到今日卡片底边缘的距离，与右边到今日卡片右边缘的距离相等，并稍微远离右下角
                .padding(end = 26.dp, bottom = 160.dp)
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
        onDelete = viewModel::deleteTodoNow
    )
}
