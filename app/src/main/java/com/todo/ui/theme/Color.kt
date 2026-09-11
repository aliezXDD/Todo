package com.todo.ui.theme

import androidx.compose.ui.graphics.Color

// ---- 文字 ----
val LightPrimaryText = Color(0xFF171A21)
val DarkPrimaryText = Color(0xFFE8EBF2)

val LightSecondaryText = Color(0xFF565F6E)
val DarkSecondaryText = Color(0xFF9AA1B0)

// ---- 主色（靛蓝）：新拟态里唯一的强调色，其余层级全部交给光影 ----
val LightAccent = Color(0xFF5A67D8)
val DarkAccent = Color(0xFF95A1F0)

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
// 与 Neumorph 的表面色同步微调（浅色累计 -9、深色累计 +13，保持彼此关系不变）
val LightSurfaceVariant = Color(0xFFD9DEE3)
val DarkSurfaceVariant = Color(0xFF383C41)
val LightOutline = Color(0xFFBDC5D1)
val DarkOutline = Color(0xFF494E56)
val LightPrimaryContainer = Color(0xFFDDE2F8)
val DarkPrimaryContainer = Color(0xFF3A4265)
