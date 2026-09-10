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

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

@Singleton
class ThemePreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey(Constants.THEME_MODE_KEY)
    }

    val themeModeFlow: Flow<String> = context.themeDataStore.data.map { preferences ->
        preferences[Keys.THEME_MODE] ?: "system"
    }

    suspend fun setThemeMode(mode: String) {
        context.themeDataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode
        }
    }
}
