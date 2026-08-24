package com.todo.ui.screen.todo.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todo.domain.model.Preset
import com.todo.ui.component.GlassBottomSheet
import com.todo.ui.component.GlassButton
import com.todo.ui.component.GlassCard
import com.todo.ui.component.glassTextFieldColors
import com.todo.ui.component.glassTextFieldShape
import com.todo.ui.theme.DarkGlassBorder
import com.todo.ui.theme.DarkGlassSurface
import com.todo.ui.theme.LightGlassBorder
import com.todo.ui.theme.LightGlassSurface
import com.todo.ui.theme.MotionTokens

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
    val secondaryTextColor = if (isDark) Color(0xFFB6BDCA) else Color(0xFF6F7785)
    val primaryTextColor = if (isDark) Color(0xFFEDEFF4) else MaterialTheme.colorScheme.onSurface
    val tabContainerColor = if (isDark) DarkGlassSurface.copy(alpha = 0.56f) else LightGlassSurface.copy(alpha = 0.54f)
    val tabBorderColor = if (isDark) DarkGlassBorder else LightGlassBorder
    val tabIndicatorColor = if (isDark) Color(0xFFDDE2EB) else Color(0xFF778191)
    val tabSelectedColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val tabUnselectedColor = if (isDark) Color.White.copy(alpha = 0.72f) else Color(0xFF6F7785)
    val presetItemBgColor = if (isDark) DarkGlassSurface.copy(alpha = 0.42f) else LightGlassSurface.copy(alpha = 0.54f)
    val presetItemBorderColor = if (isDark) DarkGlassBorder else LightGlassBorder

    GlassBottomSheet(
        onDismissRequest = onDismiss,
        opaqueBackground = true
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            highlightScale = 0.5f
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, tabBorderColor, RoundedCornerShape(14.dp)),
                    containerColor = tabContainerColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = tabIndicatorColor
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
                            slideInVertically(
                                animationSpec = tween(
                                    durationMillis = MotionTokens.SheetTabSwitch,
                                    easing = MotionTokens.StandardEasing
                                ),
                                initialOffsetY = { it / 3 }
                            ) togetherWith slideOutVertically(
                                animationSpec = tween(
                                    durationMillis = MotionTokens.SheetTabSwitch,
                                    easing = MotionTokens.StandardEasing
                                ),
                                targetOffsetY = { -it / 3 }
                            )
                        } else {
                            slideInVertically(
                                animationSpec = tween(
                                    durationMillis = MotionTokens.SheetTabSwitch,
                                    easing = MotionTokens.StandardEasing
                                ),
                                initialOffsetY = { -it / 3 }
                            ) togetherWith slideOutVertically(
                                animationSpec = tween(
                                    durationMillis = MotionTokens.SheetTabSwitch,
                                    easing = MotionTokens.StandardEasing
                                ),
                                targetOffsetY = { it / 3 }
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
                                    OutlinedTextField(
                                        value = manualInput,
                                        onValueChange = { manualInput = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = glassTextFieldShape(),
                                        colors = glassTextFieldColors(),
                                        placeholder = { Text("输入待办内容...") }
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        GlassButton(
                                            text = "完成",
                                            onClick = onDismiss,
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
                                            enabled = manualInput.isNotBlank(),
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
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = onSearchQueryChange,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = glassTextFieldShape(),
                                        colors = glassTextFieldColors(),
                                        placeholder = { Text("搜索预设...") }
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
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(presetItemBgColor)
                                                        .border(1.dp, presetItemBorderColor, RoundedCornerShape(14.dp))
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
                                                            Icon(
                                                                imageVector = Icons.Filled.Check,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(18.dp),
                                                                tint = secondaryTextColor
                                                            )
                                                        } else {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(16.dp)
                                                                    .border(1.5.dp, secondaryTextColor.copy(alpha = 0.5f), CircleShape)
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

