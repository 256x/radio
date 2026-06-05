package fumi.day.literalradio.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fumi.day.literalradio.data.prefs.UserPreferences
import fumi.day.literalradio.data.prefs.UserPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    prefs: UserPreferences,
) : ViewModel() {
    val userPrefs = prefs.prefs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000),
        UserPrefs()
    )
}
