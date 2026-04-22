package com.graywizard.filemanager.ui.screens.main

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.graywizard.filemanager.CyberFileManagerApp
import com.graywizard.filemanager.data.model.FileItem
import com.graywizard.filemanager.data.model.SortOrder
import com.graywizard.filemanager.data.model.SortType
import com.graywizard.filemanager.data.preferences.PreferencesManager
import com.graywizard.filemanager.data.repository.FileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class MainUiState(
    val currentDirectory: File = Environment.getExternalStorageDirectory(),
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFiles: Set<FileItem> = emptySet(),
    val isSelectionMode: Boolean = false,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val sortType: SortType = SortType.NAME,
    val sortOrder: SortOrder = SortOrder.ASCENDING,
    val showHiddenFiles: Boolean = false,
    val isGridView: Boolean = false,
    val selectedTab: BottomTab = BottomTab.FILES,
    val directoryStack: List<File> = listOf(Environment.getExternalStorageDirectory())
)

class MainViewModel : ViewModel() {
    private val repository = FileRepository(CyberFileManagerApp.instance.getAppContext())
    private val preferencesManager = PreferencesManager(CyberFileManagerApp.instance.getAppContext())
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    init {
        loadPreferences()
        loadFiles()
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            launch {
                preferencesManager.showHiddenFilesFlow.collect { showHidden ->
                    _uiState.update { it.copy(showHiddenFiles = showHidden) }
                    loadFiles()
                }
            }
            
            launch {
                preferencesManager.gridViewFlow.collect { isGrid ->
                    _uiState.update { it.copy(isGridView = isGrid) }
                }
            }
            
            launch {
                preferencesManager.sortByFlow.collect { sortBy ->
                    val sortType = when (sortBy) {
                        "name" -> SortType.NAME
                        "date" -> SortType.DATE
                        "size" -> SortType.SIZE
                        "type" -> SortType.TYPE
                        else -> SortType.NAME
                    }
                    _uiState.update { it.copy(sortType = sortType) }
                    loadFiles()
                }
            }
            
            launch {
                preferencesManager.sortOrderFlow.collect { order ->
                    val sortOrder = if (order == "asc") SortOrder.ASCENDING else SortOrder.DESCENDING
                    _uiState.update { it.copy(sortOrder = sortOrder) }
                    loadFiles()
                }
            }
        }
    }
    
    fun loadFiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val files = repository.getFiles(
                    directory = _uiState.value.currentDirectory,
                    showHidden = _uiState.value.showHiddenFiles,
                    sortType = _uiState.value.sortType,
                    sortOrder = _uiState.value.sortOrder
                )
                
                _uiState.update {
                    it.copy(
                        files = files,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun navigateToDirectory(directory: File) {
        _uiState.update {
            it.copy(
                currentDirectory = directory,
                directoryStack = it.directoryStack + directory
            )
        }
        loadFiles()
    }
    
    fun navigateBack(): Boolean {
        val stack = _uiState.value.directoryStack
        if (stack.size <= 1) return false
        
        val newStack = stack.dropLast(1)
        _uiState.update {
            it.copy(
                currentDirectory = newStack.last(),
                directoryStack = newStack
            )
        }
        loadFiles()
        return true
    }
    
    fun toggleFileSelection(file: FileItem) {
        val selectedFiles = _uiState.value.selectedFiles.toMutableSet()
        if (selectedFiles.contains(file)) {
            selectedFiles.remove(file)
        } else {
            selectedFiles.add(file)
        }
        
        _uiState.update {
            it.copy(
                selectedFiles = selectedFiles,
                isSelectionMode = selectedFiles.isNotEmpty()
            )
        }
    }
    
    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedFiles = emptySet(),
                isSelectionMode = false
            )
        }
    }
    
    fun selectAll() {
        _uiState.update {
            it.copy(
                selectedFiles = it.files.toSet(),
                isSelectionMode = true
            )
        }
    }
    
    fun deleteSelectedFiles() {
        viewModelScope.launch {
            _uiState.value.selectedFiles.forEach { fileItem ->
                repository.deleteFile(fileItem.file)
            }
            clearSelection()
            loadFiles()
        }
    }
    
    fun createFolder(name: String) {
        viewModelScope.launch {
            repository.createFolder(_uiState.value.currentDirectory, name)
            loadFiles()
        }
    }
    
    fun renameFile(file: File, newName: String) {
        viewModelScope.launch {
            repository.renameFile(file, newName)
            loadFiles()
        }
    }
    
    fun search(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(searchQuery = "", isSearching = false) }
            loadFiles()
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(searchQuery = query, isSearching = true, isLoading = true) }
            
            val results = repository.searchFiles(
                directory = _uiState.value.currentDirectory,
                query = query,
                showHidden = _uiState.value.showHiddenFiles
            )
            
            _uiState.update {
                it.copy(
                    files = results,
                    isLoading = false
                )
            }
        }
    }
    
    fun toggleViewMode() {
        viewModelScope.launch {
            val newMode = !_uiState.value.isGridView
            preferencesManager.setGridView(newMode)
        }
    }
    
    fun onTabSelected(tab: BottomTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
