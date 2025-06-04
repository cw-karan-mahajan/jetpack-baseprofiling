package tv.cloudwalker.cloudwalkercompose.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tv.cloudwalker.cloudwalkercompose.model.MovieResponse
import tv.cloudwalker.cloudwalkercompose.model.MovieRow
import tv.cloudwalker.cloudwalkercompose.model.MovieTile
import tv.cloudwalker.cloudwalkercompose.util.Resource
import javax.inject.Inject
import tv.cloudwalker.cloudwalkercompose.repository.MovieRepository

@HiltViewModel
class LauncherViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    init {
        loadHomeScreenData()
    }

    fun loadHomeScreenData() {
        viewModelScope.launch {
            repository.getHomeScreenData().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    }
                    is Resource.Success -> {
                        val data = resource.data!!
                        val processedRows = processMovieRows(data.rows)

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

    private fun processMovieRows(rows: List<MovieRow>): List<MovieRow> {
        return rows.filter { row ->
            row.rowItems.isNotEmpty()
        }.map { row ->
            // Process each row based on its layout and requirements
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

    private fun handleAdTileClick(tile: MovieTile) {
        // Implement ad click handling
        // Track click events, open URLs, etc.
    }

    private fun handleDetailPageNavigation(tile: MovieTile) {
        // Navigate to detail screen
        // You can emit navigation events or use navigation component
    }

    private fun handleAppLaunch(tile: MovieTile) {
        // Launch the app using package name
        // Check if app is installed, launch or redirect to play store
    }

    private fun handleContentTileClick(tile: MovieTile) {
        // Handle regular content tiles
        // Play content, open streaming app, etc.
    }

    fun onRefresh() {
        loadHomeScreenData()
    }
}

data class LauncherUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val contentRows: List<MovieRow> = emptyList(),
    val movieResponse: MovieResponse? = null
)