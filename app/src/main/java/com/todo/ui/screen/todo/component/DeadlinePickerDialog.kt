package com.todo.ui.screen.todo.component

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.todo.ui.component.neumorph
import com.todo.ui.theme.DarkScrim
import com.todo.ui.theme.LightScrim
import com.todo.ui.theme.MotionTokens
import com.todo.ui.theme.Neumorph
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

/**
 * 弹窗面板占容器宽度的比例（与其它弹窗一致：左右留出一点给新拟态阴影）。
 */
private const val PanelWidthFraction = 0.92f

/**
 * 面板的横向内边距：只留一点。日历要的是**宽度**，宽度不够就会把日子画成"椭圆"（见 [CalendarPanelMaxWidth]）。
 */
private val PanelHorizontalPadding = 8.dp

/**
 * 日历面板的宽度上限 = **7 列 × 48dp + 日历自己左右各 12dp 内边距 = 360dp**，再加面板两侧各 8dp。
 *
 * 360dp 是 M3 日历的"设计宽度"：它自己的 `DatePickerDialog` 就是 `requiredWidth(360.dp)`。
 * 关键在日子单元格 —— 边长 = `max(40dp, LocalMinimumInteractiveComponentSize)`（默认 48dp），
 * 一行七列就要 336dp；而单元格的形状是 `CircleShape`（= `RoundedCornerShape(50%)`，圆角半径取**短边**的一半），
 * 所以**只要那一行放不下七列，最后一列就会被 Row 挤窄**（宽 42、高仍 48）→ 画出来是个**椭圆**，
 * 与中间那些正圆的日子对不上。这正是"当前日是椭圆、选中的是圆形、看着不舒服"的成因。
 *
 * 反过来也得限宽：超过 360dp 时，星期表头（按整宽平分七列）与日子（各自定宽 48dp）会逐渐错位。
 * 所以宽屏上就停在 376dp，让日历内部恰好回到它的设计尺寸。
 */
private val CalendarPanelMaxWidth = 376.dp

/** M3 日历自己的左右内边距（其源码里的 `DatePickerHorizontalPadding = 12.dp`）。 */
private val CalendarHorizontalPadding = 12.dp

/** 日历一行七天。 */
private const val CalendarColumns = 7

/**
 * 日子单元格的下限：M3 的日期方框本身就是 40dp 见方，比这更窄也压不下去。
 * 因此在约 330dp 以下的（Android 12 上基本见不到的）极窄屏上，最后一列仍会被挤一点。
 */
private val MinDayCellSize = 40.dp

/**
 * 选截止日期的日历弹窗：遮罩 + 与背景同色的凸起面板，内部是 M3 的日历（沿用应用配色），
 * 与 `PresetEditDialog` / `GlassDialog` 同一套进出场与形状语言。
 *
 * **必须是独立的 Dialog 窗口**：调用它的是「编辑待办」面板，而面板本身是一层 `ModalBottomSheet`
 * 弹窗；同窗口的兄弟节点会被那层弹窗盖住（点不到也看不见），只有新开一个窗口才能浮在它之上。
 *
 * 只管"选哪一天"：[onConfirm] 把选中的日期交给调用方当草稿（是否落盘由编辑面板的「保存」决定），
 * [onClear] 表示"不要截止日期了"。**今天之前的日期不可选**：期限只能落在今天及以后
 * （截止日填今天 = 期限到明天凌晨 4 点为止，见 `DateUtils.DAY_START_HOUR`）。
 *
 * 日历本身很高（约 500dp），所以面板限高在容器的九成以内、内容超出时可滚动：
 * 矮屏或键盘弹起（"输入日期"那一档）时不会把按钮顶出屏幕。
 *
 * 日历内部一律显示**简体中文**（见 [ChineseLocale]）：库里的那套文案是跟系统语言的，
 * 而本应用自己的文字全是写死的中文，系统语言不是中文时这里就会突然冒出英文。
 *
 * 面板宽度按日历的设计宽度取（见 [CalendarPanelMaxWidth]）：给不够宽度时 M3 会把最后一列日子挤窄，
 * `CircleShape` 于是画成椭圆 —— 今天那个圈会与选中的圆形对不上。
 */
@Composable
fun DeadlinePickerDialog(
    visible: Boolean,
    today: String,
    selectedDate: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onClear: () -> Unit
) {
    val transitionState = remember { MutableTransitionState(false) }
    LaunchedEffect(visible) {
        transitionState.targetState = visible
    }
    if (!transitionState.currentState && !transitionState.targetState) return

    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val scrimColor = if (isDark) DarkScrim else LightScrim
    val noRipple = remember { MutableInteractionSource() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            // 遮罩与面板要铺满整屏（这也是本应用弹窗的一贯做法）：不受平台默认宽度限制
            usePlatformDefaultWidth = false,
            // 点面板外由下面的遮罩自己处理，免得两套"点外面关闭"打架
            dismissOnClickOutside = false,
            // 与 Activity 一样走 edge-to-edge，键盘弹出时才拿得到 IME 内边距（见下面的 imePadding）
            decorFitsSystemWindows = false
        )
    ) {
        AnimatedVisibility(
            visibleState = transitionState,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = MotionTokens.DialogEnter,
                    easing = MotionTokens.StandardEasing
                )
            ) + scaleIn(
                initialScale = 0.96f,
                animationSpec = tween(
                    durationMillis = MotionTokens.Standard,
                    easing = MotionTokens.StandardEasing
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = MotionTokens.DialogExit,
                    easing = MotionTokens.StandardEasing
                )
            ) + scaleOut(
                targetScale = 0.96f,
                animationSpec = tween(
                    durationMillis = MotionTokens.DialogExit,
                    easing = MotionTokens.StandardEasing
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    // 键盘弹起时向上让位（"输入日期"那一档会拉起键盘）：否则面板下半截与按钮会被盖住
                    .imePadding()
                    .background(scrimColor)
                    .clickable(
                        interactionSource = noRipple,
                        indication = null,
                        onClick = onDismiss
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 按当前截止日期重建选择器：选过/清除之后再打开，日历停的就是新值
                //（rememberDatePickerState 只在首次组合时取初值，不换 key 的话会一直停在旧值上）。
                // today 也一起当 key：对话框开着跨过凌晨 4 点时，"今天及以后"的可选范围要跟着换一天。
                key(selectedDate, today) {
                    DeadlinePickerPanel(
                        today = today,
                        selectedDate = selectedDate,
                        isDark = isDark,
                        onDismiss = onDismiss,
                        onConfirm = onConfirm,
                        onClear = onClear
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeadlinePickerPanel(
    today: String,
    selectedDate: String?,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onClear: () -> Unit
) {
    val noRipple = remember { MutableInteractionSource() }
    val contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f)

    // 可选范围：今天及以后。"今天"按逻辑日算（凌晨 4 点分界），与软件其它地方一致
    val selectableDates = remember(today) {
        val todayYear = LocalDate.parse(today).year
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                dateOfUtcMillis(utcTimeMillis) >= today

            override fun isSelectableYear(year: Int): Boolean = year >= todayYear
        }
    }

    // 外层不给尺寸修饰符：这样 scope 里的 maxWidth 就是**容器**宽度（下面按比例算面板宽度），
    // 而 Box 自己按内容（= 面板）收拢，由父级居中。
    BoxWithConstraints {
        val panelMaxHeight = maxHeight * 0.92f
        // 面板实际宽度（宽屏停在上限），再据此算出一列日子能分到多少宽度，喂给 M3 的
        // LocalMinimumInteractiveComponentSize —— 单元格因此恒为正方形（正圆），窄屏上只是整体小一点。
        val panelWidth = minOf(maxWidth * PanelWidthFraction, CalendarPanelMaxWidth)
        val dayCellSize = (
            (panelWidth - PanelHorizontalPadding * 2 - CalendarHorizontalPadding * 2) / CalendarColumns
            ).coerceAtLeast(MinDayCellSize)

        Surface(
            modifier = Modifier
                .width(panelWidth)
                .heightIn(max = panelMaxHeight)
                .neumorph(
                    shape = RoundedCornerShape(NeumorphShapes.Large),
                    isDark = isDark,
                    depth = 1f,
                    // 弹窗不带上高光：只用投影表达"浮在遮罩之上"（与其它弹窗一致）
                    elevation = NeumorphElevation.Dialog
                )
                .clickable(
                    interactionSource = noRipple,
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(NeumorphShapes.Large),
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = PanelHorizontalPadding, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 日历整块（含库里自己的文案与日期格式）都锁成简体中文
                ChineseLocale {
                    // 列宽单独喂给 M3：单元格恒为正方形，今天那个圈与选中那颗圆点形状才一致
                    CompositionLocalProvider(
                        LocalMinimumInteractiveComponentSize provides dayCellSize
                    ) {
                        // 状态必须在中文语境**里面**创建：M3 的 rememberDatePickerState() 没有 locale 参数，
                        // 它的 locale 就取当前 LocalConfiguration 的 locales[0]，星期/月份/日期格式全跟着它走
                        val pickerState = rememberDatePickerState(
                            // 没设过截止日期时默认停在今天：直接点「确定」就是"今天截止"
                            initialSelectedDateMillis = utcMillisOf(selectedDate ?: today),
                            selectableDates = selectableDates
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            DatePicker(
                                state = pickerState,
                                // 这里**不能**给透明色。面板本身就是弹窗里的一块表面，日历不必再铺一层底色，
                                // 但 M3 的年份列表正是拿这个色当背景的
                                //（`LazyVerticalGrid(modifier.background(colors.containerColor))`）：
                                // 透明的话，点小箭头选年份时年份数字会直接压在日历文字上（两层文字重叠）。
                                // 所以给"和面板同色"的一层：铺上去看不出来，年份列表却因此是不透明的。
                                colors = DatePickerDefaults.colors(containerColor = Neumorph.surface(isDark))
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (selectedDate != null) {
                                    // 已有截止日期时才提供"清除"（没有的东西不必给一个清除入口）
                                    PickerTextButton(
                                        text = "清除截止日期",
                                        color = MaterialTheme.colorScheme.error,
                                        onClick = onClear
                                    )
                                }
                                PickerTextButton(text = "取消", color = contentColor, onClick = onDismiss)
                                PickerTextButton(
                                    text = "确定",
                                    color = MaterialTheme.colorScheme.primary,
                                    onClick = {
                                        pickerState.selectedDateMillis?.let { onConfirm(dateOfUtcMillis(it)) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 与面板同形的文字按钮。
 *
 * M3 的 `TextButton` 默认是胶囊水波纹，与面板的圆角矩形不一致，这里显式对齐形状
 * （`GlassDialog` 里那颗按"角色"取色，这里三颗按钮颜色各不相同，所以单独一个）。
 */
@Composable
private fun PickerTextButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(NeumorphShapes.Corner),
        // 面板为了把日子排成正圆，把 LocalMinimumInteractiveComponentSize 压到了一列日子的宽度
        //（见 DeadlinePickerPanel 里的 dayCellSize），这三颗按钮会跟着被压到 40~48dp 之间。
        // 这里显式要回 M3 的建议触摸目标：48dp。
        modifier = Modifier.heightIn(min = 48.dp)
    ) {
        Text(text = text, color = color)
    }
}

/**
 * 把这块子树锁成**简体中文**。
 *
 * M3 的日历不像本应用其它文案那样写死中文：标题（"选择日期"）、星期与月份的写法、年选择器提示
 * 都来自库里跟随**系统语言**的多语言资源，日期格式则取自 `Configuration.locales`。
 * 所以系统语言是英文时，只有这一块日历会变英文——日期选择器自己又带着键盘输入等功能，
 * 英文界面会让人不知道该怎么填。这里给它单独换一套中文语境，两条路都覆盖：
 *
 * - [LocalContext] / [LocalResources] 决定资源文案（库里那些字符串）；
 * - [LocalConfiguration] 决定日期与星期的格式化（M3 的 `defaultLocale()` 就是读它的 `locales[0]`）。
 *
 * [LocalResources] 的默认实现本就是从这两者算出来的（官方 sample 也只给前两者），这里显式再给一份，
 * 读起来更直白。只改 locale，其余字段（屏幕尺寸、密度、方向）原样复制，因此不影响任何布局与测量。
 */
@Composable
private fun ChineseLocale(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val localizedContext = remember(context) {
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(Locale.SIMPLIFIED_CHINESE)
        }
        context.createConfigurationContext(configuration)
    }
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalResources provides localizedContext.resources,
        LocalConfiguration provides localizedContext.resources.configuration
    ) {
        content()
    }
}

/**
 * 选择器给的是 **UTC 零点**的时间戳（M3 的既定约定），换算日期字符串必须用 UTC 反解：
 * 用本地时区会整体偏移一天（东八区会往前多一天）。
 */
private fun dateOfUtcMillis(utcTimeMillis: Long): String =
    Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().toString()

private fun utcMillisOf(date: String): Long =
    LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
