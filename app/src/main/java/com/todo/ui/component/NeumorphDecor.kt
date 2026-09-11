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
 *
 * [innerElevation] 只影响**凹陷**（内阴影）那套几何与强度：像"勾选后按进去的待办条目"这类元素，
 * 需要内阴影的扩散比列表项的默认档位再收一点，而凸起时的外阴影保持不变。
 */
fun Modifier.neumorph(
    shape: Shape,
    isDark: Boolean,
    depth: Float = 1f,
    surface: Color = Neumorph.surface(isDark),
    elevation: NeumorphElevation = NeumorphElevation.Medium,
    innerElevation: NeumorphElevation = elevation
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
        val innerOffsetPx = innerElevation.offset.toPx()
        val innerBlurPx = innerElevation.blur.toPx().coerceAtLeast(0.1f)

        // 亮影（高光）比暗影收一点：暗影负责"沉下去"的体积感，亮影只做点睛——
        // 铺得和暗影一样开，元素边缘会整圈发白、发脏。
        // 只作用于**凸起**的那层外高光；凹陷的内高光与所有暗影都保持原样。
        val highlightScale = 0.75f
        val highlightOffsetPx = offsetPx * highlightScale
        val highlightBlurPx = blurPx * highlightScale

        // 高光（亮影）整体收弱：纯白亮影在浅色底上容易"过曝"，反而比暗影更抢眼。
        // 层级主要交给暗影与底色差异，亮影只做点睛；深色底上需要收得更狠。
        //
        // 凸起（画在形状之外）与凹陷（画在形状之内）分开给强度：
        // 深色底本来就暗，凹陷再叠一层 0.95 的暗影会糊成一个"黑洞"（凹槽、滑轨、按进去的条目
        // 全都发死），所以深色下的**凹陷暗影单独收弱**；浅色下两者一致，凸起的外阴影也完全不受影响。
        val convexDark = elevation.darkAlpha * if (isDark) 0.95f else 1f
        val convexLight = elevation.lightAlpha * if (isDark) 0.42f else 0.68f
        val concaveDark = innerElevation.darkAlpha * if (isDark) 0.5f else 1f
        val concaveLight = innerElevation.lightAlpha * if (isDark) 0.42f else 0.68f

        val darkPaint = shadowPaint(Neumorph.shadowDark(isDark).toArgb(), blurPx)
        // 凸起的外高光用收窄后的模糊半径（BlurMaskFilter 建在画笔上，所以必须单独一支）
        val lightPaint = shadowPaint(Neumorph.shadowLight(isDark).toArgb(), highlightBlurPx)
        // 凹陷用自己的一套画笔（模糊半径不同，而 BlurMaskFilter 是建在画笔上的）
        val innerDarkPaint = if (clampedDepth < 1f) {
            shadowPaint(Neumorph.shadowDark(isDark).toArgb(), innerBlurPx)
        } else {
            darkPaint
        }
        val innerLightPaint = if (clampedDepth < 1f) {
            shadowPaint(Neumorph.shadowLight(isDark).toArgb(), innerBlurPx)
        } else {
            lightPaint
        }

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
                darkPaint.alpha = alphaOf(convexDark * outerScale)
                lightPaint.alpha = alphaOf(convexLight * outerScale)
                drawIntoCanvas { canvas ->
                    val native = canvas.nativeCanvas
                    native.save()
                    native.translate(offsetPx, offsetPx)
                    native.drawPath(androidPath, darkPaint)
                    native.restore()
                    native.save()
                    native.translate(-highlightOffsetPx, -highlightOffsetPx)
                    native.drawPath(androidPath, lightPaint)
                    native.restore()
                }
            }

            // 与背景同色的表面：元素因此没有边界线
            drawPath(path, surface)

            if (innerScale > 0.01f && inversePath != null) {
                innerDarkPaint.alpha = alphaOf(concaveDark * innerScale)
                innerLightPaint.alpha = alphaOf(concaveLight * innerScale)
                clipPath(path) {
                    drawIntoCanvas { canvas ->
                        val native = canvas.nativeCanvas
                        native.save()
                        native.translate(innerOffsetPx, innerOffsetPx)
                        native.drawPath(inversePath, innerDarkPaint)
                        native.restore()
                        native.save()
                        native.translate(-innerOffsetPx, -innerOffsetPx)
                        native.drawPath(inversePath, innerLightPaint)
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
 * 适用于按钮、FAB、卡片等可点"表面"。[restDepth] 是没按下时的深度（默认完全凸起）。
 */
@Composable
fun Modifier.neumorphPress(
    shape: Shape,
    isDark: Boolean,
    pressed: Boolean,
    surface: Color = Neumorph.surface(isDark),
    elevation: NeumorphElevation = NeumorphElevation.Medium,
    restDepth: Float = 1f
): Modifier {
    val depth by animateFloatAsState(
        targetValue = if (pressed) 0f else restDepth,
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
