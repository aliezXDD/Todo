package com.todo.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes
import com.todo.ui.theme.dropOnly

/**
 * 新拟态列表项：默认凸起；[depth] 传 0 表示"被按进去"（用于已完成/选中态）。
 * [elevation] 传零阴影（offset/blur 均为 0）可得到"与背景齐平"的平面行。
 *
 * [shape] 与 [minHeight] 是**所有列表条目的统一规格**（待办、预设、回收站）：
 * 调用方一般不要再各自指定，否则同样的圆角放在不同行高上会显得一个更圆一个更方。
 *
 * **点击反馈的形状也由这里统一保证**：波纹画在表面填充**之上**，并被 Surface 裁到 [shape] 之内，
 * 所以波纹与条目永远同形。调用方因此**不要**自己挂 `Modifier.clickable` /
 * `combinedClickable` / `indication`（挂在 neumorph 之外会被同色填充盖住，只从四个圆角漏出
 * 方形亮块），直接把 [onClick] / [onLongClick] / [onDoubleClick] 传进来即可。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassListItem(
    modifier: Modifier = Modifier,
    depth: Float = 1f,
    elevation: NeumorphElevation = NeumorphElevation.Medium,
    shape: Shape = RoundedCornerShape(NeumorphShapes.Medium),
    minHeight: Int = 52,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    dropOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    // 二级界面的条目只留投影（不带左上高光），见 NeumorphElevation.dropOnly
    val effectiveElevation = if (dropOnly) elevation.dropOnly(isDark) else elevation
    val itemModifier = modifier.neumorph(
        shape = shape,
        isDark = isDark,
        depth = depth,
        elevation = effectiveElevation
    )

    // 点击层挂在内容行上，而不是挂在 neumorph 之外的调用方 modifier 上：
    // 这样它在"填充之后"绘制（波纹不会被同色填充盖住），又落在 Surface 的 .clip(shape) 之内。
    val hasInteraction = onClick != null || onLongClick != null || onDoubleClick != null
    val interactionSource = remember { MutableInteractionSource() }
    val interactionModifier = when {
        !hasInteraction -> Modifier
        onLongClick != null || onDoubleClick != null -> Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            role = Role.Button,
            onLongClick = onLongClick,
            onDoubleClick = onDoubleClick,
            onClick = { onClick?.invoke() }
        )
        else -> Modifier.clickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            role = Role.Button,
            onClick = { onClick?.invoke() }
        )
    }

    Surface(
        modifier = itemModifier,
        shape = shape,
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = minHeight.dp)
                .then(interactionModifier)
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}
