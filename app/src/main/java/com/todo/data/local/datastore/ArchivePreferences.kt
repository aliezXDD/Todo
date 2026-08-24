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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.archiveDataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.ARCHIVE_PREFERENCES_NAME
)

class ArchivePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val LAST_ARCHIVE_DATE = stringPreferencesKey(Constants.LAST_ARCHIVE_DATE_KEY)
    }

    val lastArchiveDateFlow: Flow<String?> = context.archiveDataStore.data.map { preferences ->
        preferences[Keys.LAST_ARCHIVE_DATE]
    }

    suspend fun setLastArchiveDate(date: String) {
        context.archiveDataStore.edit { preferences ->
            preferences[Keys.LAST_ARCHIVE_DATE] = date
        }
    }
}
