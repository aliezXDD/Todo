package com.todo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.Neumorph

/**
 * 渐隐带高度。
 *
 * 列表内容的上下内衬（LazyColumn 的 `contentPadding`、普通滚动的 `padding`）**应当 ≥ 这个值**：
 * 这样静止时首/尾条目正好落在渐变完全透明的那一端，不会被无缘无故淡化，而滚动越过边界的条目
 * 才在这条带子里逐渐消失。
 */
val NeumorphScrollFadeHeight: Dp = 16.dp

/**
 * 列表上下缘的渐隐。
 *
 * **为什么需要**：条目靠成对阴影"从背景里凸起"，阴影必然溢出条目自身的矩形；而滚动容器只能把内容
 * 裁到自己的矩形边界上，于是溢出的那部分光影被**硬切**——在边界处留下一条突然消失的直线。
 * 这里在容器上下缘各铺一条与背景同色的竖直渐变，把那道切边盖住：新拟态的表面色本来就等于背景色，
 * 所以同色渐变等价于"真的把内容淡化下去"，既不引入第二种颜色，也不需要混合模式或离屏图层。
 *
 * **用它就不要再用"能滚才显示"的开关式渐隐**：那种做法在滚到端点时会突然出现/消失（也是一种硬切），
 * 而这里的两条带子常驻，静止时它们只覆盖内衬区域（与背景同色，什么也看不见）。
 *
 * [fadeColor] 必须和列表**背后的那一层**一致：列表直接落在页面上（预设/回收站/往日记录）用默认的
 * [Neumorph.background]；列表若在一个卡片/面板内部（今日），要传 [Neumorph.surface]。
 */
@Composable
fun NeumorphScrollFade(
    modifier: Modifier = Modifier,
    fadeColor: Color = Neumorph.background(MaterialTheme.colorScheme.onSurface.luminance() > 0.7f),
    content: @Composable () -> Unit
) {
    val clear = fadeColor.copy(alpha = 0f)

    Box(modifier = modifier) {
        content()

        // 上缘：靠边完全不透明（盖住切边），向内渐变到透明（对准内容区的起点）
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(NeumorphScrollFadeHeight)
                .background(Brush.verticalGradient(colors = listOf(fadeColor, clear)))
        )
        // 下缘：方向相反
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(NeumorphScrollFadeHeight)
                .background(Brush.verticalGradient(colors = listOf(clear, fadeColor)))
        )
    }
}
