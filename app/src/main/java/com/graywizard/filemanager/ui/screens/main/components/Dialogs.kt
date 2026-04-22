package com.graywizard.filemanager.ui.screens.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.graywizard.filemanager.data.model.SortOrder
import com.graywizard.filemanager.data.model.SortType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Folder") },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Folder Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (folderName.isNotBlank()) {
                        onCreate(folderName)
                    }
                },
                enabled = folderName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    fileCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Files") },
        text = {
            Text("Are you sure you want to delete $fileCount ${if (fileCount == 1) "item" else "items"}? This action cannot be undone.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newName.isNotBlank() && newName != currentName) {
                        onRename(newName)
                    }
                },
                enabled = newName.isNotBlank() && newName != currentName
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SortDialog(
    currentSortType: SortType,
    currentSortOrder: SortOrder,
    onDismiss: () -> Unit,
    onSort: (SortType, SortOrder) -> Unit
) {
    var selectedSortType by remember { mutableStateOf(currentSortType) }
    var selectedSortOrder by remember { mutableStateOf(currentSortOrder) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort By") },
        text = {
            Column {
                Text(
                    text = "Sort Type",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                SortType.values().forEach { sortType ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSortType == sortType,
                            onClick = { selectedSortType = sortType }
                        )
                        Text(
                            text = sortType.name.lowercase().capitalize(),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Order",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                SortOrder.values().forEach { sortOrder ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSortOrder == sortOrder,
                            onClick = { selectedSortOrder = sortOrder }
                        )
                        Text(
                            text = sortOrder.name.lowercase().capitalize(),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSort(selectedSortType, selectedSortOrder)
            }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileOptionsBottomSheet(
    file: com.graywizard.filemanager.data.model.FileItem,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    var showSheet by remember { mutableStateOf(true) }
    
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                onDismiss()
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                HorizontalDivider()
                
                BottomSheetOption(
                    icon = androidx.compose.material.icons.Icons.Default.Edit,
                    text = "Rename",
                    onClick = {
                        showSheet = false
                        onRename()
                    }
                )
                
                BottomSheetOption(
                    icon = androidx.compose.material.icons.Icons.Default.Share,
                    text = "Share",
                    onClick = {
                        showSheet = false
                        onShare()
                    }
                )
                
                BottomSheetOption(
                    icon = androidx.compose.material.icons.Icons.Default.Delete,
                    text = "Delete",
                    onClick = {
                        showSheet = false
                        onDelete()
                    }
                )
                
                BottomSheetOption(
                    icon = androidx.compose.material.icons.Icons.Default.Info,
                    text = "Properties",
                    onClick = {
                        showSheet = false
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomSheetOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
