package com.graywizard.filemanager.data.repository

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.graywizard.filemanager.data.model.FileItem
import com.graywizard.filemanager.data.model.SortOrder
import com.graywizard.filemanager.data.model.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class FileRepository(private val context: Context) {
    
    suspend fun getFiles(
        directory: File,
        showHidden: Boolean = false,
        sortType: SortType = SortType.NAME,
        sortOrder: SortOrder = SortOrder.ASCENDING
    ): List<FileItem> = withContext(Dispatchers.IO) {
        try {
            val files = directory.listFiles()?.toList() ?: emptyList()
            
            val filteredFiles = if (showHidden) files else files.filter { !it.isHidden }
            
            val fileItems = filteredFiles.map { FileItem(it) }
            
            sortFiles(fileItems, sortType, sortOrder)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    private fun sortFiles(
        files: List<FileItem>,
        sortType: SortType,
        sortOrder: SortOrder
    ): List<FileItem> {
        val sorted = when (sortType) {
            SortType.NAME -> files.sortedBy { it.name.lowercase() }
            SortType.DATE -> files.sortedBy { it.lastModified }
            SortType.SIZE -> files.sortedBy { it.size }
            SortType.TYPE -> files.sortedBy { it.extension }
        }
        
        val directoriesFirst = sorted.partition { it.isDirectory }
        val result = directoriesFirst.first + directoriesFirst.second
        
        return if (sortOrder == SortOrder.DESCENDING) {
            result.reversed()
        } else {
            result
        }
    }
    
    suspend fun createFolder(parent: File, name: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val newFolder = File(parent, name)
            newFolder.mkdirs()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun deleteFile(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun renameFile(file: File, newName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val newFile = File(file.parent, newName)
            file.renameTo(newFile)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun copyFile(source: File, destination: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (source.isDirectory) {
                copyDirectory(source, destination)
            } else {
                FileInputStream(source).use { input ->
                    FileOutputStream(destination).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun copyDirectory(source: File, destination: File) {
        if (!destination.exists()) {
            destination.mkdirs()
        }
        
        source.listFiles()?.forEach { file ->
            val dest = File(destination, file.name)
            if (file.isDirectory) {
                copyDirectory(file, dest)
            } else {
                file.copyTo(dest, overwrite = true)
            }
        }
    }
    
    suspend fun moveFile(source: File, destination: File): Boolean = withContext(Dispatchers.IO) {
        try {
            source.renameTo(destination) || run {
                copyFile(source, destination) && deleteFile(source)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun searchFiles(
        directory: File,
        query: String,
        showHidden: Boolean = false
    ): List<FileItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<FileItem>()
        
        fun search(dir: File) {
            dir.listFiles()?.forEach { file ->
                if (!showHidden && file.isHidden) return@forEach
                
                if (file.name.contains(query, ignoreCase = true)) {
                    results.add(FileItem(file))
                }
                
                if (file.isDirectory) {
                    search(file)
                }
            }
        }
        
        search(directory)
        results
    }
    
    fun getStorageInfo(): StorageInfo {
        val internalPath = Environment.getExternalStorageDirectory()
        val internalStat = StatFs(internalPath.path)
        
        val internalTotal = internalStat.blockCountLong * internalStat.blockSizeLong
        val internalFree = internalStat.availableBlocksLong * internalStat.blockSizeLong
        
        return StorageInfo(
            internalTotal = internalTotal,
            internalFree = internalFree,
            internalUsed = internalTotal - internalFree
        )
    }
    
    fun getInternalStorageDirectory(): File = Environment.getExternalStorageDirectory()
    
    fun getDownloadsDirectory(): File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    
    fun getDCIMDirectory(): File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
    
    fun getDocumentsDirectory(): File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
}

data class StorageInfo(
    val internalTotal: Long,
    val internalFree: Long,
    val internalUsed: Long
)
