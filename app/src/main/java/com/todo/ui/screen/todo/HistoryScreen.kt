package com.todo.ui.screen.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
            onNavigationClick = onBack,
            overlayBelowContent = true
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 84.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "最近 7 天",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (uiState.historyRecords.isEmpty()) {
                EmptyState(
                    text = "暂无往日记录",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // 间距按"阴影扩散范围"给足（偏移 + 模糊的一半 ≈ 12dp），否则相邻卡片的光影互相压盖
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
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
