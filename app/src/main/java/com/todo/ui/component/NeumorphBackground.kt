package com.todo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import com.todo.ui.theme.Neumorph

/**
 * 新拟态背景：**必须是纯色**。
 *
 * 这是新拟态的前提——元素表面色与背景完全相同，靠一对亮/暗阴影"从背景里挤出来"。
 * 原来那层渐变会让每个元素的同色表面与背景对不上，凸凹错觉立刻破功，所以这里不再使用渐变。
 *
 * 注意用的是 [Neumorph.background] 而不是 [Neumorph.surface]：浅色下前者比后者深一档，
 * 于是"页面底"和"卡片面"能分开一点（元素的颜色完全不受影响；深色下两者同色）。
 */
@Composable
fun NeumorphBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Neumorph.background(isDark))
    ) {
        content()
    }
}
