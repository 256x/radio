package fumi.day.literalradio.ui.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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

private val TABS = listOf("Genre", "Country", "Language", "Name")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationListScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    appViewModel: AppViewModel,
    viewModel: StationListViewModel = hiltViewModel(),
) {
    val tab by viewModel.tab.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val nameQuery by viewModel.nameQuery.collectAsState()
    val playerState by appViewModel.playerState.collectAsState()

    var filterQuery by remember { mutableStateOf("") }
    LaunchedEffect(tab, uiState) { filterQuery = "" }

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
            when (val state = uiState) {
                is ListUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                is ListUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.msg, color = MaterialTheme.colorScheme.error)
                }

                is ListUiState.Idle -> {
                    if (tab == SearchTab.NAME) {
                        NameSearchBar(
                            query = nameQuery,
                            onQueryChanged = viewModel::onNameQueryChanged,
                            onSearch = viewModel::searchByName,
                        )
                    }
                }

                is ListUiState.Items -> {
                    if (tab == SearchTab.NAME) {
                        NameSearchBar(
                            query = nameQuery,
                            onQueryChanged = viewModel::onNameQueryChanged,
                            onSearch = viewModel::searchByName,
                        )
                    }
                    val filtered = if (filterQuery.isBlank()) state.items
                    else state.items.filter { it.contains(filterQuery, ignoreCase = true) }
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filtered) { item ->
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectItem(item) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                    if (tab != SearchTab.NAME) {
                        FilterBar(query = filterQuery, onQueryChanged = { filterQuery = it })
                    }
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
                                onClick = { appViewModel.play(station) },
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

@Composable
private fun StationRow(station: Station, isPlaying: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
        Text(
            text = station.countryCode,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
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

@Composable
private fun NameSearchBar(query: String, onQueryChanged: (String) -> Unit, onSearch: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            placeholder = { Text("Station name…") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onSearch) {
            Icon(Icons.Default.Search, contentDescription = "Search")
        }
    }
}
