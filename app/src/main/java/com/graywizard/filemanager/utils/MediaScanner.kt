package com.graywizard.filemanager.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

object MediaScanner {
    
    suspend fun scanFile(context: Context, file: File): Uri? = suspendCancellableCoroutine { continuation ->
        MediaScannerConnection.scanFile(
            context,
            arrayOf(file.absolutePath),
            null
        ) { _, uri ->
            continuation.resume(uri)
        }
    }
    
    suspend fun scanFiles(context: Context, files: List<File>): List<Uri?> {
        return files.map { scanFile(context, it) }
    }
}
