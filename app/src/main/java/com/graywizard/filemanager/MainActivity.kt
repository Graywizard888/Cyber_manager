package com.graywizard.filemanager

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.graywizard.filemanager.ui.theme.CyberFileManagerTheme
import com.graywizard.filemanager.ui.navigation.NavigationGraph
import com.graywizard.filemanager.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private val preferencesManager by lazy { PreferencesManager(this) }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }
    
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            val scope = rememberCoroutineScope()
            var darkMode by remember { mutableStateOf(isSystemInDarkTheme()) }
            
            LaunchedEffect(Unit) {
                scope.launch {
                    preferencesManager.darkModeFlow.collect { isDark ->
                        darkMode = isDark ?: isSystemInDarkTheme()
                    }
                }
            }
            
            CyberFileManagerTheme(darkTheme = darkMode) {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !darkMode
                
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = androidx.compose.ui.graphics.Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                }
                
                // Permission handling
                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    listOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                } else {
                    listOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
                
                val multiplePermissionsState = rememberMultiplePermissionsState(permissions)
                
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!Environment.isExternalStorageManager()) {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            intent.data = Uri.parse("package:$packageName")
                            requestPermissionLauncher.launch(intent)
                        }
                    } else {
                        if (!multiplePermissionsState.allPermissionsGranted) {
                            multiplePermissionsState.launchMultiplePermissionRequest()
                        }
                    }
                }
                
                NavigationGraph(
                    hasStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Environment.isExternalStorageManager()
                    } else {
                        multiplePermissionsState.allPermissionsGranted
                    },
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            intent.data = Uri.parse("package:$packageName")
                            requestPermissionLauncher.launch(intent)
                        } else {
                            multiplePermissionsState.launchMultiplePermissionRequest()
                        }
                    }
                )
            }
        }
    }
}
