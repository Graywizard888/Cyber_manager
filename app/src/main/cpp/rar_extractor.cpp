#include "rar_extractor.h"
#include "utils.h"
#include <android/log.h>
#include <sys/stat.h>

// Include UnRAR library headers
extern "C" {
    #include "third_party/unrar/dll.hpp"
}

#define LOG_TAG "RarExtractor"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace archive {

struct ExtractionData {
    std::string outputPath;
    ProgressCallback callback;
    int totalFiles;
    int currentFile;
};

int CALLBACK CallbackProc(UINT msg, LPARAM UserData, LPARAM P1, LPARAM P2) {
    ExtractionData* data = reinterpret_cast<ExtractionData*>(UserData);
    
    switch (msg) {
        case UCM_CHANGEVOLUME:
            LOGD("Processing volume");
            break;
            
        case UCM_PROCESSDATA: {
            // Progress update during extraction
            break;
        }
            
        case UCM_NEEDPASSWORD: {
            // Password required
            LOGE("Archive is password protected");
            return -1;
        }
    }
    
    return 1;
}

bool RarExtractor::extract(const std::string& rarPath,
                          const std::string& outputPath,
                          ProgressCallback callback) {
    LOGD("Extracting RAR: %s", rarPath.c_str());
    
    ExtractionData data;
    data.outputPath = outputPath;
    data.callback = callback;
    data.totalFiles = 0;
    data.currentFile = 0;
    
    // Create output directory
    Utils::createDirectoryRecursive(outputPath);
    
    // Open archive
    RAROpenArchiveDataEx archiveData;
    memset(&archiveData, 0, sizeof(archiveData));
    archiveData.ArcName = const_cast<char*>(rarPath.c_str());
    archiveData.OpenMode = RAR_OM_EXTRACT;
    archiveData.Callback = CallbackProc;
    archiveData.UserData = reinterpret_cast<LPARAM>(&data);
    
    HANDLE hArcData = RAROpenArchiveEx(&archiveData);
    
    if (hArcData == nullptr || archiveData.OpenResult != ERAR_SUCCESS) {
        LOGE("Failed to open RAR archive: %d", archiveData.OpenResult);
        return false;
    }
    
    // First pass: count files
    RARHeaderDataEx headerData;
    memset(&headerData, 0, sizeof(headerData));
    
    while (RARReadHeaderEx(hArcData, &headerData) == ERAR_SUCCESS) {
        data.totalFiles++;
        RARProcessFile(hArcData, RAR_SKIP, nullptr, nullptr);
        memset(&headerData, 0, sizeof(headerData));
    }
    
    RARCloseArchive(hArcData);
    
    // Second pass: extract files
    memset(&archiveData, 0, sizeof(archiveData));
    archiveData.ArcName = const_cast<char*>(rarPath.c_str());
    archiveData.OpenMode = RAR_OM_EXTRACT;
    archiveData.Callback = CallbackProc;
    archiveData.UserData = reinterpret_cast<LPARAM>(&data);
    
    hArcData = RAROpenArchiveEx(&archiveData);
    
    if (hArcData == nullptr) {
        LOGE("Failed to reopen RAR archive");
        return false;
    }
    
    int result;
    memset(&headerData, 0, sizeof(headerData));
    
    while ((result = RARReadHeaderEx(hArcData, &headerData)) == ERAR_SUCCESS) {
        std::string fileName = headerData.FileName;
        std::string fullPath = outputPath + "/" + fileName;
        
        LOGD("Extracting: %s", fileName.c_str());
        
        // Create parent directory if needed
        size_t lastSlash = fullPath.find_last_of('/');
        if (lastSlash != std::string::npos) {
            Utils::createDirectoryRecursive(fullPath.substr(0, lastSlash));
        }
        
        // Extract file
        char destPath[1024];
        strncpy(destPath, outputPath.c_str(), sizeof(destPath) - 1);
        
        result = RARProcessFile(hArcData, RAR_EXTRACT, destPath, nullptr);
        
        if (result != ERAR_SUCCESS) {
            LOGE("Failed to extract file: %s (error %d)", fileName.c_str(), result);
        } else {
            data.currentFile++;
            if (callback) {
                int progress = (data.currentFile * 100) / data.totalFiles;
                callback(progress, fileName);
            }
        }
        
        memset(&headerData, 0, sizeof(headerData));
    }
    
    RARCloseArchive(hArcData);
    
    return data.currentFile == data.totalFiles;
}

} // namespace archive
