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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todo.ui.theme.Neumorph
import com.todo.ui.theme.NeumorphShapes

/**
 * 新拟态底部面板：面板表面与页面背景同色（仍是"同一块材质"），靠上缘圆角与浮层阴影区分。
 *
 * [dragEnabled] = false 会关掉"下滑关闭"手势（点面板外、按返回键仍可关闭）。
 * 用于面板里放了**自己需要纵向拖动**的内容时——例如备注面板的文本框：
 * 面板的下滑手势会和"在框内上下拖动查看长文本"抢同一个手势，拖下去会把面板拖走。
 * 关了手势就同时隐藏拖拽把手：一个拖不动的把手比没有把手更容易误导。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dragEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetGesturesEnabled = dragEnabled,
        shape = RoundedCornerShape(topStart = NeumorphShapes.Large, topEnd = NeumorphShapes.Large),
        containerColor = Neumorph.surface(isDark),
        dragHandle = {
            if (dragEnabled) {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .height(4.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // 拖拽把手用中性灰：新拟态是浅色表面，原来那抹白色在浅色下会完全看不见
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .fillMaxWidth(0.15f)
                            .background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(NeumorphShapes.Pill)
                            )
                    )
                }
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
