package com.graywizard.filemanager.data.model

import java.io.File

data class FileItem(
    val file: File,
    val name: String = file.name,
    val path: String = file.absolutePath,
    val size: Long = if (file.isDirectory) 0 else file.length(),
    val lastModified: Long = file.lastModified(),
    val isDirectory: Boolean = file.isDirectory,
    val extension: String = file.extension.lowercase(),
    val mimeType: String = getMimeType(file),
    val isHidden: Boolean = file.isHidden
) {
    companion object {
        private fun getMimeType(file: File): String {
            if (file.isDirectory) return "folder"
            
            return when (file.extension.lowercase()) {
                // Images
                "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif" -> "image"
                
                // Videos
                "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "3gp" -> "video"
                
                // Audio
                "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus" -> "audio"
                
                // Documents
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "odt" -> "document"
                
                // Archives
                "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "apk", "apks", "apkm", "xapk" -> "archive"
                
                // Code
                "java", "kt", "cpp", "c", "h", "py", "js", "html", "css", "xml", "json" -> "code"
                
                else -> "unknown"
            }
        }
    }
}

enum class SortType {
    NAME, DATE, SIZE, TYPE
}

enum class SortOrder {
    ASCENDING, DESCENDING
}
