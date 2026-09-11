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
import androidx.compose.foundation.layout.height
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
import com.todo.ui.component.NeumorphScrollFade
import com.todo.ui.component.NeumorphScrollFadeHeight
import com.todo.ui.component.NeumorphTextField
import com.todo.ui.component.neumorphPress
import com.todo.ui.theme.MotionTokens
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes

/** 面板内容的左右留白。滚动列表用"视口占满整宽 + 条目自己内缩这个值"的方式（见下），因此其余内容也按这个值对齐 */
private val AddSheetGutter = 20.dp

/** 面板里所有按钮的统一高度：顶部的二选一按钮与底部动作按钮同高，视觉上成节奏 */
private val AddSheetActionHeight = 48.dp

/**
 * 点击 + 后出现的面板。
 *
 * 结构上与其它页面保持同一套语言：
 * 1. 面板本身就是那块"表面"（[GlassBottomSheet]），内部**不再叠一张卡片**——两层同色不透明表面
 *    叠在一起会糊成一整块（往日记录当初就是这么出问题的）；
 * 2. 顶部的二选一改成**两个并列的状态按钮**：当前选中的那块凹进去、另一块凸起（凹陷 = 已选中，
 *    凸起 = 可点），未选中那块按下时也会由凸转凹；
 * 3. 预设条目是列表项，和预设页一样**凸起**，多选模式下被选中的那条才凹进去；列表视口比条目宽，
 *    上下缘另有常驻渐隐，滚过边界的条目光影不会被硬切；
 * 4. 底部动作与别处一致：次要动作用同色玻璃面、主行动用主题色，高度与顶部按钮一致。
 */
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

    GlassBottomSheet(
        onDismissRequest = onDismiss,
        // 只给纵向内边距：横向由各块自己按 AddSheetGutter 对齐，列表才能拿到整宽的滚动视口
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AddSheetGutter),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (tabIndex) {
                        0 -> {
                            NeumorphTextField(
                                value = manualInput,
                                onValueChange = { manualInput = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AddSheetGutter),
                                placeholder = "输入待办内容..."
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AddSheetGutter),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GlassButton(
                                    text = "完成",
                                    onClick = onDismiss,
                                    glassSurface = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(AddSheetActionHeight)
                                )
                                GlassButton(
                                    text = "添加",
                                    onClick = {
                                        onAddManual(manualInput)
                                        manualInput = ""
                                    },
                                    // 有内容才是主行动（主题色）；空的时候连按都不该亮起来
                                    enabled = manualInput.isNotBlank(),
                                    glassSurface = !manualInput.isNotBlank(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(AddSheetActionHeight)
                                )
                            }
                        }

                        1 -> {
                            NeumorphTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AddSheetGutter),
                                placeholder = "搜索预设..."
                            )

                            if (presets.isEmpty()) {
                                Text(
                                    text = "暂无预设，去预设页面添加吧",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = secondaryTextColor,
                                    modifier = Modifier.padding(horizontal = AddSheetGutter)
                                )
                            } else {
                                // 视口占满整宽（条目的光影因此有地方落），条目自己内缩 AddSheetGutter；
                                // 上下缘的常驻渐隐负责让滚过边界的条目平滑消失
                                NeumorphScrollFade(modifier = Modifier.fillMaxWidth()) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 300.dp),
                                        contentPadding = PaddingValues(
                                            start = AddSheetGutter,
                                            end = AddSheetGutter,
                                            top = NeumorphScrollFadeHeight,
                                            bottom = NeumorphScrollFadeHeight
                                        ),
                                        // 间距按阴影扩散范围给足，相邻条目的光影才不互相压盖
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(presets, key = { it.id }) { preset ->
                                            AddSheetPresetRow(
                                                preset = preset,
                                                isDark = isDark,
                                                isMultiSelectMode = isMultiSelectMode,
                                                isSelected = preset.id in selectedPresetIds,
                                                onClick = {
                                                    if (isMultiSelectMode) {
                                                        onTogglePresetSelect(preset)
                                                    } else {
                                                        onPresetClick(preset)
                                                    }
                                                },
                                                onLongClick = { onPresetLongPress(preset) }
                                            )
                                        }
                                    }
                                }
                            }

                            if (isMultiSelectMode) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = AddSheetGutter),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    GlassButton(
                                        text = "取消",
                                        onClick = onExitMultiSelect,
                                        glassSurface = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(AddSheetActionHeight)
                                    )
                                    GlassButton(
                                        text = "添加选中项",
                                        onClick = onAddSelected,
                                        enabled = selectedPresetIds.isNotEmpty(),
                                        glassSurface = selectedPresetIds.isEmpty(),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(AddSheetActionHeight)
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

/**
 * 面板顶部的"二选一"按钮。
 *
 * 当前选中的那块**凹进去**（凹陷 = 已选中），另一块**凸起**（凸起 = 可点）；未选中那块按下时
 * 由凸转凹、松开弹回，于是"状态"和"按下"用的是同一种语言。波纹与按钮同形（先裁形状再挂点击）。
 */
@Composable
private fun AddSheetSwitchButton(
    text: String,
    selected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(NeumorphShapes.Small)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .height(AddSheetActionHeight)
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
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.secondary
        )
    }
}

/**
 * 面板里的预设条目：与预设页的条目同规格（凸起、同圆角、同内边距），
 * 只有多选模式下被选中的那条才凹进去（凹陷 = 选中）。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AddSheetPresetRow(
    preset: Preset,
    isDark: Boolean,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val shape = RoundedCornerShape(NeumorphShapes.Small)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val recessed = isMultiSelectMode && isSelected

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neumorphPress(
                shape = shape,
                isDark = isDark,
                pressed = pressed && !recessed,
                elevation = NeumorphElevation.Medium,
                restDepth = if (recessed) 0f else 1f
            )
            .clip(shape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = preset.content,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
        )

        if (isMultiSelectMode) {
            if (isSelected) {
                // 与待办勾选框同款：选中 = 主题色实底 + 白勾（小方块圆角，不用正圆）
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
