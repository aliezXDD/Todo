package com.todo.ui.component

import android.graphics.BlurMaskFilter
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
fun Modifier.glassOverlay(
    shape: Shape,
    isDark: Boolean,
    topAlphaLight: Float,
    topAlphaDark: Float,
    bottomAlphaLight: Float,
    bottomAlphaDark: Float,
    drawOuterShadow: Boolean = true,
    inset: Dp = 0.dp
): Modifier = drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        val insetPx = inset.toPx().coerceAtLeast(0f)
        val mask = when (outline) {
            is Outline.Generic -> outline.path
            is Outline.Rounded -> {
                val rr = outline.roundRect
                val left = (rr.left + insetPx).coerceAtMost(rr.right)
                val top = (rr.top + insetPx).coerceAtMost(rr.bottom)
                val right = (rr.right - insetPx).coerceAtLeast(left)
                val bottom = (rr.bottom - insetPx).coerceAtLeast(top)
                val corner = CornerRadius(
                    x = (rr.topLeftCornerRadius.x - insetPx).coerceAtLeast(0f),
                    y = (rr.topLeftCornerRadius.y - insetPx).coerceAtLeast(0f)
                )
                Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(left, top, right, bottom),
                            topLeft = corner,
                            topRight = CornerRadius(
                                x = (rr.topRightCornerRadius.x - insetPx).coerceAtLeast(0f),
                                y = (rr.topRightCornerRadius.y - insetPx).coerceAtLeast(0f)
                            ),
                            bottomRight = CornerRadius(
                                x = (rr.bottomRightCornerRadius.x - insetPx).coerceAtLeast(0f),
                                y = (rr.bottomRightCornerRadius.y - insetPx).coerceAtLeast(0f)
                            ),
                            bottomLeft = CornerRadius(
                                x = (rr.bottomLeftCornerRadius.x - insetPx).coerceAtLeast(0f),
                                y = (rr.bottomLeftCornerRadius.y - insetPx).coerceAtLeast(0f)
                            )
                        )
                    )
                }
            }
            is Outline.Rectangle -> Path().apply {
                val left = insetPx.coerceAtMost(outline.rect.right)
                val top = insetPx.coerceAtMost(outline.rect.bottom)
                val right = (outline.rect.right - insetPx).coerceAtLeast(left)
                val bottom = (outline.rect.bottom - insetPx).coerceAtLeast(top)
                addRect(Rect(left, top, right, bottom))
            }
            else -> Path().apply { addRect(outline.bounds) }
        }

        val shadowAlpha = if (isDark) 0.18f else 0.12f
        val shadowBlur = if (isDark) 18.dp.toPx() else 22.dp.toPx()
        val shadowOffsetY = if (isDark) 2.dp.toPx() else 3.dp.toPx()
        val shadowPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
            color = Color.Black.copy(alpha = shadowAlpha).toArgb()
            maskFilter = BlurMaskFilter(shadowBlur, BlurMaskFilter.Blur.NORMAL)
        }
        val topSweep = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) topAlphaDark * 0.54f else topAlphaLight * 0.70f),
                Color.Transparent
            ),
            start = Offset(0f, 0f),
            end = Offset(size.width * 0.75f, size.height * 0.32f)
        )
        val rim = Brush.horizontalGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.025f else 0.050f),
                Color.Transparent,
                Color.Transparent,
                Color.White.copy(alpha = if (isDark) 0.014f else 0.032f)
            )
        )
        val topLine = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.058f else 0.126f),
                Color.Transparent
            ),
            startY = 0f,
            endY = size.height * 0.07f
        )
        val bottomSweep = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = if (isDark) bottomAlphaDark * 0.324f else bottomAlphaLight * 0.43f)
            ),
            startY = size.height * 0.73f,
            endY = size.height
        )

        onDrawWithContent {
            if (drawOuterShadow) {
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.save()
                    canvas.nativeCanvas.translate(0f, shadowOffsetY)
                    canvas.nativeCanvas.drawPath(mask.asAndroidPath(), shadowPaint)
                    canvas.nativeCanvas.restore()
                }
            }

            drawContent()

            clipPath(mask) {
                drawRect(brush = topSweep)
                drawRect(brush = rim)
                drawRect(brush = topLine)
                drawRect(brush = bottomSweep)
            }
        }
    }
