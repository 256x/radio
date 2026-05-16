package fumi.day.literalradio.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fumi.day.literalradio.data.api.GENRES
import fumi.day.literalradio.data.model.Station
import fumi.day.literalradio.data.repository.StationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchTab { GENRE, COUNTRY, LANGUAGE, NAME }

sealed class ListUiState {
    data object Idle : ListUiState()
    data object Loading : ListUiState()
    data class Items(val items: List<String>) : ListUiState()
    data class Stations(val stations: List<Station>, val label: String = "") : ListUiState()
    data class Error(val msg: String) : ListUiState()
}

@HiltViewModel
class StationListViewModel @Inject constructor(
    private val repo: StationRepository,
) : ViewModel() {

    private val _tab = MutableStateFlow(SearchTab.GENRE)
    val tab: StateFlow<SearchTab> = _tab.asStateFlow()

    private val _uiState = MutableStateFlow<ListUiState>(ListUiState.Items(GENRES))
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    private val _nameQuery = MutableStateFlow("")
    val nameQuery: StateFlow<String> = _nameQuery.asStateFlow()

    fun selectTab(tab: SearchTab) {
        _tab.update { tab }
        _nameQuery.update { "" }
        when (tab) {
            SearchTab.GENRE -> _uiState.update { ListUiState.Items(GENRES) }
            SearchTab.COUNTRY -> loadCountries()
            SearchTab.LANGUAGE -> loadLanguages()
            SearchTab.NAME -> _uiState.update { ListUiState.Idle }
        }
    }

    fun onNameQueryChanged(query: String) {
        _nameQuery.update { query }
    }

    fun searchByName() {
        val q = _nameQuery.value.trim()
        if (q.isBlank()) return
        loadStations(q) { repo.fetchByName(q) }
    }

    fun selectItem(item: String) {
        when (_tab.value) {
            SearchTab.GENRE -> loadStations(item) { repo.fetchByGenre(item) }
            SearchTab.COUNTRY -> loadStations(item) { repo.fetchByCountry(item) }
            SearchTab.LANGUAGE -> loadStations(item) { repo.fetchByLanguage(item) }
            SearchTab.NAME -> {}
        }
    }

    fun goBack() {
        when (_tab.value) {
            SearchTab.GENRE -> _uiState.update { ListUiState.Items(GENRES) }
            SearchTab.COUNTRY -> loadCountries()
            SearchTab.LANGUAGE -> loadLanguages()
            SearchTab.NAME -> _uiState.update { ListUiState.Idle }
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            _uiState.update { ListUiState.Loading }
            runCatching { repo.getCountries() }
                .onSuccess { _uiState.update { _ -> ListUiState.Items(it) } }
                .onFailure { _uiState.update { _ -> ListUiState.Error(it.message ?: "Error") } }
        }
    }

    private fun loadLanguages() {
        viewModelScope.launch {
            _uiState.update { ListUiState.Loading }
            runCatching { repo.getLanguages() }
                .onSuccess { _uiState.update { _ -> ListUiState.Items(it) } }
                .onFailure { _uiState.update { _ -> ListUiState.Error(it.message ?: "Error") } }
        }
    }

    private fun loadStations(label: String, fetch: suspend () -> List<Station>) {
        viewModelScope.launch {
            _uiState.update { ListUiState.Loading }
            runCatching { fetch() }
                .onSuccess { _uiState.update { _ -> ListUiState.Stations(it, label) } }
                .onFailure { _uiState.update { _ -> ListUiState.Error(it.message ?: "Error") } }
        }
    }
}
