package com.todo.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.DarkGlassBorder
import com.todo.ui.theme.DarkGlassSurface
import com.todo.ui.theme.LightGlassBorder
import com.todo.ui.theme.LightGlassSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    glassEffect: Boolean = false,
    overlayBelowContent: Boolean = false,
    titleAlignStart: Boolean = false,
    actions: @Composable () -> Unit = {}
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val bg = if (isDark) DarkGlassSurface.copy(alpha = 0.80f) else LightGlassSurface.copy(alpha = 0.86f)
    val borderColor = if (isDark) DarkGlassBorder else LightGlassBorder
    val shape = RoundedCornerShape(22.dp)

    val barContent: @Composable () -> Unit = {
        if (titleAlignStart) {
            TopAppBar(
                modifier = Modifier,
                title = { Text(text = title) },
                navigationIcon = {
                    if (navigationIcon != null && onNavigationClick != null) {
                        IconButton(onClick = onNavigationClick) {
                            Icon(imageVector = navigationIcon, contentDescription = title)
                        }
                    }
                },
                actions = { actions() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        } else {
            CenterAlignedTopAppBar(
                modifier = Modifier,
                title = { Text(text = title) },
                navigationIcon = {
                    if (navigationIcon != null && onNavigationClick != null) {
                        IconButton(onClick = onNavigationClick) {
                            Icon(imageVector = navigationIcon, contentDescription = title)
                        }
                    }
                },
                actions = { actions() },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    }

    if (glassEffect) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = shape,
            color = bg,
            border = BorderStroke(1.dp, borderColor),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .then(
                        if (overlayBelowContent) {
                            Modifier.glassTopBarOverlay(shape = shape, isDark = isDark)
                        } else {
                            Modifier
                        }
                    )
            ) {
                barContent()
            }
        }
    } else {
        Box(
            modifier = modifier
                .glassOverlay(
                    shape = shape,
                    isDark = isDark,
                    topAlphaLight = 0.16f,
                    topAlphaDark = 0.08f,
                    bottomAlphaLight = 0.04f,
                    bottomAlphaDark = 0.10f
                )
                .fillMaxWidth()
                .background(bg)
        ) {
            barContent()
        }
    }
}

private fun Modifier.glassTopBarOverlay(
    shape: Shape,
    isDark: Boolean
): Modifier = drawWithContent {
    val outline = shape.createOutline(size, layoutDirection, this)
    val mask = when (outline) {
        is Outline.Generic -> outline.path
        is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
        is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
        else -> Path().apply { addRect(outline.bounds) }
    }

    clipPath(mask) {
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isDark) 0.10f else 0.16f),
                    Color.Transparent
                ),
                start = Offset.Zero,
                end = Offset(size.width * 0.75f, size.height * 0.28f)
            )
        )
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isDark) 0.03f else 0.05f),
                    Color.Transparent,
                    Color.Transparent,
                    Color.White.copy(alpha = if (isDark) 0.02f else 0.03f)
                )
            )
        )
    }
    drawContent()
}

