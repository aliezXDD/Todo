package com.todo.ui.screen.todo.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todo.ui.component.GlassCard
import com.todo.ui.theme.MotionTokens

/**
 * 今日进度（卡片本身是"表面"，仍走新拟态）。
 *
 * 进度环属于**状态指示类**小元素，按要求保持平面设计：不做凹槽/凸起，
 * 立体语言只用于卡片、面板、按钮这类"表面"。
 *
 * [isLoading] 为 true 时显示不定态转圈与"加载中…"，而不是 `0/0 已完成`：
 * 首帧早于数据库首次发射，若直接渲染就会报出一个并不成立的空进度。
 */
@Composable
fun MiniStatsCard(
    completedCount: Int,
    totalCount: Int,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = MotionTokens.Progress, easing = FastOutSlowInEasing),
        label = "todayProgress"
    )
    val isDarkTheme = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val progressTrackColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.30f)
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(4.dp))
            } else {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.padding(4.dp),
                    trackColor = progressTrackColor
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isLoading) "加载中…" else "$completedCount/$totalCount 已完成",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "今日进度",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
