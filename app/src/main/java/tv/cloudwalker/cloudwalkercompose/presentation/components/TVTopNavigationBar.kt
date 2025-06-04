package tv.cloudwalker.cloudwalkercompose.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TVTopNavigationBar(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onWifiClick: () -> Unit = {},
    onExitClick: () -> Unit = {},
    onAppsClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .focusGroup(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navItems = listOf(
                Triple(Icons.Default.Person, "Profile", onProfileClick),
                Triple(Icons.Default.Search, "Search", onSearchClick),
                Triple(Icons.Default.Settings, "Settings", onSettingsClick),
                Triple(Icons.Default.Wifi, "WiFi", onWifiClick),
                Triple(Icons.AutoMirrored.Filled.ExitToApp, "Exit", onExitClick),
                Triple(Icons.Default.Apps, "Apps", onAppsClick)
            )

            itemsIndexed(navItems) { index, (icon, label, onClick) ->
                var isFocused by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .onFocusChanged { isFocused = it.isFocused }
                        .then(
                            if (isFocused) {
                                Modifier
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                            } else {
                                Modifier.background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isFocused) Color.White else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = "CLOUD TV",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}
