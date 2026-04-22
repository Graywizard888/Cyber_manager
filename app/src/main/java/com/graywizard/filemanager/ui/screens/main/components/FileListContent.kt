package com.graywizard.filemanager.ui.screens.main.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.graywizard.filemanager.data.model.FileItem
import com.graywizard.filemanager.ui.navigation.Screen
import com.graywizard.filemanager.ui.screens.main.MainViewModel
import com.graywizard.filemanager.utils.FileUtils
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileListContent(
    navController: NavController,
    viewModel: MainViewModel,
    filterType: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var fileToRename by remember { mutableStateOf<FileItem?>(null) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showFileOptions by remember { mutableStateOf<FileItem?>(null) }
    
    val filteredFiles = remember(uiState.files, filterType) {
        if (filterType != null) {
            uiState.files.filter { it.mimeType == filterType }
        } else {
            uiState.files
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (uiState.isSelectionMode) {
                                "${uiState.selectedFiles.size} selected"
                            } else {
                                uiState.currentDirectory.name.ifEmpty { "Storage" }
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (!uiState.isSelectionMode) {
                            Text(
                                text = uiState.currentDirectory.absolutePath,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (uiState.isSelectionMode) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear selection")
                        }
                    } else if (uiState.directoryStack.size > 1) {
                        IconButton(onClick = { viewModel.navigateBack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (uiState.isSelectionMode) {
                        IconButton(onClick = { viewModel.selectAll() }) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Select all")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    } else {
                        IconButton(onClick = { showSortDialog = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        IconButton(onClick = { viewModel.toggleViewMode() }) {
                            Icon(
                                if (uiState.isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = "Toggle view"
                            )
                        }
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("New Folder") },
                                onClick = {
                                    showCreateFolderDialog = true
                                    showOptionsMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.CreateNewFolder, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!uiState.isSelectionMode && filterType == null) {
                FloatingActionButton(
                    onClick = { showCreateFolderDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create folder")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && filteredFiles.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                filteredFiles.isEmpty() -> {
                    EmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        message = if (filterType != null) {
                            "No ${filterType}s found"
                        } else {
                            "This folder is empty"
                        }
                    )
                }
                
                else -> {
                    if (uiState.isGridView) {
                        GridFileList(
                            files = filteredFiles,
                            selectedFiles = uiState.selectedFiles,
                            isSelectionMode = uiState.isSelectionMode,
                            onFileClick = { file ->
                                handleFileClick(
                                    file = file,
                                    navController = navController,
                                    viewModel = viewModel,
                                    isSelectionMode = uiState.isSelectionMode
                                )
                            },
                            onFileLongClick = { file ->
                                viewModel.toggleFileSelection(file)
                            },
                            onFileOptions = { file ->
                                showFileOptions = file
                            }
                        )
                    } else {
                        ListFileList(
                            files = filteredFiles,
                            selectedFiles = uiState.selectedFiles,
                            isSelectionMode = uiState.isSelectionMode,
                            onFileClick = { file ->
                                handleFileClick(
                                    file = file,
                                    navController = navController,
                                    viewModel = viewModel,
                                    isSelectionMode = uiState.isSelectionMode
                                )
                            },
                            onFileLongClick = { file ->
                                viewModel.toggleFileSelection(file)
                            },
                            onFileOptions = { file ->
                                showFileOptions = file
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onCreate = { name ->
                viewModel.createFolder(name)
                showCreateFolderDialog = false
            }
        )
    }
    
    if (showSortDialog) {
        SortDialog(
            currentSortType = uiState.sortType,
            currentSortOrder = uiState.sortOrder,
            onDismiss = { showSortDialog = false },
            onSort = { sortType, sortOrder ->
                // Handle sorting via preferences
                showSortDialog = false
            }
        )
    }
    
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            fileCount = uiState.selectedFiles.size,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteSelectedFiles()
                showDeleteDialog = false
            }
        )
    }
    
    if (showRenameDialog && fileToRename != null) {
        RenameDialog(
            currentName = fileToRename!!.name,
            onDismiss = {
                showRenameDialog = false
                fileToRename = null
            },
            onRename = { newName ->
                viewModel.renameFile(fileToRename!!.file, newName)
                showRenameDialog = false
                fileToRename = null
            }
        )
    }
    
    showFileOptions?.let { file ->
        FileOptionsBottomSheet(
            file = file,
            onDismiss = { showFileOptions = null },
            onRename = {
                fileToRename = file
                showRenameDialog = true
                showFileOptions = null
            },
            onDelete = {
                viewModel.toggleFileSelection(file)
                showDeleteDialog = true
                showFileOptions = null
            },
            onShare = {
                FileUtils.shareFile(navController.context, file.file)
                showFileOptions = null
            }
        )
    }
}

private fun handleFileClick(
    file: FileItem,
    navController: NavController,
    viewModel: MainViewModel,
    isSelectionMode: Boolean
) {
    if (isSelectionMode) {
        viewModel.toggleFileSelection(file)
        return
    }
    
    when {
        file.isDirectory -> {
            viewModel.navigateToDirectory(file.file)
        }
        
        file.mimeType == "image" -> {
            val encodedPath = URLEncoder.encode(file.path, StandardCharsets.UTF_8.toString())
            navController.navigate(Screen.ImageViewer.createRoute(encodedPath))
        }
        
        file.mimeType == "video" -> {
            val encodedPath = URLEncoder.encode(file.path, StandardCharsets.UTF_8.toString())
            navController.navigate(Screen.VideoPlayer.createRoute(encodedPath))
        }
        
        file.mimeType == "archive" -> {
            // Handle archive extraction
        }
        
        else -> {
            // Open with system app
            FileUtils.openFile(navController.context, file.file)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListFileList(
    files: List<FileItem>,
    selectedFiles: Set<FileItem>,
    isSelectionMode: Boolean,
    onFileClick: (FileItem) -> Unit,
    onFileLongClick: (FileItem) -> Unit,
    onFileOptions: (FileItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = files,
            key = { it.path }
        ) { file ->
            FileListItem(
                file = file,
                isSelected = selectedFiles.contains(file),
                isSelectionMode = isSelectionMode,
                onClick = { onFileClick(file) },
                onLongClick = { onFileLongClick(file) },
                onOptions = { onFileOptions(file) },
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridFileList(
    files: List<FileItem>,
    selectedFiles: Set<FileItem>,
    isSelectionMode: Boolean,
    onFileClick: (FileItem) -> Unit,
    onFileLongClick: (FileItem) -> Unit,
    onFileOptions: (FileItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = files,
            key = { it.path }
        ) { file ->
            FileGridItem(
                file = file,
                isSelected = selectedFiles.contains(file),
                isSelectionMode = isSelectionMode,
                onClick = { onFileClick(file) },
                onLongClick = { onFileLongClick(file) },
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    file: FileItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FileIcon(
                file = file,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (file.isDirectory) {
                            "${FileUtils.getFileCount(file.file)} items"
                        } else {
                            FileUtils.formatFileSize(file.size)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = FileUtils.formatDate(file.lastModified),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null
                )
            } else {
                IconButton(onClick = onOptions) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileGridItem(
    file: FileItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                FileIcon(
                    file = file,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            if (isSelectionMode && isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                )
            }
        }
    }
}

@Composable
fun FileIcon(
    file: FileItem,
    modifier: Modifier = Modifier
) {
    when {
        file.isDirectory -> {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                modifier = modifier,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        file.mimeType == "image" -> {
            FileThumbnail(
                file = file,
                modifier = modifier
            )
        }
        
        file.mimeType == "video" -> {
            FileThumbnail(
                file = file,
                modifier = modifier,
                showPlayIcon = true
            )
        }
        
        file.mimeType == "audio" -> {
            Icon(
                imageVector = Icons.Default.AudioFile,
                contentDescription = null,
                modifier = modifier,
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        
        file.mimeType == "document" -> {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = modifier,
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
        
        file.mimeType == "archive" -> {
            Icon(
                imageVector = Icons.Default.Archive,
                contentDescription = null,
                modifier = modifier,
                tint = MaterialTheme.colorScheme.error
            )
        }
        
        else -> {
            Icon(
                imageVector = Icons.Default.InsertDriveFile,
                contentDescription = null,
                modifier = modifier,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FileThumbnail(
    file: FileItem,
    modifier: Modifier = Modifier,
    showPlayIcon: Boolean = false
) {
    Box(modifier = modifier) {
        coil.compose.AsyncImage(
            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(file.file)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        
        if (showPlayIcon) {
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(32.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    message: String
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
