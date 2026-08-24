package com.todo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.DarkGlassSurface
import com.todo.ui.theme.DarkGlassSurfaceStrong
import com.todo.ui.theme.LightGlassSurface
import com.todo.ui.theme.LightGlassSurfaceStrong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassBottomSheet(
    onDismissRequest: () -> Unit,
    opaqueBackground: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val bgColor = when {
        opaqueBackground && isDark -> DarkGlassSurfaceStrong
        opaqueBackground && !isDark -> LightGlassSurfaceStrong
        isDark -> DarkGlassSurface.copy(alpha = 0.96f)
        else -> LightGlassSurface.copy(alpha = 0.92f)
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = bgColor,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .height(4.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .fillMaxWidth(0.15f)
                        .background(
                            color = Color.White.copy(alpha = if (isDark) 0.56f else 0.65f),
                            shape = RoundedCornerShape(999.dp)
                        )
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .padding(bottom = 10.dp)
        ) {
            content()
        }
    }
}
