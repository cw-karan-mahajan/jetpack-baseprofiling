package tv.cloudwalker.cloudwalkercompose.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tv.cloudwalker.cloudwalkercompose.model.MovieResponse
import tv.cloudwalker.cloudwalkercompose.model.MovieRow
import tv.cloudwalker.cloudwalkercompose.model.MovieTile
import tv.cloudwalker.cloudwalkercompose.repository.MovieRepository
import tv.cloudwalker.cloudwalkercompose.util.Resource
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    // Debounced loading to prevent multiple rapid calls
    private var loadingJob: Job? = null

    init {
        loadHomeScreenData()
    }

    fun loadHomeScreenData() {
        loadingJob?.cancel()
        loadingJob = viewModelScope.launch {
            repository.getHomeScreenData()
                .flowOn(Dispatchers.IO) // Process on IO dispatcher
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = true,
                                error = null
                            )
                        }
                        is Resource.Success -> {
                            val data = resource.data!!
                            // Process in background thread
                            val processedRows = withContext(Dispatchers.Default) {
                                processMovieRowsOptimized(data.rows)
                            }

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = null,
                                contentRows = processedRows,
                                movieResponse = data
                            )
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = resource.message
                            )
                        }
                    }
                }
        }
    }

    private fun processMovieRowsOptimized(rows: List<MovieRow>): List<MovieRow> {
        return rows.asSequence()
            .filter { row -> row.rowItems.isNotEmpty() }
            .map { row ->
                when {
                    row.rowIndex == 0 -> {
                        // First row - hero banner row (landscape layout)
                        row.copy(
                            rowLayout = "landscape",
                            rowItems = row.rowItems.map { tile ->
                                tile.copy(
                                    tileWidth = tile.tileWidth ?: "1200",
                                    tileHeight = tile.tileHeight ?: "313"
                                )
                            }
                        )
                    }
                    row.rowLayout == "square" -> {
                        // Square layout rows
                        row.copy(
                            rowItems = row.rowItems.map { tile ->
                                tile.copy(
                                    tileWidth = tile.tileWidth ?: "220",
                                    tileHeight = tile.tileHeight ?: "220"
                                )
                            }
                        )
                    }
                    row.rowLayout == "portrait" -> {
                        // Portrait layout rows
                        row.copy(
                            rowItems = row.rowItems.map { tile ->
                                tile.copy(
                                    tileWidth = tile.tileWidth ?: "180",
                                    tileHeight = tile.tileHeight ?: "240"
                                )
                            }
                        )
                    }
                    else -> row
                }
            }
            .toList()
    }

    fun onTileClick(tile: MovieTile) {
        // Handle tile click based on tile type
        viewModelScope.launch {
            when {
                tile.isAdTile -> {
                    // Handle ad tile click
                    handleAdTileClick(tile)
                }
                tile.detailPage -> {
                    // Navigate to detail page
                    handleDetailPageNavigation(tile)
                }
                !tile.packageName.isNullOrEmpty() -> {
                    // Launch app or handle app-specific logic
                    handleAppLaunch(tile)
                }
                else -> {
                    // Handle regular content tile
                    handleContentTileClick(tile)
                }
            }
        }
    }

    private suspend fun handleAdTileClick(tile: MovieTile) {
        // Implement ad click handling
        // Track click events, open URLs, etc.
        withContext(Dispatchers.IO) {
            // Ad click logic here
        }
    }

    private suspend fun handleDetailPageNavigation(tile: MovieTile) {
        // Navigate to detail screen
        // You can emit navigation events or use navigation component
        withContext(Dispatchers.Main) {
            // Navigation logic here
        }
    }

    private suspend fun handleAppLaunch(tile: MovieTile) {
        // Launch the app using package name
        // Check if app is installed, launch or redirect to play store
        withContext(Dispatchers.IO) {
            // App launch logic here
        }
    }

    private suspend fun handleContentTileClick(tile: MovieTile) {
        // Handle regular content tiles
        // Play content, open streaming app, etc.
        withContext(Dispatchers.IO) {
            // Content tile click logic here
        }
    }

    fun onRefresh() {
        loadHomeScreenData()
    }

    override fun onCleared() {
        super.onCleared()
        loadingJob?.cancel()
    }
}

data class LauncherUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val contentRows: List<MovieRow> = emptyList(),
    val movieResponse: MovieResponse? = null
)