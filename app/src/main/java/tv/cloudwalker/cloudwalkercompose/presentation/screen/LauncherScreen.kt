package tv.cloudwalker.cloudwalkercompose.presentation.screen

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import kotlinx.coroutines.delay
import tv.cloudwalker.cloudwalkercompose.model.MovieRow
import tv.cloudwalker.cloudwalkercompose.model.MovieTile
import tv.cloudwalker.cloudwalkercompose.presentation.viewmodel.LauncherViewModel
import kotlin.math.abs

@Stable
data class OptimizedMovieRow(
    val originalRow: MovieRow,
    val distanceFromViewport: Int,
    val isVisible: Boolean
)

@Stable
data class OptimizedMovieTile(
    val originalTile: MovieTile,
    val shouldLoadHighQuality: Boolean,
    val isInViewport: Boolean,
    val itemIndex: Int
)

@Composable
fun LauncherScreen(
    modifier: Modifier = Modifier,
    viewModel: LauncherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
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
                UltraSmoothContent(
                    rows = uiState.contentRows,
                    onTileClick = viewModel::onTileClick
                )
            }
        }
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
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = "Retry")
        }
    }
}

@Composable
private fun UltraSmoothContent(
    rows: List<MovieRow>,
    onTileClick: (MovieTile) -> Unit
) {
    val listState = rememberLazyListState()

    // ADVANCED OPTIMIZATION: Calculate viewport and virtualization
    val visibleItemsInfo by remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo
        }
    }

    val firstVisibleIndex by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex
        }
    }

    // VIRTUALIZATION: Only process rows near viewport
    val optimizedRows by remember(rows, firstVisibleIndex) {
        derivedStateOf {
            rows.mapIndexed { index, row ->
                val distanceFromViewport = abs(index - firstVisibleIndex)
                OptimizedMovieRow(
                    originalRow = row,
                    distanceFromViewport = distanceFromViewport,
                    isVisible = distanceFromViewport <= 2 // Visible + 2 buffer rows
                )
            }.filter { it.distanceFromViewport <= 5 } // Only render nearby rows
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // GPU OPTIMIZATION: Hardware acceleration
                compositingStrategy = CompositingStrategy.Offscreen
            },
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(vertical = 32.dp),
        // PERFORMANCE: Optimize fling behavior
        flingBehavior = ScrollableDefaults.flingBehavior()
    ) {
        items(
            items = optimizedRows,
            key = { optRow -> "row_${optRow.originalRow.rowIndex}_${optRow.originalRow.rowHeader}" },
            contentType = { optRow -> if (optRow.originalRow.rowIndex == 0) "hero" else "content" }
        ) { optimizedRow ->
            if (optimizedRow.originalRow.rowItems.isNotEmpty()) {
                if (optimizedRow.originalRow.rowIndex == 0) {
                    // Hero Banner Row with original sizes
                    UltraSmoothHeroBannerSection(
                        optimizedRow = optimizedRow,
                        onTileClick = onTileClick
                    )
                } else {
                    // Regular Content Row with original sizes
                    UltraSmoothContentSection(
                        optimizedRow = optimizedRow,
                        onTileClick = onTileClick
                    )
                }
            }
        }
    }
}

@Composable
private fun UltraSmoothHeroBannerSection(
    optimizedRow: OptimizedMovieRow,
    onTileClick: (MovieTile) -> Unit
) {
    val row = optimizedRow.originalRow

    // MEMOIZATION: Prevent unnecessary recomposition
    val memoizedRow = remember(row.rowItems.size, row.rowIndex) { row }
    val listState = rememberLazyListState()

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (memoizedRow.rowHeader.isNotBlank()) {
            Text(
                text = memoizedRow.rowHeader,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 32.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 32.dp),
            modifier = Modifier.graphicsLayer {
                // GPU OPTIMIZATION for horizontal scrolling
                compositingStrategy = CompositingStrategy.Offscreen
            }
        ) {
            items(
                items = memoizedRow.rowItems.mapIndexed { index, tile ->
                    OptimizedMovieTile(
                        originalTile = tile,
                        shouldLoadHighQuality = optimizedRow.isVisible && index < 10, // Limit high quality to first 10
                        isInViewport = optimizedRow.distanceFromViewport == 0,
                        itemIndex = index
                    )
                },
                key = { optTile -> "hero_${memoizedRow.rowIndex}_${optTile.itemIndex}_${optTile.originalTile.tid}" }
            ) { optimizedTile ->
                UltraSmoothHeroBannerTile(
                    optimizedTile = optimizedTile,
                    onTileClick = onTileClick
                )
            }
        }
    }
}

@Composable
private fun UltraSmoothContentSection(
    optimizedRow: OptimizedMovieRow,
    onTileClick: (MovieTile) -> Unit
) {
    val row = optimizedRow.originalRow

    // MEMOIZATION: Stable reference
    val memoizedRow = remember(row.rowIndex, row.rowItems.size) { row }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = memoizedRow.rowHeader,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 32.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 32.dp),
            modifier = Modifier.graphicsLayer {
                // GPU OPTIMIZATION
                compositingStrategy = CompositingStrategy.Offscreen
            }
        ) {
            items(
                items = memoizedRow.rowItems.mapIndexed { index, tile ->
                    OptimizedMovieTile(
                        originalTile = tile,
                        shouldLoadHighQuality = optimizedRow.isVisible && index < 15, // Limit high quality
                        isInViewport = optimizedRow.distanceFromViewport == 0,
                        itemIndex = index
                    )
                },
                key = { optTile -> "content_${memoizedRow.rowIndex}_${optTile.itemIndex}_${optTile.originalTile.tid}" }
            ) { optimizedTile ->
                UltraSmoothMovieTileCard(
                    optimizedTile = optimizedTile,
                    rowLayout = memoizedRow.rowLayout,
                    onTileClick = onTileClick
                )
            }
        }
    }
}

@Composable
private fun UltraSmoothHeroBannerTile(
    optimizedTile: OptimizedMovieTile,
    onTileClick: (MovieTile) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val tile = optimizedTile.originalTile

    // ORIGINAL SIZES MAINTAINED
    val tileWidth = tile.tileWidth?.toIntOrNull()?.dp ?: 1200.dp
    val tileHeight = tile.tileHeight?.toIntOrNull()?.dp ?: 313.dp

    Card(
        onClick = { onTileClick(tile) },
        modifier = Modifier
            .size(width = tileWidth, height = tileHeight)
            .onFocusChanged { isFocused = it.isFocused }
            .then(
                if (isFocused) {
                    Modifier.border(
                        width = 4.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            )
            .graphicsLayer {
                // GPU OPTIMIZATION: Reduce overdraw
                compositingStrategy = if (isFocused) {
                    CompositingStrategy.Offscreen
                } else {
                    CompositingStrategy.Auto
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // SMART IMAGE LOADING: Quality based on viewport distance
            val imageUrl = tile.poster?.replace("http://", "https://")
                ?: tile.background?.replace("http://", "https://")

            // PROGRESSIVE IMAGE LOADING
            var imageLoaded by remember { mutableStateOf(false) }

            LaunchedEffect(optimizedTile.shouldLoadHighQuality) {
                if (optimizedTile.shouldLoadHighQuality) {
                    delay(50) // Small delay to prevent loading storm
                    imageLoaded = true
                }
            }

            if (imageLoaded || optimizedTile.isInViewport) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .size(
                            if (optimizedTile.shouldLoadHighQuality) {
                                Size.ORIGINAL // Full quality when visible
                            } else {
                                Size(600, 200) // Lower quality when distant
                            }
                        )
                        .scale(Scale.FILL)
                        .crossfade(if (optimizedTile.isInViewport) 300 else 150)
                        .memoryCacheKey("hero_${tile.tid}_${optimizedTile.shouldLoadHighQuality}")
                        .build(),
                    contentDescription = null, // Remove for performance
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Ad label
            if (tile.isAdTile) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Ad",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // CONDITIONAL OVERLAYS: Only when focused to reduce GPU load
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 150f
                            )
                        )
                )

                // Content overlay - only when focused
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
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
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UltraSmoothMovieTileCard(
    optimizedTile: OptimizedMovieTile,
    rowLayout: String,
    onTileClick: (MovieTile) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val tile = optimizedTile.originalTile

    // ORIGINAL SIZES MAINTAINED
    val (tileWidth, tileHeight) = when (rowLayout) {
        "landscape" -> {
            val width = tile.tileWidth?.toIntOrNull()?.dp ?: 400.dp
            val height = tile.tileHeight?.toIntOrNull()?.dp ?: 225.dp
            width to height
        }
        "square" -> {
            val width = tile.tileWidth?.toIntOrNull()?.dp ?: 220.dp
            val height = tile.tileHeight?.toIntOrNull()?.dp ?: 220.dp
            width to height
        }
        "portrait" -> {
            val width = tile.tileWidth?.toIntOrNull()?.dp ?: 180.dp
            val height = tile.tileHeight?.toIntOrNull()?.dp ?: 240.dp
            width to height
        }
        else -> 220.dp to 220.dp
    }

    Card(
        onClick = { onTileClick(tile) },
        modifier = Modifier
            .size(width = tileWidth, height = tileHeight)
            .onFocusChanged { isFocused = it.isFocused }
            .then(
                if (isFocused) {
                    Modifier.border(
                        width = 3.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
            .graphicsLayer {
                // GPU OPTIMIZATION
                compositingStrategy = if (isFocused) {
                    CompositingStrategy.Offscreen
                } else {
                    CompositingStrategy.Auto
                }
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box {
            // Choose the right image based on layout
            val imageUrl = when (rowLayout) {
                "landscape" -> tile.poster ?: tile.background
                "portrait", "square" -> tile.portrait ?: tile.poster
                else -> tile.displayImage
            }?.replace("http://", "https://")

            // PROGRESSIVE IMAGE LOADING
            var imageLoaded by remember { mutableStateOf(false) }

            LaunchedEffect(optimizedTile.shouldLoadHighQuality) {
                if (optimizedTile.shouldLoadHighQuality) {
                    delay(30) // Stagger loading
                    imageLoaded = true
                }
            }

            if (imageLoaded || optimizedTile.isInViewport) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .size(
                            if (optimizedTile.shouldLoadHighQuality) {
                                Size(tileWidth.value.toInt(), tileHeight.value.toInt())
                            } else {
                                Size(tileWidth.value.toInt() / 2, tileHeight.value.toInt() / 2)
                            }
                        )
                        .scale(Scale.FILL)
                        .crossfade(if (optimizedTile.isInViewport) 300 else 150)
                        .memoryCacheKey("tile_${tile.tid}_${rowLayout}_${optimizedTile.shouldLoadHighQuality}")
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // CONDITIONAL OVERLAYS: Only when focused and in viewport
            if (rowLayout == "landscape" && isFocused && optimizedTile.isInViewport) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.8f)
                                ),
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
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Ad label
            if (tile.isAdTile) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            RoundedCornerShape(4.dp)
                        )
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
        }
    }
}