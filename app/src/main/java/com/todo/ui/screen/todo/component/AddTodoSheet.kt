package com.todo.ui.screen.todo.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todo.domain.model.Preset
import com.todo.ui.component.GlassBottomSheet
import com.todo.ui.component.GlassButton
import com.todo.ui.component.GlassCard
import com.todo.ui.component.NeumorphTextField
import com.todo.ui.component.neumorph
import com.todo.ui.component.neumorphPress
import com.todo.ui.theme.MotionTokens
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddTodoSheet(
    visible: Boolean,
    presets: List<Preset>,
    isMultiSelectMode: Boolean,
    selectedPresetIds: Set<Long>,
    searchQuery: String,
    onDismiss: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAddManual: (String) -> Unit,
    onPresetClick: (Preset) -> Unit,
    onPresetLongPress: (Preset) -> Unit,
    onTogglePresetSelect: (Preset) -> Unit,
    onAddSelected: () -> Unit,
    onExitMultiSelect: () -> Unit
) {
    if (!visible) return

    var selectedTabIndex by remember { mutableStateOf(0) }
    var manualInput by remember { mutableStateOf("") }
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val secondaryTextColor = MaterialTheme.colorScheme.secondary
    val primaryTextColor = MaterialTheme.colorScheme.onSurface

    GlassBottomSheet(
        onDismissRequest = onDismiss
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            // 内边距必须 ≥ 内侧按钮的光影扩散（偏移 7 + 模糊 11 ≈ 18dp），否则那圈光影会与卡片
            // 边缘相交：要么被边缘切掉，要么越出边缘压在卡片的框上。给到 20dp 后，按钮的光影完整地
            // 待在卡片之内，框与影互不相干。
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 与「编辑待办」面板同一套排布：标题 → 内容 → 两颗动作按钮
                Text(
                    text = "添加待办",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // 二选一由 Tab 行改成两颗并列的状态按钮：当前选中的那块**凹进去**（凹陷 = 已选中），
                // 另一块**凸起**（凸起 = 可点）；未选中那块按下时由凸转凹，状态与按下是同一种语言
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AddSheetSwitchButton(
                        text = "手动输入",
                        selected = selectedTabIndex == 0,
                        isDark = isDark,
                        onClick = { selectedTabIndex = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    AddSheetSwitchButton(
                        text = "从预设选择",
                        selected = selectedTabIndex == 1,
                        isDark = isDark,
                        onClick = { selectedTabIndex = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }

                AnimatedContent(
                    targetState = selectedTabIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (
                                fadeIn(tween(MotionTokens.SheetTabSwitch, easing = MotionTokens.StandardEasing)) +
                                    slideInVertically(
                                        animationSpec = tween(MotionTokens.SheetTabSwitch, easing = MotionTokens.StandardEasing),
                                        initialOffsetY = { it / 3 }
                                    )
                                ) togetherWith (
                                fadeOut(tween(MotionTokens.SheetTabSwitch, easing = MotionTokens.StandardEasing)) +
                                    slideOutVertically(
                                        animationSpec = tween(MotionTokens.SheetTabSwitch, easing = MotionTokens.StandardEasing),
                                        targetOffsetY = { -it / 3 }
                                    )
                                )
                        } else {
                            (
                                fadeIn(tween(MotionTokens.SheetTabSwitch, easing = MotionTokens.StandardEasing)) +
                                    slideInVertically(
                                        animationSpec = tween(MotionTokens.SheetTabSwitch, easing = MotionTokens.StandardEasing),
                                        initialOffsetY = { -it / 3 }
                                    )
                                ) togetherWith (
                                fadeOut(tween(MotionTokens.SheetTabSwitch, easing = MotionTokens.StandardEasing)) +
                                    slideOutVertically(
                                        animationSpec = tween(MotionTokens.SheetTabSwitch, easing = MotionTokens.StandardEasing),
                                        targetOffsetY = { it / 3 }
                                    )
                                )
                        }
                    },
                    label = "addSheetTabSlide"
                ) { tabIndex ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = MotionTokens.SheetResize,
                                    easing = MotionTokens.StandardEasing
                                )
                            )
                    ) {
                        when (tabIndex) {
                            0 -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    NeumorphTextField(
                                        value = manualInput,
                                        onValueChange = { manualInput = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = "输入待办内容..."
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        GlassButton(
                                            text = "完成",
                                            onClick = onDismiss,
                                            // 与「编辑待办」里的“删除”同色：玻璃面 = 次要行动
                                            glassSurface = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        GlassButton(
                                            text = "添加",
                                            onClick = {
                                                if (manualInput.isNotBlank()) {
                                                    onAddManual(manualInput)
                                                    manualInput = ""
                                                }
                                            },
                                            // 主行动用主题色，和「编辑待办」里的“保存”一致
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            1 -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    NeumorphTextField(
                                        value = searchQuery,
                                        onValueChange = onSearchQueryChange,
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = "搜索预设..."
                                    )

                                    if (presets.isEmpty()) {
                                        Text(
                                            text = "暂无预设，去预设页面添加吧",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = secondaryTextColor
                                        )
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 300.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(presets, key = { it.id }) { preset ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .neumorph(
                                                            shape = RoundedCornerShape(NeumorphShapes.Small),
                                                            isDark = isDark,
                                                            depth = 0f,
                                                            elevation = NeumorphElevation.Small
                                                        )
                                                        // 同滑轨：先裁成条目形状，水波纹才不会从圆角外露出方块
                                                        .clip(RoundedCornerShape(NeumorphShapes.Small))
                                                        .combinedClickable(
                                                            onClick = {
                                                                if (isMultiSelectMode) {
                                                                    onTogglePresetSelect(preset)
                                                                } else {
                                                                    onPresetClick(preset)
                                                                }
                                                            },
                                                            onLongClick = {
                                                                onPresetLongPress(preset)
                                                            }
                                                        )
                                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(text = preset.content, color = primaryTextColor)
                                                    if (isMultiSelectMode) {
                                                        if (preset.id in selectedPresetIds) {
                                                            // 与待办勾选框同款：选中 = 主题色实底 + 白勾，
                                                            // 形状为小方块圆角（不用正圆）
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(18.dp)
                                                                    .background(
                                                                        MaterialTheme.colorScheme.primary,
                                                                        RoundedCornerShape(NeumorphShapes.Marker)
                                                                    ),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Filled.Check,
                                                                    contentDescription = null,
                                                                    modifier = Modifier.size(13.dp),
                                                                    tint = MaterialTheme.colorScheme.onPrimary
                                                                )
                                                            }
                                                        } else {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(18.dp)
                                                                    .background(
                                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                                                        shape = RoundedCornerShape(NeumorphShapes.Marker)
                                                                    )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (isMultiSelectMode) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            GlassButton(
                                                text = "取消",
                                                onClick = onExitMultiSelect,
                                                // 玻璃面 = 次要行动（与「编辑待办」里的“删除”同色）
                                                glassSurface = true,
                                                modifier = Modifier.weight(1f)
                                            )
                                            GlassButton(
                                                text = "添加选中项",
                                                onClick = onAddSelected,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 面板顶部的"二选一"按钮（手动输入 / 从预设选择）。
 *
 * 与「编辑待办」面板里的按钮同高同形（同上下的 12dp 内边距、同圆角），区别只在状态：
 * 当前选中的那块**凹进去**（凹陷 = 已选中），另一块**凸起**（凸起 = 可点）；
 * 未选中那块按下时由凸转凹、松手弹回，所以"状态"和"按下"用的是同一种语言。
 * 波纹被裁进按钮形状内（和列表条目同一套规则）。
 */
@Composable
private fun AddSheetSwitchButton(
    text: String,
    selected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(NeumorphShapes.Corner)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .neumorphPress(
                shape = shape,
                isDark = isDark,
                // 已经凹着的那块不需要再"按进去"
                pressed = pressed && !selected,
                elevation = NeumorphElevation.Medium,
                restDepth = if (selected) 0f else 1f
            )
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            // 与 GlassButton 相同的上下内边距，两颗按钮因此和底部动作按钮一样高
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.secondary
        )
    }
}

