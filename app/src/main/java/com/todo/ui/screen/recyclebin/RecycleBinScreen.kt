package com.todo.ui.screen.recyclebin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todo.ui.component.DialogButtonRole
import com.todo.domain.model.RecycleBinItem
import com.todo.ui.component.EmptyState
import com.todo.ui.component.GlassDialog
import com.todo.ui.component.GlassCard
import com.todo.ui.component.GlassListItem
import com.todo.ui.component.GlassTopBar
import com.todo.ui.component.glassOverlay
import com.todo.ui.theme.DarkGlassBorder
import com.todo.ui.theme.DarkGlassSurface
import com.todo.ui.theme.LightGlassBorder
import com.todo.ui.theme.LightGlassSurface
import com.todo.ui.theme.MotionTokens
import com.todo.ui.theme.GradientDarkBottom
import com.todo.ui.theme.GradientLightBottom
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecycleBinScreen(
    onBack: () -> Unit,
    viewModel: RecycleBinViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val topActionColor = if (isDark) androidx.compose.ui.graphics.Color(0xFFE6E9F0) else androidx.compose.ui.graphics.Color(0xFF3E4652)
    val groupedItems = remember(uiState.items) {
        uiState.items.groupBy { formatGroupDate(it.deletedAt) }
    }
    val selectedItems = remember(uiState.items, uiState.selectedIds) {
        uiState.items.filter { it.id in uiState.selectedIds }
    }
    val canRestore = remember(selectedItems) {
        selectedItems.isNotEmpty() && selectedItems.all { isDeletedToday(it.deletedAt) }
    }
    val listBottomInset = 24.dp

    BackHandler(enabled = uiState.isMultiSelectMode) {
        viewModel.clearMultiSelect()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GlassTopBar(
            title = if (uiState.isMultiSelectMode) "已选\n${uiState.selectedIds.size}项" else "回收站",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = {
                if (uiState.isMultiSelectMode) {
                    viewModel.clearMultiSelect()
                } else {
                    onBack()
                }
            },
            glassEffect = true,
            overlayBelowContent = true,
            titleAlignStart = uiState.isMultiSelectMode,
            actions = {
                if (uiState.isMultiSelectMode) {
                    TextButton(
                        onClick = viewModel::clearMultiSelect
                    ) {
                        Text("取消", color = topActionColor)
                    }
                    TextButton(
                        onClick = viewModel::selectAll
                    ) {
                        Text("全选", color = topActionColor)
                    }
                    TextButton(
                        onClick = viewModel::requestDeleteSelected
                    ) {
                        Text("永久删除", color = topActionColor)
                    }
                    if (canRestore) {
                        TextButton(
                            onClick = viewModel::restoreSelected
                        ) {
                            Text("还原", color = topActionColor)
                        }
                    }
                }
            }
        )

        if (uiState.items.isEmpty()) {
            EmptyState(
                text = "回收站为空",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                val listState = rememberLazyListState()
                val showFade by remember { derivedStateOf { listState.canScrollForward } }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = listBottomInset),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    groupedItems.forEach { (dateLabel, groupItems) ->
                        item(key = "header_$dateLabel") {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = dateLabel,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
                                )
                            }
                        }
                        items(groupItems, key = { it.id }) { item ->
                            RecycleBinRow(
                                item = item,
                                isMultiSelectMode = uiState.isMultiSelectMode,
                                isSelected = item.id in uiState.selectedIds,
                                onClick = { viewModel.onItemClick(item) },
                                onLongClick = { viewModel.onItemLongClick(item) }
                            )
                        }
                    }
                }

                if (showFade) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        if (isDark) GradientDarkBottom else GradientLightBottom
                                    )
                                )
                            )
                    )
                }
            }
        }
    }

    GlassDialog(
        visible = uiState.showDeleteConfirm,
        title = "永久删除",
        message = "确定永久删除${uiState.selectedIds.size}条记录？此操作不可撤销",
        confirmText = "确定",
        cancelText = "取消",
        confirmButtonRole = DialogButtonRole.DANGER,
        cancelButtonRole = DialogButtonRole.SECONDARY,
        onConfirm = viewModel::confirmDeleteSelected,
        onCancel = viewModel::dismissDeleteConfirm
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecycleBinRow(
    item: RecycleBinItem,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f

    GlassListItem(
        modifier = Modifier
            .fillMaxWidth()
            .glassOverlay(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                isDark = isDark,
                topAlphaLight = 0.18f,
                topAlphaDark = 0.11f,
                bottomAlphaLight = 0.06f,
                bottomAlphaDark = 0.14f
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = remember(item.originalDate) { "原日期：${formatOriginalDate(item.originalDate)}" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = if (item.wasCompleted) "状态：已完成" else "状态：未完成",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (isMultiSelectMode) {
                val indicatorScale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.86f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = MotionTokens.SpringMediumLow
                    ),
                    label = "recycleSelectScale"
                )
                val indicatorColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    },
                    label = "recycleSelectColor"
                )
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .scale(indicatorScale)
                        .background(indicatorColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.offset(y = with(LocalDensity.current) { (-1.dp.toPx() + 1f).toDp() }).size(16.dp),
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun formatGroupDate(timestamp: Long): String {
    val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    return "${date.year}年${date.monthValue}月${date.dayOfMonth}日"
}

private fun formatOriginalDate(date: String): String {
    val parts = date.split("-")
    if (parts.size != 3) return date
    return "${parts[0]}年${parts[1].toInt()}月${parts[2].toInt()}日"
}

private fun isDeletedToday(timestamp: Long): Boolean {
    val deletedDate = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    return deletedDate == LocalDate.now()
}
