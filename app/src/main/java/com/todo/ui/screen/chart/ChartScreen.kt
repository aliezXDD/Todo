package com.todo.ui.screen.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todo.domain.model.DailyStats
import com.todo.ui.component.EmptyState
import com.todo.ui.component.GlassCard
import com.todo.ui.component.GlassTopBar
import com.todo.ui.component.neumorphOverlay
import com.todo.ui.theme.NeumorphShapes
import com.todo.ui.theme.RateHigh
import com.todo.ui.theme.RateLow
import com.todo.ui.theme.RateMid

@Composable
fun ChartScreen(
    onBack: () -> Unit,
    viewModel: ChartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        GlassTopBar(
            title = "完成统计",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            // 与回收站/往日记录同理：吸顶栏抬到内容之上，它的阴影才不会被下方卡片的不透明填充硬切
            modifier = Modifier.neumorphOverlay(),
            onNavigationClick = onBack,
            overlayBelowContent = true,
            actions = {
                // 与顶栏同形（默认 TextButton 是胶囊水波纹）
                TextButton(
                    onClick = viewModel::toggleChartType,
                    shape = RoundedCornerShape(NeumorphShapes.Corner)
                ) {
                    Text(if (uiState.chartType == ChartViewModel.ChartType.BAR) "折线图" else "柱状图")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 84.dp)
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                if (!uiState.hasData) {
                    EmptyState(text = "暂无统计数据", modifier = Modifier.fillMaxSize())
                } else {
                    StatsChart(
                        stats = uiState.stats,
                        chartType = uiState.chartType,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard("平均完成率", "${uiState.averageRate}%", Modifier.weight(1f))
                StatCard("累计完成", "${uiState.totalCompleted}", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard("连续全部完成", "${uiState.consecutiveDays} 天", Modifier.weight(1f))
                StatCard("累计全部完成", "${uiState.fullCompletionDays} 天", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StatsChart(
    stats: List<DailyStats>,
    chartType: ChartViewModel.ChartType,
    modifier: Modifier = Modifier
) {
    val sorted = remember(stats) { stats.sortedBy { it.date } }
    val values = remember(sorted) {
        sorted.map { (it.completionRate * 100f).coerceIn(0f, 100f) }
    }
    val xLabels = remember(sorted) {
        sorted.mapIndexedNotNull { index, daily ->
            if (index % 5 == 0 || index == sorted.lastIndex) {
                val parts = daily.date.split("-")
                val month = parts.getOrNull(1)?.toIntOrNull()
                val day = parts.getOrNull(2)?.toIntOrNull()
                if (month != null && day != null) "$month/$day" else null
            } else {
                null
            }
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            YAxisLabels()
            Spacer(modifier = Modifier.width(8.dp))
            when (chartType) {
                ChartViewModel.ChartType.BAR -> BarChart(values = values, modifier = Modifier.weight(1f))
                ChartViewModel.ChartType.LINE -> LineChart(values = values, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            xLabels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun YAxisLabels() {
    Column(
        modifier = Modifier
            .width(32.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End
    ) {
        listOf("100%", "75%", "50%", "25%", "0%").forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun BarChart(
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEach { value ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight((value / 100f).coerceIn(0f, 1f))
                        .background(
                            color = valueToRateColor(value / 100f),
                            // 柱顶圆角与全局唯一圆角一致（窄柱会被自动收敛为半圆顶）
                            shape = RoundedCornerShape(
                                topStart = NeumorphShapes.Corner,
                                topEnd = NeumorphShapes.Corner
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun LineChart(
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Canvas(modifier = modifier.fillMaxSize()) {
        if (values.isEmpty()) return@Canvas

        val height = size.height
        val width = size.width

        repeat(5) { index ->
            val y = height * (index / 4f)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        val stepX = if (values.size > 1) width / (values.size - 1) else width
        val path = Path()
        val points = values.mapIndexed { index, value ->
            val x = index * stepX
            val y = height - (value / 100f).coerceIn(0f, 1f) * height
            Offset(x, y)
        }

        points.forEachIndexed { index, point ->
            if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        points.forEach { point ->
            drawCircle(color = lineColor, radius = 5f, center = point)
        }
    }
}

private fun valueToRateColor(rate: Float): Color {
    val clamped = rate.coerceIn(0f, 1f)
    // 数据语义色统一放在 Color.kt，不再散落在屏幕代码里
    return if (clamped < 0.5f) {
        lerp(RateLow, RateMid, clamped / 0.5f)
    } else {
        lerp(RateMid, RateHigh, (clamped - 0.5f) / 0.5f)
    }
}
