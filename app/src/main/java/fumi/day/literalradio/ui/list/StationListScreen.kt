package fumi.day.literalradio.ui.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fumi.day.literalradio.data.model.Station
import fumi.day.literalradio.ui.AppViewModel
import fumi.day.literalradio.ui.shared.MiniPlayer

private val TABS = listOf("Genre", "Country", "Language", "Favorites")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StationListScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    appViewModel: AppViewModel,
    viewModel: StationListViewModel = hiltViewModel(),
) {
    val tab by viewModel.tab.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val globalSearch by viewModel.globalSearch.collectAsState()
    val favorites by appViewModel.favorites.collectAsState()
    val playerState by appViewModel.playerState.collectAsState()

    var filterQuery by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    LaunchedEffect(tab, uiState) { filterQuery = "" }
    LaunchedEffect(tab) { searchQuery = ""; viewModel.clearGlobalSearch() }

    val isShowingStations = uiState is ListUiState.Stations
    if (isShowingStations) {
        BackHandler { viewModel.goBack() }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { Text("Literal Radio", style = MaterialTheme.typography.titleMedium) },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                )
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                TabRow(selectedTabIndex = tab.ordinal) {
                    TABS.forEachIndexed { i, label ->
                        Tab(
                            selected = tab.ordinal == i,
                            onClick = { viewModel.selectTab(SearchTab.entries[i]) },
                            text = { Text(label, style = MaterialTheme.typography.bodySmall) },
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (playerState.station != null) {
                MiniPlayer(
                    playerState = playerState,
                    onToggle = { appViewModel.togglePlayback() },
                    onClick = onNavigateToPlayer,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (tab == SearchTab.FAVORITES) {
                FavoritesContent(
                    favorites = favorites,
                    globalSearch = globalSearch,
                    searchQuery = searchQuery,
                    onSearchQueryChanged = { searchQuery = it },
                    onSearch = { viewModel.searchGlobal(searchQuery) },
                    onClearSearch = { searchQuery = ""; viewModel.clearGlobalSearch() },
                    playerState = playerState,
                    onPlay = { appViewModel.play(it) },
                    onToggleFavorite = { appViewModel.toggleFavorite(it) },
                    isFavorite = { appViewModel.isFavorite(it) },
                )
            } else {
                when (val state = uiState) {
                    is ListUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                    is ListUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.msg, color = MaterialTheme.colorScheme.error)
                    }

                    is ListUiState.Idle -> {}

                    is ListUiState.Items -> {
                        val filtered = if (filterQuery.isBlank()) state.items
                        else state.items.filter { it.contains(filterQuery, ignoreCase = true) }
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(filtered) { item ->
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(onClick = { viewModel.selectItem(item) })
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                        FilterBar(query = filterQuery, onQueryChanged = { filterQuery = it })
                    }

                    is ListUiState.Stations -> {
                        val filtered = if (filterQuery.isBlank()) state.stations
                        else state.stations.filter {
                            it.name.contains(filterQuery, ignoreCase = true) ||
                            it.countryCode.contains(filterQuery, ignoreCase = true)
                        }
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            if (state.label.isNotBlank()) {
                                item {
                                    Text(
                                        text = state.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                            items(filtered) { station ->
                                StationRow(
                                    station = station,
                                    isPlaying = playerState.station?.url == station.url && playerState.isPlaying,
                                    isFavorite = appViewModel.isFavorite(station),
                                    onClick = { appViewModel.play(station) },
                                    onLongClick = { appViewModel.toggleFavorite(station) },
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                        FilterBar(query = filterQuery, onQueryChanged = { filterQuery = it })
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesContent(
    favorites: List<Station>,
    globalSearch: GlobalSearchState,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onClearSearch: () -> Unit,
    playerState: fumi.day.literalradio.ui.PlayerState,
    onPlay: (Station) -> Unit,
    onToggleFavorite: (Station) -> Unit,
    isFavorite: (Station) -> Boolean,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        when (globalSearch) {
            is GlobalSearchState.Idle -> {
                if (favorites.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "No favorites yet.\nLong-press any station to add.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(favorites, key = { it.url }) { station ->
                            StationRow(
                                station = station,
                                isPlaying = playerState.station?.url == station.url && playerState.isPlaying,
                                isFavorite = isFavorite(station),
                                onClick = { onPlay(station) },
                                onLongClick = { onToggleFavorite(station) },
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
            is GlobalSearchState.Loading -> {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is GlobalSearchState.Results -> {
                if (globalSearch.stations.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "No results for \"${globalSearch.query}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(globalSearch.stations, key = { it.url }) { station ->
                            StationRow(
                                station = station,
                                isPlaying = playerState.station?.url == station.url && playerState.isPlaying,
                                isFavorite = isFavorite(station),
                                onClick = { onPlay(station) },
                                onLongClick = { onToggleFavorite(station) },
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
            is GlobalSearchState.Error -> {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(globalSearch.msg, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        GlobalSearchBar(
            query = searchQuery,
            onQueryChanged = onSearchQueryChanged,
            onSearch = onSearch,
            onClear = onClearSearch,
            isActive = globalSearch !is GlobalSearchState.Idle,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StationRow(
    station: Station,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = station.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isFavorite) {
                Text(
                    text = "★",
                    style = MaterialTheme.typography.bodySmall,
                    color = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.padding(bottom = 1.dp),
                )
            }
            Text(
                text = station.countryCode,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GlobalSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    isActive: Boolean,
) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text("Search all stations…", style = MaterialTheme.typography.bodySmall) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (isActive) {
                IconButton(onClick = onClear) {
                    Text("×", style = MaterialTheme.typography.titleMedium)
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun FilterBar(query: String, onQueryChanged: (String) -> Unit) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text("Filter…", style = MaterialTheme.typography.bodySmall) },
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}
