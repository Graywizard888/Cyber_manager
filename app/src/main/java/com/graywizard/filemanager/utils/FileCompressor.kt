package com.graywizard.filemanager.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object FileCompressor {
    
    suspend fun compressFiles(
        files: List<File>,
        outputZipFile: File,
        onProgress: (Int) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            ZipOutputStream(FileOutputStream(outputZipFile)).use { zipOut ->
                files.forEachIndexed { index, file ->
                    if (file.isDirectory) {
                        zipDirectory(file, file.name, zipOut)
                    } else {
                        zipFile(file, file.name, zipOut)
                    }
                    
                    val progress = ((index + 1) * 100 / files.size)
                    onProgress(progress)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun zipDirectory(
        directory: File,
        baseName: String,
        zipOut: ZipOutputStream
    ) {
        directory.listFiles()?.forEach { file ->
            val entryName = "$baseName/${file.name}"
            if (file.isDirectory) {
                zipDirectory(file, entryName, zipOut)
            } else {
                zipFile(file, entryName, zipOut)
            }
        }
    }
    
    private fun zipFile(
        file: File,
        entryName: String,
        zipOut: ZipOutputStream
    ) {
        FileInputStream(file).use { fis ->
            val zipEntry = ZipEntry(entryName)
            zipOut.putNextEntry(zipEntry)
            
            val buffer = ByteArray(8192)
            var length: Int
            while (fis.read(buffer).also { length = it } > 0) {
                zipOut.write(buffer, 0, length)
            }
            
            zipOut.closeEntry()
        }
    }
}
