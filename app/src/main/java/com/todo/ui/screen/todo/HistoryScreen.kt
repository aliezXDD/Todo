package com.todo.ui.screen.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todo.ui.component.EmptyState
import com.todo.ui.component.GlassTopBar
import com.todo.ui.component.NeumorphScrollFade
import com.todo.ui.component.NeumorphScrollFadeHeight
import com.todo.ui.component.neumorphOverlay
import com.todo.ui.screen.todo.component.HistoryDayCard

/**
 * 往日记录。
 *
 * 这里**不再**套一层大卡片：新拟态的表面色与背景完全相同，"外层卡片 + 内层卡片"两层不透明同色
 * 叠在一起时，视觉上会糊成一整块（看起来像被一块纯色遮罩盖住）。现在让每一天自己作为
 * 一张凸起卡片直接落在背景上，边界由各自的光影给出。
 */
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        GlassTopBar(
            title = "往日记录",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            // 与回收站同理：吸顶栏抬到列表之上，它的阴影才不会被列表的渐隐带硬切
            modifier = Modifier.neumorphOverlay(),
            onNavigationClick = onBack,
            overlayBelowContent = true
        )

        Column(
            // 不再叠加纵向间距：列表自己带 16dp 顶部内衬（与渐隐带同高），间距由它提供
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp, bottom = 84.dp)
        ) {
            // 左右的 16dp 移到各子项上：列表需要比卡片更宽的视口，卡片自己内缩同样的 16dp，
            // 这样卡片溢出的光影才不会被滚动视口硬切
            Text(
                text = "最近 7 天",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (uiState.historyRecords.isEmpty()) {
                EmptyState(
                    text = "暂无往日记录",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // 与待办/预设/回收站同样的边界渐隐：卡片滚出列表边界时，溢出的光影会被容器硬切
                NeumorphScrollFade(modifier = Modifier.fillMaxSize()) {
                    // 间距按"阴影扩散范围"给足（偏移 + 模糊的一半 ≈ 12dp），否则相邻卡片的光影互相压盖。
                    // 上下内衬与渐隐带同高，静止时首尾卡片的光影才完整
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = NeumorphScrollFadeHeight,
                            bottom = NeumorphScrollFadeHeight
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.historyRecords, key = { it.date }) { record ->
                            HistoryDayCard(record = record)
                        }
                    }
                }
            }
        }
    }
}
