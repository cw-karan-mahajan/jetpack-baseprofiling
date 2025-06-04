package tv.cloudwalker.cloudwalkercompose.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import tv.cloudwalker.cloudwalkercompose.model.MovieRow
import tv.cloudwalker.cloudwalkercompose.model.MovieTile
import tv.cloudwalker.cloudwalkercompose.presentation.viewmodel.LauncherViewModel
import kotlin.math.abs

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
                SimpleContent(
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
private fun SimpleContent(
    rows: List<MovieRow>,
    onTileClick: (MovieTile) -> Unit
) {
    val listState = rememberLazyListState()

    // Simple viewport detection (optional optimization)
    val visibleRowIndices by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            firstVisible..lastVisible
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = 1f }, // Simple GPU optimization
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(vertical = 32.dp)
    ) {
        items(
            items = rows,
            key = { row -> "row_${row.rowIndex}_${row.rowHeader}" },
            contentType = { row -> if (row.rowIndex == 0) "hero" else "content" }
        ) { row ->
            if (row.rowItems.isNotEmpty()) {
                val isVisible = row.rowIndex in visibleRowIndices

                if (row.rowIndex == 0) {
                    HeroBannerRow(
                        row = row,
                        onTileClick = onTileClick,
                        isVisible = isVisible
                    )
                } else {
                    ContentRow(
                        row = row,
                        onTileClick = onTileClick,
                        isVisible = isVisible
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroBannerRow(
    row: MovieRow,
    onTileClick: (MovieTile) -> Unit,
    isVisible: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) {
            items(
                items = row.rowItems.mapIndexed { index, tile -> index to tile },
                key = { (index, tile) -> "hero_${row.rowIndex}_${index}_${tile.tid}" }
            ) { (index, tile) ->
                HeroBannerTile(
                    tile = tile,
                    onTileClick = onTileClick,
                    loadHighQuality = isVisible && index < 5 // Only first 5 visible tiles get high quality
                )
            }
        }
    }
}

@Composable
private fun ContentRow(
    row: MovieRow,
    onTileClick: (MovieTile) -> Unit,
    isVisible: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) {
            items(
                items = row.rowItems.mapIndexed { index, tile -> index to tile },
                key = { (index, tile) -> "content_${row.rowIndex}_${index}_${tile.tid}" }
            ) { (index, tile) ->
                ContentTile(
                    tile = tile,
                    rowLayout = row.rowLayout,
                    onTileClick = onTileClick,
                    loadHighQuality = isVisible && index < 8 // Only first 8 visible tiles get high quality
                )
            }
        }
    }
}

@Composable
private fun HeroBannerTile(
    tile: MovieTile,
    onTileClick: (MovieTile) -> Unit,
    loadHighQuality: Boolean
) {
    var isFocused by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val tileWidth = remember(tile.tid) { tile.tileWidth?.toIntOrNull()?.dp ?: 1200.dp }
    val tileHeight = remember(tile.tid) { tile.tileHeight?.toIntOrNull()?.dp ?: 313.dp }

    Card(
        onClick = { onTileClick(tile) },
        modifier = Modifier
            .size(width = tileWidth, height = tileHeight)
            .onFocusChanged { isFocused = it.isFocused }
            .then(
                if (isFocused) {
                    Modifier.border(4.dp, Color.White, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imageUrl = remember(tile.tid) {
                tile.poster?.replace("http://", "https://")
                    ?: tile.background?.replace("http://", "https://")
            }

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .size(if (loadHighQuality) Size.ORIGINAL else Size(600, 200))
                    .scale(Scale.FILL)
                    .crossfade(200)
                    .memoryCacheKey("hero_${tile.tid}")
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Ad indicator
            if (tile.isAdTile) {
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

            // Overlay when focused
            if (isFocused) {
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
                            color = Color.White.copy(0.9f),
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
private fun ContentTile(
    tile: MovieTile,
    rowLayout: String,
    onTileClick: (MovieTile) -> Unit,
    loadHighQuality: Boolean
) {
    var isFocused by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val (tileWidth, tileHeight) = remember(tile.tid, rowLayout) {
        when (rowLayout) {
            "landscape" -> {
                (tile.tileWidth?.toIntOrNull()?.dp ?: 400.dp) to
                        (tile.tileHeight?.toIntOrNull()?.dp ?: 225.dp)
            }
            "square" -> {
                (tile.tileWidth?.toIntOrNull()?.dp ?: 220.dp) to
                        (tile.tileHeight?.toIntOrNull()?.dp ?: 220.dp)
            }
            "portrait" -> {
                (tile.tileWidth?.toIntOrNull()?.dp ?: 180.dp) to
                        (tile.tileHeight?.toIntOrNull()?.dp ?: 240.dp)
            }
            else -> 220.dp to 220.dp
        }
    }

    Card(
        onClick = { onTileClick(tile) },
        modifier = Modifier
            .size(width = tileWidth, height = tileHeight)
            .onFocusChanged { isFocused = it.isFocused }
            .then(
                if (isFocused) {
                    Modifier.border(3.dp, Color.White, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box {
            val imageUrl = remember(tile.tid, rowLayout) {
                when (rowLayout) {
                    "landscape" -> tile.poster ?: tile.background
                    "portrait", "square" -> tile.portrait ?: tile.poster
                    else -> tile.displayImage
                }?.replace("http://", "https://")
            }

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .size(
                        if (loadHighQuality) {
                            Size(tileWidth.value.toInt(), tileHeight.value.toInt())
                        } else {
                            Size(tileWidth.value.toInt() / 2, tileHeight.value.toInt() / 2)
                        }
                    )
                    .scale(Scale.FILL)
                    .crossfade(150)
                    .memoryCacheKey("tile_${tile.tid}_${rowLayout}")
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Ad indicator
            if (tile.isAdTile) {
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

            // Title overlay for landscape tiles when focused
            if (rowLayout == "landscape" && isFocused) {
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
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}