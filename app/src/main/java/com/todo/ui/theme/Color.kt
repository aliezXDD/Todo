package com.todo.ui.theme

import androidx.compose.ui.graphics.Color

// ---- 文字 ----
val LightPrimaryText = Color(0xFF171A21)
val DarkPrimaryText = Color(0xFFE8EBF2)

val LightSecondaryText = Color(0xFF565F6E)
val DarkSecondaryText = Color(0xFF9AA1B0)

// ---- 主色：新拟态里唯一的强调色，其余层级全部交给光影。
// 浅色沿用原来的靛蓝（白字/白勾压得住）；深色用偏浅的橙（配深色字）。两者都再淡一档 ----
val LightAccent = Color(0xFF7B86DE)
val DarkAccent = Color(0xFFF1AA6E)

/**
 * 主题色表面上的内容色（浅色模式）：`+` 图标、主要按钮文字、勾选标记的 `√` 都用它。
 *
 * 不用纯白 `#FFFFFF`：压在已经调浅的靛蓝上，纯白会显得偏"跳"、边缘发刺眼；略暗约 5%
 * 更贴合整体的柔和调子。**代价是对比度从约 3.3:1 降到约 3.0:1**（本来就在及格线附近），
 * 若以后要继续压缩这个白，建议同时把 `LightAccent` 调深一点来补回对比。
 */
val LightOnAccent = Color(0xFFF1F3F8)

// ---- 语义色 ----
val LightDanger = Color(0xFFD95B5B)
val DarkDanger = Color(0xFFF08A8A)

// ---- 数据语义色：完成率热力（低 → 中 → 高），供图表复用 ----
val RateLow = Color(0xFFF8756C)
val RateMid = Color(0xFFF7C476)
val RateHigh = Color(0xFF96C797)

// ---- 弹层遮罩：新拟态仍需要压暗背景来突出浮层 ----
val LightScrim = Color(0x33000000)
val DarkScrim = Color(0x66000000)

// ---- Material 补充 token（配新拟态底色）----
val LightSurfaceVariant = Color(0xFFE2E7EC)
val DarkSurfaceVariant = Color(0xFF2B2F36)
val LightOutline = Color(0xFFC6CEDA)
val DarkOutline = Color(0xFF3C4149)
val LightPrimaryContainer = Color(0xFFEAEDFC)
val DarkPrimaryContainer = Color(0xFF563A22)
