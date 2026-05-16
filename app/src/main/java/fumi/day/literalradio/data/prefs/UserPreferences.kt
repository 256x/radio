package fumi.day.literalradio.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class AppFont { DEFAULT, SERIF, MONOSPACE, SCOPE_ONE }

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class UserPrefs(
    val accentColorHex: String = "",
    val textColorHex: String = "",
    val backgroundColorHex: String = "",
    val font: AppFont = AppFont.DEFAULT,
    val fontSize: Float = 16f,
    val lastStationJson: String = "",
    val favoritesJson: String = "[]",
)

@Singleton
class UserPreferences @Inject constructor(private val context: Context) {
    private val accentColorKey = stringPreferencesKey("accent_color")
    private val textColorKey = stringPreferencesKey("text_color")
    private val bgColorKey = stringPreferencesKey("bg_color")
    private val fontKey = stringPreferencesKey("font")
    private val fontSizeKey = floatPreferencesKey("font_size")
    private val lastStationKey = stringPreferencesKey("last_station")
    private val favoritesKey = stringPreferencesKey("favorites")

    val prefs: Flow<UserPrefs> = context.dataStore.data.map { p ->
        UserPrefs(
            accentColorHex = p[accentColorKey] ?: "",
            textColorHex = p[textColorKey] ?: "",
            backgroundColorHex = p[bgColorKey] ?: "",
            font = AppFont.entries.find { it.name == p[fontKey] } ?: AppFont.DEFAULT,
            fontSize = p[fontSizeKey] ?: 16f,
            lastStationJson = p[lastStationKey] ?: "",
            favoritesJson = p[favoritesKey] ?: "[]",
        )
    }

    suspend fun setAccentColor(hex: String) {
        context.dataStore.edit { it[accentColorKey] = hex }
    }

    suspend fun setTextColor(hex: String) {
        context.dataStore.edit { it[textColorKey] = hex }
    }

    suspend fun setBackgroundColor(hex: String) {
        context.dataStore.edit { it[bgColorKey] = hex }
    }

    suspend fun setFont(font: AppFont) {
        context.dataStore.edit { it[fontKey] = font.name }
    }

    suspend fun setFontSize(size: Float) {
        context.dataStore.edit { it[fontSizeKey] = size }
    }

    suspend fun setLastStation(json: String) {
        context.dataStore.edit { it[lastStationKey] = json }
    }

    suspend fun setFavorites(json: String) {
        context.dataStore.edit { it[favoritesKey] = json }
    }
}
