#include "seven_zip_extractor.h"
#include "utils.h"
#include <android/log.h>
#include <fstream>
#include <sys/stat.h>

extern "C" {
    #include "third_party/lzma/C/7z.h"
    #include "third_party/lzma/C/7zAlloc.h"
    #include "third_party/lzma/C/7zBuf.h"
    #include "third_party/lzma/C/7zCrc.h"
    #include "third_party/lzma/C/7zFile.h"
    #include "third_party/lzma/C/7zVersion.h"
}

#define LOG_TAG "7zExtractor"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace archive {

static ISzAlloc g_Alloc = { SzAlloc, SzFree };

bool SevenZipExtractor::extract(const std::string& sevenZipPath,
                                const std::string& outputPath,
                                ProgressCallback callback) {
    LOGD("Extracting 7z: %s", sevenZipPath.c_str());
    
    CFileInStream archiveStream;
    CLookToRead2 lookStream;
    CSzArEx db;
    SRes res;
    UInt16 *temp = nullptr;
    size_t tempSize = 0;
    
    // Initialize CRC table
    CrcGenerateTable();
    
    // Open archive file
    if (InFile_Open(&archiveStream.file, sevenZipPath.c_str()) != 0) {
        LOGE("Failed to open 7z file");
        return false;
    }
    
    FileInStream_CreateVTable(&archiveStream);
    LookToRead2_CreateVTable(&lookStream, False);
    lookStream.buf = nullptr;
    lookStream.bufSize = 0;
    lookStream.realStream = &archiveStream.vt;
    LookToRead2_Init(&lookStream);
    
    // Initialize 7z database
    SzArEx_Init(&db);
    
    res = SzArEx_Open(&db, &lookStream.vt, &g_Alloc, &g_Alloc);
    
    if (res != SZ_OK) {
        LOGE("Failed to open 7z archive: %d", res);
        File_Close(&archiveStream.file);
        return false;
    }
    
    Utils::createDirectoryRecursive(outputPath);
    
    UInt32 blockIndex = 0xFFFFFFFF;
    Byte *outBuffer = nullptr;
    size_t outBufferSize = 0;
    
    int totalFiles = db.NumFiles;
    int extractedFiles = 0;
    
    for (UInt32 i = 0; i < db.NumFiles; i++) {
        size_t offset = 0;
        size_t outSizeProcessed = 0;
        
        // Get file info
        Bool isDir = SzArEx_IsDir(&db, i);
        size_t len = SzArEx_GetFileNameUtf16(&db, i, nullptr);
        
        if (len > tempSize) {
            SzFree(nullptr, temp);
            tempSize = len;
            temp = (UInt16 *)SzAlloc(nullptr, tempSize * sizeof(UInt16));
            if (!temp) {
                LOGE("Memory allocation failed");
                break;
            }
        }
        
        SzArEx_GetFileNameUtf16(&db, i, temp);
        
        // Convert UTF-16 to UTF-8
        std::string fileName;
        for (size_t j = 0; j < len - 1; j++) {
            if (temp[j] < 0x80) {
                fileName += static_cast<char>(temp[j]);
            }
        }
        
        std::string fullPath = outputPath + "/" + fileName;
        
        if (isDir) {
            Utils::createDirectoryRecursive(fullPath);
            continue;
        }
        
        LOGD("Extracting: %s", fileName.c_str());
        
        // Create parent directory
        size_t lastSlash = fullPath.find_last_of('/');
        if (lastSlash != std::string::npos) {
            Utils::createDirectoryRecursive(fullPath.substr(0, lastSlash));
        }
        
        // Extract file
        res = SzArEx_Extract(&db, &lookStream.vt, i,
                            &blockIndex, &outBuffer, &outBufferSize,
                            &offset, &outSizeProcessed,
                            &g_Alloc, &g_Alloc);
        
        if (res != SZ_OK) {
            LOGE("Failed to extract file: %s", fileName.c_str());
            continue;
        }
        
        // Write file
        std::ofstream outFile(fullPath, std::ios::binary);
        if (outFile.is_open()) {
            outFile.write(reinterpret_cast<char*>(outBuffer + offset), outSizeProcessed);
            outFile.close();
            extractedFiles++;
            
            if (callback) {
                int progress = (extractedFiles * 100) / totalFiles;
                callback(progress, fileName);
            }
        } else {
            LOGE("Failed to create output file: %s", fullPath.c_str());
        }
    }
    
    // Cleanup
    ISzAlloc_Free(&g_Alloc, outBuffer);
    SzFree(nullptr, temp);
    SzArEx_Free(&db, &g_Alloc);
    File_Close(&archiveStream.file);
    
    return extractedFiles == totalFiles;
}

} // namespace archive
