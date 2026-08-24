package com.todo.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.DarkAction
import com.todo.ui.theme.DarkActionDisabled
import com.todo.ui.theme.DarkGlassBorder
import com.todo.ui.theme.DarkGlassSurface
import com.todo.ui.theme.LightAction
import com.todo.ui.theme.LightActionDisabled
import com.todo.ui.theme.LightGlassBorder
import com.todo.ui.theme.LightGlassSurface

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    glassSurface: Boolean = false
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val containerColor = when {
        glassSurface && isDark -> DarkGlassSurface.copy(alpha = 0.80f)
        glassSurface -> LightGlassSurface.copy(alpha = 0.84f)
        isDark -> DarkAction
        else -> LightAction
    }
    val disabledContainerColor = if (isDark) DarkActionDisabled else LightActionDisabled
    val borderColor = when {
        glassSurface && isDark -> DarkGlassBorder
        glassSurface -> LightGlassBorder
        else -> Color.White.copy(alpha = if (isDark) 0.18f else 0.10f)
    }
    val contentColor = if (glassSurface) MaterialTheme.colorScheme.onSurface else Color.White

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor.copy(alpha = if (enabled) 0.95f else 0.82f),
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = contentColor.copy(alpha = 0.7f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(text = text)
    }
}
