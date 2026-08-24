package com.todo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.todo.R

private val AppFontFamily = FontFamily(
    Font(R.font.stzhongs, FontWeight.Normal),
    Font(R.font.stzhongs, FontWeight.Medium),
    Font(R.font.stzhongs, FontWeight.SemiBold),
    Font(R.font.stzhongs, FontWeight.Bold)
)

private val BaseTypography = Typography()

val Typography = Typography(
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = AppFontFamily),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = AppFontFamily),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = AppFontFamily),
    headlineLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = AppFontFamily),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = AppFontFamily),
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = AppFontFamily),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = AppFontFamily),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = AppFontFamily),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = AppFontFamily),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = AppFontFamily),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp
    )
)
