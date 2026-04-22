package com.graywizard.filemanager.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import java.io.*
import java.util.zip.ZipInputStream

object ArchiveExtractor {
    
    private external fun extractNative(archivePath: String, outputPath: String): Boolean
    
    suspend fun extractArchive(
        archiveFile: File,
        outputDirectory: File,
        onProgress: (Int) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs()
            }
            
            when (archiveFile.extension.lowercase()) {
                "zip", "apk", "apks", "apkm", "xapk" -> extractZip(archiveFile, outputDirectory, onProgress)
                "7z" -> extract7z(archiveFile, outputDirectory, onProgress)
                "tar" -> extractTar(archiveFile, outputDirectory, onProgress)
                "gz" -> extractGz(archiveFile, outputDirectory, onProgress)
                "xz" -> extractXz(archiveFile, outputDirectory, onProgress)
                "rar" -> extractNative(archiveFile.absolutePath, outputDirectory.absolutePath)
                else -> false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun extractZip(
        zipFile: File,
        outputDir: File,
        onProgress: (Int) -> Unit
    ): Boolean {
        ZipArchiveInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zipInput ->
            var entry = zipInput.nextEntry
            var count = 0
            
            while (entry != null) {
                val file = File(outputDir, entry.name)
                
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { output ->
                        zipInput.copyTo(output)
                    }
                }
                
                count++
                onProgress(count)
                entry = zipInput.nextEntry
            }
        }
        return true
    }
    
    private fun extract7z(
        sevenZFile: File,
        outputDir: File,
        onProgress: (Int) -> Unit
    ): Boolean {
        SevenZFile(sevenZFile).use { sevenZ ->
            var entry = sevenZ.nextEntry
            var count = 0
            
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
                onProgress(count)
                entry = sevenZ.nextEntry
            }
        }
        return true
    }
    
    private fun extractTar(
        tarFile: File,
        outputDir: File,
        onProgress: (Int) -> Unit
    ): Boolean {
        TarArchiveInputStream(BufferedInputStream(FileInputStream(tarFile))).use { tarInput ->
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
                onProgress(count)
                entry = tarInput.nextEntry
            }
        }
        return true
    }
    
    private fun extractGz(
        gzFile: File,
        outputDir: File,
        onProgress: (Int) -> Unit
    ): Boolean {
        val outputFile = File(outputDir, gzFile.nameWithoutExtension)
        GzipCompressorInputStream(BufferedInputStream(FileInputStream(gzFile))).use { gzInput ->
            FileOutputStream(outputFile).use { output ->
                gzInput.copyTo(output)
            }
        }
        onProgress(100)
        return true
    }
    
    private fun extractXz(
        xzFile: File,
        outputDir: File,
        onProgress: (Int) -> Unit
    ): Boolean {
        val outputFile = File(outputDir, xzFile.nameWithoutExtension)
        XZCompressorInputStream(BufferedInputStream(FileInputStream(xzFile))).use { xzInput ->
            FileOutputStream(outputFile).use { output ->
                xzInput.copyTo(output)
            }
        }
        onProgress(100)
        return true
    }
}
