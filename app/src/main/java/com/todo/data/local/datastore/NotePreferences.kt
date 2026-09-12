package com.todo.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.todo.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.noteDataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.NOTE_PREFERENCES_NAME
)

/**
 * 笔记：全 App **只有一份**草稿，永久保存、随时编辑。
 *
 * 之所以单独放一份 DataStore 而不是建表：它是一条不增长、也永远不需要查询/关联的文档，
 * 为它把数据库升到 v2 再写迁移，风险和收益不成比例。将来若要做"按日期的笔记"或把笔记接进
 * 往日记录，那时再迁到 Room 更合适。
 */
@Singleton
class NotePreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private object Keys {
        val CONTENT = stringPreferencesKey(Constants.NOTE_CONTENT_KEY)
    }

    /** 从未写过时为空串（不是 null）：调用方拿到的永远是可直接显示的文本。 */
    val contentFlow: Flow<String> = context.noteDataStore.data.map { preferences ->
        preferences[Keys.CONTENT] ?: ""
    }

    suspend fun setContent(content: String) {
        context.noteDataStore.edit { preferences ->
            preferences[Keys.CONTENT] = content
        }
    }
}
