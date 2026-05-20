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

enum class SearchTab { GENRE, COUNTRY, LANGUAGE, FAVORITES }

sealed class ListUiState {
    data object Idle : ListUiState()
    data object Loading : ListUiState()
    data class Items(val items: List<String>) : ListUiState()
    data class Stations(val stations: List<Station>, val label: String = "") : ListUiState()
    data class Error(val msg: String) : ListUiState()
}

sealed class GlobalSearchState {
    data object Idle : GlobalSearchState()
    data object Loading : GlobalSearchState()
    data class Results(val stations: List<Station>, val query: String) : GlobalSearchState()
    data class Error(val msg: String) : GlobalSearchState()
}

@HiltViewModel
class StationListViewModel @Inject constructor(
    private val repo: StationRepository,
) : ViewModel() {

    private val _tab = MutableStateFlow(SearchTab.GENRE)
    val tab: StateFlow<SearchTab> = _tab.asStateFlow()

    private val _uiState = MutableStateFlow<ListUiState>(ListUiState.Items(GENRES))
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    private val _globalSearch = MutableStateFlow<GlobalSearchState>(GlobalSearchState.Idle)
    val globalSearch: StateFlow<GlobalSearchState> = _globalSearch.asStateFlow()

    fun selectTab(tab: SearchTab) {
        _tab.update { tab }
        _globalSearch.update { GlobalSearchState.Idle }
        when (tab) {
            SearchTab.GENRE -> _uiState.update { ListUiState.Items(GENRES) }
            SearchTab.COUNTRY -> loadCountries()
            SearchTab.LANGUAGE -> loadLanguages()
            SearchTab.FAVORITES -> _uiState.update { ListUiState.Idle }
        }
    }

    fun selectItem(item: String) {
        when (_tab.value) {
            SearchTab.GENRE -> loadStations(item) { repo.fetchByGenre(item) }
            SearchTab.COUNTRY -> loadStations(item) { repo.fetchByCountry(item) }
            SearchTab.LANGUAGE -> loadStations(item) { repo.fetchByLanguage(item) }
            SearchTab.FAVORITES -> {}
        }
    }

    fun goBack() {
        when (_tab.value) {
            SearchTab.GENRE -> _uiState.update { ListUiState.Items(GENRES) }
            SearchTab.COUNTRY -> loadCountries()
            SearchTab.LANGUAGE -> loadLanguages()
            SearchTab.FAVORITES -> _uiState.update { ListUiState.Idle }
        }
    }

    fun searchGlobal(query: String) {
        if (query.isBlank()) {
            _globalSearch.update { GlobalSearchState.Idle }
            return
        }
        viewModelScope.launch {
            _globalSearch.update { GlobalSearchState.Loading }
            runCatching { repo.fetchByName(query) }
                .onSuccess { _globalSearch.update { _ -> GlobalSearchState.Results(it, query) } }
                .onFailure { _globalSearch.update { _ -> GlobalSearchState.Error(it.message ?: "Error") } }
        }
    }

    fun clearGlobalSearch() {
        _globalSearch.update { GlobalSearchState.Idle }
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
