package fumi.day.literalradio.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fumi.day.literalradio.data.prefs.AppFont
import fumi.day.literalradio.data.prefs.UserPreferences
import fumi.day.literalradio.data.repository.StationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferences,
    private val repo: StationRepository,
) : ViewModel() {

    val state = prefs.prefs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000),
        fumi.day.literalradio.data.prefs.UserPrefs()
    )

    fun setAccentColor(hex: String) { viewModelScope.launch { prefs.setAccentColor(hex) } }
    fun setTextColor(hex: String) { viewModelScope.launch { prefs.setTextColor(hex) } }
    fun setBackgroundColor(hex: String) { viewModelScope.launch { prefs.setBackgroundColor(hex) } }
    fun setFont(font: AppFont) { viewModelScope.launch { prefs.setFont(font) } }
    fun setFontSize(size: Float) { viewModelScope.launch { prefs.setFontSize(size.coerceIn(12f, 24f)) } }
    fun clearCache() { repo.clearListCache() }
}
