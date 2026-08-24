package com.todo.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.DarkGlassBorder
import com.todo.ui.theme.DarkGlassSurface
import com.todo.ui.theme.LightGlassBorder
import com.todo.ui.theme.LightGlassSurface

@Composable
fun glassTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = if (MaterialTheme.colorScheme.onSurface.luminance() > 0.7f) Color.White else MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = if (MaterialTheme.colorScheme.onSurface.luminance() > 0.7f) Color.White else MaterialTheme.colorScheme.onSurface,
    disabledTextColor = (if (MaterialTheme.colorScheme.onSurface.luminance() > 0.7f) Color.White else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.5f),
    focusedContainerColor = if (MaterialTheme.colorScheme.onSurface.luminance() > 0.7f) DarkGlassSurface.copy(alpha = 0.58f) else LightGlassSurface.copy(alpha = 0.54f),
    unfocusedContainerColor = if (MaterialTheme.colorScheme.onSurface.luminance() > 0.7f) DarkGlassSurface.copy(alpha = 0.58f) else LightGlassSurface.copy(alpha = 0.54f),
    disabledContainerColor = if (MaterialTheme.colorScheme.onSurface.luminance() > 0.7f) DarkGlassSurface.copy(alpha = 0.58f) else LightGlassSurface.copy(alpha = 0.54f),
    errorContainerColor = if (MaterialTheme.colorScheme.onSurface.luminance() > 0.7f) DarkGlassSurface.copy(alpha = 0.58f) else LightGlassSurface.copy(alpha = 0.54f),
    cursorColor = if (MaterialTheme.colorScheme.onSurface.luminance() > 0.7f) Color.White else MaterialTheme.colorScheme.onSurface,
    focusedBorderColor = if (MaterialTheme.colorScheme.onSurface.luminance() > 0.7f) DarkGlassBorder else LightGlassBorder,
    unfocusedBorderColor = if (MaterialTheme.colorScheme.onSurface.luminance() > 0.7f) Color.White.copy(alpha = 0.24f) else Color(0xFF9EA6B3),
    focusedPlaceholderColor = if (MaterialTheme.colorScheme.onSurface.luminance() > 0.7f) Color.White.copy(alpha = 0.58f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f),
    unfocusedPlaceholderColor = if (MaterialTheme.colorScheme.onSurface.luminance() > 0.7f) Color.White.copy(alpha = 0.58f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f)
)

fun glassTextFieldShape() = RoundedCornerShape(20.dp)
