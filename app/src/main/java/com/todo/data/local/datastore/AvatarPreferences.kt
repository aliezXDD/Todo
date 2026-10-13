package com.todo.data.local.datastore

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.todo.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.avatarDataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.AVATAR_PREFERENCES_NAME
)

/**
 * 首页那张头像：用户可以换成自己的图片，也可以恢复成默认头像。
 *
 * 存储拆成两处，各有各的理由：
 * - **图片本体**落在 `filesDir/avatar.png`（应用私有目录）。选图拿到的是相册的 `content://` URI，
 *   它的读取授权只在本次会话有效，直接把 URI 存下来，下次冷启动就指向一张读不到的图；因此在
 *   **用户确认裁剪的那一刻就把结果复制进私有目录**，以后只认这个文件。
 * - **DataStore 只存一个计数**（[Constants.AVATAR_REVISION_KEY]）。头像没有"查询"需求，为它建表
 *   要连带数据库升版和迁移，风险和收益不成比例（和笔记同一考量）；而只存计数是因为 Flow 需要
 *   一个会变的值来驱动重新发射 —— 图片内容变了但键不变的话，界面不会刷新。
 *
 * 计数为 0（默认）或文件读不出来时，[avatarFlow] 发射 null，由界面回落到内置默认头像：
 * 文件被系统清理、或保存中途失败，界面都不会空一块。
 */
@Singleton
class AvatarPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private object Keys {
        val REVISION = longPreferencesKey(Constants.AVATAR_REVISION_KEY)
    }

    private val avatarFile: File
        get() = File(context.filesDir, AVATAR_FILE_NAME)

    /** 解码在 IO 线程完成（[flowOn]），主线程只拿到成品。 */
    val avatarFlow: Flow<ImageBitmap?> = context.avatarDataStore.data
        .map { preferences -> preferences[Keys.REVISION] ?: 0L }
        .map { revision -> if (revision <= 0L) null else readAvatar()?.asImageBitmap() }
        .flowOn(Dispatchers.IO)

    /**
     * 保存裁剪好的头像。写文件成功之后才推进计数：顺序反过来的话，一次写失败就会让界面
     * 一直去读那张不存在（或还是旧内容）的图。
     */
    suspend fun save(bitmap: Bitmap) = withContext(Dispatchers.IO) {
        val written = runCatching {
            avatarFile.outputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            }
        }.isSuccess
        if (!written) return@withContext

        context.avatarDataStore.edit { preferences ->
            preferences[Keys.REVISION] = (preferences[Keys.REVISION] ?: 0L) + 1L
        }
    }

    /** 恢复默认头像：删掉文件并把计数归零。 */
    suspend fun clear() = withContext(Dispatchers.IO) {
        runCatching { avatarFile.delete() }
        context.avatarDataStore.edit { preferences ->
            preferences[Keys.REVISION] = 0L
        }
    }

    private fun readAvatar(): Bitmap? {
        if (!avatarFile.exists()) return null
        return runCatching { BitmapFactory.decodeFile(avatarFile.absolutePath) }.getOrNull()
    }

    private companion object {
        const val AVATAR_FILE_NAME = "avatar.png"
    }
}
