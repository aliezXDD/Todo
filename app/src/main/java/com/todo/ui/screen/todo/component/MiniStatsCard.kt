package com.todo.ui.screen.todo.component

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.todo.ui.component.GlassCard
import com.todo.ui.theme.Neumorph

/**
 * 今日进度：新拟态的「凹陷圆盘 + 抬起的进度弧」。
 *
 * 凹槽沿用与 [com.todo.ui.component.neumorph] 同一套原理（一对偏移模糊圆 + 圆盘裁剪），
 * 只是这里必须画在 Canvas 上——Material 的 CircularProgressIndicator 是平面的，无法表达凹陷。
 */
@Composable
fun MiniStatsCard(
    completedCount: Int,
    totalCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "todayProgress"
    )
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProgressDial(
                progress = animatedProgress,
                isDark = isDark,
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${completedCount}/${totalCount} 已完成",
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

@Composable
private fun ProgressDial(
    progress: Float,
    isDark: Boolean,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val recessed = Neumorph.recessedSurface(isDark)
    val shadowDark = Neumorph.shadowDark(isDark).toArgb()
    val shadowLight = Neumorph.shadowLight(isDark).toArgb()
    val accentArgb = accent.toArgb()

    Canvas(modifier = modifier) {
        val band = size.minDimension * 0.17f
        val radius = size.minDimension / 2f - band / 2f - 1f
        val center = Offset(size.width / 2f, size.height / 2f)
        val shift = radius * 0.11f

        // ① 凹陷圆槽：同色底 → 裁剪到圆内 → 一对偏移模糊圆（左上暗、右下亮）
        drawCircle(color = recessed, radius = radius, center = center)
        clipPath(Path().apply { addOval(Rect(center, radius)) }) {
            drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    style = android.graphics.Paint.Style.FILL
                    maskFilter = BlurMaskFilter(shift * 1.7f, BlurMaskFilter.Blur.NORMAL)
                }
                paint.color = shadowDark
                paint.alpha = 240
                native.save()
                native.translate(shift, shift)
                native.drawCircle(center.x, center.y, radius, paint)
                native.restore()
                paint.color = shadowLight
                paint.alpha = if (isDark) 150 else 255
                native.save()
                native.translate(-shift, -shift)
                native.drawCircle(center.x, center.y, radius, paint)
                native.restore()
            }
        }

        // ② 进度弧：先铺一层模糊光晕再画实弧，让圆环读起来是"从凹槽里抬起来的"
        if (progress > 0.001f) {
            drawIntoCanvas { canvas ->
                val glow = android.graphics.Paint().apply {
                    isAntiAlias = true
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = band
                    strokeCap = android.graphics.Paint.Cap.ROUND
                    color = accentArgb
                    alpha = 80
                    maskFilter = BlurMaskFilter(band * 0.55f, BlurMaskFilter.Blur.NORMAL)
                }
                canvas.nativeCanvas.drawArc(
                    center.x - radius,
                    center.y - radius,
                    center.x + radius,
                    center.y + radius,
                    -90f,
                    360f * progress,
                    false,
                    glow
                )
            }
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = band, cap = StrokeCap.Round)
            )
        }
    }
}
