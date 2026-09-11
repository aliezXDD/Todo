package com.todo.ui.component

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.zIndex
import com.todo.ui.theme.MotionTokens
import com.todo.ui.theme.Neumorph
import com.todo.ui.theme.NeumorphElevation

/**
 * 新拟态的「凸 / 凹」修饰符。
 *
 * [depth] = 1 完全凸起（外阴影在形状之外：暗影落右下、亮影落左上）；
 * [depth] = 0 完全凹陷（内阴影落在形状内部：暗影在左上、亮影在右下，与凸起相反）；
 * 中间值用于按下/勾选时的连续形变，因此「凸→凹」是一次真实的挤压过渡，而不是两个状态硬切。
 *
 * 约定：**表面色由本修饰符填充**，调用方把自己的 Surface/Box 底色设为 [Color.Transparent]，
 * 这样凹陷的内阴影才能落在「同色底之上、文字内容之下」（这是新拟态的关键层级）。
 */
fun Modifier.neumorph(
    shape: Shape,
    isDark: Boolean,
    depth: Float = 1f,
    surface: Color = Neumorph.surface(isDark),
    elevation: NeumorphElevation = NeumorphElevation.Medium
): Modifier {
    val clampedDepth = depth.coerceIn(0f, 1f)
    return drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        val path = when (outline) {
            is Outline.Generic -> outline.path
            is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
            is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
        }
        val androidPath = path.asAndroidPath()
        val offsetPx = elevation.offset.toPx()
        val blurPx = elevation.blur.toPx().coerceAtLeast(0.1f)

        // 高光（亮影）整体收弱：纯白亮影在浅色底上容易"过曝"，反而比暗影更抢眼。
        // 层级主要交给暗影与底色差异，亮影只做点睛；深色底上需要收得更狠。
        val darkBase = elevation.darkAlpha * if (isDark) 0.95f else 1f
        val lightBase = elevation.lightAlpha * if (isDark) 0.42f else 0.68f

        val darkPaint = shadowPaint(Neumorph.shadowDark(isDark).toArgb(), blurPx)
        val lightPaint = shadowPaint(Neumorph.shadowLight(isDark).toArgb(), blurPx)

        // 凹陷的阴影源：把「形状以外」的区域偏移后画进形状内部，等价于 CSS 的 inset box-shadow。
        val inversePath = if (clampedDepth < 1f) {
            val cover = Path().apply {
                addRect(Rect(-size.width, -size.height, size.width * 2f, size.height * 2f))
            }
            Path().apply { op(cover, path, PathOperation.Difference) }.asAndroidPath()
        } else {
            null
        }

        onDrawWithContent {
            val outerScale = clampedDepth
            val innerScale = 1f - clampedDepth

            if (outerScale > 0.01f) {
                darkPaint.alpha = alphaOf(darkBase * outerScale)
                lightPaint.alpha = alphaOf(lightBase * outerScale)
                drawIntoCanvas { canvas ->
                    val native = canvas.nativeCanvas
                    native.save()
                    native.translate(offsetPx, offsetPx)
                    native.drawPath(androidPath, darkPaint)
                    native.restore()
                    native.save()
                    native.translate(-offsetPx, -offsetPx)
                    native.drawPath(androidPath, lightPaint)
                    native.restore()
                }
            }

            // 与背景同色的表面：元素因此没有边界线
            drawPath(path, surface)

            if (innerScale > 0.01f && inversePath != null) {
                darkPaint.alpha = alphaOf(darkBase * innerScale)
                lightPaint.alpha = alphaOf(lightBase * innerScale)
                clipPath(path) {
                    drawIntoCanvas { canvas ->
                        val native = canvas.nativeCanvas
                        native.save()
                        native.translate(offsetPx, offsetPx)
                        native.drawPath(inversePath, darkPaint)
                        native.restore()
                        native.save()
                        native.translate(-offsetPx, -offsetPx)
                        native.drawPath(inversePath, lightPaint)
                        native.restore()
                    }
                }
            }

            drawContent()
        }
    }
}

/**
 * 把元素抬到同级内容之上：用于"浮在滚动内容之上"的吸顶栏这类元素。
 *
 * **为什么需要**：同级元素按声明顺序绘制，后声明的在上。滚动列表上下缘那条与背景同色的渐隐带
 * （以及次级条目自身的不透明同色填充）会盖住声明在它之前的元素；吸顶栏溢出的光影一旦落进列表范围，
 * 就会被盖掉——看起来就是栏的阴影被一条直线硬切。抬到更高图层后，栏的阴影自然落在列表内容之上。
 */
fun Modifier.neumorphOverlay(): Modifier = this.zIndex(1f)

/**
 * 带按压形变的版本：按下时由凸转凹（新拟态最具标志性的交互）。
 * 适用于按钮、FAB、列表项等可点元素。
 */
@Composable
fun Modifier.neumorphPress(
    shape: Shape,
    isDark: Boolean,
    pressed: Boolean,
    surface: Color = Neumorph.surface(isDark),
    elevation: NeumorphElevation = NeumorphElevation.Medium
): Modifier {
    val depth by animateFloatAsState(
        targetValue = if (pressed) 0f else 1f,
        animationSpec = tween(durationMillis = MotionTokens.ItemState, easing = MotionTokens.StandardEasing),
        label = "neumorphDepth"
    )
    return this.neumorph(
        shape = shape,
        isDark = isDark,
        depth = depth,
        surface = surface,
        elevation = elevation
    )
}

private fun shadowPaint(color: Int, blurPx: Float) = android.graphics.Paint().apply {
    isAntiAlias = true
    style = android.graphics.Paint.Style.FILL
    this.color = color
    alpha = 255
    maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
}

private fun alphaOf(value: Float): Int = (255f * value).coerceIn(0f, 255f).toInt()
