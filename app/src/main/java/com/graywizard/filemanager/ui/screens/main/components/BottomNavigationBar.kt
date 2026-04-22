package com.graywizard.filemanager.ui.screens.main.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.graywizard.filemanager.ui.screens.main.BottomTab

@Composable
fun BottomNavigationBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    NavigationBar {
        BottomNavItem.values().forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = selectedTab == item.tab,
                onClick = { onTabSelected(item.tab) }
            )
        }
    }
}

enum class BottomNavItem(
    val tab: BottomTab,
    val icon: ImageVector,
    val label: String
) {
    FILES(BottomTab.FILES, Icons.Default.Folder, "Files"),
    IMAGES(BottomTab.IMAGES, Icons.Default.Image, "Images"),
    VIDEOS(BottomTab.VIDEOS, Icons.Default.VideoLibrary, "Videos"),
    ABOUT(BottomTab.ABOUT, Icons.Default.Info, "About")
}
