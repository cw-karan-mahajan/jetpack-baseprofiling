package tv.cloudwalker.cloudwalkercompose.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@Composable
fun RightSideNavigationDrawer(
    isOpen: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    drawerWidth: Float = 0.5f,
    userName: String = "Sandra Adams",
    userEmail: String = "sandra_a88@gmail.com",
    onMyFilesClick: () -> Unit = {},
    onSharedClick: () -> Unit = {},
    onStarredClick: () -> Unit = {},
    onRecentClick: () -> Unit = {},
    onOfflineClick: () -> Unit = {},
    onUploadsClick: () -> Unit = {},
    onBackupsClick: () -> Unit = {}
) {
    // Smooth spring animation for drawer slide
    val drawerOffset by animateFloatAsState(
        targetValue = if (isOpen) 0f else 1f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f
        ),
        label = "drawer_slide"
    )

    // Smooth background dim animation
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isOpen) 0.6f else 0f,
        animationSpec = spring(
            dampingRatio = 0.9f,
            stiffness = 400f
        ),
        label = "background_dim"
    )

    if (isOpen || drawerOffset < 1f) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .zIndex(100f)
                // Handle Back button at the top level
                .onKeyEvent { keyEvent ->
                    if (isOpen && keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Back) {
                        onClose()
                        true
                    } else {
                        false
                    }
                }
        ) {
            // Background dim overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = backgroundAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (isOpen) onClose()
                    }
            )

            // Right side drawer with smooth animation
            DrawerContent(
                userName = userName,
                userEmail = userEmail,
                onClose = onClose,
                onMyFilesClick = onMyFilesClick,
                onSharedClick = onSharedClick,
                onStarredClick = onStarredClick,
                onRecentClick = onRecentClick,
                onOfflineClick = onOfflineClick,
                onUploadsClick = onUploadsClick,
                onBackupsClick = onBackupsClick,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(drawerWidth)
                    .align(Alignment.CenterEnd)
                    .graphicsLayer {
                        // Smooth slide animation from right
                        translationX = size.width * drawerOffset
                        // Add slight scale for more mobile-like feel
                        scaleX = 0.95f + (0.05f * (1f - drawerOffset))
                        scaleY = 0.95f + (0.05f * (1f - drawerOffset))
                    }
            )
        }
    }
}

@Composable
private fun DrawerContent(
    userName: String,
    userEmail: String,
    onClose: () -> Unit,
    onMyFilesClick: () -> Unit,
    onSharedClick: () -> Unit,
    onStarredClick: () -> Unit,
    onRecentClick: () -> Unit,
    onOfflineClick: () -> Unit,
    onUploadsClick: () -> Unit,
    onBackupsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profileFocusRequester = remember { FocusRequester() }
    val menuFocusRequesters = remember { List(7) { FocusRequester() } }

    Card(
        modifier = modifier.focusGroup(),
        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // User Profile Header
            item(key = "profile_header") {
                UserProfileHeader(
                    userName = userName,
                    userEmail = userEmail,
                    modifier = Modifier.focusRequester(profileFocusRequester)
                )
            }

            // Divider
            item(key = "divider") {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = Color.Gray.copy(alpha = 0.3f)
                )
            }

            // Navigation Menu Items
            val menuItems = listOf(
                DrawerMenuItem(Icons.Default.Folder, "My Files", true, onMyFilesClick),
                DrawerMenuItem(Icons.Default.Group, "Shared with me", false, onSharedClick),
                DrawerMenuItem(Icons.Default.Star, "Starred", false, onStarredClick),
                DrawerMenuItem(Icons.Default.History, "Recent", false, onRecentClick),
                DrawerMenuItem(Icons.Default.OfflinePin, "Offline", false, onOfflineClick),
                DrawerMenuItem(Icons.Default.Upload, "Uploads", false, onUploadsClick),
                DrawerMenuItem(Icons.Default.Backup, "Backups", false, onBackupsClick)
            )

            itemsIndexed(
                items = menuItems,
                key = { index, item -> "menu_item_${index}_${item.title}" }
            ) { index, item ->
                DrawerMenuItemComponent(
                    icon = item.icon,
                    title = item.title,
                    isSelected = item.isSelected,
                    onClick = item.onClick,
                    modifier = Modifier.focusRequester(menuFocusRequesters[index])
                )
            }

            // Bottom spacing
            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Auto-focus first menu item (My Files) when drawer opens
    LaunchedEffect(Unit) {
        menuFocusRequesters[0].requestFocus()
    }
}

@Composable
private fun UserProfileHeader(
    userName: String,
    userEmail: String,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        onClick = { /* Handle profile click */ },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .then(
                if (isFocused) {
                    Modifier.border(2.dp, Color.Blue, RoundedCornerShape(12.dp))
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Avatar",
                    tint = Color.Gray.copy(alpha = 0.7f),
                    modifier = Modifier.size(32.dp)
                )
            }

            // User Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = userEmail,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Dropdown Arrow
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "More options",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun DrawerMenuItemComponent(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .then(
                if (isFocused) {
                    Modifier.border(2.dp, Color.Blue, RoundedCornerShape(8.dp))
                } else Modifier
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> Color.Blue.copy(alpha = 0.1f)
                isFocused -> Color.Gray.copy(alpha = 0.1f)
                else -> Color.Transparent
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint =  Color.Gray,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color =  Color.Black,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Data class for menu items
private data class DrawerMenuItem(
    val icon: ImageVector,
    val title: String,
    val isSelected: Boolean,
    val onClick: () -> Unit
)