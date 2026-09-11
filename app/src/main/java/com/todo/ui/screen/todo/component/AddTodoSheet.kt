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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.todo.domain.model.Preset
import com.todo.ui.component.GlassBottomSheet
import com.todo.ui.component.GlassButton
import com.todo.ui.component.GlassCard
import com.todo.ui.component.NeumorphTextField
import com.todo.ui.component.neumorph
import com.todo.ui.theme.MotionTokens
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes
import kotlinx.coroutines.delay

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

    // 点 + 打开面板后直接弹出键盘（与「编辑待办」同一做法：等 sheet 的节点挂上再请求焦点）。
    // 切回"手动输入"这一档时也会重新拉起键盘。
    val manualInputFocus = remember { FocusRequester() }
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 0) {
            delay(80)
            manualInputFocus.requestFocus()
        }
    }

    GlassBottomSheet(
        onDismissRequest = onDismiss
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            // 卡片只当布局容器用：**不画任何光影**。
            // 面板外围那一圈"深色边框"就是这张卡片自己的暗影——它与按钮溢出的光影在同一处相遇，
            // 于是按钮的影子看起来被那圈深色框挡掉了。面板本身就是那块"表面"（GlassBottomSheet
            // 与页面同色），内容直接落在它上面即可，不需要再套一圈有阴影的框。
            elevation = NeumorphElevation.None
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 与「编辑待办」面板同一套排布：标题 → 内容 → 两颗动作按钮
                Text(
                    text = "添加待办",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // 二选一 = 两颗普通 GlassButton（与下面「完成/添加」完全同一套样式），
                // 只用深度区分状态：当前选中的那颗凹着（凹陷 = 已选中），另一颗凸起
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        text = "手动输入",
                        onClick = { selectedTabIndex = 0 },
                        glassSurface = true,
                        recessed = selectedTabIndex == 0,
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "从预设选择",
                        onClick = { selectedTabIndex = 1 },
                        glassSurface = true,
                        recessed = selectedTabIndex == 1,
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
                                // 手动输入不再有「完成/添加」按钮：面板一打开就带起键盘，
                                // 输入不为空时按键盘上的"完成"（回车）即添加，添加后面板与键盘都留着，
                                // 方便连着录好几条；要收起就下滑面板或点空白处
                                NeumorphTextField(
                                    value = manualInput,
                                    onValueChange = { manualInput = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(manualInputFocus),
                                    placeholder = "输入待办内容...",
                                    singleLine = true,
                                    imeAction = ImeAction.Done,
                                    onImeAction = {
                                        if (manualInput.isNotBlank()) {
                                            onAddManual(manualInput)
                                            manualInput = ""
                                        }
                                    }
                                )
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
