package com.todo.ui.screen.todo.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.todo.R

/**
 * 今日进度卡右侧那个圆角正方形头像位。
 *
 * 刻意**不做任何立体处理**：不加外投影，也不做凹陷的小槽。头像位里装的是一张照片，照片自带内容与
 * 明暗，再叠一层新拟态的光影只会让边缘发脏、让照片看起来"陷进卡片里"；它就该是一张贴在卡片上的图。
 * 全站其它元素（卡片、按钮、输入框）仍然走各自的新拟态语言，这里是个例外。
 *
 * 圆角 [AvatarCorner] 也刻意小于全站的 18dp：18dp 落在 52dp 的方块上接近三成，读起来是"圆"，
 * 而照片栏要读作"方"；12dp 只负责把四个尖角收掉。
 *
 * 尺寸取 [AvatarSlotSize]：比左边的进度环（40dp + 4dp 内边距 = 48dp）略大一圈，两者并排才不显小，
 * 又矮于"两行文字"的高度，不撑高卡片。
 *
 * 未设置自定义头像时回落到内置的 [R.drawable.avatar_default]，因此这个位置任何时候都有东西。
 */
@Composable
fun AvatarSlot(
    avatar: ImageBitmap?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slotModifier = modifier
        .size(AvatarSlotSize)
        .clip(AvatarShape)
        // 波纹交给默认实现：前面的 clip 已经把它裁成圆角方形，不会从四角露出方块
        .clickable(
            role = Role.Button,
            onClick = onClick
        )

    if (avatar != null) {
        Image(
            bitmap = avatar,
            contentDescription = "头像",
            contentScale = ContentScale.Crop,
            modifier = slotModifier
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.avatar_default),
            contentDescription = "头像",
            contentScale = ContentScale.Crop,
            modifier = slotModifier
        )
    }
}

/** 头像位的边长。见组件注释里的取值理由。 */
private val AvatarSlotSize = 52.dp

/** 头像位的圆角：比全站的 18dp 小，照片栏要读作"方"。 */
private val AvatarCorner = 12.dp

// 顶层属性按声明顺序初始化，所以 AvatarCorner 必须写在它前面
private val AvatarShape = RoundedCornerShape(AvatarCorner)
