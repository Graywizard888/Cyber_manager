#include "zip_extractor.h"
#include "utils.h"
#include "third_party/minizip/unzip.h"
#include <android/log.h>
#include <sys/stat.h>
#include <fstream>

#define LOG_TAG "ZipExtractor"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace archive {

bool ZipExtractor::createDirectoryRecursive(const std::string& path) {
    size_t pos = 0;
    while ((pos = path.find('/', pos + 1)) != std::string::npos) {
        std::string subPath = path.substr(0, pos);
        mkdir(subPath.c_str(), 0755);
    }
    return mkdir(path.c_str(), 0755) == 0 || errno == EEXIST;
}

bool ZipExtractor::extractFile(void* unzFilePtr, const std::string& outputPath) {
    unzFile uf = static_cast<unzFile>(unzFilePtr);
    
    char filename[256];
    unz_file_info fileInfo;
    
    if (unzGetCurrentFileInfo(uf, &fileInfo, filename, sizeof(filename),
                             nullptr, 0, nullptr, 0) != UNZ_OK) {
        return false;
    }
    
    std::string fullPath = outputPath + "/" + filename;
    
    // Check if it's a directory
    if (filename[strlen(filename) - 1] == '/') {
        createDirectoryRecursive(fullPath);
        return true;
    }
    
    // Create parent directory
    size_t lastSlash = fullPath.find_last_of('/');
    if (lastSlash != std::string::npos) {
        createDirectoryRecursive(fullPath.substr(0, lastSlash));
    }
    
    // Open file in archive
    if (unzOpenCurrentFile(uf) != UNZ_OK) {
        LOGE("Failed to open file in archive: %s", filename);
        return false;
    }
    
    // Extract file
    std::ofstream outFile(fullPath, std::ios::binary);
    if (!outFile.is_open()) {
        LOGE("Failed to create output file: %s", fullPath.c_str());
        unzCloseCurrentFile(uf);
        return false;
    }
    
    char buffer[8192];
    int bytesRead;
    
    while ((bytesRead = unzReadCurrentFile(uf, buffer, sizeof(buffer))) > 0) {
        outFile.write(buffer, bytesRead);
    }
    
    outFile.close();
    unzCloseCurrentFile(uf);
    
    // Set file permissions
    chmod(fullPath.c_str(), fileInfo.external_fa >> 16);
    
    return bytesRead >= 0;
}

bool ZipExtractor::extract(const std::string& zipPath,
                          const std::string& outputPath,
                          ProgressCallback callback) {
    unzFile uf = unzOpen64(zipPath.c_str());
    if (uf == nullptr) {
        LOGE("Failed to open ZIP file: %s", zipPath.c_str());
        return false;
    }
    
    unz_global_info globalInfo;
    if (unzGetGlobalInfo(uf, &globalInfo) != UNZ_OK) {
        LOGE("Failed to get ZIP info");
        unzClose(uf);
        return false;
    }
    
    createDirectoryRecursive(outputPath);
    
    int totalFiles = globalInfo.number_entry;
    int currentFile = 0;
    
    for (int i = 0; i < totalFiles; i++) {
        char filename[256];
        unz_file_info fileInfo;
        
        if (unzGetCurrentFileInfo(uf, &fileInfo, filename, sizeof(filename),
                                 nullptr, 0, nullptr, 0) != UNZ_OK) {
            LOGE("Failed to get file info at index %d", i);
            break;
        }
        
        if (!extractFile(uf, outputPath)) {
            LOGE("Failed to extract file: %s", filename);
        } else {
            currentFile++;
            if (callback) {
                int progress = (currentFile * 100) / totalFiles;
                callback(progress, filename);
            }
        }
        
        if (i < totalFiles - 1) {
            if (unzGoToNextFile(uf) != UNZ_OK) {
                LOGE("Failed to go to next file");
                break;
            }
        }
    }
    
    unzClose(uf);
    
    return currentFile == totalFiles;
}

} // namespace archive
