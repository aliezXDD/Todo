package com.todo.ui.screen.preset

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todo.ui.component.DialogButtonRole
import com.todo.ui.component.EmptyState
import com.todo.ui.component.GlassDialog
import com.todo.ui.component.GlassToast
import com.todo.ui.component.NeumorphScrollFade
import com.todo.ui.component.NeumorphScrollFadeHeight
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

    BackHandler(enabled = uiState.isMultiSelectMode) {
        viewModel.clearMultiSelect()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearMultiSelect() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        // 不再叠加纵向间距：列表自己带 16dp 顶部内衬（与渐隐带同高），间距由它提供
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        // 左右的 16dp 不再统一加在 Column 上：列表需要比条目更宽的视口，
        // 条目自己内缩同样的 16dp（见下方 contentPadding），这样溢出的光影才不会被视口硬切
        PresetTopBar(
            isMultiSelectMode = uiState.isMultiSelectMode,
            selectedCount = uiState.selectedIds.size,
            onCreateClick = viewModel::onCreateClick,
            onCancelClick = viewModel::clearMultiSelect,
            onSelectAllClick = viewModel::selectAll,
            onDeleteClick = viewModel::requestDeleteSelected,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (uiState.presets.isEmpty()) {
            EmptyState(
                text = "点击右上角 + 创建常用待办预设",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        } else {
            val listState = rememberLazyListState()
            // 上下缘各一条常驻渐隐（旧写法是"还能滚才出现"的 56dp 底隐，滚到端点时会突然出现/消失，
            // 那本身也是一种硬切；常驻的两条带子静止时只覆盖与背景同色的内衬区，看不见）
            NeumorphScrollFade(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 140.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    // 上下内衬与渐隐带同高（静止时首尾条目的光影完整，滚过边界的才被淡化）；
                    // 左右 16dp 是条目的实际位置，视口本身仍是全宽，用来容纳条目溢出的光影
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = NeumorphScrollFadeHeight,
                        bottom = NeumorphScrollFadeHeight
                    ),
                    // 间距按阴影扩散范围给足（偏移 7dp + 模糊 11dp 的一半），避免相邻条目光影互相压盖
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
