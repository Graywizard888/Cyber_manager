package com.graywizard.filemanager.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import java.io.*

object ArchiveExtractor {
    
    init {
        try {
            System.loadLibrary("archive_extractor")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }
    
    // Native methods
    private external fun extractNative(archivePath: String, outputPath: String): Boolean
    private external fun getArchiveType(archivePath: String): String
    
    // Progress callback for JNI
    @Suppress("unused")
    private fun onExtractionProgress(progress: Int, currentFile: String) {
        // This will be called from native code
        android.util.Log.d("ArchiveExtractor", "Progress: $progress%, File: $currentFile")
    }
    
    suspend fun extractArchive(
        archiveFile: File,
        outputDirectory: File,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs()
            }
            
            val extension = archiveFile.extension.lowercase()
            
            when (extension) {
                "zip", "apk", "jar" -> extractZipJava(archiveFile, outputDirectory, onProgress)
                "apks", "apkm", "xapk" -> extractZipJava(archiveFile, outputDirectory, onProgress)
                "7z" -> extract7zJava(archiveFile, outputDirectory, onProgress)
                "tar" -> extractTarJava(archiveFile, outputDirectory, onProgress)
                "gz", "tgz" -> extractGzJava(archiveFile, outputDirectory, onProgress)
                "xz" -> extractXzJava(archiveFile, outputDirectory, onProgress)
                "rar" -> {
                    // Try native extraction for RAR
                    val result = extractNative(archiveFile.absolutePath, outputDirectory.absolutePath)
                    if (!result) {
                        throw IOException("Failed to extract RAR archive")
                    }
                    result
                }
                else -> {
                    // Try to detect type and use native extraction
                    extractNative(archiveFile.absolutePath, outputDirectory.absolutePath)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun extractZipJava(
        zipFile: File,
        outputDir: File,
        onProgress: (Int, String) -> Unit
    ): Boolean {
        var count = 0
        var totalEntries = 0
        
        // Count total entries
        ZipArchiveInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zipInput ->
            while (zipInput.nextEntry != null) {
                totalEntries++
            }
        }
        
        ZipArchiveInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zipInput ->
            var entry = zipInput.nextEntry
            
            while (entry != null) {
                val file = File(outputDir, entry.name)
                
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { output ->
                        zipInput.copyTo(output)
                    }
                    
                    // Set executable permission if needed
                    if (entry.unixMode and 0x40 != 0) {
                        file.setExecutable(true)
                    }
                }
                
                count++
                val progress = (count * 100) / totalEntries
                onProgress(progress, entry.name)
                
                entry = zipInput.nextEntry
            }
        }
        return true
    }
    
    private fun extract7zJava(
        sevenZFile: File,
        outputDir: File,
        onProgress: (Int, String) -> Unit
    ): Boolean {
        var count = 0
        
        SevenZFile(sevenZFile).use { sevenZ ->
            val totalEntries = sevenZ.entries.count()
            var entry = sevenZ.nextEntry
            
            while (entry != null) {
                val file = File(outputDir, entry.name)
                
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { output ->
                        val buffer = ByteArray(8192)
                        var length: Int
                        while (sevenZ.read(buffer).also { length = it } != -1) {
                            output.write(buffer, 0, length)
                        }
                    }
                }
                
                count++
                val progress = (count * 100) / totalEntries
                onProgress(progress, entry.name)
                
                entry = sevenZ.nextEntry
            }
        }
        return true
    }
    
    private fun extractTarJava(
        tarFile: File,
        outputDir: File,
        onProgress: (Int, String) -> Unit
    ): Boolean {
        var count = 0
        
        TarArchiveInputStream(BufferedInputStream(FileInputStream(tarFile))).use { tarInput ->
            var entry = tarInput.nextEntry
            
            while (entry != null) {
                val file = File(outputDir, entry.name)
                
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { output ->
                        tarInput.copyTo(output)
                    }
                }
                
                count++
                onProgress(count, entry.name)
                
                entry = tarInput.nextEntry
            }
        }
        return true
    }
    
    private fun extractGzJava(
        gzFile: File,
        outputDir: File,
        onProgress: (Int, String) -> Unit
    ): Boolean {
        GzipCompressorInputStream(BufferedInputStream(FileInputStream(gzFile))).use { gzInput ->
            // Check if it's a tar.gz
            val firstBytes = ByteArray(512)
            gzInput.mark(512)
            val read = gzInput.read(firstBytes)
            gzInput.reset()
            
            if (read > 257 && String(firstBytes, 257, 5) == "ustar") {
                // It's a tar.gz
                TarArchiveInputStream(gzInput).use { tarInput ->
                    var entry = tarInput.nextEntry
                    var count = 0
                    
                    while (entry != null) {
                        val file = File(outputDir, entry.name)
                        
                        if (entry.isDirectory) {
                            file.mkdirs()
                        } else {
                            file.parentFile?.mkdirs()
                            FileOutputStream(file).use { output ->
                                tarInput.copyTo(output)
                            }
                        }
                        
                        count++
                        onProgress(count, entry.name)
                        entry = tarInput.nextEntry
                    }
                }
            } else {
                // Plain GZ file
                val outputFile = File(outputDir, gzFile.nameWithoutExtension)
                FileOutputStream(outputFile).use { output ->
                    gzInput.copyTo(output)
                }
                onProgress(100, outputFile.name)
            }
        }
        return true
    }
    
    private fun extractXzJava(
        xzFile: File,
        outputDir: File,
        onProgress: (Int, String) -> Unit
    ): Boolean {
        val outputFile = File(outputDir, xzFile.nameWithoutExtension)
        XZCompressorInputStream(BufferedInputStream(FileInputStream(xzFile))).use { xzInput ->
            FileOutputStream(outputFile).use { output ->
                xzInput.copyTo(output)
            }
        }
        onProgress(100, outputFile.name)
        return true
    }
    
    fun getArchiveTypeKt(file: File): String {
        return try {
            getArchiveType(file.absolutePath)
        } catch (e: Exception) {
            "UNKNOWN"
        }
    }
}
