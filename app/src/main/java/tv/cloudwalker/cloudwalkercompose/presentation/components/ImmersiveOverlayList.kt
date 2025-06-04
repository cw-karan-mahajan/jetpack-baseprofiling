package tv.cloudwalker.cloudwalkercompose.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size

@Composable
fun ImmersiveOverlayList(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onItemClick: (Int, Int) -> Unit = { _, _ -> }, // row, column
    modifier: Modifier = Modifier,
    title: String = "Apps & Games",
    rowCount: Int = 5,
    columnCount: Int = 10
) {
    // Animation for overlay fade in/out
    val overlayAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f
        ),
        label = "overlay_alpha"
    )

    // Animation for background dim
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isVisible) 0.5f else 0f,
        animationSpec = spring(
            dampingRatio = 0.9f,
            stiffness = 400f
        ),
        label = "background_dim"
    )

    if (isVisible || overlayAlpha > 0f) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .zIndex(200f)
                .graphicsLayer { alpha = overlayAlpha }
                // Handle Back button to close overlay
                .onKeyEvent { keyEvent ->
                    if (isVisible && keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Back) {
                        onDismiss()
                        true
                    } else {
                        false
                    }
                }
        ) {
            // Dimmed background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = backgroundAlpha))
            )

            // Grid overlay content positioned like hero banner
            ImmersiveGridContent(
                title = title,
                rowCount = rowCount,
                columnCount = columnCount,
                onItemClick = onItemClick,
                onDismiss = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 120.dp) // Position below top nav like hero banner
            )
        }
    }
}

@Composable
private fun ImmersiveGridContent(
    title: String,
    rowCount: Int,
    columnCount: Int,
    onItemClick: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val verticalListState = rememberLazyListState()
    val staticImageUrl = "http://asset.s4.cloudwalker.tv/images/tiles/d7b965c133961039465fb8a6d603f85e/Barroz_4_600x800.webp".replace("http://", "https://")
    // Simplified focus management - just use focusGroup
    val firstItemFocusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .focusGroup()
    ) {
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                letterSpacing = 1.sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp)
        )

        // Subtitle
        Text(
            text = getSubtitleForTitle(title),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp
            ),
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 24.dp)
        )

        // Grid with scrolling enabled
        LazyColumn(
            state = verticalListState,
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp), // Increased height to accommodate focus overlays
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 40.dp), // Slightly increased padding
            userScrollEnabled = true // Enable scrolling
        ) {
            itemsIndexed(
                items = (0 until rowCount).toList(),
                key = { rowIndex, _ -> "grid_row_$rowIndex" }
            ) { rowIndex, _ ->
                ImmersiveGridRow(
                    rowIndex = rowIndex,
                    columnCount = columnCount,
                    imageUrl = staticImageUrl,
                    onItemClick = onItemClick,
                    firstItemFocusRequester = if (rowIndex == 0) firstItemFocusRequester else null
                )
            }
        }
    }

    // Request focus on first item when overlay opens
    LaunchedEffect(Unit) {
        firstItemFocusRequester.requestFocus()
    }
}

@Composable
private fun ImmersiveGridRow(
    rowIndex: Int,
    columnCount: Int,
    imageUrl: String,
    onItemClick: (Int, Int) -> Unit,
    firstItemFocusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier
) {
    val horizontalListState = rememberLazyListState()

    LazyRow(
        state = horizontalListState,
        modifier = modifier
            .fillMaxWidth()
            .focusGroup(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 32.dp),
        userScrollEnabled = true // Enable horizontal scrolling
    ) {
        itemsIndexed(
            items = (0 until columnCount).toList(),
            key = { colIndex, _ -> "grid_item_${rowIndex}_$colIndex" }
        ) { colIndex, _ ->
            ImmersiveGridTile(
                rowIndex = rowIndex,
                colIndex = colIndex,
                imageUrl = imageUrl,
                title = generateItemTitle(rowIndex, colIndex),
                onItemClick = { onItemClick(rowIndex, colIndex) },
                modifier = if (colIndex == 0 && firstItemFocusRequester != null) {
                    Modifier.focusRequester(firstItemFocusRequester) // Only first row, first item
                } else {
                    Modifier
                }
            )
        }
    }
}

@Composable
private fun ImmersiveGridTile(
    rowIndex: Int,
    colIndex: Int,
    imageUrl: String,
    title: String,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Fixed tile dimensions like your movie tiles
    val tileWidth = 180.dp
    val tileHeight = 240.dp

    // Focus animation
    val focusScale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 300f
        ),
        label = "focus_scale"
    )

    val imageRequest = remember(imageUrl, rowIndex, colIndex) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .size(Size(tileWidth.value.toInt(), tileHeight.value.toInt()))
            .scale(Scale.FILL)
            .crossfade(200)
            .memoryCacheKey("immersive_${rowIndex}_$colIndex")
            .build()
    }

    Card(
        onClick = onItemClick,
        modifier = modifier
            .size(width = tileWidth, height = tileHeight)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .graphicsLayer {
                scaleX = focusScale
                scaleY = focusScale
                clip = false // Don't clip the scaled content
            }
            .then(
                if (isFocused) {
                    Modifier.border(3.dp, Color.White, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 8.dp else 2.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Light dim overlay on all tiles
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.15f)) // Light dim overlay
            )

            // Focus overlay (like your existing tiles)
            if (isFocused) {
                // Stronger gradient overlay when focused
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(0.7f)),
                                startY = 100f
                            )
                        )
                )

                // Title overlay
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(Alignment.Bottom)
                        .padding(8.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Helper functions for dynamic content
private fun getSubtitleForTitle(title: String): String {
    return when (title) {
        "My Files" -> "Navigate with D-Pad • Press OK to open • Back to close"
        "Shared with me" -> "Files shared by others • Navigate with D-Pad • Back to close"
        "Starred" -> "Your starred files • Navigate with D-Pad • Back to close"
        "Recent" -> "Recently accessed files • Navigate with D-Pad • Back to close"
        "Offline" -> "Available offline • Navigate with D-Pad • Back to close"
        "Uploads" -> "Your uploaded files • Navigate with D-Pad • Back to close"
        "Backups" -> "Backup files • Navigate with D-Pad • Back to close"
        "Apps & Games" -> "Navigate with D-Pad • Press OK to launch • Back to close"
        else -> "Navigate with D-Pad • Press OK to select • Back to close"
    }
}

private fun generateItemTitle(rowIndex: Int, colIndex: Int): String {
    val itemNumber = (rowIndex * 10) + colIndex + 1
    return "App $itemNumber"
}