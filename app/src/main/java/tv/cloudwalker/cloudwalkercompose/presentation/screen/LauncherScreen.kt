package tv.cloudwalker.cloudwalkercompose.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import tv.cloudwalker.cloudwalkercompose.model.MovieRow
import tv.cloudwalker.cloudwalkercompose.model.MovieTile
import tv.cloudwalker.cloudwalkercompose.presentation.viewmodel.LauncherViewModel
import tv.cloudwalker.cloudwalkercompose.presentation.components.RightSideNavigationDrawer
import tv.cloudwalker.cloudwalkercompose.presentation.components.TVTopNavigationBar

@Composable
fun LauncherScreen(
    modifier: Modifier = Modifier,
    viewModel: LauncherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigation Drawer State
    var isDrawerOpen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main Content - DISABLE when drawer is open
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isDrawerOpen) {
                        // When drawer is open, make background non-focusable
                        Modifier
                            .graphicsLayer { alpha = 0.3f } // Dim the background
                            .onKeyEvent { true } // Block ALL key events
                    } else {
                        Modifier // Normal behavior when drawer is closed
                    }
                )
        ) {
            when {
                uiState.isLoading && uiState.contentRows.isEmpty() -> {
                    LoadingContent()
                }

                uiState.error != null && uiState.contentRows.isEmpty() -> {
                    ErrorContent(
                        error = uiState.error ?: "Unknown error",
                        onRetry = viewModel::onRefresh
                    )
                }

                else -> {
                    ScrollableContentWithTopNav(
                        rows = uiState.contentRows,
                        onTileClick = viewModel::onTileClick,
                        isDrawerOpen = isDrawerOpen, // Pass drawer state
                        onProfileClick = {
                            isDrawerOpen = true
                        },
                        onSearchClick = {
                            // Handle search click
                        },
                        onSettingsClick = {
                            // Handle settings click
                        },
                        onWifiClick = {
                            // Handle wifi click
                        },
                        onExitClick = {
                            // Handle exit click
                        },
                        onAppsClick = {
                            // Handle apps click
                        }
                    )
                }
            }
        }

        // Right Side Navigation Drawer
        RightSideNavigationDrawer(
            isOpen = isDrawerOpen,
            onClose = { isDrawerOpen = false },
            drawerWidth = 0.5f,
            userName = "Sandra Adams",
            userEmail = "sandra_a88@gmail.com",
            onMyFilesClick = {
                isDrawerOpen = false
            },
            onSharedClick = {
                isDrawerOpen = false
            },
            onStarredClick = {
                isDrawerOpen = false
            },
            onRecentClick = {
                isDrawerOpen = false
            },
            onOfflineClick = {
                isDrawerOpen = false
            },
            onUploadsClick = {
                isDrawerOpen = false
            },
            onBackupsClick = {
                isDrawerOpen = false
            }
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = Color.White
        )
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error loading content",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.focusRequester(focusRequester),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = "Retry")
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun ScrollableContentWithTopNav(
    rows: List<MovieRow>,
    onTileClick: (MovieTile) -> Unit,
    isDrawerOpen: Boolean, // NEW: Drawer state
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onWifiClick: () -> Unit,
    onExitClick: () -> Unit,
    onAppsClick: () -> Unit
) {
    val listState = rememberLazyListState()
    val (mainColumn, topNavSection) = remember { FocusRequester.createRefs() }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (!isDrawerOpen) {
                    // Only allow focus when drawer is closed
                    Modifier
                        .focusRequester(mainColumn)
                        .focusRestorer(topNavSection)
                } else {
                    // When drawer is open, disable all focus
                    Modifier
                }
            )
            .graphicsLayer { alpha = 1f },
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        userScrollEnabled = !isDrawerOpen // Disable scrolling when drawer is open
    ) {
        item(key = "top_nav") {
            TVTopNavigationBar(
                modifier = if (!isDrawerOpen) {
                    Modifier.focusRequester(topNavSection)
                } else {
                    Modifier // No focus when drawer is open
                },
                onProfileClick = onProfileClick,
                onSearchClick = onSearchClick,
                onSettingsClick = onSettingsClick,
                onWifiClick = onWifiClick,
                onExitClick = onExitClick,
                onAppsClick = onAppsClick
            )
        }

        // Content rows
        itemsIndexed(
            items = rows,
            key = { _, row -> "row_${row.rowIndex}_${row.rowHeader}" },
            contentType = { _, row -> if (row.rowIndex == 0) "hero" else "content" }
        ) { index, row ->
            if (row.rowItems.isNotEmpty()) {
                if (row.rowIndex == 0) {
                    HeroBannerRowHeader(
                        row = row,
                        onTileClick = onTileClick,
                        isDrawerOpen = isDrawerOpen, // Pass drawer state
                        modifier = Modifier.padding(top = 24.dp)
                    )
                } else {
                    ContentRowHeader(
                        row = row,
                        onTileClick = onTileClick,
                        isDrawerOpen = isDrawerOpen, // Pass drawer state
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroBannerRowHeader(
    row: MovieRow,
    onTileClick: (MovieTile) -> Unit,
    isDrawerOpen: Boolean, // NEW: Drawer state
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(vertical = 8.dp)
            .then(
                if (!isDrawerOpen) {
                    Modifier.focusGroup() // Only allow focus when drawer is closed
                } else {
                    Modifier // No focus when drawer is open
                }
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val (lazyRow, firstItem) = remember { FocusRequester.createRefs() }

        if (row.rowHeader.isNotBlank()) {
            Text(
                text = row.rowHeader,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 32.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        LazyRow(
            modifier = if (!isDrawerOpen) {
                Modifier
                    .focusRequester(lazyRow)
                    .focusRestorer(firstItem)
            } else {
                Modifier // No focus when drawer is open
            },
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 32.dp),
            userScrollEnabled = !isDrawerOpen // Disable scrolling when drawer is open
        ) {
            itemsIndexed(
                items = row.rowItems,
                key = { index, tile -> "hero_${row.rowIndex}_${index}_${tile.tid}" }
            ) { index, tile ->
                HeroBannerTile(
                    tile = tile,
                    onTileClick = onTileClick,
                    isDrawerOpen = isDrawerOpen, // Pass drawer state
                    modifier = if (index == 0 && !isDrawerOpen) {
                        Modifier.focusRequester(firstItem)
                    } else {
                        Modifier
                    }
                )
            }
        }
    }
}

@Composable
private fun ContentRowHeader(
    row: MovieRow,
    onTileClick: (MovieTile) -> Unit,
    isDrawerOpen: Boolean, // NEW: Drawer state
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(vertical = 8.dp)
            .then(
                if (!isDrawerOpen) {
                    Modifier.focusGroup() // Only allow focus when drawer is closed
                } else {
                    Modifier // No focus when drawer is open
                }
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val (lazyRow, firstItem) = remember { FocusRequester.createRefs() }

        Text(
            text = row.rowHeader,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 32.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        LazyRow(
            modifier = if (!isDrawerOpen) {
                Modifier
                    .focusRequester(lazyRow)
                    .focusRestorer(firstItem)
            } else {
                Modifier // No focus when drawer is open
            },
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 32.dp),
            userScrollEnabled = !isDrawerOpen // Disable scrolling when drawer is open
        ) {
            itemsIndexed(
                items = row.rowItems,
                key = { index, tile -> "content_${row.rowIndex}_${index}_${tile.tid}" }
            ) { index, tile ->
                ContentTile(
                    tile = tile,
                    rowLayout = row.rowLayout,
                    onTileClick = onTileClick,
                    isDrawerOpen = isDrawerOpen, // Pass drawer state
                    modifier = if (index == 0 && !isDrawerOpen) {
                        Modifier.focusRequester(firstItem)
                    } else {
                        Modifier
                    }
                )
            }
        }
    }
}

// Pre-calculate tile dimensions to avoid recomposition
@Stable
data class TileDimensions(
    val width: Dp,
    val height: Dp
)

@Composable
private fun getTileDimensions(tile: MovieTile, rowLayout: String): TileDimensions {
    return remember(tile.tid, rowLayout) {
        when (rowLayout) {
            "landscape" -> TileDimensions(
                width = tile.tileWidth?.toIntOrNull()?.dp ?: 400.dp,
                height = tile.tileHeight?.toIntOrNull()?.dp ?: 225.dp
            )
            "square" -> TileDimensions(
                width = tile.tileWidth?.toIntOrNull()?.dp ?: 220.dp,
                height = tile.tileHeight?.toIntOrNull()?.dp ?: 220.dp
            )
            "portrait" -> TileDimensions(
                width = tile.tileWidth?.toIntOrNull()?.dp ?: 180.dp,
                height = tile.tileHeight?.toIntOrNull()?.dp ?: 240.dp
            )
            else -> TileDimensions(width = 220.dp, height = 220.dp)
        }
    }
}

@Composable
private fun getImageUrl(tile: MovieTile, rowLayout: String): String? {
    return remember(tile.tid, rowLayout) {
        when (rowLayout) {
            "landscape" -> tile.poster ?: tile.background
            "portrait", "square" -> tile.portrait ?: tile.poster
            else -> tile.displayImage
        }?.replace("http://", "https://")
    }
}

@Composable
private fun HeroBannerTile(
    tile: MovieTile,
    onTileClick: (MovieTile) -> Unit,
    isDrawerOpen: Boolean, // NEW: Drawer state
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Reset focus when drawer opens
    LaunchedEffect(isDrawerOpen) {
        if (isDrawerOpen) {
            isFocused = false
        }
    }

    // Pre-calculate dimensions
    val tileWidth = remember(tile.tid) { tile.tileWidth?.toIntOrNull()?.dp ?: 1200.dp }
    val tileHeight = remember(tile.tid) { tile.tileHeight?.toIntOrNull()?.dp ?: 313.dp }
    val imageUrl = remember(tile.tid) {
        tile.poster?.replace("http://", "https://") ?: tile.background?.replace("http://", "https://")
    }

    val imageRequest = remember(tile.tid, imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .size(Size.ORIGINAL)
            .scale(Scale.FILL)
            .crossfade(200)
            .memoryCacheKey("hero_${tile.tid}")
            .build()
    }

    Card(
        onClick = { if (!isDrawerOpen) onTileClick(tile) }, // Only clickable when drawer is closed
        modifier = modifier
            .size(width = tileWidth, height = tileHeight)
            .then(
                if (!isDrawerOpen) {
                    Modifier.onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    }
                } else {
                    Modifier // No focus handling when drawer is open
                }
            )
            .then(
                if (isFocused && !isDrawerOpen) {
                    Modifier.border(4.dp, Color.White, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        HeroBannerTileContent(
            tile = tile,
            imageRequest = imageRequest,
            isFocused = isFocused && !isDrawerOpen // Only show focus when drawer is closed
        )
    }
}

@Composable
private fun ContentTile(
    tile: MovieTile,
    rowLayout: String,
    onTileClick: (MovieTile) -> Unit,
    isDrawerOpen: Boolean, // NEW: Drawer state
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Reset focus when drawer opens
    LaunchedEffect(isDrawerOpen) {
        if (isDrawerOpen) {
            isFocused = false
        }
    }

    val dimensions = getTileDimensions(tile, rowLayout)
    val imageUrl = getImageUrl(tile, rowLayout)

    val imageRequest = remember(tile.tid, rowLayout, imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .size(Size(dimensions.width.value.toInt(), dimensions.height.value.toInt()))
            .scale(Scale.FILL)
            .crossfade(150)
            .memoryCacheKey("tile_${tile.tid}_${rowLayout}")
            .build()
    }

    Card(
        onClick = { if (!isDrawerOpen) onTileClick(tile) }, // Only clickable when drawer is closed
        modifier = modifier
            .size(width = dimensions.width, height = dimensions.height)
            .then(
                if (!isDrawerOpen) {
                    Modifier.onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    }
                } else {
                    Modifier // No focus handling when drawer is open
                }
            )
            .then(
                if (isFocused && !isDrawerOpen) {
                    Modifier.border(3.dp, Color.White, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        ContentTileContent(
            tile = tile,
            rowLayout = rowLayout,
            imageRequest = imageRequest,
            isFocused = isFocused && !isDrawerOpen // Only show focus when drawer is closed
        )
    }
}

@Composable
private fun HeroBannerTileContent(
    tile: MovieTile,
    imageRequest: ImageRequest,
    isFocused: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = imageRequest,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Ad indicator - only if needed
        if (tile.isAdTile) {
            AdIndicator()
        }

        // Focus overlay - separate composable to minimize recomposition
        if (isFocused) {
            FocusOverlay(tile = tile)
        }
    }
}

@Composable
private fun ContentTileContent(
    tile: MovieTile,
    rowLayout: String,
    imageRequest: ImageRequest,
    isFocused: Boolean
) {
    Box {
        AsyncImage(
            model = imageRequest,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Ad indicator
        if (tile.isAdTile) {
            AdIndicator()
        }

        // Title overlay for landscape tiles when focused
        if (rowLayout == "landscape" && isFocused) {
            LandscapeFocusOverlay(tile = tile)
        }
    }
}

@Composable
private fun AdIndicator() {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .background(Color.Black.copy(0.7f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Ad",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FocusOverlay(tile: MovieTile) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(0.7f)),
                    startY = 150f
                )
            )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight(Alignment.Bottom)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = tile.title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (!tile.synopsis.isNullOrBlank()) {
            Text(
                text = tile.synopsis,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(0.9f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LandscapeFocusOverlay(tile: MovieTile) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(0.8f)),
                    startY = 100f
                )
            )
    )

    Text(
        text = tile.title,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        ),
        color = Color.White,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight(Alignment.Bottom)
            .padding(12.dp),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}