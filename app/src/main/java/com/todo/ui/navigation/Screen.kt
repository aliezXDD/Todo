package com.todo.ui.navigation

sealed class Screen(val route: String) {
    object Todo : Screen("todo")
    object History : Screen("history")
    object Preset : Screen("preset")
    object Settings : Screen("settings")

    object RecycleBin : Screen("recycle_bin")
    object Chart : Screen("chart")
}
