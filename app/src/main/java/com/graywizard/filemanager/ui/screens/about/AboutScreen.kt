package com.graywizard.filemanager.ui.screens.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.graywizard.filemanager.BuildConfig
import com.graywizard.filemanager.R

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // App Icon
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 4.dp,
            modifier = Modifier.size(120.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "App Icon",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // App Name
        Text(
            text = "CyberFile Manager",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Version
        Text(
            text = "Version ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Created By Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Created by",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "GrayWizard",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Technologies Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Technologies",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                TechnologyItem(
                    name = "Jetpack Compose",
                    description = "Modern Android UI Toolkit"
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                TechnologyItem(
                    name = "ExoPlayer",
                    description = "Version ${getExoPlayerVersion()}"
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                TechnologyItem(
                    name = "Kotlin",
                    description = "Modern Programming Language"
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                TechnologyItem(
                    name = "Material Design 3",
                    description = "Beautiful UI Components"
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                TechnologyItem(
                    name = "Coil",
                    description = "Image Loading Library"
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                TechnologyItem(
                    name = "Apache Commons Compress",
                    description = "Archive Extraction"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Features Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Features",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                FeatureItem(Icons.Default.Folder, "Advanced File Management")
                FeatureItem(Icons.Default.Image, "Beautiful Image Viewer")
                FeatureItem(Icons.Default.VideoLibrary, "Powerful Video Player")
                FeatureItem(Icons.Default.Archive, "Archive Extraction Support")
                FeatureItem(Icons.Default.DarkMode, "Dark & Light Themes")
                FeatureItem(Icons.Default.Speed, "Optimized Performance")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Copyright
        Text(
            text = "© 2024 GrayWizard. All rights reserved.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TechnologyItem(
    name: String,
    description: String
) {
    Column {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun getExoPlayerVersion(): String {
    return "2.19.1"
}
