package com.todo.ui.screen.preset

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todo.ui.component.DialogButtonRole
import com.todo.ui.component.EmptyState
import com.todo.ui.component.GlassDialog
import com.todo.ui.screen.preset.component.PresetEditDialog
import com.todo.ui.screen.preset.component.PresetItemRow
import com.todo.ui.screen.preset.component.PresetTopBar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PresetScreen(
    viewModel: PresetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showEditDialog by viewModel.showEditDialog.collectAsStateWithLifecycle()
    val editingPreset by viewModel.editingPreset.collectAsStateWithLifecycle()
    val showDeleteConfirm by viewModel.showDeleteConfirm.collectAsStateWithLifecycle()
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val showBottomShadow = uiState.presets.size > 8

    BackHandler(enabled = uiState.isMultiSelectMode) {
        viewModel.clearMultiSelect()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 84.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        PresetTopBar(
            isMultiSelectMode = uiState.isMultiSelectMode,
            selectedCount = uiState.selectedIds.size,
            onCreateClick = viewModel::onCreateClick,
            onCancelClick = viewModel::clearMultiSelect,
            onSelectAllClick = viewModel::selectAll,
            onDeleteClick = viewModel::requestDeleteSelected
        )

        if (uiState.presets.isEmpty()) {
            EmptyState(
                text = "点击右上角 + 创建常用待办预设",
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = if (showBottomShadow) 44.dp else 0.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.presets, key = { it.id }) { preset ->
                        PresetItemRow(
                            preset = preset,
                            isMultiSelectMode = uiState.isMultiSelectMode,
                            isSelected = preset.id in uiState.selectedIds,
                            modifier = Modifier.animateItem(),
                            onClick = { viewModel.onPresetClick(preset) },
                            onLongClick = { viewModel.onPresetLongClick(preset) }
                        )
                    }
                }

                if (showBottomShadow) {
                    val shadowBottomColor = if (isDark) Color(0xFF171819) else Color(0xFFDEDBE4)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        shadowBottomColor
                                    )
                                )
                            )
                    )
                }
            }
        }
    }

    PresetEditDialog(
        visible = showEditDialog,
        editingPreset = editingPreset,
        onDismiss = viewModel::dismissEditDialog,
        onSave = viewModel::savePreset
    )

    GlassDialog(
        visible = showDeleteConfirm,
        title = "删除预设",
        message = "确定删除选中的 ${uiState.selectedIds.size} 条预设吗？",
        confirmText = "确定",
        cancelText = "取消",
        confirmButtonRole = DialogButtonRole.DANGER,
        cancelButtonRole = DialogButtonRole.SECONDARY,
        surfaceAlphaDelta = 0.04f,
        onConfirm = viewModel::confirmDeleteSelected,
        onCancel = viewModel::dismissDeleteConfirm
    )
}
