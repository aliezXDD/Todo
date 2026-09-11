package com.todo.ui.screen.recyclebin

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todo.ui.component.DialogButtonRole
import com.todo.domain.model.RecycleBinItem
import com.todo.ui.component.EmptyState
import com.todo.ui.component.GlassDialog
import com.todo.ui.component.GlassListItem
import com.todo.ui.component.GlassTopBar
import com.todo.ui.component.NeumorphScrollFade
import com.todo.ui.component.NeumorphScrollFadeHeight
import com.todo.ui.component.neumorph
import com.todo.ui.component.neumorphOverlay
import com.todo.ui.theme.MotionTokens
import com.todo.ui.theme.Neumorph
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes
import java.time.Instant
import java.time.ZoneId

@Composable
fun RecycleBinScreen(
    onBack: () -> Unit,
    viewModel: RecycleBinViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    // 顶栏动作统一用主题色，不再硬编码（原来浅/深色各写死一个色值，与全局字色体系脱节）
    val topActionColor = MaterialTheme.colorScheme.secondary
    val groupedItems = remember(uiState.items) {
        uiState.items.groupBy { formatGroupDate(it.deletedAt) }
    }
    val selectedItems = remember(uiState.items, uiState.selectedIds) {
        uiState.items.filter { it.id in uiState.selectedIds }
    }
    val canRestore = remember(selectedItems) {
        selectedItems.isNotEmpty()
    }
    val listBottomInset = 24.dp

    BackHandler(enabled = uiState.isMultiSelectMode) {
        viewModel.clearMultiSelect()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        GlassTopBar(
            title = if (uiState.isMultiSelectMode) "" else "回收站",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            // 吸顶栏浮在列表之上：不抬高的话，它溢出到列表范围内的阴影会被列表的渐隐带盖掉，
            // 看起来像被一条直线硬切（下方的次级条目仍按列表的渐隐处理）
            modifier = Modifier.neumorphOverlay(),
            onNavigationClick = {
                if (uiState.isMultiSelectMode) {
                    viewModel.clearMultiSelect()
                } else {
                    onBack()
                }
            },
            overlayBelowContent = true,
            titleAlignStart = uiState.isMultiSelectMode,
            actions = {
                if (uiState.isMultiSelectMode) {
                    // 顶栏动作按钮统一与顶栏同形（默认 TextButton 是胶囊水波纹）
                    val actionShape = RoundedCornerShape(NeumorphShapes.Corner)
                    TextButton(
                        onClick = viewModel::clearMultiSelect,
                        shape = actionShape
                    ) {
                        Text("取消", color = topActionColor)
                    }
                    TextButton(
                        onClick = viewModel::selectAll,
                        shape = actionShape
                    ) {
                        Text("全选", color = topActionColor)
                    }
                    // 选中 0 项时只显示「取消」「全选」；有选中才显示 永久删除/还原
                    if (uiState.selectedIds.size > 0) {
                        TextButton(
                            onClick = viewModel::requestDeleteSelected,
                            shape = actionShape
                        ) {
                            Text("永久删除", color = topActionColor)
                        }
                        if (canRestore) {
                            TextButton(
                                onClick = viewModel::restoreSelected,
                                shape = actionShape
                            ) {
                                Text("还原", color = topActionColor)
                            }
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
            val listState = rememberLazyListState()
            // 上下缘各一条常驻渐隐：条目滚出列表边界时，溢出的光影会被容器硬切
            NeumorphScrollFade(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = listBottomInset)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    // 上下内衬与渐隐带同高（列表第一个元素就是日期标题，顶部内衬不足会被淡化）；
                    // 左右 16dp 是条目的实际位置，视口本身仍比条目宽，用来容纳条目溢出的光影
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = NeumorphScrollFadeHeight,
                        bottom = NeumorphScrollFadeHeight
                    ),
                    // 同上：让相邻条目的光影不互相压盖
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    groupedItems.forEach { (dateLabel, groupItems) ->
                        item(key = "header_$dateLabel") {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = dateLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    // 必须显式给色：这里不在任何 Surface 内，LocalContentColor 会退回
                                    // 它的默认值 Color.Black，深色模式下就是"黑字压在深底上"
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                // 分组分隔改用"刻出来的一道凹槽"：新拟态里没有描边，层级只能靠光影。
                                // 细条要用更小的偏移/模糊，否则阴影比凹槽本身还大；
                                // 深色下还要把内高光单独提回来：全局把深色内高光统一压到 0.42 是为大表面调的，
                                // 这条只有 5dp 高的凹槽下半截因此几乎等于背景色，整条看着像"一道渐渐消失的暗影"
                                // 而不是刻出来的线。这里让它拿到和浅色同量的内高光（0.42 × 1.5 ≈ 0.63）
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .neumorph(
                                            shape = RoundedCornerShape(NeumorphShapes.Corner),
                                            isDark = isDark,
                                            depth = 0f,
                                            surface = Neumorph.recessedSurface(isDark),
                                            // 细条要用更小的偏移/模糊，否则阴影比凹槽本身还大
                                            elevation = NeumorphElevation(
                                                offset = 2.dp,
                                                blur = 3.dp,
                                                lightAlpha = if (isDark) 1.5f else 1f
                                            )
                                        )
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

@Composable
private fun RecycleBinRow(
    item: RecycleBinItem,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f

    // 点击/长按交给 GlassListItem：波纹会与条目同形（自己挂在 modifier 上会被同色填充盖住）
    GlassListItem(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        onLongClick = onLongClick
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
                        // 多选标记用"小方块圆角"，与待办勾选框、预设标记一致
                        .background(
                            color = indicatorColor,
                            shape = RoundedCornerShape(NeumorphShapes.Marker)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.offset(y = with(LocalDensity.current) { (-1.dp.toPx() + 1f).toDp() }).size(16.dp),
                            // 与 + 号同一取色（浅色白勾、深色深色勾）
                            tint = MaterialTheme.colorScheme.onPrimary
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

