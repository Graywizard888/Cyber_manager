package com.graywizard.filemanager.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    
    companion object {
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val SORT_BY = stringPreferencesKey("sort_by")
        private val SORT_ORDER = stringPreferencesKey("sort_order")
        private val SHOW_HIDDEN_FILES = booleanPreferencesKey("show_hidden_files")
        private val GRID_VIEW = booleanPreferencesKey("grid_view")
    }
    
    val darkModeFlow: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE]
    }
    
    suspend fun setDarkMode(enabled: Boolean?) {
        context.dataStore.edit { preferences ->
            if (enabled == null) {
                preferences.remove(DARK_MODE)
            } else {
                preferences[DARK_MODE] = enabled
            }
        }
    }
    
    val sortByFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SORT_BY] ?: "name"
    }
    
    suspend fun setSortBy(sortBy: String) {
        context.dataStore.edit { preferences ->
            preferences[SORT_BY] = sortBy
        }
    }
    
    val sortOrderFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SORT_ORDER] ?: "asc"
    }
    
    suspend fun setSortOrder(order: String) {
        context.dataStore.edit { preferences ->
            preferences[SORT_ORDER] = order
        }
    }
    
    val showHiddenFilesFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_HIDDEN_FILES] ?: false
    }
    
    suspend fun setShowHiddenFiles(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_HIDDEN_FILES] = show
        }
    }
    
    val gridViewFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[GRID_VIEW] ?: false
    }
    
    suspend fun setGridView(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[GRID_VIEW] = enabled
        }
    }
}
