package com.graywizard.filemanager.utils

import android.webkit.MimeTypeMap
import java.io.File

object MimeTypeDetector {
    
    fun getMimeType(file: File): String {
        val extension = file.extension.lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) 
            ?: getCustomMimeType(extension)
    }
    
    private fun getCustomMimeType(extension: String): String {
        return when (extension) {
            "apk" -> "application/vnd.android.package-archive"
            "apks", "apkm", "xapk" -> "application/vnd.android.package-archive"
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "7z" -> "application/x-7z-compressed"
            "tar" -> "application/x-tar"
            "gz" -> "application/gzip"
            "bz2" -> "application/x-bzip2"
            "xz" -> "application/x-xz"
            "pdf" -> "application/pdf"
            "doc", "docx" -> "application/msword"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            "ppt", "pptx" -> "application/vnd.ms-powerpoint"
            "txt" -> "text/plain"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }
    }
    
    fun isArchive(file: File): Boolean {
        return file.extension.lowercase() in listOf(
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz",
            "apk", "apks", "apkm", "xapk"
        )
    }
    
    fun isImage(file: File): Boolean {
        return file.extension.lowercase() in listOf(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif"
        )
    }
    
    fun isVideo(file: File): Boolean {
        return file.extension.lowercase() in listOf(
            "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "3gp"
        )
    }
    
    fun isAudio(file: File): Boolean {
        return file.extension.lowercase() in listOf(
            "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus"
        )
    }
}
