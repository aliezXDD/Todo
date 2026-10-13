package com.todo.ui.screen.todo

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todo.ui.component.GlassButton
import com.todo.ui.component.GlassFAB
import com.todo.ui.screen.todo.component.AddTodoSheet
import com.todo.ui.screen.todo.component.AvatarActionDialog
import com.todo.ui.screen.todo.component.AvatarCropDialog
import com.todo.ui.screen.todo.component.EditTodoSheet
import com.todo.ui.screen.todo.component.MiniStatsCard
import com.todo.ui.screen.todo.component.NoteSheet
import com.todo.ui.screen.todo.component.TodaySection
import com.todo.ui.screen.todo.component.decodePickedImage
import com.todo.ui.theme.NeumorphShapes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TodoScreen(
    onNavigateToChart: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 回到前台时校正"当前逻辑日"：进程可能在后台被冻结过，跨过凌晨 4 点也不会自己醒
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshDay()
    }
    // 「往日记录」按钮：与卡片同一套圆角语言
    val historyButtonShape = RoundedCornerShape(NeumorphShapes.Small)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // 选好图、还没裁剪的原始图片。只活在界面里：对话框一关就该释放，没必要进 ViewModel。
    var pendingCropSource by remember { mutableStateOf<ImageBitmap?>(null) }

    // 相册选图。用 PickVisualMedia（系统相册选择器）而不是 GetContent：
    // 它不需要任何权限，因此"零权限"这条不变；API 31/32 上没有系统选择器时，
    // androidx 会自动回落到 ACTION_OPEN_DOCUMENT，同样不申请权限。
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            // 解码（含 EXIF 旋转、降采样）在 IO 线程做，主线程不卡
            val decoded = withContext(Dispatchers.IO) { decodePickedImage(context, uri) }
            if (decoded != null) pendingCropSource = decoded
        }
    }

    // 弹出的系统相册选择器只认"只要图片"，不申请任何读取权限
    val launchImagePicker = {
        pickImage.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
        Unit
    }

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
                reservedCount = uiState.todayReservedCount,
                isLoading = uiState.isLoading,
                avatar = uiState.avatar,
                onAvatarClick = { viewModel.setAvatarDialogVisible(true) },
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
                    date = uiState.today,
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
                        contentDescription = "备注",
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
        // 截止日期的日历以当前逻辑日为最早可选的一天（今天之前的日期没有意义）
        today = uiState.today,
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

    AvatarActionDialog(
        visible = uiState.avatarDialogVisible,
        hasCustomAvatar = uiState.avatar != null,
        onPickImage = {
            viewModel.setAvatarDialogVisible(false)
            launchImagePicker()
        },
        onResetToDefault = viewModel::resetAvatar,
        onDismiss = { viewModel.setAvatarDialogVisible(false) }
    )

    // 选好图之后接一层裁剪：用户确认的才是最终头像（自动居中裁常会把主体切掉）
    pendingCropSource?.let { source ->
        AvatarCropDialog(
            source = source,
            onDismiss = { pendingCropSource = null },
            onConfirm = { cropped ->
                pendingCropSource = null
                viewModel.saveAvatar(cropped)
            }
        )
    }
}
