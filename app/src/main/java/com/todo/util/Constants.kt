package com.todo.util

object Constants {
    const val DATABASE_NAME = "todo_database"
    const val THEME_MODE_KEY = "theme_mode"
    const val THEME_MODE_SYSTEM = "system"
    const val THEME_MODE_LIGHT = "light"
    const val THEME_MODE_DARK = "dark"
    const val ARCHIVE_PREFERENCES_NAME = "archive_preferences"
    const val LAST_ARCHIVE_DATE_KEY = "last_archive_date"
    const val NOTE_PREFERENCES_NAME = "note_preferences"
    const val NOTE_CONTENT_KEY = "note_content"
    const val AVATAR_PREFERENCES_NAME = "avatar_preferences"

    /**
     * 自定义头像的"版本号"（从 0 起，每次保存 +1）。
     *
     * 头像图片本身存在 `filesDir/avatar.png`，这里只存一个计数：只有它变了 Flow 才会重新发射。
     * 0 表示没有自定义头像 —— 此时显示内置的 `avatar_default`。
     */
    const val AVATAR_REVISION_KEY = "avatar_revision"

    const val DATE_PATTERN = "yyyy-MM-dd"
    const val DAILY_ARCHIVE_WORK_NAME = "daily_archive"
}
