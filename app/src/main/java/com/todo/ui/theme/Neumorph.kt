package com.todo.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 新拟态（Neumorphism）设计令牌。
 *
 * 三条硬规则（来自 mkblog.cn/2081 所讲的思路）：
 * 1. 表面色 = 背景色，元素与背景是**同一种材质**，所以不能有描边；
 * 2. 阴影必须**成对且方向对称**——亮面落左上（光源固定左上）、暗面落右下，两个强度接近；
 * 3. 深度承载状态语义：凸起 = 未选/可点，凹陷 = 选中/输入/进度槽，按下 = 由凸转凹。
 */
object Neumorph {
    // ---- 浅色：采用原文给出的经典配色 ----
    val LightSurface = Color(0xFFECF0F3)
    val LightShadowDark = Color(0xFFD1D9E6)
    val LightShadowLight = Color(0xFFFFFFFF)

    // ---- 深色：原文只给了浅色方案，这里按同一原理推导（底=略亮的深灰，暗影更暗、亮影更亮）----
    val DarkSurface = Color(0xFF23262B)
    val DarkShadowDark = Color(0xFF191B1F)
    val DarkShadowLight = Color(0xFF2E3239)

    fun surface(isDark: Boolean): Color = if (isDark) DarkSurface else LightSurface

    fun shadowDark(isDark: Boolean): Color = if (isDark) DarkShadowDark else LightShadowDark

    fun shadowLight(isDark: Boolean): Color = if (isDark) DarkShadowLight else LightShadowLight
}

/**
 * 阴影的几何与强度。偏移量取元素尺寸的 6%~10%、模糊半径约为偏移量的 1.8~2 倍，
 * 与原文示例（200px 方块 / 18px 偏移 / 30px 模糊）保持同一比例关系。
 */
@Immutable
data class NeumorphElevation(
    val offset: Dp,
    val blur: Dp,
    val darkAlpha: Float = 1f,
    val lightAlpha: Float = 1f
) {
    companion object {
        /** 无阴影：用于"与背景齐平"的平面元素（如未选中的导航项） */
        val None = NeumorphElevation(offset = 0.dp, blur = 0.dp)

        /** 小元素：勾选框、小圆点、chip */
        val Small = NeumorphElevation(offset = 3.dp, blur = 6.dp)

        /** 列表项、按钮、开关 */
        val Medium = NeumorphElevation(offset = 5.dp, blur = 10.dp)

        /** 卡片、面板、输入框 */
        val Large = NeumorphElevation(offset = 7.dp, blur = 14.dp)

        /** 浮层：FAB、吸顶栏、底部面板 */
        val XLarge = NeumorphElevation(offset = 9.dp, blur = 18.dp)
    }
}

/**
 * 新拟态常用圆角：与元素尺寸成比例（原文 200px 方块用 20px，即 10%）。
 */
object NeumorphShapes {
    val Small = 12.dp
    val Medium = 18.dp
    val Large = 24.dp
    val Pill = 999.dp
}
