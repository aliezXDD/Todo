package com.todo.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

/**
 * 新拟态的圆角语言：圆角与元素尺寸成比例（原文示例 200px 方块配 20px 圆角，即 10%），
 * 整体比扁平风格更大、更软，否则凸起会显得生硬。
 */
val Shapes = Shapes(
    small = RoundedCornerShape(NeumorphShapes.Small),
    medium = RoundedCornerShape(NeumorphShapes.Medium),
    large = RoundedCornerShape(NeumorphShapes.Large)
)
