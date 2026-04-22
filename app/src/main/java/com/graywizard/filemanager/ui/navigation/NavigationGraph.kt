package com.graywizard.filemanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.graywizard.filemanager.ui.screens.main.MainScreen
import com.graywizard.filemanager.ui.screens.permission.PermissionScreen
import com.graywizard.filemanager.ui.screens.imageviewer.ImageViewerScreen
import com.graywizard.filemanager.ui.screens.videoplayer.VideoPlayerScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Permission : Screen("permission")
    object Main : Screen("main")
    object ImageViewer : Screen("image_viewer/{filePath}") {
        fun createRoute(filePath: String) = "image_viewer/$filePath"
    }
    object VideoPlayer : Screen("video_player/{filePath}") {
        fun createRoute(filePath: String) = "video_player/$filePath"
    }
}

@Composable
fun NavigationGraph(
    hasStoragePermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val navController = rememberNavController()
    
    val startDestination = if (hasStoragePermission) {
        Screen.Main.route
    } else {
        Screen.Permission.route
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Permission.route) {
            PermissionScreen(
                onPermissionGranted = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Permission.route) { inclusive = true }
                    }
                },
                onRequestPermission = onRequestPermission
            )
        }
        
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }
        
        composable(
            route = Screen.ImageViewer.route,
            arguments = listOf(navArgument("filePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("filePath") ?: ""
            val filePath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.toString())
            ImageViewerScreen(
                filePath = filePath,
                onBack = { navController.navigateUp() }
            )
        }
        
        composable(
            route = Screen.VideoPlayer.route,
            arguments = listOf(navArgument("filePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("filePath") ?: ""
            val filePath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.toString())
            VideoPlayerScreen(
                filePath = filePath,
                onBack = { navController.navigateUp() }
            )
        }
    }
}
