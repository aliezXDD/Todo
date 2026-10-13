package com.todo.ui.screen.todo.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.todo.ui.component.DialogActionTextButton
import com.todo.ui.component.DialogButtonRole
import com.todo.ui.component.NeumorphDialog
import com.todo.ui.theme.NeumorphShapes
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** 选中的原图在进内存前的长边上限：再大的图对一张 52dp 的头像也没有意义，只会吃内存。 */
private const val MaxSourceDimension = 2048

/** 裁剪结果的边长。存盘后按 52dp 显示，512 在 3x 屏上仍有近 3 倍余量。 */
private const val OutputSize = 512

private const val MinZoom = 1f
private const val MaxZoom = 4f

/**
 * 读取用户从相册选中的图片。
 *
 * 用 [ImageDecoder] 而不是 `BitmapFactory`，关键原因是它会**自动应用 EXIF 旋转**：相机竖拍的照片
 * 在 `BitmapFactory` 下会躺着进来，裁剪框里看到的就是横的。它同时按长边上限降采样，避免一张
 * 几千万像素的原图直接把内存打满。
 *
 * 分配器必须指定为软件位图：后续要按区域裁剪（`Bitmap.createBitmap(region)`）并压缩存盘，
 * 这两件事硬件位图都做不了。
 */
fun decodePickedImage(
    context: Context,
    uri: Uri,
    maxDimension: Int = MaxSourceDimension
): ImageBitmap? = runCatching {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        decoder.isMutableRequired = false
        val longest = max(info.size.width, info.size.height)
        if (longest > maxDimension) {
            var sample = 1
            while (longest / (sample * 2) >= maxDimension) sample *= 2
            decoder.setTargetSampleSize(sample)
        }
    }
    bitmap.asImageBitmap()
}.getOrNull()

/**
 * 正方形裁剪框：中间一个正方形视口，图片先按"铺满"缩放，用户可以拖动平移、双指或滑杆缩放，
 * 框内所见即最终头像。
 *
 * 为什么不是"自动居中裁"：头像的取景（人物在画面的哪一侧、要留多少背景）只有用户自己知道，
 * 居中裁经常把主体切掉一半。
 *
 * 缩放与位移用 [mutableFloatStateOf] / [mutableStateOf] 的**快照对象**而不是普通变量：
 * [pointerInput] 的手势回调只在重建时捕获一次外部变量，读 `.floatValue` / `.value` 才能拿到此刻
 * 的值。若捕获的是值本身，回调会永远停在首次组合时的那个快照上（拖一次就"卡住"）。
 */
@Composable
fun AvatarCropDialog(
    source: ImageBitmap,
    onDismiss: () -> Unit,
    onConfirm: (Bitmap) -> Unit
) {
    val zoomState = remember(source) { mutableFloatStateOf(MinZoom) }
    val offsetState = remember(source) { mutableStateOf(Offset.Zero) }

    NeumorphDialog(visible = true, onDismiss = onDismiss) {
        Text(text = "裁剪头像", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "拖动调整位置，双指或下面的滑杆缩放，框内就是最终效果。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val shape = RoundedCornerShape(NeumorphShapes.Corner)
            // 面板内可用的宽度就是视口边长（正方形）
            val viewport = maxWidth
            val viewportPx = with(LocalDensity.current) { viewport.toPx() }
            // 铺满视口所需的基准缩放：图片在任何缩放档位下都不会露出视口以外的空白
            val baseScale = max(viewportPx / source.width, viewportPx / source.height)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(viewport)
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                        .pointerInput(source, baseScale) {
                            detectTransformGestures { _, pan, gestureZoom, _ ->
                                val previous = zoomState.floatValue
                                val next = (previous * gestureZoom).coerceIn(MinZoom, MaxZoom)
                                zoomState.floatValue = next
                                // 以视口中心为锚点：图片中心的位移按缩放比例放大，再叠加本次拖动
                                offsetState.value = clampOffset(
                                    offset = offsetState.value * (next / previous) + pan,
                                    zoom = next,
                                    baseScale = baseScale,
                                    viewportPx = viewportPx,
                                    source = source
                                )
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val drawnWidth = source.width * baseScale * zoomState.floatValue
                        val drawnHeight = source.height * baseScale * zoomState.floatValue
                        val left = (size.width - drawnWidth) / 2f + offsetState.value.x
                        val top = (size.height - drawnHeight) / 2f + offsetState.value.y
                        drawImage(
                            image = source,
                            srcOffset = IntOffset.Zero,
                            srcSize = IntSize(source.width, source.height),
                            dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                            dstSize = IntSize(
                                drawnWidth.roundToInt().coerceAtLeast(1),
                                drawnHeight.roundToInt().coerceAtLeast(1)
                            ),
                            filterQuality = FilterQuality.High
                        )
                    }
                }

                Slider(
                    value = zoomState.floatValue,
                    onValueChange = { value ->
                        val previous = zoomState.floatValue
                        zoomState.floatValue = value
                        offsetState.value = clampOffset(
                            offset = offsetState.value * (value / previous),
                            zoom = value,
                            baseScale = baseScale,
                            viewportPx = viewportPx,
                            source = source
                        )
                    },
                    valueRange = MinZoom..MaxZoom
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    DialogActionTextButton(
                        text = "取消",
                        role = DialogButtonRole.SECONDARY,
                        onClick = onDismiss
                    )
                    DialogActionTextButton(
                        text = "确定",
                        role = DialogButtonRole.PRIMARY,
                        onClick = {
                            cropSquare(
                                source = source,
                                zoom = zoomState.floatValue,
                                offset = offsetState.value,
                                baseScale = baseScale,
                                viewportPx = viewportPx
                            )?.let(onConfirm)
                        }
                    )
                }
            }
        }
    }
}

/**
 * 把位移夹在"图片始终盖满视口"的范围内。
 *
 * 图片中心相对视口中心最多只能偏出 `(图片边长 - 视口边长) / 2`，再多就会从视口边缘露出空白。
 * 某个方向上图片本来就与视口等长（细长图）时上限为 0 —— 那一维锁死，这也正是期望的行为。
 */
private fun clampOffset(
    offset: Offset,
    zoom: Float,
    baseScale: Float,
    viewportPx: Float,
    source: ImageBitmap
): Offset {
    val drawnWidth = source.width * baseScale * zoom
    val drawnHeight = source.height * baseScale * zoom
    val maxX = max(0f, (drawnWidth - viewportPx) / 2f)
    val maxY = max(0f, (drawnHeight - viewportPx) / 2f)
    return Offset(
        x = offset.x.coerceIn(-maxX, maxX),
        y = offset.y.coerceIn(-maxY, maxY)
    )
}

/**
 * 按当前位移/缩放把视口对应的那块原图裁出来，并缩放到 [OutputSize]。
 *
 * 坐标换算是上面绘制的逆运算：绘制时原图的一个像素被放大了 `drawnWidth / source.width` 倍，
 * 所以视口里的 `viewportPx` 对应原图 `viewportPx * source.width / drawnWidth` 个像素。
 * 取整后的误差最多一个像素，再夹一次边界，避免越界。
 */
private fun cropSquare(
    source: ImageBitmap,
    zoom: Float,
    offset: Offset,
    baseScale: Float,
    viewportPx: Float
): Bitmap? {
    if (viewportPx <= 0f || source.width <= 0 || source.height <= 0) return null

    val drawnWidth = source.width * baseScale * zoom
    val drawnHeight = source.height * baseScale * zoom
    if (drawnWidth <= 0f || drawnHeight <= 0f) return null

    val left = (viewportPx - drawnWidth) / 2f + offset.x
    val top = (viewportPx - drawnHeight) / 2f + offset.y
    val factor = source.width / drawnWidth

    val x = (-left * factor).roundToInt().coerceIn(0, (source.width - 1).coerceAtLeast(0))
    val y = (-top * factor).roundToInt().coerceIn(0, (source.height - 1).coerceAtLeast(0))
    val side = (viewportPx * factor).roundToInt()
        .coerceIn(1, min(source.width - x, source.height - y))

    val region = Bitmap.createBitmap(source.asAndroidBitmap(), x, y, side, side)
    return Bitmap.createScaledBitmap(region, OutputSize, OutputSize, true)
}
