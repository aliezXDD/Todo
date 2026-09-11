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
    // ---- 浅色：背景沿用原文的 #ECF0F3，但阴影整体加深一档（原 #D1D9E6 在真机上偏弱）----
    val LightSurface = Color(0xFFECF0F3)
    /** 浅色下"最底层页面底色"：比 [LightSurface] 深两档（元素本身不变） */
    val LightBackground = Color(0xFFD9DEE2)
    val LightShadowDark = Color(0xFFBFCBDE)
    val LightShadowLight = Color(0xFFFFFFFF)
    val LightRecessed = Color(0xFFDCE2E9)

    // ---- 深色：原文只给了浅色方案，这里按同一原理推导（暗影更深、亮影更亮，保证对比可见）----
    val DarkSurface = Color(0xFF23262B)
    /** 深色下"最底层页面底色"：比 [DarkSurface] 亮一点点（元素本身不变） */
    val DarkBackground = Color(0xFF282B30)
    val DarkShadowDark = Color(0xFF111316)
    val DarkShadowLight = Color(0xFF373D46)
    val DarkRecessed = Color(0xFF191C20)

    fun surface(isDark: Boolean): Color = if (isDark) DarkSurface else LightSurface

    /**
     * **最底层页面底色**：只有它背后的那一大片空处用它，卡片、条目、按钮、浮层等全部仍用 [surface]。
     *
     * 两个主题各比自己的表面再亮/再暗一档，让"页面底"与"卡片面"分开一点：
     * 浅色下更深、深色下稍亮（深色里卡片因此比底略暗，靠暗影和亮影撑起层次）。
     */
    fun background(isDark: Boolean): Color = if (isDark) DarkBackground else LightBackground

    /**
     * 凹陷区域（滑轨、输入框、选中项）的专用底色，比背景**明显暗一档**。
     *
     * 这是本设计表达"范围"的主要手段：不靠描边，也不靠强光影，而是靠**颜色差**让凹陷区一眼可辨，
     * 阴影只补一点点纵深。相应地，亮影（高光）已在 [com.todo.ui.component.neumorph] 里统一收弱。
     */
    fun recessedSurface(isDark: Boolean): Color = if (isDark) DarkRecessed else LightRecessed

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
        val Small = NeumorphElevation(offset = 4.dp, blur = 7.dp)

        /** 列表项、按钮、开关、凹陷滑轨 */
        val Medium = NeumorphElevation(offset = 7.dp, blur = 11.dp)

        /** 卡片、面板、输入框 */
        val Large = NeumorphElevation(offset = 10.dp, blur = 15.dp)

        /** 浮层：FAB、吸顶栏、底部面板 */
        val XLarge = NeumorphElevation(offset = 13.dp, blur = 20.dp)

        /**
         * 弹窗面板：[XLarge] 的**无左上高光**版本（暗影强度不变）。
         *
         * 弹窗是浮在遮罩之上的一块面板，"悬浮"用下方投影表达就够了；那圈左上亮影在深色下会读成
         * 一道发光的边，反而把弹窗与遮罩的关系弄脏。深浅色一致：弹窗都不带上高光。
         */
        val Dialog = XLarge.copy(lightAlpha = 0f)
    }
}

/**
 * 全局唯一的圆角。
 *
 * 全站只有这一个圆角值：卡片、面板、顶栏、底部导航、列表条目、按钮、输入框、指示条都用它。
 * 小元素不必单独定小圆角——Compose 会把圆角收敛到不超过自身半径的一半，因此同一个值会自动变成
 * 胶囊或正圆（勾选框、多选标记这类小方块另有 [NeumorphShapes.Marker]，见下）。
 *
 * 取 18dp：新拟态靠光影表达体积，圆角过大会让列表条目、按钮、导航条往胶囊方向走，和"方块感"
 * 的凹陷/凸起语言冲突；过小则失去圆润过渡、显得生硬。18dp 取两者之间，且仍在勾选框的正圆阈值
 * （半边长 12dp / 9dp）之上，小元素的表现不变。要再调只需改这一个数。
 *
 * [Small] / [Medium] / [Large] / [Pill] 是旧的分层名，现在都是同一值的别名（全站已不再分层级），
 * 保留仅为避免逐处改名；后续可一次性更名收尾（纯改名、零视觉变化）。
 */
object NeumorphShapes {
    val Corner = 18.dp

    /**
     * 勾选框、多选标记这类**小方块**的圆角（勾选/未勾选两态共用同一个形状）。
     *
     * 它不能沿用 [Corner]：圆角一旦达到或超过自身半边长，Compose 会把它收敛成正圆——这正是这类
     * 标记以前看起来是圆形的原因（18dp 方块配 18dp 圆角被收敛成 9dp = 正圆）。这里取明显小于
     * 半边的值，在 18~24dp 上仍然读作"带圆角的方形"。
     */
    val Marker = 6.dp

    val Small = Corner
    val Medium = Corner
    val Large = Corner
    val Pill = Corner
}
