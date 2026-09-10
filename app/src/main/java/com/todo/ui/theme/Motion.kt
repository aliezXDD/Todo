package com.todo.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring

object MotionTokens {
    val StandardEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    const val Fast = 120
    const val Medium = 220
    const val Standard = 260
    const val Slow = 320

    const val ThemeCrossfade = 320
    const val ScreenSlide = 320
    const val ItemReveal = 180
    const val DialogEnter = 220
    const val DialogExit = 180
    const val SheetTabSwitch = 220
    const val SheetResize = 260
    const val ItemState = 300
    const val ItemFadeIn = 250
    const val ItemFadeOut = 200
    const val ItemPlacement = 200

    const val SpringMedium = Spring.StiffnessMedium
    const val SpringMediumLow = Spring.StiffnessMediumLow
    const val SpringLow = Spring.StiffnessLow
}
