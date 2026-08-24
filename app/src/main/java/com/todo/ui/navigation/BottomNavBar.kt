package com.todo.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.todo.ui.component.glassOverlay
import com.todo.ui.theme.DarkGlassBorder
import com.todo.ui.theme.DarkGlassSurface
import com.todo.ui.theme.LightGlassBorder
import com.todo.ui.theme.LightGlassSurface
import com.todo.ui.theme.MotionTokens

@Composable
fun BottomNavBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = navBackStackEntry?.destination
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val container = if (isDark) DarkGlassSurface.copy(alpha = 0.82f) else Color.White
    val border = if (isDark) Color.White.copy(alpha = 0.48f) else Color.White
    val shape = RoundedCornerShape(24.dp)

    val items = listOf(
        Triple(Screen.Preset, "预设", Icons.AutoMirrored.Filled.List),
        Triple(Screen.Todo, "待办", Icons.Filled.CheckCircle),
        Triple(Screen.Settings, "设置", Icons.Filled.Settings)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(shape)
            .background(container)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .glassOverlay(
                    shape = shape,
                    isDark = isDark,
                    topAlphaLight = 0.34f,
                    topAlphaDark = 0.14f,
                    bottomAlphaLight = 0.18f,
                    bottomAlphaDark = 0.18f
                ),
            color = Color.Transparent,
            shape = shape,
            border = BorderStroke(1.dp, border)
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                items.forEach { (screen, label, icon) ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    val iconScale by animateFloatAsState(
                        targetValue = if (selected) 1.12f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = MotionTokens.SpringMedium
                        ),
                        label = "bottomIconScale"
                    )
                    val iconTint by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else if (isDark) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            Color(0xFF4E5661)
                        },
                        label = "bottomIconTint"
                    )
                    val labelAlpha by animateFloatAsState(
                        targetValue = if (selected) 1f else 0.78f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = MotionTokens.SpringMediumLow
                        ),
                        label = "bottomLabelAlpha"
                    )
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(Screen.Todo.route) {
                                    saveState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.scale(iconScale),
                                tint = iconTint
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                color = iconTint.copy(alpha = labelAlpha)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = iconTint,
                            selectedTextColor = iconTint,
                            unselectedIconColor = iconTint,
                            unselectedTextColor = iconTint,
                        indicatorColor = if (isDark) Color.White.copy(alpha = 0.24f) else Color.White
                    ),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}
