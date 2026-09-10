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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todo.ui.component.DialogButtonRole
import com.todo.ui.component.EmptyState
import com.todo.ui.component.GlassDialog
import com.todo.ui.component.GlassToast
import com.todo.ui.screen.preset.component.PresetEditDialog
import com.todo.ui.screen.preset.component.PresetItemRow
import com.todo.ui.screen.preset.component.PresetTopBar
import com.todo.ui.theme.Neumorph

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PresetScreen(
    viewModel: PresetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showEditDialog by viewModel.showEditDialog.collectAsStateWithLifecycle()
    val editingPreset by viewModel.editingPreset.collectAsStateWithLifecycle()
    val showDeleteConfirm by viewModel.showDeleteConfirm.collectAsStateWithLifecycle()

    BackHandler(enabled = uiState.isMultiSelectMode) {
        viewModel.clearMultiSelect()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearMultiSelect() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 0.dp),
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
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 140.dp)) {
                val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
                val listState = rememberLazyListState()
                val showFade by remember { derivedStateOf { listState.canScrollForward } }
                // 背景已是纯色，底部渐隐直接用背景色即可，无需再采样渐变
                val bottomFadeColor = Neumorph.surface(isDark)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.presets, key = { it.id }) { preset ->
                        PresetItemRow(
                            preset = preset,
                            isMultiSelectMode = uiState.isMultiSelectMode,
                            isSelected = preset.id in uiState.selectedIds,
                            modifier = Modifier.animateItem(),
                            onClick = { viewModel.onPresetClick(preset) },
                            onLongClick = { viewModel.onPresetLongClick(preset) },
                            onDoubleClick = { viewModel.onPresetDoubleClick(preset) }
                        )
                    }
                }

                if (showFade) {
                    Box(
                        // 列表容器已整体上收 140dp（止于底栏上沿），渐隐条落在列表底缘即可，
                        // 无需位移；这样滚动途中经过底栏的条目会被列表裁剪，不会透出底栏。
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        bottomFadeColor.copy(alpha = 0f),
                                        bottomFadeColor
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
        onConfirm = viewModel::confirmDeleteSelected,
        onCancel = viewModel::dismissDeleteConfirm
    )

    GlassToast(
        messageFlow = viewModel.addedToTodayMessage,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = 20.dp)
            .padding(bottom = 140.dp)
    )
    }
}
