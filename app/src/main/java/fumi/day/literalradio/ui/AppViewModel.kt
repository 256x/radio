package fumi.day.literalradio.ui

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import fumi.day.literalradio.data.model.Station
import fumi.day.literalradio.data.prefs.UserPreferences
import fumi.day.literalradio.service.RadioService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerState(
    val station: Station? = null,
    val isPlaying: Boolean = false,
    val trackTitle: String = "",
)

@HiltViewModel
class AppViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: UserPreferences,
    private val gson: Gson,
) : ViewModel() {

    val userPrefs = prefs.prefs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000),
        fumi.day.literalradio.data.prefs.UserPrefs()
    )

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _favorites = MutableStateFlow<List<Station>>(emptyList())
    val favorites: StateFlow<List<Station>> = _favorites.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.prefs.collect { p ->
                val type = object : TypeToken<List<Station>>() {}.type
                _favorites.value = runCatching {
                    gson.fromJson<List<Station>>(p.favoritesJson, type) ?: emptyList()
                }.getOrDefault(emptyList())
            }
        }
    }

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    init {
        val sessionToken = SessionToken(context, ComponentName(context, RadioService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            controller?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playerState.update { it.copy(isPlaying = isPlaying) }
                }

                override fun onMediaMetadataChanged(mediaMetadata: androidx.media3.common.MediaMetadata) {
                    val title = mediaMetadata.title?.toString() ?: ""
                    _playerState.update { it.copy(trackTitle = title) }
                }
            })
        }, MoreExecutors.directExecutor())
    }

    fun play(station: Station) {
        _playerState.update { it.copy(station = station, trackTitle = "") }
        viewModelScope.launch {
            prefs.setLastStation(stationToJson(station))
        }
        val item = MediaItem.Builder()
            .setUri(station.url)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(station.name)
                    .build()
            )
            .build()
        controller?.setMediaItem(item)
        controller?.prepare()
        controller?.play()
    }

    fun stop() {
        controller?.stop()
        _playerState.update { it.copy(isPlaying = false) }
    }

    fun togglePlayback() {
        if (_playerState.value.isPlaying) stop() else {
            _playerState.value.station?.let { play(it) }
        }
    }

    fun toggleFavorite(station: Station) {
        val current = _favorites.value.toMutableList()
        if (current.any { it.url == station.url }) current.removeAll { it.url == station.url }
        else current.add(0, station)
        viewModelScope.launch { prefs.setFavorites(gson.toJson(current)) }
    }

    fun isFavorite(station: Station): Boolean = _favorites.value.any { it.url == station.url }

    override fun onCleared() {
        MediaController.releaseFuture(controllerFuture ?: return)
        super.onCleared()
    }

    private fun stationToJson(station: Station): String =
        """{"name":"${station.name}","country":"${station.country}","countryCode":"${station.countryCode}","bitrate":${station.bitrate},"codec":"${station.codec}","language":"${station.language}","url":"${station.url}"}"""
}
