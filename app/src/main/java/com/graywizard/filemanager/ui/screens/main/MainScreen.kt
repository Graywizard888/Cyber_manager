package com.graywizard.filemanager.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.graywizard.filemanager.ui.screens.main.components.BottomNavigationBar
import com.graywizard.filemanager.ui.screens.main.components.FileListContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.onTabSelected(it) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.selectedTab) {
                BottomTab.FILES -> FileListContent(
                    navController = navController,
                    viewModel = viewModel
                )
                BottomTab.IMAGES -> FileListContent(
                    navController = navController,
                    viewModel = viewModel,
                    filterType = "image"
                )
                BottomTab.VIDEOS -> FileListContent(
                    navController = navController,
                    viewModel = viewModel,
                    filterType = "video"
                )
                BottomTab.ABOUT -> com.graywizard.filemanager.ui.screens.about.AboutScreen()
            }
        }
    }
}

enum class BottomTab {
    FILES, IMAGES, VIDEOS, ABOUT
}
