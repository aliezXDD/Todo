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
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todo.domain.model.Preset
import com.todo.ui.component.GlassBottomSheet
import com.todo.ui.component.GlassButton
import com.todo.ui.component.GlassCard
import com.todo.ui.component.NeumorphTextField
import com.todo.ui.component.neumorph
import com.todo.ui.theme.MotionTokens
import com.todo.ui.theme.Neumorph
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
    val tabIndicatorColor = MaterialTheme.colorScheme.primary
    val tabSelectedColor = MaterialTheme.colorScheme.onSurface
    val tabUnselectedColor = MaterialTheme.colorScheme.secondary

    GlassBottomSheet(
        onDismissRequest = onDismiss
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val tabTrackShape = RoundedCornerShape(NeumorphShapes.Small)
                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    // 滑轨靠"更暗一档的底色"划定范围（颜色负责区分、光影只做辅助），避免高光抢戏
                    // clip 必须跟在 neumorph 之后：Tab 自带的水波纹是方块，不裁的话会从滑轨四角露出来
                    modifier = Modifier
                        .neumorph(
                            shape = tabTrackShape,
                            isDark = isDark,
                            depth = 0f,
                            surface = Neumorph.recessedSurface(isDark),
                            elevation = NeumorphElevation.Small
                        )
                        .clip(tabTrackShape),
                    containerColor = Color.Transparent,
                    indicator = {
                        // 选中项下方的主题色指示条：凹陷滑轨 + 指示条，全程无描边
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(selectedTabIndex)
                                .padding(horizontal = 7.dp)
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(NeumorphShapes.Corner))
                                .background(tabIndicatorColor)
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        selectedContentColor = tabSelectedColor,
                        unselectedContentColor = tabUnselectedColor,
                        text = { Text("手动输入") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        selectedContentColor = tabSelectedColor,
                        unselectedContentColor = tabUnselectedColor,
                        text = { Text("从预设选择") }
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
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    NeumorphTextField(
                                        value = manualInput,
                                        onValueChange = { manualInput = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = "输入待办内容..."
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        GlassButton(
                                            text = "完成",
                                            onClick = onDismiss,
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
                                            // 输入框有内容 → 主题浅紫色（主操作）；无内容 → 与“完成”相同的玻璃色（次要）
                                            glassSurface = !manualInput.isNotBlank(),
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
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            GlassButton(
                                                text = "添加选中项",
                                                onClick = onAddSelected,
                                                modifier = Modifier.weight(1f)
                                            )
                                            GlassButton(
                                                text = "取消",
                                                onClick = onExitMultiSelect,
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

