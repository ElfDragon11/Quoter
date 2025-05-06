package com.example.quoter.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define DataStore instance at the top level
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val context: Context) {

    // Define Preference Keys
    private object PreferencesKeys {
        val LOCATION = stringPreferencesKey("pref_location")
        val SCENE = stringPreferencesKey("pref_scene")
        val STYLE = stringPreferencesKey("pref_style")
        val FONT_STYLE = stringPreferencesKey("pref_font_style") // New key for font style
        val FONT_SIZE = stringPreferencesKey("pref_font_size") // New key for font size
    }

    // Default values
    private val defaultLocation = "Mountains"
    private val defaultScene = "Natural"
    private val defaultStyle = "Photorealistic"

    // Flow to observe all preferences as a CustomizationState object
    val userPreferencesFlow: Flow<com.example.quoter.CustomizationState> = context.dataStore.data
        .map { preferences ->
            com.example.quoter.CustomizationState(
                location = preferences[PreferencesKeys.LOCATION] ?: defaultLocation,
                scene = preferences[PreferencesKeys.SCENE] ?: defaultScene,
                style = preferences[PreferencesKeys.STYLE] ?: defaultStyle,
                fontStyle = preferences[PreferencesKeys.FONT_STYLE] ?: "Bold",
                fontSize = preferences[PreferencesKeys.FONT_SIZE]?.toFloatOrNull() ?: 16f // Default to 16f if not set
            )
        }

    // --- Update Functions --- 

    suspend fun updateLocation(location: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCATION] = location
        }
    }

    suspend fun updateScene(scene: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SCENE] = scene
        }
    }

    suspend fun updateStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STYLE] = style
        }
    }

    suspend fun updateFontStyle(fontStyle: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_STYLE] = fontStyle
        }
    }

    suspend fun updateFontSize(fontSize: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = fontSize.toString()
        }
    }
}