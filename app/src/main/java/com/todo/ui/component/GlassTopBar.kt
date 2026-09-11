package com.todo.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes
import com.todo.ui.theme.dropOnly

/**
 * 新拟态吸顶栏：一条与背景同色的圆角浮条，靠成对阴影凸起，无描边。
 *
 * [overlayBelowContent] 表示这条栏是**浮在内容之上**（详情页/设置页等），用更厚的阴影；
 * 内联在布局流中的顶栏用中等阴影，避免"飘得太高"。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    overlayBelowContent: Boolean = false,
    titleAlignStart: Boolean = false,
    actions: @Composable () -> Unit = {}
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val shape: Shape = RoundedCornerShape(NeumorphShapes.Large)
    // 浮在内容之上的栏（二级界面）只用投影表达悬浮：深色下那圈左上亮影会读成"发光的边"
    val elevation = if (overlayBelowContent) {
        NeumorphElevation.XLarge.dropOnly(isDark)
    } else {
        NeumorphElevation.Medium
    }

    val barContent: @Composable () -> Unit = {
        if (titleAlignStart) {
            TopAppBar(
                modifier = Modifier,
                title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    if (navigationIcon != null && onNavigationClick != null) {
                        // IconButton 默认是圆形水波纹（材料默认值），与本页的圆角矩形语言不一致，
                        // 显式传形状让它和顶栏同形
                        IconButton(onClick = onNavigationClick, shape = shape) {
                            Icon(imageVector = navigationIcon, contentDescription = title)
                        }
                    }
                },
                actions = { actions() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        } else {
            CenterAlignedTopAppBar(
                modifier = Modifier,
                title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    if (navigationIcon != null && onNavigationClick != null) {
                        IconButton(onClick = onNavigationClick, shape = shape) {
                            Icon(imageVector = navigationIcon, contentDescription = title)
                        }
                    }
                },
                actions = { actions() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .neumorph(shape = shape, isDark = isDark, depth = 1f, elevation = elevation),
        shape = shape,
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            barContent()
        }
    }
}
